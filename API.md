# API 接口文档

## 基础信息

- Base URL：`http://localhost:8080`
- 数据格式：JSON
- 字符编码：UTF-8
- 认证方式：JWT（Header: `Authorization: Bearer <token>`）

## 通用响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录 / token 过期 |
| 500 | 服务器错误 |

---

## 一、认证接口

### 1.1 微信登录

```
POST /api/auth/wechat-login
```

**请求参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | string | 是 | wx.login() 返回的临时 code |

**请求示例**
```json
{
  "code": "0a3b2c1d4e5f6789abcdef0123456789"
}
```

**响应示例**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "userId": 1,
    "nickname": "微信用户",
    "phone": null
  }
}
```

**说明**
- 首次登录自动创建用户
- 已注册用户直接返回 token
- phone 为 null 表示未绑定手机号

---

### 1.2 绑定手机号

```
POST /api/auth/bind-phone
```

**请求头**
```
Authorization: Bearer <token>
```

**请求参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| phone | string | 是 | 手机号码 |
| code | string | 是 | 验证码（开发环境填 "1234"） |

**请求示例**
```json
{
  "phone": "13800138000",
  "code": "1234"
}
```

**响应示例**
```json
{
  "code": 200,
  "message": "绑定成功",
  "data": null
}
```

---

## 二、任务接口

> 以下接口全部需要登录，请求头带 `Authorization: Bearer <token>`

### 2.1 获取任务列表

```
GET /api/tasks
```

**响应示例**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "复习面试题",
      "pomodoroCount": 3,
      "done": false,
      "createdAt": "2026-01-15 10:30:00"
    },
    {
      "id": 2,
      "title": "整理项目经验",
      "pomodoroCount": 1,
      "done": true,
      "createdAt": "2026-01-15 09:00:00"
    }
  ]
}
```

---

### 2.2 创建任务

```
POST /api/tasks
```

**请求参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | 是 | 任务标题（1-200字） |

**请求示例**
```json
{
  "title": "复习面试题"
}
```

**响应示例**
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 3,
    "title": "复习面试题",
    "pomodoroCount": 0,
    "done": false,
    "createdAt": "2026-01-15 11:00:00"
  }
}
```

---

### 2.3 更新任务

```
PUT /api/tasks/{id}
```

**请求参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | 否 | 新标题 |
| done | boolean | 否 | 是否完成 |

**请求示例**
```json
{
  "title": "复习面试题2.0",
  "done": true
}
```

**响应示例**
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 3,
    "title": "复习面试题2.0",
    "pomodoroCount": 0,
    "done": true,
    "createdAt": "2026-01-15 11:00:00"
  }
}
```

---

### 2.4 删除任务

```
DELETE /api/tasks/{id}
```

**响应示例**
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 2.5 完成一个番茄钟

```
POST /api/tasks/{id}/pomodoro
```

**请求参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| duration | int | 否 | 专注时长（秒），默认 1500（25分钟） |

**请求示例**
```json
{
  "duration": 1500
}
```

**响应示例**
```json
{
  "code": 200,
  "message": "番茄钟 +1",
  "data": {
    "id": 3,
    "title": "复习面试题",
    "pomodoroCount": 1,
    "done": false,
    "createdAt": "2026-01-15 11:00:00"
  }
}
```

**说明**
- 该任务的 pomodoroCount 会 +1
- 同时向 pomodoro_sessions 表插入一条记录

---

### 2.6 获取统计数据

```
GET /api/stats
```

**响应示例**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalPomodoros": 42,
    "totalMinutes": 1050,
    "taskCount": 8,
    "doneCount": 5
  }
}
```

| 字段 | 含义 |
|------|------|
| totalPomodoros | 历史总番茄数 |
| totalMinutes | 历史总专注分钟数 |
| taskCount | 总任务数 |
| doneCount | 已完成任务数 |

---

## 错误响应示例

### 未登录
```json
{
  "code": 401,
  "message": "请先登录",
  "data": null
}
```

### 参数校验失败
```json
{
  "code": 400,
  "message": "任务标题不能为空",
  "data": null
}
```

### 操作别人的任务
```json
{
  "code": 403,
  "message": "无权操作此任务",
  "data": null
}
```
