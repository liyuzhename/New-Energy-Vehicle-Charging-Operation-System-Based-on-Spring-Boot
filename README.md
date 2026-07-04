新能源汽车充电桩运营管理系统 · 安装使用说明
本文档为前后端分离架构的《新能源汽车充电桩运营管理系统》的完整安装、配置与使用手册。
建议严格按照本文档的顺序（环境准备 ➔ 数据库 ➔ 后端 ➔ 前端 ➔ 联调使用）进行部署。
一、 系统架构总览
本系统采用现代化的前后端分离架构进行开发：
后端技术栈：Spring Boot 3.2.5 + MyBatis-Plus + MySQL 8.0 + Redis + Spring Security + JWT (Java 21)
前端技术栈：Vue 3 + Element Plus + Vue Router 4 + Pinia + Vite (Node.js)
前后端协作关系图：

二、 基础环境要求
在开始部署前，请确保您的服务器或本地开发机已安装以下环境工具：
软件环境	最低版本建议	用途说明
JDK	21	编译及运行 Spring Boot 后端应用
Maven	3.8+	后端依赖管理与项目构建
Node.js	>=20.19.0 或 >=22.12.0	运行 Vite 前端服务与 npm 包管理
MySQL	8.0+	核心业务数据存储
Redis	5.0+	存储会话状态及邮箱验证码缓存
三、 数据库与中间件配置
系统必须依赖 MySQL 和 Redis 才能正常启动和运行。
1. 启动 Redis
确保 Redis 服务已在后台运行，默认监听 6379 端口。测试连接可使用命令行：redis-cli ping（预期返回 PONG）。
2. 初始化 MySQL 数据库
在 MySQL 中执行后端源码 src/main/resources/sql/ 目录下的脚本：
1.执行 init.sql：创建 charging_db 数据库、建表并初始化管理员账号。
2.执行 fix_test_data.sql：导入完整的测试数据（包含运营商、充电站、订单等）。
数据规范提示： 数据库设计中关于充电枪的设备编号核心标识统一使用 gun_no 字段，后续若您需要手动维护测试数据或编写自定义 SQL 查询充电桩状态，请认准此字段。
四、 后端服务安装与启动
1.修改配置文件
打开后端项目文件 src/main/resources/application.yml，修改以下关键配置以匹配您的本地环境：
oMySQL：修改 spring.datasource.password 为您的 root 密码。
oRedis：确认 spring.data.redis.host 和 port 正确。
oJWT：修改 jwt.secret 为安全的随机字符串。
2.编译与启动
在后端项目根目录下打开终端，执行以下命令：
DOS
# 1. 下载依赖并编译打包
mvn clean package -DskipTests

# 2. 启动 Spring Boot 后端服务
mvn spring-boot:run
当控制台输出 Tomcat started on port 8090 (http) 即表示后端服务启动成功。
五、 前端服务安装与启动
前端项目通过 Vite 代理解决了跨域问题，必须在后端启动完成后再启动前端。
1.安装前端依赖
在前端项目根目录（charging_vue，即包含 package.json 的目录）执行：
Bash
npm install
(若下载缓慢，可先执行 npm config set registry https://registry.npmmirror.com 切换淘宝镜像)
2.启动开发服务器
Bash
npm run dev
终端输出完成后，在浏览器中访问：http://localhost:5174。前端会自动将以 /api 开头的请求代理到 http://localhost:8090 的后端接口。
六、 系统操作与使用步骤
当前后端均启动成功后，即可按照以下步骤使用系统的各项功能。
1. 登录系统
打开浏览器访问 http://localhost:5174。系统默认提供以下测试账号（密码均为 admin123）：
角色类型	测试账号	默认登录后首页	主要职责与功能范围
普通用户	user01	/station/list	找站充电、订单支付、钱包充值、故障报修
运营商	operator01	/operator/station	管理名下充电站及 gun_no 状态、计费规则配置
系统管理员	admin	/admin/dashboard	全局数据监控、用户与运营商管理、发布系统公告
2. 核心业务操作流程
场景 A：普通用户充电流程
1.查找站点：登录 user01，进入【充电站列表】，浏览或搜索附近的站点。
2.发起充电：点击站点进入详情，选择一个状态为“空闲”的充电桩，点击【开始充电】。
3.查看进度：系统跳转至充电监控页，可实时查看已充电量、时长与当前费用。
4.结束与支付：点击【停止充电】生成订单。进入【我的订单】或使用【我的钱包】余额进行结算支付。
场景 B：运营商建站与管桩流程
1.创建站点：登录 operator01，进入左侧【充电站管理】，点击新增，填写经纬度与站点信息。
2.配置电价：进入【计费规则】，为新站点设置峰、平、谷时段的电价和服务费标准。
3.设备管理：进入【充电桩管理】，为新站点添加充电桩设备（绑定具体的 gun_no 编号），并控制其上线/下线状态。
场景 C：管理员平台维护流程
1.数据巡查：登录 admin，在【仪表盘】查看全平台的日活、充电总度数及营收统计。
2.账号管理：在【运营商管理】中为新加盟的商家开设后台账号。
3.信息发布：在【公告管理】中发布停机维护或优惠活动公告，发布后所有用户端及运营商端首页均可查看。
七、 常见问题排查 (FAQ)
Q: 前端页面能打开，但提示“网络连接失败”或接口报错？
o检查1：确认后端 Spring Boot (8090端口) 是否正常运行。
o检查2：确认前端 vite.config.js 中的代理 target 是否准确指向了 http://localhost:8090。
o检查3：确认 MySQL 数据库及 Redis 服务已启动。
Q: 登录提示“密码错误”或“用户不存在”？
o说明数据库测试数据未正确导入。请重新执行后端的 fix_test_data.sql 脚本。
Q: 前后端端口冲突如何修改？
o后端：修改 application.yml 中的 server.port。同时必须修改前端 vite.config.js 的代理目标地址。
o前端：修改 vite.config.js 中 server.port 的值。并在后端 SecurityConfig.java 的 CORS 白名单中放行新端口。
