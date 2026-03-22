# Novel-RAG

基于 RAG（检索增强生成）技术的智能小说生成系统。

## 项目简介

Novel-RAG 允许用户通过 Web 界面，基于已有小说内容自动生成新的章节。系统利用向量检索技术找到相关内容片段，结合大语言模型生成连贯、风格一致的章节内容。同时支持网络爬虫功能，可根据小说名自动抓取网络上的小说章节。

## 核心功能

- 小说文档上传与解析（支持 txt、pdf、epub、docx）
- 网络爬虫自动抓取小说
- 文档分块与向量化存储
- 基于语义相似度的内容检索
- 智能章节生成
- SSE 流式输出
- Web 交互界面

## 技术栈

### 后端
- Java 17+ / Spring Boot 3.x
- Spring AI
- 阿里云百炼（LLM & Embedding）
- Redis Stack（向量数据库）
- MySQL 8.0
- Maven 3.9+
- Jsoup + Spring WebFlux（爬虫）

### 前端
- Vue 3
- TypeScript
- Webpack

### 部署
- Docker Compose

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                   Vue 3 + TypeScript 前端                    │
│           (上传 / 爬虫 / 生成 / 展示)                         │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTP/REST API
┌─────────────────────────▼───────────────────────────────────┐
│                    Spring Boot 后端                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ 小说管理模块  │  │  RAG服务模块  │  │  生成服务模块 │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│  ┌──────────────┐                                           │
│  │  爬虫服务模块  │                                           │
│  └──────────────┘                                           │
└──────────────────────────────────────────────────────────────┘
        │                  │                  │
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  Redis Stack  │  │    MySQL     │  │  阿里云百炼   │
│  (向量存储)   │  │  (关系数据)   │  │  (LLM API)   │
└──────────────┘  └──────────────┘  └──────────────┘
```

## 快速开始

### 环境要求
- Docker & Docker Compose
- JDK 17+
- Maven 3.9+
- Node.js 18+（前端开发）

### 环境配置

1. 设置环境变量：
```bash
export DASHSCOPE_API_KEY=your_api_key_here
```

2. 启动服务：
```bash
docker-compose up -d
```

3. 访问应用：
- 前端界面：http://localhost
- 后端 API：http://localhost:8080
- Redis Insight：http://localhost:8001

### 开发模式

1. 启动基础服务：
```bash
docker-compose up -d redis-stack mysql
```

2. 运行后端：
```bash
mvn spring-boot:run
```

3. 运行前端：
```bash
cd frontend
npm install
npm run dev
```

## API 接口

### 小说管理
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/novels/upload | 上传小说文件 |
| GET | /api/novels | 获取小说列表 |
| GET | /api/novels/{id} | 获取小说详情 |
| DELETE | /api/novels/{id} | 删除小说 |

### 爬虫服务
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/crawler/search | 搜索小说来源 |
| POST | /api/crawler/start | 启动爬虫任务 |
| GET | /api/crawler/task/{taskId} | 查询任务状态 |

### 章节生成
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/generate/chapter | 生成章节 |
| GET | /api/generate/chapter/stream | 流式生成（SSE） |

## 项目结构

```
novel-rag/
├── pom.xml
├── docker-compose.yml
├── Dockerfile
├── README.md
├── PLAN.md
├── src/
│   └── main/
│       ├── java/com/novel/rag/
│       │   ├── config/
│       │   ├── controller/
│       │   ├── service/
│       │   ├── crawler/
│       │   ├── model/
│       │   └── rag/
│       └── resources/
│           ├── application.yml
│           └── prompts/
└── frontend/
    ├── package.json
    ├── src/
    │   ├── views/
    │   ├── components/
    │   ├── api/
    │   └── types/
    └── public/
```

## 配置说明

主要配置项（`application.yml`）：

```yaml
# 阿里云百炼配置
spring.ai.dashscope:
  api-key: ${DASHSCOPE_API_KEY}
  chat.options.model: qwen-max-longcontext
  embedding.options.model: text-embedding-v3

# Redis 向量配置
vector.redis:
  index-name: novel_vectors
  dimensions: 1024
```

## 许可证

本项目仅供学习研究使用。爬取小说内容时请尊重版权。

## 参考资源

- [Spring AI 文档](https://docs.spring.io/spring-ai/reference/)
- [阿里云百炼平台](https://bailian.console.aliyun.com/)
- [Redis Stack 向量搜索](https://redis.io/docs/interact/search-and-query/search/vectors/)
- [Vue 3 文档](https://vuejs.org/)
