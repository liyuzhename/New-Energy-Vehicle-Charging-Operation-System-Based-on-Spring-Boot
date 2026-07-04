# 新能源汽车充电桩运营管理系统

> 本项目是一个基于**前后端分离**架构的新能源汽车充电桩运营管理系统。
> 前端采用 Vue 3 生态，后端采用 Spring Boot 3.2.5 + MyBatis-Plus。
> 前端仓库URL:https://github.com/liyuzhename/charing-vue

---

## 📖 目录

- [一、系统架构总览](#一系统架构总览)
- [二、基础环境要求](#二基础环境要求)
- [三、数据库与中间件配置](#三数据库与中间件配置)
- [四、后端服务安装与启动](#四后端服务安装与启动)
- [五、前端服务安装与启动](#五前端服务安装与启动)
- [六、系统操作与使用步骤](#六系统操作与使用步骤)
- [七、常见问题排查](#七常见问题排查)

---

## 一、系统架构总览

* **后端核心技术**：Spring Boot 3.2.5 + MyBatis-Plus + MySQL 8.0 + Redis + Spring Security + JWT (基于 Java 21)
* **前端核心技术**：Vue 3 + Element Plus + Vue Router 4 + Pinia + Vite (基于 Node.js)

**前后端通信机制：**
前端 (端口 `5174`) 通过 HTTP/JSON 与后端 REST API (端口 `8090`) 进行交互，接口通过 JWT 进行鉴权认证。

---

## 二、基础环境要求

请确保部署或开发机器上已安装以下依赖软件：

| 环境依赖 | 最低版本建议 | 核心用途 |
| :--- | :--- | :--- |
| **JDK** | 21 | 编译及运行 Spring Boot 后端应用 |
| **Maven** | 3.8+ | 后端依赖管理与项目构建 |
| **Node.js** | `>=20.19.0` 或 `>=22.12.0` | 运行 Vite 前端服务与 npm 包管理 |
| **MySQL** | 8.0+ | 核心业务数据存储 |
| **Redis** | 5.0+ | 存储会话状态及验证码缓存 |

---

## 三、数据库与中间件配置

系统强依赖 MySQL 和 Redis，请先完成此部分配置。

### 1. 启动 Redis
确保 Redis 服务已运行，默认监听 `6379` 端口。

### 2. 初始化 MySQL 数据库
进入后端源码目录 `src/main/resources/sql/`，按顺序在 MySQL 中执行以下脚本：
1. 执行 `init.sql`：创建 `charging_db` 数据库、表结构及初始管理员。
2. 执行 `fix_test_data.sql`：导入系统完整测试数据（含用户、充电站、订单等）。

> **💡 数据库字段开发须知：** > 实体类和数据库中关于充电枪的标识字段统一为 `gun_no`。系统设计时并未手动设计 `gunId`，相关 ID 由数据库自动生成，请在后续进行数据维护或二次开发时注意字段匹配。

---

## 四、后端服务安装与启动

1. **配置文件修改**
   编辑 `src/main/resources/application.yml`：
   * 配置 `spring.datasource.password` 为您的 MySQL root 密码。
   * 配置 `jwt.secret` 为自定义的复杂安全密钥。

2. **编译与运行**
   在后端项目根目录下执行：
   ```bash
   # 下载依赖并跳过测试打包
   mvn clean package -DskipTests
   
   # 启动 Spring Boot 后端服务
   mvn spring-boot:run

---

## 五、前端服务安装与启动

⚠️ 重要提醒： 由于开发环境配置了 Vite 的 API 代理，必须在后端启动完成后，再启动前端服务。

1. **安装依赖**
   在前端项目目录（charging_vue）下执行：
   npm install

2. **启动开发服务器**
   ```bash
   npm run dev
  
