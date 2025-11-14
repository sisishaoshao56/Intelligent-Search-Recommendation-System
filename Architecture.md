**1. 场景和数据定稿**

- 选定业务场景（视频推荐）。
- 准备初始数据：可用公开数据集或自造 CSV/JSON，包含用户、内容、行为三表。
- 设计推荐指标：如点击率、转化率、覆盖率，后面展示用。

**2. 数据管道与存储**

- ETL：写简单脚本/定时任务，把原始数据导入 MySQL（业务数据）+ Elasticsearch（搜索召回）。
- 可引入 Kafka→Flink 只是加分项，时间不够可先用定时批处理。
- 建好 Redis 缓存结构（热门榜、用户特征缓存）。

**3. 推荐服务核心**

- 技术栈：Spring Boot + MyBatis + Redis + Elasticsearch。
- 召回：实现多策略（内容相似、协同过滤、热门）组合，用 ES/向量相似度相似。
- 粗排：给召回结果打分（权重函数或轻量模型，如 XGBoost 线下训练，线上加载模型文件）。
- 重排：加入业务规则（多样性、曝光控制、冷启动 fallback）。

**4. 反馈闭环与监控**

- 暴露 /recommend、/feedback 等 REST API；反馈写入行为表并触发模型/特征更新。
- 加一个简易仪表盘（前端或 Swagger + Grafana）展示调用量、CTR、延迟。
- 加 A/B 实验参数（header/请求参数决定策略），结果写库供分析。

**5. 工程化与展示**

- OpenAPI/Swagger 文档确保接口清晰。
- 提供 Docker Compose 启动（app+MySQL+Redis+ES）。
- 准备示例脚本或 Postman collection 演示推荐→反馈→指标更新流程。
- README 讲架构、数据流程和亮点；附 demo 截图或短录屏。



