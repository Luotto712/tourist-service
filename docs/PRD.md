# 武侯祠游客服务中心 产品需求文档（PRD）

| 项目 | 内容 |
|------|------|
| 文档版本 | v0.3 |
| 需求来源 | `docs/source/游客服务系统.docx` |
| 生成日期 | 2026-09-08 |
| 项目范围 | 成都武侯祠景区一站式游客服务中心：投诉 + 信息发布 + 多类目查询 + 营销 |
| 技术栈 | Spring Boot 2.7.18 + MyBatis-Plus + MySQL + JWT；Vue 3 + Vite + Element Plus + Pinia |

> **范围界定**：本系统为**武侯祠游客服务中心**，「景区」均指武侯祠景区；属地化的天气/路况等数据以**成都市武侯区**为取数口径。领域统一语言与决策记录见 [CONTEXT.md](../CONTEXT.md) 与 [docs/adr/](./adr/)。

---

## 1. 项目概述

### 1.1 背景
武侯祠游客服务中心作为武侯祠景区服务与宣传的主要渠道，为游客提供行前信息查询与行中投诉求助能力，同时为景区平台方提供内容发布、审批、营销与投诉处理的管理后台。系统围绕武侯祠景区覆盖全域旅游服务。

### 1.2 目标
- 为游客提供一站式查询与求助入口：酒店/景区/线路/餐饮/演出/交通/天气/路况查询、投诉提交与处理进度跟踪、应急信息获取。
- 为平台方提供内容管理：应急信息发布与审批、酒店营销推荐、各查询类数据维护、投诉全流程处理与结案。
- 为酒店方提供实时房态自助录入。

### 1.3 目标用户（角色）
系统采取**多角色 RBAC**，角色清单（沿用招生系统基于字符串的角色机制）：

| 角色 | 英文常量 | 说明 |
|------|---------|------|
| 游客 | `TOURIST` | 查询各类信息、提交/跟踪/确认/评分投诉 |
| 平台管理员 | `PLATFORM_ADMIN` | 查询类数据维护、应急/营销发布、投诉分派与结案、数据看板 |
| 审批人员 | `APPROVER` | 仅负责「是否批准/发布」：投诉审批、应急信息审批 |
| 投诉处理人员 | `COMPLAINT_HANDLER` | 处理分派的投诉并提交结果、证据附件 |
| 酒店管理员 | `HOTEL_ADMIN` | 录入本酒店房间实时预订信息 |

> 角色职责划分经确认：**审批（是否发布）归审批人员，分派/结案/发布/维护归平台管理员**，实现审批与执行分离。见 [ADR-0001](./adr/0001-approval-role-separation.md)。

---

## 2. 角色与权限矩阵

「✅ 可见/操作」「👁 仅查看」「— 无权限」

| 模块 / 能力 | 游客 | 平台管理员 | 审批人员 | 投诉处理人员 | 酒店管理员 |
|------------|:----:|:---------:|:------:|:----------:|:---------:|
| 登录 / 修改密码 / 重置密码 | ✅ | ✅ | ✅ | ✅ | ✅ |
| 游客投诉：提交/回复/查看 | ✅ | — | — | — | — |
| 游客投诉：确认处理意见 | ✅ | — | — | — | — |
| 游客投诉：评价打分 | ✅ | — | — | — | — |
| 游客投诉：审批（是否发布）| — | — | ✅ | — | — |
| 游客投诉：分派处理人员 | — | ✅ | — | — | — |
| 游客投诉：处理并提交结果 | — | — | — | ✅ | — |
| 游客投诉：结案 | — | ✅ | — | — | — |
| 旅游应急信息：发布/修改/删除/查询 | 👁(游客查询) | ✅ | — | — | — |
| 旅游应急信息：审批 | — | — | ✅ | — | — |
| 星级酒店：查询 | ✅ | — | — | — | — |
| 星级酒店：房间实时信息录入 | — | — | — | — | ✅ |
| 星级酒店：营销推荐录入 | — | ✅ | — | — | — |
| 非星级/乡村旅游酒店：查询 | ✅ | — | — | — | — |
| 非星级/乡村酒店：房态录入 | — | — | — | — | ✅ |
| 非星级/乡村酒店：营销推荐 | — | ✅ | — | — | — |
| 景区(点)、线路、餐饮、演出、交通 查询 | ✅ | — | — | — | — |
| 景区(点)、线路、餐饮、演出、交通 维护 | — | ✅ | — | — | — |
| 天气、路况 查询（外部 API） | ✅ | — | — | — | — |
| 消息通知：查看/已读 | ✅ | ✅ | ✅ | ✅ | ✅ |
| 数据看板（统计） | — | ✅ | — | — | — |

