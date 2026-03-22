# Project: [novel-backend]

## 技术栈
- Java 21
- Spring Boot 3.3
- MyBatis-Plus
- MySQL 8
- Maven 3.9+

## 代码规范
- 缩进2空格
- 优先保证可读性
- 禁止过度设计
- 禁止滥用 `Object`
- 禁止把业务逻辑堆在 Controller
- Controller / Service / Mapper 职责清晰
- 优先使用构造器注入
- 禁止字段注入
- DTO、VO、Entity 分层明确
- 命名清晰，避免无意义缩写
- 实体类命名规范：Entity 用 `XxxEntity`，DTO 用 `XxxDTO`，VO 用 `XxxVO`
- 金额使用 `BigDecimal`
- 时间使用 `java.time`
- 不要直接返回数据库实体给前端
- 禁止硬编码配置、密钥、账号信息
- 单个方法保持简洁，复杂逻辑及时拆分
- 公共常量、枚举、工具类按语义归类
- 返回给前端的数据使用统一Result<T>类，确保一致性和可扩展性

## 项目结构
```text
src/
├── main/
│   ├── java/com/example/project/
│   │   ├── controller/   # 接口层
│   │   ├── service/      # 业务层
│   │   ├── mapper/       # MyBatis-Plus Mapper
│   │   ├── entity/       # 实体类
│   │   ├── dto/          # 请求对象
│   │   ├── vo/           # 响应对象
│   │   ├── config/       # 配置类
│   │   ├── common/       # 通用基础设施
│   │   ├── exception/    # 异常定义
│   │   └── utils/        # 工具类
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── mapper/       # XML文件（如需要）
└── test/
    └── java/com/example/project/