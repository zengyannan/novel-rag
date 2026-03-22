# 小说RAG应用开发计划

## 1. 项目概述

### 1.1 项目名称
Novel-RAG：基于RAG技术的智能小说生成系统

### 1.2 项目描述
构建一个小说RAG（检索增强生成）应用，用户可以通过前端界面，基于RAG小说库中的内容，自动生成新的小说章节。系统利用向量检索技术找到相关内容片段，结合大语言模型生成连贯、风格一致的章节内容。同时支持网络爬虫功能，可根据小说名自动抓取网络上的小说章节。

### 1.3 核心功能
- 小说文档上传与解析
- **网络爬虫自动抓取小说**
- 文档分块与向量化存储
- 基于语义相似度的内容检索
- 智能章节生成
- 前端交互界面

---

## 2. 技术架构

### 2.1 技术栈

| 层级 | 技术选型 |
|------|----------|
| 后端框架 | Java 17+ / Spring Boot 3.x |
| 构建工具 | Maven 3.9+ |
| AI框架 | Spring AI (最新稳定版) |
| LLM提供商 | 阿里云百炼平台 |
| 向量数据库 | Redis Stack (Docker) |
| 关系数据库 | MySQL 8.0 (Docker) |
| 爬虫框架 | Jsoup + Spring WebFlux (WebClient) |
| 前端框架 | Vue 3 + TypeScript + Webpack |
| 部署环境 | 本地 Docker Compose |

### 2.2 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                   Vue 3 + TypeScript 前端                    │
│        (小说上传 / 爬虫抓取 / 章节生成 / 内容展示)              │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTP/REST API
┌─────────────────────────▼───────────────────────────────────┐
│                    Spring Boot 后端                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │  小说管理模块  │  │  RAG服务模块  │  │  生成服务模块  │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│  ┌──────────────┐                                           │
│  │  爬虫服务模块  │                                           │
│  └──────────────┘                                           │
│                          │                                   │
│              ┌───────────▼───────────┐                       │
│              │     Spring AI        │                       │
│              │  (Embedding + LLM)   │                       │
│              └───────────┬───────────┘                       │
└──────────────────────────┼──────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        ▼                  ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  Redis Stack  │  │    MySQL     │  │  阿里云百炼   │
│  (向量存储)   │  │  (关系数据)   │  │  (LLM API)   │
└──────────────┘  └──────────────┘  └──────────────┘
        │
        │         ┌──────────────────┐
        └────────►│  小说网站/平台    │
                  │  (免费/正版API)   │
                  └──────────────────┘
                           │
                  ┌────────┴────────┐
                  │   Docker 环境    │
                  └─────────────────┘
```

### 2.3 Docker 服务架构

```
docker-compose.yml
├── redis-stack      # 向量数据库 + 缓存 (端口: 6379, 8001)
├── mysql            # 关系数据库 (端口: 3306)
├── novel-rag-app    # Spring Boot 应用 (端口: 8080)
└── frontend         # Vue 前端应用 (端口: 80)
```

---

## 3. 阿里云百炼模型选型

### 3.1 推荐模型配置

| 用途 | 推荐模型 | 说明 |
|------|----------|------|
| **文本生成** | qwen-max-longcontext | 32K上下文，适合长文本小说生成 |
| **文本生成(备选)** | qwen-max | 更强的推理能力，4K上下文 |
| **文本生成(经济)** | qwen-plus | 性价比高，适合测试阶段 |
| **文本向量化** | text-embedding-v3 | 最新版嵌入模型，1024维向量 |

### 3.2 模型详细说明

#### 生成模型推荐

| 模型名称 | 上下文长度 | 输出长度 | 特点 | 适用场景 |
|----------|-----------|----------|------|----------|
| **qwen-max-longcontext** ⭐推荐 | 32K | 6K | 超长上下文，适合小说创作 | 正式生产环境 |
| qwen-max | 4K | 2K | 最强推理，创意写作能力强 | 短章节生成 |
| qwen-plus | 128K | 6K | 性价比高，速度快 | 开发测试 |
| qwen-turbo | 128K | 6K | 最快响应，成本最低 | 简单场景 |

#### Embedding模型推荐

| 模型名称 | 向量维度 | 最大文本长度 | 特点 |
|----------|----------|--------------|------|
| **text-embedding-v3** ⭐推荐 | 1024 | 8192 tokens | 最新版，支持多语言 |
| text-embedding-v2 | 1536 | 2048 tokens | 稳定版，兼容性好 |

### 3.3 百炼平台配置

```yaml
# Spring AI 阿里云百炼配置
spring:
  ai:
    dashscope:  # 阿里云百炼API
      api-key: ${DASHSCOPE_API_KEY}
      chat:
        enabled: true
        options:
          model: qwen-max-longcontext
          temperature: 0.8  # 创意写作可适当提高
          top-p: 0.9
          max-tokens: 4096
      embedding:
        enabled: true
        options:
          model: text-embedding-v3