---

## 3. 子系统功能分解

### 3.1 游客投诉系统

**目标**：接收并管理武侯祠景区游客投诉，对投诉全过程（提交→审批→分派→处理→确认→结案→评价）做闭环跟踪管理。

**用户故事**
- 作为游客，我希望提交投诉（文字+照片/视频）并随时查看处理进度，以便问题被跟进。
- 作为审批人员，我希望审批决定投诉是否公开发布，通过后进入分派。
- 作为平台管理员，我希望将已通过审批的投诉分派给处理人员，并对处理完成的投诉结案。
- 作为投诉处理人员，我希望查看被分派的投诉，现场处理并提交处理意见与结果、上传证据附件。
- 作为游客，我希望确认处理人员提交的处理意见，并对结案的处理结果评价打分。

**核心流程**
```
游客提交/回复投诉 ──► 审批人员 审批(是否发布)
                              │ 通过                │ 拒绝
                              ▼                     ▼
                        平台管理员 分派处理人员      不予发布(REJECTED)
                              │
                              ▼
                      处理人员处理，提交处理意见+结果+附件
                              │
                              ▼
                      游客确认处理意见 ──► 平台管理员结案
                              │                  │
                              ▼                  ▼
                        游客查看处理结果 ──► 游客评价打分(结案后)
```

> 顺序要点：**「游客评价」发生在结案之后**；「游客确认意见」发生在处理完成之后、结案之前。二者都是游客侧反馈，但处于状态机的不同阶段。

**功能点**
1. 提交/回复投诉：游客填写投诉内容，上传照片/视频等附件；可对已有投诉追加回复。
2. 查看投诉：游客查看投诉列表、投诉内容、处理信息。
3. 投诉处理：处理人员查看待处理投诉，提交处理意见/结果，上传附件。
4. 游客确认意见：游客对处理人员提交的处理意见/结果确认。
5. 投诉审批：审批人员查看投诉内容，决定是否发布；通过后进入分派。
6. 投诉分派：平台管理员将已通过审批的投诉指派给处理人员。
7. 投诉结案：平台管理员查看处理结果与游客确认意见，对处理完成的投诉结案。
8. 投诉评价：游客对已结案的处理结果评价打分（1–5 星）。
9. **状态回退**：游客追加回复时——若投诉为「未通过」则回到「待审批」（需重新审批）；若为「处理完成」则回到「处理中」（需重新处理）。
10. **附件归属**：提交投诉的图片/视频显示在「投诉内容」处缩略图（点击下载）；各参与人回复的附件以 `related_type='COMPLAINT_REPLY'` 关联，显示在其**回复记录**内。
11. **岗位详情视图**：审批/处理/平台人员在列表以「详情」进入与游客相同的详情页，但**无回复按钮**（回复仅游客本人）。

**页面**：我的投诉(列表+新建/回复)、投诉详情(时间线)、投诉审批列表、投诉分派列表、投诉处理列表、投诉结案列表。

**接口（REST，均以 `/api/` 为前缀）**
| 接口 | 说明 | 角色 |
|------|------|------|
| `POST  /complaints` | 提交投诉 | TOURIST |
| `POST  /complaints/{id}/reply` | 回复投诉（返回新回复，用于上传回复附件） | TOURIST/平台/审批/处理 |
| `GET   /complaints/mine` | 我的投诉（分页） | TOURIST |
| `GET   /complaints/{id}` | 投诉详情+处理时间线 | TOURIST/后台 |
| `PUT   /complaints/{id}/approve` | 审批（通过/拒绝） | APPROVER |
| `PUT   /complaints/{id}/assign` | 分派处理人员 | PLATFORM_ADMIN |
| `GET   /complaints/pending` | 待处理投诉（分页） | COMPLAINT_HANDLER |
| `POST  /complaints/{id}/process` | 提交处理意见+结果 | COMPLAINT_HANDLER |
| `PUT   /complaints/{id}/confirm` | 游客确认处理意见 | TOURIST |
| `PUT   /complaints/{id}/close` | 结案 | PLATFORM_ADMIN |
| `POST  /complaints/{id}/rate` | 评价打分 | TOURIST |
| `PUT   /complaints/{id}/reject` | 审批驳回 | APPROVER |
| `GET   /complaints/for-approval` | 待审批投诉（分页） | APPROVER |
| `GET   /complaints/approved` / `/confirmed` | 待分派 / 待结案列表 | PLATFORM_ADMIN |
| `DELETE /complaints/{id}` | 删除本人投诉（物理删除 + 级联删回复/审批流） | TOURIST |

