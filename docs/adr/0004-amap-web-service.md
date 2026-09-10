# 高德平台类型：使用 Web服务（Web Service）而非 Web端（JS API）

后端 `AmapClientImpl` 需在服务端（非浏览器）用 HTTP GET 拉取成都武侯区（adcode `510107`）天气与路况 JSON，再下发给 Vue 前端展示。决定选用**Web服务（Web Service）**平台及其 `restapi.amap.com` REST 接口，而不是 Web端（JS API）。原因：数据由 Spring Boot 服务端去拉取，无需任何浏览器侧 SDK 或密钥安全下发，且 Web服务 Key 不配域名白名单、不依赖浏览器 Referer；Web服务在整个请求-响应链路中 Key 只出现在后端，前端仅拿到结果 JSON。

## Considered Options

- **Web端（JS API）**：浏览器端 JS SDK（`AMap.` 全局对象，JS API v2.0），加载于 `https://webapi.amap.com/maps?v=2.0&key=...`。需 Web端 JS API Key + **安全密钥 `securityJsCode`**（自 2021-12-02 后申请必须）、并在控制台配置**域名白名单（安全域名/Referer 校验）**，Key 暴露在前端可被嗅探→生产上常需代理转发。**不适合本项目**：需求是后端拉取数据，且前端无交互地图，无必要引入浏览器 SDK 与密钥泄露风险。
- **Web服务（Web Service）**：服务端 REST API，域名 `https://restapi.amap.com`，Key 在控制台「创建应用→添加 Key→服务类型选择『Web服务』」获取，仅需在网络侧做**IP 白名单**（可选），无需安全密钥与域名白名单。**本项目选用**：服务端无 CORS 问题、Key 不落浏览器、无需安全密钥/白名单。

## 结论要点（含引用文档）

**1. 平台与 Key**：Web服务基址 `https://restapi.amap.com`；Key 在控制台「应用管理→我的应用→创建新应用→添加 Key→服务类型选「Web服务」」申请，key 为必填参数（探测返回 `INVALID_USER_KEY`/infocode 10001，证明接口路径有效、仅 key 无效）。本文 `amap.key` 应持有 **Web服务 Key**（非 JS API Key）。

**2. 天气**：`GET /v3/weather/weatherInfo`，必填 `key`、`city`（adcode，如 `510107`，勿传城市名）。⚠️ 实测：`extensions=all` 返回 `forecasts[]`（预报）而**不含 `lives`**；要获得实况 `lives[]` 必须用 `extensions=base`。`lives[]` 含 `province/city/weather/temperature/winddirection/windpower/reporttime`。现状代码已改为 `extensions=base` 读取 `lives[]`。

**3. 路况**：⚠️ 实测 `GET /v3/traffic/status/road`（按 `name` 道路名）返回 `INVALID_PARAMS(20000)/UNKNOWN_ERROR(20003)`，道路名需与高德路网精确匹配、稳定性差。**改用矩形批量接口 `GET /v3/traffic/status/rectangle`**（必填 `rectangle` 左下、右上经纬度分号分隔，如 `104.03,30.60;104.10,30.66`），响应 `trafficinfo.roads[].name/.status`，其中 `status` 为数值等级（1=畅通/2=缓慢/3=拥堵/4=严重拥堵），`status_desc` 在该接口多为空。现状代码用 rectangle 并把 `status` 映射为文字文案。

**4. 配额/必填**：天气为免费 Web服务 API（免费额度约 30 万次/日、约 200 并发/秒，需先申请 Key）；key 一律必填。建议服务端中转调用（即本项目），避免跨域与 Key 暴露。

**5. 无需项**：Web服务**不**需要安全密钥 `securityJsCode`、也**不**需要域名白名单——该两项仅针对 Web端（JS API）。

## Primary sources

- 天气查询 Web服务文档（高德开放平台）：https://lbs.amap.com/api/webservice/guide/api-advanced/weatherinfo （镜像 https://developer.amap.com/api/webservice/guide/api-advanced/weatherinfo ）
- 交通态势查询 Web服务文档：https://lbs.amap.com/api/webservice/guide/api-advanced/traffic-situation-inquiry
- Web服务 Key 申请说明：https://lbs.amap.com/api/webservice/guide/create-project/get-key
- Web端 JS API 2.0 概述（浏览器 SDK/AMap/webapi.amap.com）：https://lbs.amap.com/api/javascript-api-v2/summary
- Web端 JS API 安全密钥 securityJsCode / 域名白名单：https://lbs.amap.com/api/javascript-api-v2/guide/abc/jscode
- 接口路径实测：`https://restapi.amap.com/v3/weather/weatherInfo`、`/v3/traffic/status/road`、`/v3/traffic/status/rectangle`（均返回 webapi 已识别、仅 key 无效）