```

---

## 4. 功能模块设计

### 4.1 小说管理模块
- **文档上传**：支持 txt、pdf、epub、docx 格式
- **文档解析**：提取文本内容，识别章节结构
- **元数据管理**：书名、作者、类型、标签等

### 4.2 爬虫服务模块 ⭐新增

#### 4.2.1 功能概述
根据小说名称自动在网络上搜索并抓取小说章节，支持多种数据源。

#### 4.2.2 数据源支持

| 数据源类型 | 支持网站/平台 | 抓取方式 |
|-----------|--------------|---------|
| 免费小说网站 | 笔趣阁、起点中文网(免费章节)等 | Jsoup解析HTML |
| 正版平台API | 阅文集团、晋江文学城等 | 官方API对接(需授权) |
| 搜索引擎 | 百度、Google搜索结果 | 搜索结果解析 |

#### 4.2.3 存储策略
- **原文链接存储**：仅保存章节索引和原文URL链接
- **元数据存储**：书名、作者、章节标题、来源平台
- **按需获取**：RAG处理时根据链接实时获取原文内容
- **预留扩展**：支持后期全量存储到本地或OSS

#### 4.2.4 核心组件设计

```
爬虫服务模块
├── CrawlerService          # 爬虫调度服务
├── SiteParserRegistry      # 站点解析器注册表
├── parser/
│   ├── SiteParser          # 站点解析器接口
│   ├── BiqugeParser        # 笔趣阁解析器
│   ├── QidianParser        # 起点解析器
│   └── GenericParser       # 通用解析器
├── fetcher/
│   ├── HttpFetcher         # HTTP请求获取器 (Jsoup)
│   └── ApiFetcher          # API请求获取器 (WebClient)
├── model/
│   ├── ChapterIndex        # 章节索引实体
│   └── CrawlTask           # 爬虫任务实体
└── scheduler/
    └── CrawlTaskScheduler  # 爬虫任务调度器
