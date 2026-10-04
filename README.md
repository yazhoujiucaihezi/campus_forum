校园论坛系统

基于 Spring Boot 3 + Vue 3 的前后端分离校园论坛系统，面向校园社区交流场景，提供用户发帖、评论、点赞、收藏、消息通知等功能，并配套管理员后台。

后端由本人独立完成，前端基于开源项目进行二次开发与接口联调。

功能

用户端

用户注册、登录、邮箱验证码、找回密码

个人资料、头像、隐私设置

帖子发布、编辑、删除、分类、搜索

评论、回复、点赞、收藏

富文本编辑及帖子图片上传

评论、点赞、收藏消息通知

天气信息展示

AI 助手对话

管理员端

帖子管理：置顶、锁定、屏蔽、删除、分类修改

分类管理：新增、编辑、删除

用户管理：查看、编辑用户信息、修改密码

邮件管理：查看发送记录、重新发送

技术栈

后端

Java 17

Spring Boot 3.5.8

Spring Web、Spring Validation

MyBatis-Plus 3.5.15

MySQL 8.0

Redis 7

RabbitMQ 3.8

Spring Mail、Spring AI

JJWT、BCrypt

Maven、Lombok、FastJSON2

前端

Vue 3

Vue Router

Pinia

Vite

Axios

Element Plus

VueQuill

第三方服务

QQ SMTP

和风天气 API

OpenAI 兼容接口的 AI 模型服务

系统架构

Vue 3 │ │ Axios ▼ Spring Boot │ ├── Controller │ ↓ ├── Service │ ↓ └── Mapper │ ├── MySQL ├── Redis └── RabbitMQ │ ▼ Notification Consumer │ ▼ 通知数据持久化 

采用前后端分离架构：

Spring Boot：提供 RESTful API 及核心业务逻辑

MySQL：存储用户、帖子、评论、通知等核心数据

Redis：缓存验证码等临时数据

RabbitMQ：处理消息通知等异步业务

核心实现

1. JWT 身份认证

用户登录成功后由后端签发 JWT，Token 中包含用户身份及角色信息。

前端通过 Authorization: Bearer <token> 携带 Token，后端解析 Token 获取当前用户，实现无 Session 的身份认证。

2. RabbitMQ 异步通知

用户评论、点赞、收藏后：

用户操作 ↓ 保存业务数据 ↓ 发送 RabbitMQ 消息 ↓ 主请求返回 ↓ 消费者异步处理 ↓ 保存通知 

将通知生成从主业务流程中解耦，避免通知处理影响核心业务请求。

3. Redis 验证码

使用 Redis 保存注册、找回密码及修改邮箱验证码，并设置过期时间。

验证码验证成功后主动删除，避免重复使用。

4. 密码安全

用户密码使用 BCrypt 加密存储，登录时通过 matches() 进行校验，不保存明文密码。

5. 图片上传

帖子图片存储于服务器本地目录，并按照日期划分目录，通过 Spring MVC 静态资源映射提供访问。

项目结构

campus-forum/ ├── backend/ │ ├── src/main/java/com/box/ │ │ ├── common/ # 公共组件 │ │ ├── config/ # 项目配置 │ │ ├── consumer/ # RabbitMQ 消费者 │ │ ├── controller/ # 接口层 │ │ ├── dto/ # 请求参数 │ │ ├── entity/ # 数据实体 │ │ ├── mapper/ # 数据访问层 │ │ ├── service/ # 业务层 │ │ ├── utils/ # 工具类 │ │ └── vo/ # 响应对象 │ └── src/main/resources/ │ ├── frontend/ │ └── src/ │ ├── components/ │ ├── net/ │ ├── router/ │ ├── store/ │ └── views/ │ ├── forum.sql └── README.md 

环境要求

JDK 17+

Maven 3.8+

Node.js 18+

MySQL 8.0+

Redis 6.0+

RabbitMQ 3.8+

快速启动

1. 初始化数据库

创建 MySQL 数据库并执行：

forum.sql 

2. 配置后端

根据本地环境配置 MySQL、Redis、RabbitMQ、SMTP 及第三方 API 等信息。

敏感配置不要提交到 Git。

3. 启动后端

cd backend mvn spring-boot:run 

4. 启动前端

cd frontend npm install npm run dev 

项目说明

后端独立完成，包括数据库设计、接口开发、业务逻辑及 RabbitMQ 异步通知等功能。

前端基于开源项目进行二次开发，并根据后端接口进行适配和联调。

项目用于学习 Spring Boot 后端开发、Redis、RabbitMQ 及前后端分离项目实践。

License

MIT
