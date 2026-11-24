# 数字贸易商品管理 MIS

实验二使用 Spring 5.3、Spring MVC、MyBatis-Plus、Druid 和 Tomcat 9。
沿用实验一的商品、品牌、客户、订单、订单明细模型。业务范围为商品管理。

## 自动验证
在项目根目录运行 `mvn test`。测试启动仅监听回环地址的 Tomcat 9，
向 H2 MySQL 兼容模式载入 sql/init.sql，执行 33 次 HTTP 核对和 5 组事务实验。
结果输出到 target/evidence。交付包 evidence 中保存本次记录。

## MySQL 和 Tomcat 部署
1. 新建独立 MySQL 数据库 trade_mis（utf8mb4），在库内执行一次 sql/init.sql。
   脚本面向新库，保留实验一原数据库。
2. 启动 Tomcat 9 前设置环境变量 TRADE_JDBC_USER、TRADE_JDBC_PASSWORD、
   TRADE_AUTH_PASSWORD。可选 TRADE_JDBC_URL、TRADE_AUTH_USER（默认 admin）。
   默认 JDBC 地址为 jdbc:mysql://localhost:3306/trade_mis。
3. 运行 mvn package，部署 target/trade-mis.war 到 Tomcat 9 的 webapps。
   在独立的测试环境设置账号口令，避免把真实口令写入代码或测试集合。
4. 在 Apifox 中按 Postman Collection 2.1 导入 apifox 目录中的集合，
   填写 baseUrl、username、password。从无登录 Cookie 的会话按用例顺序执行，
   验证动态 ID 变量和会话 Cookie 保留。运行失败中断时，用全新测试库重跑。
   随包 HTTP 记录由集成测试产生，未在 Apifox GUI 复跑。
5. GET /products 支持 name、category、brandId、status、page、size。
   其余接口见集合。状态取 ON_SALE 或 OFF_SALE。价格为非负两位小数。

## 架构
HTTP → Spring MVC 拦截器 / Controller → Spring Service / 事务代理 →
MyBatis-Plus Mapper / 分页插件 → Druid → MySQL。
集成测试使用 H2 而部署配置默认使用 MySQL。事务失效对照仅在测试中调用。

## Git 历史
交付包内 trade-mis-history.bundle 可用 git clone trade-mis-history.bundle trade-mis-history
恢复提交与 v1.0.0 标签。开发记录见 docs/AI记录.md。