```

#### 4.2.5 反爬策略
- 随机User-Agent轮换
- 请求频率限制与延迟
- 代理IP池支持(可选)
- Cookie/Session管理

### 4.3 RAG服务模块
- **文本分块**：按段落/章节进行智能分块（推荐500-1000字/块）
- **向量化**：使用阿里云 text-embedding-v3 模型
- **向量存储**：存入 Redis Stack 向量索引
- **相似度检索**：基于余弦相似度检索相关片段

### 4.4 生成服务模块
- **Prompt工程**：设计小说生成提示词模板
- **上下文构建**：整合检索结果构建生成上下文
- **流式输出**：支持SSE流式返回生成内容
- **风格控制**：保持与原作风格一致

### 4.5 前端模块
- **小说库管理**：上传、查看、删除小说
- **爬虫界面**：输入小说名，一键抓取
- **生成界面**：输入主题/大纲，生成新章节
- **内容预览**：实时展示生成的章节内容
- **流式展示**：打字机效果展示生成内容

---

## 5. 开发阶段规划

### 阶段一：项目初始化（第1-2天）
- [ ] 创建Maven项目结构
- [ ] 配置pom.xml依赖（Spring AI + 阿里云百炼 + 爬虫依赖）
- [ ] 搭建Spring Boot基础框架
- [ ] 编写Docker Compose配置
- [ ] 配置Redis Stack向量索引

### 阶段二：核心功能开发（第3-7天）
- [ ] 实现文档上传与解析服务
- [ ] 实现文本分块策略
- [ ] 集成阿里云百炼Embedding模型
- [ ] 实现Redis Stack向量存储与检索
- [ ] 实现百炼LLM调用服务
- [ ] 实现RAG生成流程

### 阶段三：爬虫模块开发（第8-12天）⭐新增
- [ ] 设计爬虫服务架构
- [ ] 实现HTTP请求获取器(Jsoup)
- [ ] 实现通用HTML解析器
- [ ] 实现笔趣阁站点解析器
- [ ] 实现章节索引存储服务
- [ ] 实现爬虫任务调度器
- [ ] 实现反爬策略(延迟、UA轮换)

### 阶段四：API接口开发（第13-15天）
- [ ] 设计RESTful API
- [ ] 实现小说管理接口
- [ ] 实现爬虫任务接口
- [ ] 实现章节生成接口
- [ ] 实现SSE流式输出接口
- [ ] 添加接口文档(Swagger/Knife4j)

### 阶段五：前端开发（第16-20天）
- [ ] 搭建Vue 3 + TypeScript + Webpack项目
- [ ] 配置前端开发环境
- [ ] 实现小说管理页面
- [ ] 实现爬虫抓取页面
- [ ] 实现章节生成界面
- [ ] 实现流式内容展示（EventSource）

### 阶段六：测试与部署（第21-23天）
- [ ] 编写单元测试
- [ ] 编写集成测试
- [ ] 编写Dockerfile
- [ ] Docker Compose编排
- [ ] 本地部署验证

---

## 6. 项目目录结构

```
novel-rag/
├── pom.xml                              # Maven配置文件
├── docker-compose.yml                   # Docker编排文件
├── Dockerfile                           # 后端镜像构建
├── README.md                            # 项目说明
├── PLAN.md                              # 开发计划
│
├── src/
│   ├── main/
│   │   ├── java/com/novel/rag/
│   │   │   ├── NovelRagApplication.java       # 启动类
│   │   │   │
│   │   │   ├── config/                        # 配置类
│   │   │   │   ├── AiConfig.java              # AI配置
│   │   │   │   ├── CrawlerConfig.java         # 爬虫配置 ⭐新增
│   │   │   │   ├── RedisVectorConfig.java     # Redis向量配置
│   │   │   │   └── WebConfig.java             # Web配置
│   │   │   │
│   │   │   ├── controller/                    # 控制器
│   │   │   │   ├── NovelController.java
│   │   │   │   ├── CrawlerController.java     # 爬虫控制器 ⭐新增
│   │   │   │   └── GenerationController.java
│   │   │   │
│   │   │   ├── service/                       # 服务层
│   │   │   │   ├── NovelService.java
│   │   │   │   ├── DocumentService.java
│   │   │   │   ├── EmbeddingService.java      # 百炼Embedding
│   │   │   │   ├── VectorStoreService.java    # Redis向量存储
│   │   │   │   ├── GenerationService.java     # 百炼LLM调用
│   │   │   │   └── crawler/                   # 爬虫服务 ⭐新增
│   │   │   │       ├── CrawlerService.java    # 爬虫调度服务
│   │   │   │       ├── SiteParserRegistry.java # 解析器注册表
│   │   │   │       └── ChapterFetchService.java # 章节获取服务
│   │   │   │
│   │   │   ├── crawler/                       # 爬虫模块 ⭐新增
│   │   │   │   ├── parser/
│   │   │   │   │   ├── SiteParser.java        # 解析器接口
│   │   │   │   │   ├── BiqugeParser.java      # 笔趣阁解析器
│   │   │   │   │   ├── QidianParser.java      # 起点解析器
│   │   │   │   │   └── GenericParser.java     # 通用解析器
│   │   │   │   ├── fetcher/
│   │   │   │   │   ├── HttpFetcher.java       # HTTP获取器
│   │   │   │   │   └── ApiFetcher.java        # API获取器
│   │   │   │   └── strategy/
│   │   │   │       ├── AntiCrawlStrategy.java # 反爬策略
│   │   │   │       └── ProxyManager.java      # 代理管理
│   │   │   │
│   │   │   ├── model/                         # 数据模型
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Novel.java
│   │   │   │   │   ├── ChapterIndex.java      # 章节索引 ⭐新增
│   │   │   │   │   ├── CrawlTask.java         # 爬虫任务 ⭐新增
│   │   │   │   │   └── DocumentChunk.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── GenerateRequest.java
│   │   │   │   │   ├── NovelUploadDto.java
│   │   │   │   │   ├── CrawlRequest.java      # 爬虫请求 ⭐新增
│   │   │   │   │   └── CrawlResultDto.java    # 爬虫结果 ⭐新增
│   │   │   │   └── vo/
│   │   │   │       └── GenerateResultVo.java
│   │   │   │
│   │   │   ├── repository/                    # 数据访问层
│   │   │   │   ├── NovelRepository.java
│   │   │   │   ├── ChapterIndexRepository.java # ⭐新增
│   │   │   │   └── CrawlTaskRepository.java   # ⭐新增
│   │   │   │
│   │   │   ├── rag/                           # RAG核心模块
│   │   │   │   ├── TextSplitter.java          # 文本分块
│   │   │   │   ├── RagPipeline.java           # RAG流水线
│   │   │   │   └── PromptTemplate.java        # 提示词模板
│   │   │   │
│   │   │   └── util/                          # 工具类
│   │   │       ├── DocumentParser.java
│   │   │       ├── RedisVectorUtil.java
│   │   │       └── CrawlerUtils.java          # 爬虫工具 ⭐新增
│   │   │
│   │   └── resources/
│   │       ├── application.yml                # 应用配置
│   │       ├── application-dev.yml            # 开发环境配置
│   │       ├── crawlers/                      # 爬虫配置 ⭐新增
│   │       │   └── sites.yml                  # 站点配置
│   │       └── prompts/                       # 提示词模板
│   │           └── novel-generation.txt
│   │
│   └── test/
│       └── java/com/novel/rag/                # 测试代码
│
└── frontend/                                 # Vue 3 + TypeScript 前端
    ├── package.json
    ├── tsconfig.json
    ├── webpack.config.js
    ├── src/
    │   ├── main.ts                           # 入口文件
    │   ├── App.vue
    │   ├── views/
    │   │   ├── NovelManage.vue               # 小说管理页
    │   │   ├── CrawlerView.vue               # 爬虫页面 ⭐新增
    │   │   └── ChapterGenerate.vue           # 章节生成页
    │   ├── components/
    │   │   ├── NovelUpload.vue               # 上传组件
    │   │   ├── CrawlerForm.vue               # 爬虫表单 ⭐新增
    │   │   ├── ChapterEditor.vue             # 编辑器组件
    │   │   └── StreamOutput.vue              # 流式输出组件
    │   ├── api/
    │   │   ├── novel.ts                      # 小说API
    │   │   ├── crawler.ts                    # 爬虫API ⭐新增
    │   │   └── generate.ts                   # 生成API
    │   ├── types/
    │   │   └── index.ts                      # TypeScript类型定义
    │   └── utils/
    │       └── request.ts                    # HTTP请求封装
    └── public/
        └── index.html
