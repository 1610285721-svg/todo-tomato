# 番茄土豆 (Todo Potato)

一个番茄钟 + Todo List 微信小程序，支持微信登录、手机号绑定、任务管理和专注计时。

## 技术栈

| 层 | 技术 |
|---|---|
| 小程序前端 | 微信小程序原生框架 |
| 后端 | Spring Boot 3.x + JDK 21 |
| 数据库 | MySQL 8.0 |
| ORM | Spring Data JPA |
| 认证 | JWT（JSON Web Token） |
| 登录 | 微信登录 + 手机号绑定 |

## 为什么选微信原生而不是 uni-app

- 面试聊小程序，原生框架更能讲清楚原理
- 原生语法和 Vue 有相似之处，上手快
- 小程序文档和社区资源都是围绕原生的

## 整体架构

```
┌─────────────────┐     HTTP/HTTPS      ┌──────────────────┐     JDBC      ┌─────────┐
│  微信小程序前端   │ ◄──────────────────► │  Spring Boot 后端  │ ◄───────────► │  MySQL  │
│  (原生框架)       │    JSON + JWT       │  (REST API)       │               │  数据库  │
└─────────────────┘                     └──────────────────┘               └─────────┘
```

## 登录流程

```
1. 用户打开小程序
2. 点击"微信登录"按钮
3. 小程序调用 wx.login() 获取临时 code
4. 小程序将 code 发到后端 /api/auth/wechat-login
5. 后端拿 code 调微信 jscode2session 接口，获取 openid 和 session_key
6. 后端根据 openid 查找或创建用户，生成 JWT
7. 后端返回 JWT 给前端
8. 前端将 token 存入 wx.storage
9. 后续所有请求在 Header 中带 Authorization: Bearer <token>

手机号绑定（可选）：
- 用户输入手机号 → 后端发验证码 → 用户输入验证码 → 后端校验 → 绑定成功
- 开发环境验证码固定为 "1234"
```

## 页面结构

| 页面 | 路径 | 说明 |
|------|------|------|
| 登录页 | pages/login/login | 微信登录按钮、手机号绑定入口 |
| 任务列表 | pages/index/index | 添加/删除/完成任务，显示番茄数 |
| 番茄钟 | pages/pomodoro/pomodoro | 25分钟倒计时、暂停/重置、震动提醒 |

## 数据流向（以完成一个番茄钟为例）

```
用户点"开始专注"
  → 番茄钟页面开始 25 分钟倒计时
  → 倒计时结束，震动提醒
  → 前端调用 POST /api/tasks/{id}/pomodoro
  → 后端 task.pomodoro_count + 1
  → 后端插入一条 pomodoro_sessions 记录
  → 返回更新后的 task
  → 前端更新页面显示
```

## 数据库表

### users 用户表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 自增主键 |
| openid | VARCHAR(64) | 微信 openid，唯一 |
| phone | VARCHAR(20) | 手机号，可空 |
| nickname | VARCHAR(64) | 微信昵称 |
| avatar | VARCHAR(255) | 头像 URL |
| created_at | DATETIME | 创建时间 |

### tasks 任务表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 自增主键 |
| user_id | BIGINT FK | 关联用户 |
| title | VARCHAR(200) | 任务标题 |
| pomodoro_count | INT | 已完成番茄数 |
| done | TINYINT | 是否完成 0/1 |
| created_at | DATETIME | 创建时间 |

### pomodoro_sessions 番茄记录表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 自增主键 |
| user_id | BIGINT FK | 关联用户 |
| task_id | BIGINT FK | 关联任务 |
| type | VARCHAR(10) | work/break |
| duration | INT | 时长（秒） |
| created_at | DATETIME | 完成时间 |

## 项目目录结构

```
D:/todo-tomato/
├── README.md                   # 本文件
├── API.md                      # 接口文档
├── backend/                    # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/pomodoro/
│       │   ├── PomodoroApplication.java
│       │   ├── config/
│       │   │   └── JwtInterceptor.java
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   └── TaskController.java
│       │   ├── entity/
│       │   │   ├── User.java
│       │   │   ├── Task.java
│       │   │   └── PomodoroSession.java
│       │   ├── repository/
│       │   │   ├── UserRepository.java
│       │   │   ├── TaskRepository.java
│       │   │   └── PomodoroSessionRepository.java
│       │   ├── service/
│       │   │   ├── AuthService.java
│       │   │   └── TaskService.java
│       │   └── dto/
│       │       └── ApiResponse.java
│       └── resources/
│           └── application.yml
├── app.js                      # 小程序入口
├── app.json                    # 小程序配置
├── pages/
│   ├── login/login.*           # 登录页
│   ├── index/index.*           # 任务列表页
│   └── pomodoro/pomodoro.*     # 番茄钟页
└── utils/
    ├── request.js              # 网络请求封装（自动带 JWT）
    └── auth.js                 # 登录状态管理
```

## 本地运行

### 1. 启动 MySQL，创建数据库
```sql
CREATE DATABASE todo_tomato CHARACTER SET utf8mb4;
```

### 2. 启动后端
在 IDEA 中打开 backend/ 目录，运行 PomodoroApplication.java

### 3. 启动前端
在微信开发者工具中打开当前目录，填写 AppID（可用测试号）

### 4. 调试
开发者工具模拟器可直接访问 localhost:8080，真机预览需要同一 WiFi 下用局域网 IP
