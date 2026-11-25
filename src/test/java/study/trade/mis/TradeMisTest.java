package study.trade.mis;

import static org.junit.Assert.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.ibatis.session.SqlSessionFactory;
import org.h2.tools.RunScript;
import org.junit.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.support.WebApplicationContextUtils;
import com.fasterxml.jackson.databind.*;

public class TradeMisTest {
    private static final String JDBC = "jdbc:h2:mem:trade;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final List<Map<String, Object>> cases = new ArrayList<>();
    private static final List<Map<String, Object>> transactions = new ArrayList<>();
    private static final String PASSWORD = UUID.randomUUID().toString();
    private static Tomcat server;
    private static Context context;
    private static HttpClient client;
    private static String base;

    @BeforeClass
    public static void start() throws Exception {
        try (var connection = java.sql.DriverManager.getConnection(JDBC, "sa", "");
             var sql = Files.newBufferedReader(Path.of("sql/init.sql"))) {
            RunScript.execute(connection, sql);
        }
        System.setProperty("trade.jdbc.url", JDBC);
        System.setProperty("trade.jdbc.user", "sa");
        System.setProperty("trade.jdbc.password", "");
        System.setProperty("trade.auth.password", PASSWORD);
        server = new Tomcat();
        server.setBaseDir(Files.createTempDirectory(Path.of("target"), "tomcat").toString());
        server.setPort(0);
        server.getConnector().setProperty("address", "127.0.0.1");
        context = server.addContext("", Files.createTempDirectory(Path.of("target"), "web").toAbsolutePath().toString());
        context.setParentClassLoader(TradeMisTest.class.getClassLoader());
        context.addServletContainerInitializer(new org.springframework.web.SpringServletContainerInitializer(),
                Set.of(WebInitializer.class));
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
        for (String key : List.of("trade.jdbc.url","trade.jdbc.user","trade.jdbc.password","trade.auth.password")) {
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

    private Product product(String name) {
        Product row = new Product();
        row.productName = name;
        row.category = "数码";
        row.brandId = 1L;
        row.price = new java.math.BigDecimal("49.90");
        row.stock = 5;
        row.saleStatus = "ON_SALE";
        return row;
    }

    @Test
    public void verifyHttpWorkflow() throws Exception {
        call("关联查询未登录", "GET", "/catalog/brands", null, 401);
        call("未登录", "GET", "/products", null, 401);
        call("错误密码", "POST", "/auth/login", Map.of("username", "admin", "password", "wrong"), 401);
        call("登录", "POST", "/auth/login", Map.of("username", "admin", "password", PASSWORD), 200);
        assertEquals("admin", call("当前用户", "GET", "/auth/me", null, 200).asText());
        JsonNode brands = call("品牌与商品数", "GET", "/catalog/brands", null, 200);
        assertEquals(2, brands.size());
        assertEquals("远川", brands.get(0).path("brandName").asText());
        assertEquals(2, brands.get(0).path("productCount").asInt());
        JsonNode orders = call("订单与客户", "GET", "/catalog/orders", null, 200);
        assertEquals(1, orders.size());
        assertEquals("LAB-0001", orders.get(0).path("orderNo").asText());
        JsonNode lines = call("订单明细联查", "GET", "/catalog/orders/1/lines", null, 200);
        assertEquals(1, lines.size());
        assertEquals("机械键盘", lines.get(0).path("productName").asText());
        assertEquals(299, lines.get(0).path("subtotal").asInt());
        call("不存在订单", "GET", "/catalog/orders/99999/lines", null, 404);
        assertEquals("机械键盘", call("商品详情", "GET", "/products/1", null, 200).path("productName").asText());
        assertEquals(1, call("按品牌类别状态分页", "GET", "/products?category=%E6%95%B0%E7%A0%81&brandId=1&status=ON_SALE&page=1&size=1", null, 200).path("records").size());
        assertEquals(1, call("名称模糊查询", "GET", "/products?name=%E9%94%AE%E7%9B%98", null, 200).path("records").size());
        assertEquals(0, call("超尾页", "GET", "/products?page=99", null, 200).path("records").size());
        call("第零页", "GET", "/products?page=0", null, 400);
        call("零页大小", "GET", "/products?size=0", null, 400);
        call("负页大小", "GET", "/products?size=-1", null, 400);
        call("非法状态", "GET", "/products?status=ERROR", null, 400);
        call("非法ID", "GET", "/products/abc", null, 400);
        call("不存在ID", "GET", "/products/99999", null, 404);
        call("删除不存在商品", "DELETE", "/products/99999", null, 404);
        call("空批删", "DELETE", "/products", List.of(), 400);
        call("有关联商品拒删", "DELETE", "/products/1", null, 409);
        call("空名称", "POST", "/products", product(" "), 400);
        call("超长名称", "POST", "/products", product("x".repeat(101)), 400);
        Product negative = product("负价格校验");
        negative.price = java.math.BigDecimal.valueOf(-1);
        call("负价格", "POST", "/products", negative, 400);
        Product noBrand = product("品牌校验");
        noBrand.brandId = 99999L;
        call("品牌不存在", "POST", "/products", noBrand, 400);
        Product edited = product("接口测试商品");
        edited.id = call("新增", "POST", "/products", edited, 200).asLong();
        edited.stock = 19;
        call("修改", "PUT", "/products", edited, 200);
        assertEquals(19, call("核对修改", "GET", "/products/" + edited.id, null, 200).path("stock").asInt());
        long other = call("新增批删样本", "POST", "/products", product("批删样本"), 200).asLong();
        call("批删中途失败", "DELETE", "/products", List.of(other, 1), 409);
        call("失败后商品仍在", "GET", "/products/" + other, null, 200);
        long second = call("新增第二批删样本", "POST", "/products", product("第二批删样本"), 200).asLong();
        call("批量删除", "DELETE", "/products", List.of(other, second), 200);
        call("批删后确认", "GET", "/products/" + other, null, 404);
        call("单条删除", "DELETE", "/products/" + edited.id, null, 200);
        call("退出", "POST", "/auth/logout", null, 200);
        call("退出后拦截", "GET", "/products", null, 401);
    }

    @Test
    public void verifyTransactionProxy() {
        var root = WebApplicationContextUtils.getRequiredWebApplicationContext(context.getServletContext());
        var examples = root.getBean(TransactionExamples.class);
        var jdbc = new JdbcTemplate(root.getBean(javax.sql.DataSource.class));
        verifyOne(jdbc, "无事务", examples::withoutTransaction, 1, true);
        verifyOne(jdbc, "代理入口开启事务", examples::withTransaction, 0, true);
        verifyOne(jdbc, "异常被吞", examples::swallowed, 1, false);
        verifyOne(jdbc, "同类自调用", examples::selfCall, 1, true);
        verifyOne(jdbc, "private 方法", examples::privateCall, 1, true);
        assertNotNull(root.getBean(SqlSessionFactory.class));
    }

    private void verifyOne(JdbcTemplate jdbc, String name, java.util.function.Consumer<Product> action,
                           int expected, boolean shouldThrow) {
        String label = "TX-" + name;
        String count = "SELECT COUNT(*) FROM lab_trade_product WHERE product_name = ?";
        int before = jdbc.queryForObject(count, Integer.class, label);
        if (shouldThrow) {
            assertThrows(IllegalStateException.class, () -> action.accept(product(label)));
        } else {
            action.accept(product(label));
        }
        int after = jdbc.queryForObject(count, Integer.class, label);
        transactions.add(Map.of("scenario", name, "before", before, "after", after, "expected", expected));
        assertEquals(0, before);
        assertEquals(expected, after);
        jdbc.update("DELETE FROM lab_trade_product WHERE product_name = ?", label);
    }
}