```

---

## 7. Maven依赖规划

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <groupId>com.novel</groupId>
    <artifactId>novel-rag</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>novel-rag</name>
    <description>小说RAG智能生成系统</description>

    <properties>
        <java.version>17</java.version>
        <spring-ai.version>1.0.0-M4</spring-ai.version>
        <jsoup.version>1.17.2</jsoup.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot WebFlux (爬虫异步HTTP) ⭐新增 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>

        <!-- Spring Boot Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Spring AI Core -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-core</artifactId>
        </dependency>

        <!-- Spring AI 阿里云百炼 -->
        <dependency>
            <groupId>com.alibaba.cloud.ai</groupId>
            <artifactId>spring-ai-alibaba-starter</artifactId>
            <version>1.0.0-M2</version>
        </dependency>

        <!-- Redis (向量存储 + 缓存) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>redis.clients</groupId>
            <artifactId>jedis</artifactId>
        </dependency>

        <!-- Jedis JSON 支持 (向量操作需要) -->
        <dependency>
            <groupId>com.redis</groupId>
            <artifactId>jedisearch</artifactId>
            <version>4.1.0</version>
        </dependency>

        <!-- MySQL -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- 爬虫相关依赖 ⭐新增 -->
        <!-- Jsoup HTML解析器 -->
        <dependency>
            <groupId>org.jsoup</groupId>
            <artifactId>jsoup</artifactId>
            <version>${jsoup.version}</version>
        </dependency>

        <!-- Selenium (动态页面抓取，可选) -->
        <dependency>
            <groupId>org.seleniumhq.selenium</groupId>
            <artifactId>selenium-java</artifactId>
            <version>4.18.1</version>
            <optional>true</optional>
        </dependency>

        <!-- 文档解析 -->
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-ooxml</artifactId>
            <version>5.2.5</version>
        </dependency>
        <dependency>
            <groupId>org.apache.pdfbox</groupId>
            <artifactId>pdfbox</artifactId>
            <version>3.0.1</version>
        </dependency>
        <dependency>
            <groupId>nl.siegmann.epublib</groupId>
            <artifactId>epublib-core</artifactId>
            <version>3.1</version>
        </dependency>

        <!-- API文档 -->
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
            <version>4.4.0</version>
        </dependency>

        <!-- 工具库 -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>5.8.25</version>
        </dependency>

        <!-- 测试 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>${spring-ai.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <repositories>
        <repository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>https://repo.spring.io/milestone</url>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </repository>
    </repositories>
</project>
```

