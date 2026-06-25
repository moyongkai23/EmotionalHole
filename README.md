# 校园匿名"树洞"情感分享平台

> 面向高校学生的匿名情感交流社区

## 项目简介

这是一个基于 Spring Boot + Vue3 + 微信小程序的前后端分离项目，包含：
- **用户端小程序**：匿名发帖、点赞抱抱、AI对话、搜索浏览
- **运营管理后台**：用户管理、内容审核、数据可视化、系统配置

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot + MyBatis + MySQL |
| 前端 | Vue 3 + Element Plus + ECharts |
| 小程序 | 微信小程序原生框架 |
| AI | 百度千帆大模型 API |
| 认证 | JWT 令牌机制 |

## 核心功能

### 用户端
- 微信一键登录（匿名身份）
- 匿名树洞发布（支持多图）
- "抱抱"互动机制（替代点赞）
- AI 情感对话助手
- 个性化内容推荐
- 敏感词实时检测
- 举报与危机预警

### 管理后台
- 多维度数据看板
- 用户管理与封禁
- 内容审核工作台
- 公告管理
- 话题分类管理
- 敏感词库管理

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Node.js 16+
- Maven 3.8+

### 1. 克隆项目

```bash
git clone https://github.com/moyongkai23/EmotionalHole.git
cd EmotionalHole
```

### 2. 数据库初始化

```bash
mysql -u root -p -e "CREATE DATABASE emotional_hole DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
```

### 3. 配置环境变量

复制 `.env.example` 为 `.env`，填入你的配置。

### 4. 启动后端

```bash
mvn clean install
mvn spring-boot:run
```

## 项目结构

```
EmotionalHole/
├── src/main/java/
│   └── com/myk/emotionalHole/
│       ├── controller/     # 控制器层
│       ├── service/        # 业务逻辑层
│       ├── mapper/         # 数据访问层
│       ├── entity/         # 实体类
│       ├── dto/            # 数据传输对象
│       ├── config/         # 配置类
│       └── util/           # 工具类
└── src/main/resources/
    ├── mapper/             # MyBatis 映射文件
    └── application.yml     # 配置文件
```

## 许可证

MIT License

## 作者

莫永凯 - [GitHub](https://github.com/moyongkai23)
