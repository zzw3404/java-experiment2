# AI 辅助开发记录

- 用户要求依据课程指导书完成实验二，确认采用实验一数字贸易业务。
- Codex 从实验一归档核对表结构、关联关系与 Mapper 功能，在独立 WAR 项目中实现 Web 层和事务层。
- 首次 Maven 离线构建因 Druid 仓库标记以及缺少 Tomcat 注解和 JBoss Logging 工件失败。联网补齐后重新构建。
- 运行环境中现有独立 Tomcat 为 11，而课程项目使用 javax.servlet 接口。集成测试改用 Tomcat 9，保持课程要求的 Servlet API。
- 测试真实执行了 33 条 HTTP 请求和 5 组事务数据对照。具体证据位于 evidence。报告未把 AI 所做的代码核对和测试写成本人手工完成。