---

## 8. 核心API设计

### 8.1 小说管理API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/novels/upload | 上传小说文件 |
| GET | /api/novels | 获取小说列表 |
| GET | /api/novels/{id} | 获取小说详情 |
| DELETE | /api/novels/{id} | 删除小说 |
| GET | /api/novels/{id}/chunks | 获取小说分块列表 |

### 8.2 爬虫API ⭐新增

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/crawler/search | 搜索小说(返回可用来源列表) |
| POST | /api/crawler/start | 启动爬虫任务 |
| GET | /api/crawler/task/{taskId} | 查询爬虫任务状态 |
| GET | /api/crawler/task/{taskId}/chapters | 获取已抓取的章节列表 |
| DELETE | /api/crawler/task/{taskId} | 取消/删除爬虫任务 |
| POST | /api/crawler/fetch-chapter | 按需获取章节内容(通过链接) |

### 8.3 章节生成API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/generate/chapter | 生成新章节 |
| GET | /api/generate/chapter/stream | 流式生成章节 (SSE) |

### 8.4 请求/响应示例

#### 爬虫搜索请求
```json
// POST /api/crawler/search
// Request
{
    "novelName": "斗破苍穹",
    "author": "天蚕土豆",
    "sources": ["biquge", "qidian"]  // 可选，指定来源
}

// Response
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "sourceId": "biquge_12345",
            "sourceName": "笔趣阁",
            "novelName": "斗破苍穹",
            "author": "天蚕土豆",
            "chapterCount": 1648,
            "lastUpdate": "2024-01-15",
            "url": "https://example.com/novel/12345"
        },
        {
            "sourceId": "qidian_67890",
            "sourceName": "起点中文网",
            "novelName": "斗破苍穹",
            "author": "天蚕土豆",
            "chapterCount": 1648,
            "lastUpdate": "2024-01-15",
            "url": "https://www.qidian.com/book/67890"
        }
    ]
}
```