---

### 3.2 旅游应急信息发布系统（针对游客）

**目标**：发布武侯祠景区相关的各类应急信息，支持发布→审批→展示→管理。

**用户故事**
- 作为平台管理员，我想发布旅游应急信息并设置有效期，经审批后对游客展示。
- 作为审批人员，我想对发布人员提交的应急信息进行审批，通过后展示在游客端。

**功能点**
1. 发布旅游应急信息：管理员填内容+有效期，提交后进入审批。
2. 审批旅游应急信息：审批人员审批，通过后展示于游客端。
3. 修改/删除：对发布有误的信息可修改、删除。
4. 查询：管理员可检索特定应急信息并处理；游客可查询以辅助行程决策。

**通知**：发布 → 通知**所有审批人员**「您有一条应急通知待审批！」；审批通过 → 通知**所有游客**「您收到了一条应急通知，请及时查看！」；驳回 → 通知**发布者**「您发布的应急消息被驳回！」。

**游客可见性规则**：游客端仅展示**审批通过（APPROVED）且处于有效期 `valid_from ≤ today ≤ valid_to`** 的应急信息。

**页面**：应急信息发布/管理列表、应急信息审批列表、游客端应急信息列表/详情。

**接口**：`POST /emergency-info`、`GET /emergency-info`（游客/管理分页）、`GET /emergency-info/{id}`、`PUT /emergency-info/{id}`、`DELETE /emergency-info/{id}`、`PUT /emergency-info/{id}/approve`（APPROVER）。

---

### 3.3 星级酒店查询及营销系统

**目标**：游客查询武侯祠景区内星级酒店房态并选择入住；酒店方实时录入；平台方做导流营销。

**用户故事**
- 作为游客，我希望按条件查询星级酒店房间预订情况，选择入住酒店。
- 作为酒店管理员，我希望凭酒店账号录入本酒店房间实时预订信息。
- 作为平台管理员，我希望根据导流情况录入星级酒店推荐营销信息。

**功能点**
1. 星级酒店查询：游客按条件查询酒店；酒店**详情内列房型**（总房量/当日已预定/剩余/价格）并可**预订**。
2. 星级酒店维护：平台管理员维护酒店基础信息。
3. **房型房态维护**：酒店管理员按酒店维护**房型**（总房量/**基准已预定**/价格）；日期只影响「当日已预定」的显示。
4. 星级酒店营销：平台管理员录入/编辑/删除营销；发布后**通知游客**（标题=酒店名，内容=营销内容）。

**页面**：酒店列表/详情（含房型与预订）、酒店管理后台（房态录入）、营销推荐管理、游客「订单详情」。

**接口**：`GET/POST/PUT/DELETE /hotels/star`、`GET /room-types`、`POST /room-types`（HOTEL_ADMIN 维护房型）、`GET /bookings/availability`（房型可用性）、`POST /bookings`（游客下单）、`GET/PUT /hotel-marketing`（营销）。

> 详见 **§3.8 酒店预订与订单**。

---

### 3.4 非星级酒店及乡村旅游酒店查询及营销系统

**目标与流程**与 3.3 一致，仅酒店分类为「非星级/乡村旅游酒店」，复用 3.3 的模式（`hotel_type=NONSTAR` / 细分为 `非星级`/`乡村旅游`）。

**接口**：与 3.3 一致（`hotel_type=NONSTAR`）：酒店 CRUD、`/room-types`、`/bookings`、`/hotel-marketing`。

