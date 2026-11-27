# 企业员工管理系统

Java 框架技术（一）实验二。基于 Spring MVC、MyBatis-Plus 和 MySQL 的员工管理应用，核心表为 `emp`。员工档案可分页查询、查看、新增、编辑、单删和批删。部门概览按员工所属部门汇总总人数及在职人数。

## 环境及启动

1. 安装 JDK 11 或更高版本、Maven 和 Tomcat 9。生产数据库采用 MySQL 8.0.16 或更高版本。
2. 创建 UTF-8 数据库 `enterprise_mis`，在该库中执行一次 `sql/init.sql`。已有符合表结构的数据库可直接配置连接。
3. 在启动 Tomcat 的进程环境中设置 `ENTERPRISE_JDBC_USER`、`ENTERPRISE_JDBC_PASSWORD`、`ENTERPRISE_AUTH_PASSWORD`。如需使用其他库址，设置 `ENTERPRISE_JDBC_URL`。登录账号默认 `admin`，可用 `ENTERPRISE_AUTH_USER` 覆盖。口令由部署人指定。
4. 运行 `mvn clean package`，将 `target/enterprise-mis.war` 部署到 Tomcat 9。项目包也提供已构建的 WAR。
5. 打开 `http://localhost:8080/enterprise-mis/demo/index.html` 并登录。界面中的新增、编辑和删除会修改当前连接数据库。

## 接口

| 方法与路径 | 用途 |
|---|---|
| POST /auth/login | 建立 Session |
| GET /auth/me | 检查会话 |
| POST /auth/logout | 结束会话 |
| GET /emps | 分页，可传 name、dept、gender、status、page、size |
| GET /emps/{id} | 查看员工档案 |
| POST /emps | 新增员工 |
| PUT /emps | 按请求体的 empId 修改员工 |
| DELETE /emps/{id} | 单条删除 |
| DELETE /emps | 请求体传员工编号数组，整批事务处理 |
| GET /emps/departments | 部门人数和在职人数 |
| GET /emps/department?name=研发部 | 查询指定部门的员工 |

## 测试及证据

`mvn test` 启动临时端口的嵌入式 Tomcat 9，使用 H2 的 MySQL 兼容模式。测试调用真实 HTTP 接口，并验证批删回滚和五个 Spring 事务场景。每次测试的原始记录写到 `target/evidence`。本次交付结果在 `evidence`。

Apifox 可导入 `apifox/enterprise-mis.postman_collection.json`。填写 baseUrl 和 password，顺序执行。新增与删除测试请使用独立数据库中的样例数据。

测试目录中的事务演示仅用于集成测试。业务接口不会暴露这些演示入口。独立 MySQL 和 Apifox UI 未实测。