#### 启动爬虫任务
```json
// POST /api/crawler/start
// Request
{
    "sourceId": "biquge_12345",
    "novelName": "斗破苍穹",
    "author": "天蚕土豆",
    "startChapter": 1,
    "endChapter": 100,  // 可选，不填则抓取全部
    "storeContent": false  // 是否存储正文内容，默认false只存链接
}

// Response
{
    "code": 200,
    "message": "success",
    "data": {
        "taskId": "crawl_20240315_001",
        "status": "RUNNING",
        "totalChapters": 100,
        "crawledChapters": 0
    }
}
```

#### 章节生成请求
```json
// POST /api/generate/chapter
// Request
{
    "novelId": 1,
    "prompt": "写一段主角与反派决斗的场景",
    "chapterTitle": "决战",
    "style": "热血",
    "maxLength": 2000,
    "temperature": 0.8
}

// Response
{
    "code": 200,
    "message": "success",
    "data": {
        "chapterTitle": "决战",
        "content": "生成的章节内容...",
        "wordCount": 1850,
        "model": "qwen-max-longcontext"
    }
}
```

### 8.5 SSE流式生成

```
GET /api/generate/chapter/stream?novelId=1&prompt=...

Response: text/event-stream
data: {"type": "start", "chapterTitle": "决战"}
data: {"type": "content", "text": "月色"}
data: {"type": "content", "text": "如水"}
data: {"type": "content", "text": "..."}
data: {"type": "done", "wordCount": 1850}
```

---

## 9. 配置文件规划

### 9.1 application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: novel-rag

  # 阿里云百炼配置
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
      chat:
        enabled: true
        options:
          model: qwen-max-longcontext
          temperature: 0.8
          top-p: 0.9
          max-tokens: 4096
      embedding:
        enabled: true
        options:
          model: text-embedding-v3

  # MySQL配置
  datasource:
    url: jdbc:mysql://localhost:3306/novel_rag?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: root
    password: ${DB_PASSWORD:root123}
    driver-class-name: com.mysql.cj.jdbc.Driver

  # Redis配置
  data:
    redis:
      host: localhost
      port: 6379
      password: ${REDIS_PASSWORD:}
      database: 0

  # JPA配置
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect

# Redis向量配置
vector:
  redis:
    index-name: novel_vectors
    prefix: "novel:chunk:"
    dimensions: 1024  # text-embedding-v3 维度

# 爬虫配置 ⭐新增
crawler:
  enabled: true
  # 请求配置
  request:
    timeout: 30000          # 请求超时(毫秒)
    retry-times: 3          # 重试次数
    retry-delay: 2000       # 重试延迟(毫秒)
  # 反爬配置
  anti-crawl:
    user-agent-rotation: true     # UA轮换
    request-delay-min: 1000       # 最小请求延迟(毫秒)
    request-delay-max: 3000       # 最大请求延迟(毫秒)
    proxy-enabled: false          # 代理启用
  # 存储配置
  storage:
    store-content: false    # 是否存储正文内容(默认只存链接)
    content-expire-days: 30 # 内容缓存过期天数

# 文件上传配置
spring.servlet.multipart:
  max-file-size: 50MB
  max-request-size: 50MB

# 日志配置
logging:
  level:
    com.novel.rag: DEBUG
    org.springframework.ai: DEBUG
```

### 9.2 爬虫站点配置 (crawlers/sites.yml) ⭐新增

```yaml
# 爬虫站点配置
sites:
  # 笔趣阁
  - id: biquge
    name: 笔趣阁
    enabled: true
    type: html  # html 或 api
    baseUrl: https://www.biquge.com.cn
    parserClass: com.novel.rag.crawler.parser.BiqugeParser
    patterns:
      search: /search.php?q={keyword}
      novel: /book/{novelId}/
      chapter: /book/{novelId}/{chapterId}.html
    rateLimit: 2  # 每秒请求数限制

  # 起点中文网
  - id: qidian
    name: 起点中文网
    enabled: true
    type: html
    baseUrl: https://www.qidian.com
    parserClass: com.novel.rag.crawler.parser.QidianParser
    patterns:
      search: /search?kw={keyword}
      novel: /book/{novelId}
    rateLimit: 1

