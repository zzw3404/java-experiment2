package study.enterprise.mis;

import static org.junit.Assert.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.h2.tools.RunScript;
import org.junit.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.support.WebApplicationContextUtils;
import com.fasterxml.jackson.databind.*;

public class EnterpriseMisTest {
    private static final String JDBC = "jdbc:h2:mem:enterprise;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final List<Map<String, Object>> cases = new ArrayList<>();
    private static final List<Map<String, Object>> transactions = new ArrayList<>();
    private static final String PASSWORD = UUID.randomUUID().toString();
    private static Tomcat server;
    private static Context context;
    private static HttpClient client;
    private static String base;

    public static class TestInitializer extends WebInitializer {
        @Override
        protected Class<?>[] getRootConfigClasses() {
            return new Class<?>[]{RootConfig.class, TxTestConfig.class};
        }
    }

    @BeforeClass
    public static void start() throws Exception {
        try (var connection = java.sql.DriverManager.getConnection(JDBC, "sa", "");
             var sql = Files.newBufferedReader(Path.of("sql/init.sql"))) {
            RunScript.execute(connection, sql);
        }
        System.setProperty("enterprise.jdbc.url", JDBC);
        System.setProperty("enterprise.jdbc.user", "sa");
        System.setProperty("enterprise.jdbc.password", "");
        System.setProperty("enterprise.auth.password", PASSWORD);
        server = new Tomcat();
        server.setBaseDir(Files.createTempDirectory(Path.of("target"), "tomcat").toString());
        server.setPort(0);
        server.getConnector().setProperty("address", "127.0.0.1");
        context = server.addContext("", Files.createTempDirectory(Path.of("target"), "web").toAbsolutePath().toString());
        context.setParentClassLoader(EnterpriseMisTest.class.getClassLoader());
        context.addServletContainerInitializer(new org.springframework.web.SpringServletContainerInitializer(),
                Set.of(TestInitializer.class));
        server.start();
        base = "http://127.0.0.1:" + server.getConnector().getLocalPort();
        client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
    }

    @AfterClass
    public static void finish() throws Exception {
        Files.createDirectories(Path.of("target/evidence"));
        JSON.writerWithDefaultPrettyPrinter().writeValue(Path.of("target/evidence/http-results.json").toFile(), cases);
        JSON.writerWithDefaultPrettyPrinter().writeValue(Path.of("target/evidence/transactions.json").toFile(), transactions);
        if (server != null) {
            server.stop();
            server.destroy();
        }
        for (String key : List.of("enterprise.jdbc.url","enterprise.jdbc.user","enterprise.jdbc.password","enterprise.auth.password")) {
            System.clearProperty(key);
        }
    }