---

### 3.5 旅游景区（点）及旅游线路查询系统

**目标**：游客查询武侯祠景区内旅游景区（点）与旅游线路信息，平台管理员后台维护。

**功能点**
1. 查询旅游景区（点）：游客按条件查询。
2. 维护旅游景区（点）：平台管理员 CRUD。
3. 查询旅游线路：游客按条件查询。
4. 维护旅游线路：平台管理员 CRUD。

**页面**：景区（点）列表/详情、线路列表/详情、平台维护页。

**接口**
| 接口 | 说明 | 角色 |
|------|------|------|
| `GET /attractions`、`GET /attractions/{id}` | 查询 | TOURIST |
| `POST/PUT/DELETE /attractions` | 维护 | PLATFORM_ADMIN |
| `GET /routes`、`GET /routes/{id}` | 查询 | TOURIST |
| `POST/PUT/DELETE /routes` | 维护 | PLATFORM_ADMIN |

---

### 3.6 餐饮娱乐及营业性演出团体信息查询系统

**目标**：游客查询武侯祠景区内餐饮娱乐信息与营业性演出团体信息，平台管理员后台维护。

**功能点**：查询餐饮娱乐、维护餐饮娱乐；查询演出团体、维护演出团体。

**页面**：餐饮娱乐列表/详情、演出团体列表/详情、平台维护页。

**接口**
| 接口 | 说明 | 角色 |
|------|------|------|
| `GET /catering`、`GET /catering/{id}` | 查询 | TOURIST |
| `POST/PUT/DELETE /catering` | 维护 | PLATFORM_ADMIN |
| `GET /performance-groups`、`GET /performance-groups/{id}` | 查询 | TOURIST |
| `POST/PUT/DELETE /performance-groups` | 维护 | PLATFORM_ADMIN |

---

### 3.7 天气及出行信息查询系统

**目标**：游客查询武侯祠景区天气状况、路况信息与景区交通（观光车/停车场/接驳站等）。

> **数据来源（已确认）**：天气、路况**不人工录入**，接入外部 API 实时获取**成都市武侯区**数据；景区交通由平台管理员**人工维护**。见 [ADR-0003](./adr/0003-query-data-sourcing-weather-api.md)。

**功能点**
1. 查询天气状况：游客查询武侯区天气（外部 API，只读）。
2. 查询路况信息：游客查询武侯区路况（外部 API，只读）。
3. 查询交通信息：游客查询景区交通/观光车/停车场/接驳站。
4. 维护交通信息：平台管理员 CRUD。

**页面**：天气查询页、路况查询页、交通查询页，平台交通维护页。

**接口**
| 接口 | 说明 | 角色 |
|------|------|------|
| `GET /weather` | 天候（外部 API） | TOURIST |
| `GET /road-conditions` | 路况（外部 API） | TOURIST |
| `GET /transport`、`GET /transport/{id}` | 景区交通查询 | TOURIST |
| `POST/PUT/DELETE /transport` | 维护 | PLATFORM_ADMIN |

---

### 3.8 酒店预订与订单

**目标**：游客在酒店详情内选择房型下单，形成订单并可查看/取消；酒店方录入的房态随预订动态变化。

**核心规则**
- **入住天数** `= 离开日期 − 入住日期 + 1`；**总价 = 单价 × 入住天数**；**入住人数不影响计价**（仅记录）。
- **某日期已预定** `= 基准已预定 + 覆盖该日期的有效预订数`；**剩余** `= 总房量 − 当日已预定`。
- **下单校验**：`离开 ≥ 入住`；逐日校验 `剩余 ≥ 1`（满房则拒绝，防超卖）。

**功能点**
1. 预订详情弹框：房型、入住日期、离开日期、入住人数、**总价**（实时计算）、确认、取消。
2. 订单详情（游客侧栏）：酒店名称、房型、日期(入住—离开)、总价、状态；**仅本人、按时间倒序、含已取消**，可**取消**。
3. 酒店管理员：房态录入列出该酒店**所有房型**（可含当日已预定/剩余），可查看**预订明细**（预定人/房型/日期/人数/总价）。