# User-Agent池
userAgents:
  - "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
  - "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
  - "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:123.0) Gecko/20100101 Firefox/123.0"
  - "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.3 Safari/605.1.15"
```

### 9.3 application-dev.yml

```yaml
spring:
  ai:
    dashscope:
      chat:
        options:
          model: qwen-plus  # 开发环境使用经济模型

crawler:
  anti-crawl:
    request-delay-min: 500   # 开发环境减少延迟
    request-delay-max: 1000

logging:
  level:
    com.novel.rag: DEBUG
    com.novel.rag.crawler: TRACE  # 爬虫详细日志
```

### 9.4 application-prod.yml

```yaml
spring:
  ai:
    dashscope:
      chat:
        options:
          model: qwen-max-longcontext  # 生产环境使用长上下文模型

crawler:
  anti-crawl:
    request-delay-min: 2000   # 生产环境增加延迟
    request-delay-max: 5000

logging:
  level:
    com.novel.rag: INFO
```

---

## 10. Docker Compose 配置

### 10.1 docker-compose.yml

```yaml
version: '3.8'

services:
  # Redis Stack (向量数据库)
  redis-stack:
    image: redis/redis-stack:latest
    container_name: novel-redis
    ports:
      - "6379:6379"    # Redis端口
      - "8001:8001"    # RedisInsight端口
    volumes:
      - redis_data:/data
    environment:
      - REDIS_ARGS=--save 60 1000
    restart: unless-stopped

  # MySQL数据库
  mysql:
    image: mysql:8.0
    container_name: novel-mysql
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: novel_rag
      MYSQL_CHARACTER_SET_SERVER: utf8mb4
      MYSQL_COLLATION_SERVER: utf8mb4_unicode_ci
    volumes:
      - mysql_data:/var/lib/mysql
    restart: unless-stopped

  # Spring Boot后端
  novel-rag-app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: novel-rag-app
    ports:
      - "8080:8080"
    environment:
      - DASHSCOPE_API_KEY=${DASHSCOPE_API_KEY}
      - DB_PASSWORD=root123
      - SPRING_PROFILES_ACTIVE=prod
    depends_on:
      - redis-stack
      - mysql
    restart: unless-stopped

  # Vue前端
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: novel-frontend
    ports:
      - "80:80"
    depends_on:
      - novel-rag-app
    restart: unless-stopped

volumes:
  redis_data:
  mysql_data:
```

### 10.2 后端 Dockerfile

```dockerfile
# Dockerfile
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 11. Redis Stack 向量索引配置

### 11.1 向量索引创建

```java
// RedisVectorConfig.java
@Configuration
public class RedisVectorConfig {

    @Bean
    public Client searchClient(RedisConnectionFactory factory) {
        Client client = new Client("novel_vectors", (Jedis) factory.getConnection().getNativeConnection());

        // 创建向量索引
        Schema schema = new Schema()
            .addTextField("novel_id", 1.0)
            .addTextField("chunk_index", 1.0)
            .addTextField("content", 5.0)
            .addVectorField("embedding",
                Schema.VectorField.VectorAlgo.HNSW,
                Map.of(
                    "TYPE", "FLOAT32",
                    "DIM", 1024,  // text-embedding-v3 维度
                    "DISTANCE_METRIC", "COSINE"
                ));

        client.createIndex(schema, Client.IndexOptions.defaultOptions());
        return client;
    }
}
```

---

## 12. 数据库表设计

### 12.1 章节索引表 (chapter_index) ⭐新增