    private JsonNode call(String label, String method, String path, Object body, int expected) throws Exception {
        HttpRequest.BodyPublisher payload = body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body), StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + path))
                .header("Content-Type", "application/json").method(method, payload).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonNode json = JSON.readTree(response.body());
        cases.add(Map.of("case", label, "method", method, "path", path, "expected", expected,
                "http", response.statusCode(), "response", json));
        assertEquals(label, expected, response.statusCode());
        assertEquals(label, expected, json.path("code").asInt());
        return json.path("data");
    }

    private Map<String, Object> employee(String name) {
        var row = new LinkedHashMap<String, Object>();
        row.put("empName", name);
        row.put("gender", "女");
        row.put("dept", "研发部");
        row.put("post", "系统工程师");
        row.put("salary", 15000);
        row.put("hireDate", "2025-03-01");
        row.put("status", 1);
        return row;
    }

    @Test
    public void verifyEmployeeWorkflow() throws Exception {
        call("未登录", "GET", "/emps", null, 401);
        call("错误口令", "POST", "/auth/login", Map.of("username", "admin", "password", "wrong"), 401);
        call("登录", "POST", "/auth/login", Map.of("username", "admin", "password", PASSWORD), 200);
        call("当前会话", "GET", "/auth/me", null, 200);
        assertEquals(5, call("员工总数", "GET", "/emps", null, 200).path("total").asInt());
        assertEquals(2, call("研发部在职", "GET", "/emps?dept=%E7%A0%94%E5%8F%91%E9%83%A8&status=1", null, 200).path("total").asInt());
        assertEquals(1, call("姓名筛选", "GET", "/emps?name=%E6%9E%97", null, 200).path("total").asInt());
        assertEquals(2, call("限制每页记录", "GET", "/emps?page=1&size=2", null, 200).path("records").size());
        assertEquals(0, call("超出末页", "GET", "/emps?page=99&size=2", null, 200).path("records").size());
        assertEquals("2023-04-17", call("员工详情", "GET", "/emps/1", null, 200).path("hireDate").asText());
        JsonNode stats = call("部门统计", "GET", "/emps/departments", null, 200);
        assertEquals(4, stats.size());
        for (JsonNode row : stats) {
            if ("财务部".equals(row.path("dept").asText())) {
                assertEquals(1, row.path("total").asInt());
                assertEquals(0, row.path("active").asInt());
            }
        }
        assertEquals(2, call("部门成员", "GET", "/emps/department?name=%E7%A0%94%E5%8F%91%E9%83%A8", null, 200).size());
        assertEquals(3, call("性别筛选", "GET", "/emps?gender=%E5%A5%B3", null, 200).path("total").asInt());
        call("非法页码", "GET", "/emps?page=0", null, 400);
        call("非法页长", "GET", "/emps?size=101", null, 400);
        call("非法状态", "GET", "/emps?status=2", null, 400);
        call("缺失员工", "GET", "/emps/999999", null, 404);
        call("空删除", "DELETE", "/emps", List.of(), 400);
        call("空姓名", "POST", "/emps", employee(" "), 400);
        var badDate = employee("日期校验");
        badDate.put("hireDate", "2025-02-29");
        call("非法日期", "POST", "/emps", badDate, 400);
        var row = employee("测试人员甲");
        long id = call("新增员工", "POST", "/emps", row, 200).asLong();
        assertTrue(id > 5);
        row.put("empId", id);
        row.put("post", "项目主管");
        call("修改员工", "PUT", "/emps", row, 200);
        assertEquals("项目主管", call("核对岗位", "GET", "/emps/" + id, null, 200).path("post").asText());

        var root = WebApplicationContextUtils.getRequiredWebApplicationContext(context.getServletContext());
        var jdbc = new JdbcTemplate(root.getBean(javax.sql.DataSource.class));
        String countSql = "SELECT COUNT(*) FROM emp WHERE emp_id = ?";
        Integer countBefore = jdbc.queryForObject(countSql, Integer.class, id);
        call("批删遇到不存在编号", "DELETE", "/emps", List.of(id, 999999L), 404);
        Integer countAfter = jdbc.queryForObject(countSql, Integer.class, id);
        assertEquals(Integer.valueOf(1), countBefore);
        assertEquals(countBefore, countAfter);
        transactions.add(Map.of("fixture", id, "before", countBefore, "after", countAfter, "reason", "第二条编号不存在"));
        call("回滚后员工仍在", "GET", "/emps/" + id, null, 200);
        long other = call("新增第二员工", "POST", "/emps", employee("测试人员乙"), 200).asLong();
        call("批删员工", "DELETE", "/emps", List.of(id, other), 200);
        call("批删后不存在", "GET", "/emps/" + id, null, 404);
        call("单删员工", "DELETE", "/emps/5", null, 200);
        call("单删后不存在", "GET", "/emps/5", null, 404);
        call("退出", "POST", "/auth/logout", null, 200);
        call("退出后未登录", "GET", "/emps", null, 401);
    }

    @Test
    public void verifyTransactionModes() {
        var root = WebApplicationContextUtils.getRequiredWebApplicationContext(context.getServletContext());
        var jdbc = new JdbcTemplate(root.getBean(javax.sql.DataSource.class));
        TxScenario demo = root.getBean(TxScenario.class);
        checkWrite(jdbc, "未开启事务", demo::withoutTransaction, false, true);
        checkWrite(jdbc, "事务代理", demo::withTransaction, true, true);
        checkWrite(jdbc, "异常被捕获", demo::swallowed, false, false);
        checkWrite(jdbc, "内部自调用", demo::selfCall, false, true);
        checkWrite(jdbc, "private 方法", demo::privateCall, false, true);
    }

    private void checkWrite(JdbcTemplate jdbc, String scenario, java.util.function.Consumer<Employee> action,
                            boolean rollback, boolean throwsError) {
        Employee row = JSON.convertValue(employee("事务样例"), Employee.class);
        String sql = "SELECT COUNT(*) FROM emp WHERE emp_name = ?";
        int before = jdbc.queryForObject(sql, Integer.class, row.empName);
        try {
            if (throwsError) assertThrows(IllegalStateException.class, () -> action.accept(row));
            else action.accept(row);
            int after = jdbc.queryForObject(sql, Integer.class, row.empName);
            assertEquals(before + (rollback ? 0 : 1), after);
            transactions.add(Map.of("scenario", scenario, "before", before, "after", after));
        } finally {
            jdbc.update("DELETE FROM emp WHERE emp_name = ?", row.empName);
        }
    }
}
