# 武侯祠游客服务中心

基于 Spring Boot + Vue 3 的武侯祠景区游客服务中心，面向游客、平台管理员、审批人员、投诉处理人员、酒店管理员多角色，提供游客投诉、旅游应急信息发布、酒店查询及营销、景区(点)及线路查询、餐饮娱乐及演出团体查询、天气及出行信息查询等功能。

> 本项目以 `docs/source/游客服务系统.docx` 为源需求，产出一套文档：
> - `docs/PRD.md`（产品需求文档）— 系统范围、角色权限、子系统、数据模型、接口
> - `docs/reuse-assessment.md`（复用评估报告）— 招生系统 → 本系统的复用/改造清单
> - `docs/VERIFICATION.md`（功能验收指南）— 账号清单 + 分角色/业务闭环验证流程
> - `docs/项目说明.md`（项目说明与使用指南）— 环境/启动/账号/功能/操作流程/成员分工
> - `docs/测试文档.md`（测试文档）— 问题与解决记录 + 单测清单
> - `CONTEXT.md`（领域统一语言）— 术语、角色命名与领域决策
> - `docs/adr/`（架构决策记录）— 角色分离、地理范围、数据来源等不可逆决策
>
> 文档见 [docs/](./docs/)，统一语言见 [CONTEXT.md](./CONTEXT.md)，决策见 [docs/adr/](./docs/adr/)。

## 运行与配置

- **后端**：`cd backend && export JAVA_HOME=D:/ETjdk17 && mvn spring-boot:run`（JDK 17，端口 8080，数据库 `tourist_service`，先 `mysql -u root -p123456 < backend/sql/init.sql`）。
- **前端**：`cd frontend && npm install && npm run dev`（端口 5173，代理 `/api` 与 `/uploads` → 8080）。
- **测试**：`cd backend && mvn test`（核心状态机 24 个 JUnit5 + Mockito 单测：投诉/应急/审批引擎/Amap 降级）。
- **账号**（密码均 `123456`）：`platform1`(平台管理员)、`approver1`(审批人员)、`handler1`/`handler2`(投诉处理)、`hotel1`(酒店管理员)、`tourist1`(游客)。

> 平台管理员登录后进入 **数据看板**（`/dashboard`）查看统计卡片 + 投诉状态饼图 + 应急状态柱状图（ECharts）。

### 高德天气/路况（Web服务 Key）

天气/路况由后端调用**高德 Web服务**接口（`restapi.amap.com`）实时获取**成都市武侯区**数据；取数在服务端，故使用「Web服务」而非「Web端 JS API」（详见 [ADR-0004](./docs/adr/0004-amap-web-service.md)，JS API 需安全密钥+域名白名单且 key 会暴露在前端）。

- 配置键：`amap.key`（`backend/src/main/resources/application.yml`，或环境变量 `AMAP_KEY`）。
- key 申请：高德开放平台 → 创建应用 → 添加 Key → 服务类型选「**Web服务**」。
- **为空/无效时自动降级为本地 mock**（不会因此中断系统）。
- 天气 `GET /api/weather`（实况 lives）、路况 `GET /api/road-conditions`（武侯区代表道路）。

