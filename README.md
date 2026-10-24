# agri-assist 乡村数字化助农服务平台

面向农户与采购方的乡村数字化助农服务平台，**以 RAG 本地知识库智能咨询为差异化内核**。

> 本项目为本科毕业设计配套工程，技术栈：Spring Boot 3 + Vue 3 + MySQL + Ollama（本地大模型）。

## 功能模块（6 核心）
1. 用户管理（三角色：农户 / 采购方 / 管理员，JWT 鉴权）
2. 农产品供需发布与智能匹配（品类 + 地区 + 价格 ±20% 规则）
3. 农产品预售预约
4. **AI 智能咨询（RAG）**：基于本地大模型（Ollama）+ 向量检索，数据不出域
5. 农事互助社区（发帖 / 评论 / 采纳最佳答案）
6. 信息审核与数据统计（多态审核：供需 / 资讯 / 帖子）

## 目录结构
```
agri-assist/
├── backend/      Spring Boot 3 后端
├── frontend/     Vue 3 + Vite 前端
└── db/schema.sql 数据库结构
```

## 环境要求
- JDK 17+（已验证：C:\Program Files\Java\jdk-17.0.2）
- Maven 3.9+
- Node 22+（前端）
- MySQL 8
- **AI 模块当前为 `ollama` 模式（本地大模型）**：需本机安装 Ollama 并拉取模型（见下方「Ollama 安装与启动」）
  - 另支持 `mode: local`（纯本地检索、零依赖）与 `mode: cloud`（本地检索 + 云端混元生成，需 `HUNYUAN_API_KEY`）两种模式，可在 `application.yml` 的 `agri.ai.mode` 自由切换

## 后端启动
```bash
# 使用 JDK 17
export JAVA_HOME="C:/Program Files/Java/jdk-17.0.2"
cd backend
mvn spring-boot:run
```
访问：http://localhost:8080/api/swagger-ui.html

> 若使用云端模式，启动前先设置环境变量（Linux/macOS：`export HUNYUAN_API_KEY=你的key`；Windows PowerShell：`$env:HUNYUAN_API_KEY="你的key"`），也可直接写进 `application.yml` 的 `agri.ai.cloud.api-key`。

## 前端启动
```bash
cd frontend
npm install
npm run dev
```

## AI 模块说明（本地检索 + 生成 架构）
`com.agriassist.service.ai` 包下：
- `LocalEmbedder`：本地轻量文本向量化（字符 n-gram + 哈希，零模型、零内存占用）
- `KnowledgeService`：知识入库（本地向量化）+ 向量检索（余弦相似度，带分数）
- `AiConsultService`：本地检索 top-3 → 按 `agri.ai.mode` 决定生成方式
- `CloudLlmClient`：云端大模型客户端（OpenAI 兼容，默认混元，key 走环境变量）
- `OllamaClient`：本地 Ollama 客户端（仅 `mode=ollama` 时启用）

三种模式（见 `application.yml` 的 `agri.ai.mode`）：
- `local`：直接返回检索到的知识片段，不调用大模型
- `cloud`：检索片段作为上下文，调用云端大模型生成自然语言回答
- `ollama`（当前启用）：检索片段作为上下文，调用本地 Ollama 模型生成回答

调试接口：`GET /api/ai/mode` 可查看当前模式与配置状态。

### Ollama 安装与启动（ollama 模式必做）
1. 下载安装：https://ollama.com/download → 选 Windows，双击安装。安装后任务栏出现 🦙 图标即本地服务已起（默认端口 11434，与 `application.yml` 的 `agri.ai.ollama.base-url` 一致）。
2. 拉取生成模型（仅需这一个，检索不依赖 Ollama）：
   ```bash
   ollama pull qwen2.5:7b
   ```
   - 若内存紧张（跑 7b 吃约 8GB），改用更小的 `qwen2.5:3b`，并把 `application.yml` 第 31 行 `model: "qwen2.5:7b"` 改为 `qwen2.5:3b`。
3. 验证就绪：
   ```bash
   ollama list        # 能看到 qwen2.5
   # 浏览器访问 http://localhost:11434/api/tags 返回 JSON 即正常
   ```
4. 启动后端后，通过 `POST /api/ai/consult?question=番茄晚疫病怎么治` 或前端 AI 咨询页体验本地问答。

> 注：本架构检索使用内置 `LocalEmbedder`（零模型、零内存），因此 ollama 模式下本机只需加载 `qwen2.5` 一个生成模型，比"embedding+生成都用 Ollama"省一半内存，且数据完全不出本机。

> 演示用将向量以 JSON 存于 `knowledge_chunk.embedding`；生产建议改用 PGVector / Chroma 向量库。