```sql
CREATE TABLE chapter_index (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    novel_id BIGINT NOT NULL COMMENT '小说ID',
    chapter_number INT NOT NULL COMMENT '章节序号',
    chapter_title VARCHAR(255) NOT NULL COMMENT '章节标题',
    source_url VARCHAR(1024) NOT NULL COMMENT '原文链接',
    source_site VARCHAR(50) COMMENT '来源站点',
    content TEXT COMMENT '章节内容(可选存储)',
    word_count INT COMMENT '字数',
    crawl_time DATETIME COMMENT '抓取时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_novel_id (novel_id),
    INDEX idx_chapter_number (novel_id, chapter_number),
    FOREIGN KEY (novel_id) REFERENCES novel(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='章节索引表';
```

### 12.2 爬虫任务表 (crawl_task) ⭐新增

```sql
CREATE TABLE crawl_task (
    id VARCHAR(50) PRIMARY KEY COMMENT '任务ID',
    novel_id BIGINT COMMENT '关联小说ID',
    novel_name VARCHAR(255) NOT NULL COMMENT '小说名称',
    author VARCHAR(100) COMMENT '作者',
    source_site VARCHAR(50) NOT NULL COMMENT '来源站点',
    source_url VARCHAR(1024) NOT NULL COMMENT '来源URL',
    status ENUM('PENDING', 'RUNNING', 'PAUSED', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    total_chapters INT DEFAULT 0 COMMENT '总章节数',
    crawled_chapters INT DEFAULT 0 COMMENT '已抓取章节数',
    start_chapter INT COMMENT '起始章节',
    end_chapter INT COMMENT '结束章节',
    store_content BOOLEAN DEFAULT FALSE COMMENT '是否存储正文',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_novel_id (novel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='爬虫任务表';
```

---

## 13. 待确认事项

- [x] ~~确定使用的LLM提供商~~ → 阿里云百炼平台
- [x] ~~确定向量数据库选型~~ → Redis Stack
- [x] ~~确定前端技术选型~~ → Vue 3 + TypeScript + Webpack
- [x] ~~确定部署环境~~ → 本地 Docker Compose
- [x] ~~确定爬虫技术选型~~ → Java (Jsoup + WebClient)
- [x] ~~确定爬虫存储策略~~ → 保存原文链接，按需获取
- [ ] 申请阿里云百炼API Key
- [ ] 确定小说文件存储方案（本地/OSS）
- [ ] 确定需要支持的爬虫站点优先级
- [ ] 评估是否需要代理IP池

---

## 14. 风险与注意事项

### 14.1 爬虫相关风险 ⭐新增

| 风险类型 | 说明 | 应对措施 |
|---------|------|---------|
| 版权问题 | 爬取的小说可能涉及版权 | 仅存储链接，不存储正文；添加免责声明 |
| 反爬机制 | 网站可能有反爬措施 | 延迟请求、UA轮换、代理支持 |
| 网站变更 | 网站结构变化导致解析失败 | 模块化解析器设计，易于更新 |
| 法律风险 | 爬虫可能违反网站条款 | 遵守robots.txt，仅用于学习研究 |

### 14.2 技术风险

| 风险类型 | 说明 | 应对措施 |
|---------|------|---------|
| API限流 | 百炼API可能有调用限制 | 实现请求队列和限流机制 |
| 向量维度 | embedding模型维度变化 | 配置化维度设置 |
| 内存占用 | 大量向量数据内存消耗 | 分批处理、定期清理 |

---

## 15. 参考资源

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [Spring Boot 官方文档](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [阿里云百炼平台](https://bailian.console.aliyun.com/)
- [通义千问API文档](https://help.aliyun.com/document_detail/2712195.html)
- [Redis Stack 向量搜索](https://redis.io/docs/interact/search-and-query/search/vectors/)
- [Redis Stack Docker](https://hub.docker.com/r/redis/redis-stack)
- [Vue 3 官方文档](https://vuejs.org/)
- [TypeScript 官方文档](https://www.typescriptlang.org/)
- [Jsoup 官方文档](https://jsoup.org/) ⭐新增
- [Spring WebFlux 官方文档](https://docs.spring.io/spring-framework/reference/web/webflux.html) ⭐新增
- [robots.txt 协议](https://www.robotstxt.org/) ⭐新增

---

*计划创建时间：2026-03-22*
*最后更新时间：2026-03-22*