**接口（`/api/`）**
| 接口 | 说明 | 角色 |
|------|------|------|
| `GET  /bookings/availability` | 某酒店某日期的房型可用性 | 登录用户 |
| `POST /bookings` | 游客下单（计价 + 余量校验） | TOURIST |
| `GET  /bookings/mine` | 我的订单（分页，倒序） | TOURIST |
| `PUT  /bookings/{id}/cancel` | 取消本人订单 | TOURIST |
| `GET  /bookings/room-type` | 某房型预订明细 | HOTEL_ADMIN/PLATFORM_ADMIN |
| `GET  /room-types` | 房型列表（含当日可用性） | 登录用户 |
| `POST /room-types` | 新增/更新房型；`DELETE /room-types/{id}` 彻底删除（连带其预订） | HOTEL_ADMIN |

---

## 4. 数据模型（实体清单）

> 建议库表（命名下划线；`id` 自增主键，时间 `create_time/update_time`，逻辑删除 `deleted`）。复用招生系统共享表：`sys_user`、`notification`、`file_attachment`、`approval_flow`、`approval_node`、`approval_record`、`operation_log`。

**业务表**

| 表 | 说明 | 关键字段 |
|----|------|---------|
| `tourist_complaint` | 投诉主表 | user_id、content、status(PENDING/APPROVED/REJECTED/PROCESSING/RESOLVED/CONFIRMED/CLOSED)、current_node_id、handler_id、result、rating、is_published、confirm_time、close_time |
| `complaint_reply` | 投诉回复 | complaint_id、user_id、content、create_time |
| `emergency_info` | 旅游应急信息 | title、content、valid_from、valid_to、status(PENDING/APPROVED/REJECTED)、publisher_id |
| `star_hotel` | 星级酒店 | name、level、address、tel、thumb_id、intro、status |
| `room_type` | **房型基准**（每酒店每房型一行） | hotel_type、hotel_id、room_type、total、**base_booked(基准已预定)**、price、update_user_id |
| `hotel_booking` | **游客预订记录** | user_id、hotel_type、hotel_id、hotel_name、room_type、price(快照)、check_in、check_out、guests、**total_price**、status(BOOKED/CANCELLED) |
| `hotel_room` | 酒店房间实时信息（**保留但弃用**，历史遗留；已由 `room_type` 取代） | hotel_id、room_type、total、booked、price、date、update_user_id |
| `nonstar_hotel` | 非星级/乡村酒店 | name、type(非星级/乡村旅游)、address、tel、intro |
| `hotel_marketing` | 酒店营销推荐记录 | hotel_id、hotel_type(STAR/NONSTAR)、content、create_by（`diversion` 字段保留但界面不再录入） |
| `attraction` | 旅游景区（点） | name、type、address、thumb_id、intro、open_time |
| `tour_route` | 旅游线路 | name、attraction_ids、duration、intro |
| `catering` | 餐饮娱乐信息 | name、type、address、price、thumb_id、intro |
| `performance_group` | 营业性演出团体 | name、type、address、intro、contact |
| `transport_info` | 景区交通 | name、type(观光车/交通车/停车场/接驳站)、area、start、end、schedule、price、intro |
| `weather_info` | 天气状况（外部API，未必存储/缓存） | area(武侯区)、date、weather、temp、wind |
| `road_info` | 路况信息（外部API，未必存储/缓存） | area(武侯区)、road、condition、note、update_time |

> **地理口径**：业务表中的「景区内」均界定为武侯祠景区范围；`area` 类字段（天气/路况）取数为成都市武侯区。

**共享表（复用招生系统）**：`sys_user`（role 字符串）、`notification`、`file_attachment`（related_type/related_id 多态）、`approval_flow/node/record`（复用投诉审批流，`subject_type='COMPLAINT'`；泛化为 `subject_type/subject_id`）。

---

## 5. 页面清单（路由 → 视图）

沿用招生系统前端路由（`beforeEach` 守卫 + `meta.roles`）与视图组织方式。建议目录：

```
src/views/
├── login/           登录、忘记密码、重置密码
├── shared/          游客端：首页、酒店/景区/线路/餐饮/演出/交通/天气/路况查询、我的投诉、应急信息
├── platform_admin/  平台管理员：应急信息管理、酒店营销、各查询类维护、投诉分派/结案、数据看板
├── approver/        审批人员：投诉审批、应急信息审批
├── handler/         投诉处理人员：待处理投诉、处理
└── hotel_admin/     酒店管理员：房态录入
```

