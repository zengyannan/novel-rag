# Novel-RAG

A intelligent novel generation system based on RAG (Retrieval-Augmented Generation) technology.

## Overview

Novel-RAG allows users to generate new novel chapters based on existing novel content through a web interface. The system uses vector retrieval technology to find relevant content fragments and combines them with LLM to generate coherent, style-consistent chapter content. It also supports web crawler functionality to automatically fetch novel chapters from the internet.

## Features

- Novel document upload and parsing (txt, pdf, epub, docx)
- Web crawler for automatic novel fetching
- Document chunking and vector storage
- Semantic similarity-based content retrieval
- Intelligent chapter generation
- Streaming output support (SSE)
- Web-based interactive interface

## Tech Stack

### Backend
- Java 17+ / Spring Boot 3.x
- Spring AI
- Alibaba Cloud Bailian (LLM & Embedding)
- Redis Stack (Vector Database)
- MySQL 8.0
- Maven 3.9+
- Jsoup + Spring WebFlux (Web Crawler)

### Frontend
- Vue 3
- TypeScript
- Webpack

### Deployment
- Docker Compose

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   Vue 3 + TypeScript Frontend                │
│        (Upload / Crawl / Generate / Display)                 │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTP/REST API
┌─────────────────────────▼───────────────────────────────────┐
│                    Spring Boot Backend                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ Novel Module │  │  RAG Module  │  │Generate Mod. │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│  ┌──────────────┐                                           │
│  │Crawler Module│                                           │
│  └──────────────┘                                           │
└──────────────────────────────────────────────────────────────┘
        │                  │                  │
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  Redis Stack  │  │    MySQL     │  │ Alibaba Cloud│
│  (Vector DB)  │  │  (Relational)│  │   Bailian    │
└──────────────┘  └──────────────┘  └──────────────┘
```

## Quick Start

### Prerequisites
- Docker & Docker Compose
- JDK 17+
- Maven 3.9+
- Node.js 18+ (for frontend development)

### Environment Setup

1. Set required environment variables:
```bash
export DASHSCOPE_API_KEY=your_api_key_here
```

2. Start services with Docker Compose:
```bash
docker-compose up -d
```

3. Access the application:
- Frontend: http://localhost
- Backend API: http://localhost:8080
- Redis Insight: http://localhost:8001

### Development Mode

1. Start infrastructure services:
```bash
docker-compose up -d redis-stack mysql
```

2. Run backend:
```bash
cd backend
mvn spring-boot:run
```

3. Run frontend:
```bash
cd frontend
npm install
npm run dev
```

## API Endpoints

### Novel Management
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/novels/upload | Upload novel file |
| GET | /api/novels | Get novel list |
| GET | /api/novels/{id} | Get novel details |
| DELETE | /api/novels/{id} | Delete novel |

### Crawler
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/crawler/search | Search novel sources |
| POST | /api/crawler/start | Start crawl task |
| GET | /api/crawler/task/{taskId} | Get task status |

### Generation
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/generate/chapter | Generate chapter |
| GET | /api/generate/chapter/stream | Stream generation (SSE) |

## Project Structure

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

## Configuration

Key configurations in `application.yml`:

```yaml
# Alibaba Cloud Bailian
spring.ai.dashscope:
  api-key: ${DASHSCOPE_API_KEY}
  chat.options.model: qwen-max-longcontext
  embedding.options.model: text-embedding-v3

# Redis Vector
vector.redis:
  index-name: novel_vectors
  dimensions: 1024
```

## License

This project is for learning and research purposes only. Please respect copyright when crawling novel content.

## References

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Alibaba Cloud Bailian](https://bailian.console.aliyun.com/)
- [Redis Stack Vector Search](https://redis.io/docs/interact/search-and-query/search/vectors/)
- [Vue 3 Documentation](https://vuejs.org/)