**核心路由表**（示意）：
| Route | 视图 | meta.roles |
|-------|------|-----------|
| `/` → home | 游客首页/查询入口 | `['TOURIST','APPROVER',...]`（公开+登录） |
| `/complaints` | 我的投诉 | `['TOURIST']` |
| `/complaints/approval` | 投诉审批 | `['APPROVER']` |
| `/complaints/assign` | 投诉分派 | `['PLATFORM_ADMIN']` |
| `/complaints/handler` | 投诉处理 | `['COMPLAINT_HANDLER']` |
| `/emergency-info` | 应急信息管理 | `['PLATFORM_ADMIN']` |
| `/hotel/rooms` | 房态录入 | `['HOTEL_ADMIN']` |
| `/transport` | 交通查询/维护 | `['TOURIST','PLATFORM_ADMIN']` |
| `/dashboard` | 数据看板 | `['PLATFORM_ADMIN']` |

---

## 6. 非功能需求

- **安全**：JWT 无状态认证；`@RequireRole` 做接口级 RBAC；富文本用 DOMPurify 净化（XSS）；登录/敏感接口限流（每 IP 每分钟 N 次）；文件上传白名单+大小上限（参考 50MB）。
- **性能**：列表/查询用 MyBatis-Plus 分页；查询类（酒店/景区/路线等）走索引；天气/路况外部 API 做**缓存 + 超时**（如超时 5s、天气缓存 10 分钟降峰值配额）。
- **可维护性**：统一 `Result/PageResult/BusinessException/GlobalExceptionHandler`；角色做集中常量（`RoleConstants`），避免散落硬编码。
- **外部依赖**：天气/路况 API 需配置 key、超时与降级（API 异常时返回缓存或友好提示）。
- **合规**：游客投诉数据需可追溯（时间线+操作日志）；应急信息需带有效期。

---

## 7. 技术栈决策与复用

**复用招生系统（recruitment-backend + recruitment-frontend）为起点**，技术栈一致；逐文件复用/改造/新增见 `docs/reuse-assessment.md`。新增的关注点：为「天气/路况」引入外部 API 集成与缓存降级；为查询类子系统补充平台管理员维护入口（对照 `DepartmentController`+`UserServiceImpl.searchUsers` 模板）。

---

## 8. 假设与待确认

**已确认（本轮）**
1. **角色归属**：审批（是否发布）归 `APPROVER`，分派/结案/发布/维护归 `PLATFORM_ADMIN`（原「两者均可审批」的宽松假设取消）。
2. **数据录入方**：景区(点)/线路/餐饮/演出/交通等查询类数据由平台管理员后台人工维护（原假设从「仅查询」改为补维护入口）。
3. **天气/路况数据来源**：接入外部 API 实时获取**成都市武侯区**数据，非人工录入（原「平台方录入、不联外部 API」假设替换）。
4. **旅游交通车**：源需求开篇提到但子系统未覆盖，已并入 §3.7「天气及出行信息查询」（见 ADR-0003）。

**仍待确认**
5. **投诉是否必须审批后才分派**：docx 流程「审批通过的投诉提交给处理人员」，本 PRD 按此建模（APPROVED→分派）；若允许「无需审批直接分派」，仅需放宽状态机。
6. **评分范围**：docx 仅说「评价打分」，本 PRD 采用 1–5 星（与招生系统 `feedback.rating` 一致），可改为自定义。
7. **游客确认意见是否必填**：「游客确认意见」为结案前置步骤（`CONFIRMED`），若允许平台管理员代确认或跳过，需放宽状态机。
8. **天气/路况 API 供应商**：具体选哪家（气象/高德/百度）未定；建议低成本免费层优先，支持多源换源。

---

## 9. 后续可扩展点

- 接入真实天气/路况/应急 API 替代人工录入（天气/路况已接 API，可扩展应急等信息源）。
- 投诉工单升级/超时预警、处理时限 SLA。
- 数据看板：投诉量、热点问题、各酒店预订分布图（ECharts）。
- 多语言、移动 H5。
- 武侯祠景区多景区/多片区扩展（当前单景区口径做泛化预留）。
