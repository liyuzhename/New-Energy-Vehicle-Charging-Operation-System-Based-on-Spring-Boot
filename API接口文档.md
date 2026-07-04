# 新能源汽车充电桩运营管理系统——API 接口文档

---

## 一、接口规范

### 1.1 基础信息

| 项目 | 说明 |
|------|------|
| 服务地址 | `http://localhost:8090` |
| 接口前缀 | `/api` |
| 数据格式 | JSON（`Content-Type: application/json`） |
| 字符编码 | UTF-8 |

### 1.2 认证方式

除公开接口外，所有接口需在请求头中携带 JWT Token：

```
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
```

Token 通过登录接口获取，有效期 **24 小时**。

### 1.3 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码：200 成功，400 参数错误，401 未认证，403 无权限，500 服务器错误 |
| message | String | 提示信息 |
| data | Object/Array/null | 响应数据 |

### 1.4 分页响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [ ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

---

## 二、认证模块 `/api/auth`

> 所有接口公开，无需认证

### 2.1 用户注册

- **POST** `/api/auth/register`

**请求体：**
```json
{
  "username": "testuser",
  "phone": "13800138000",
  "email": "test@example.com",
  "password": "password123"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

---

### 2.2 用户登录

- **POST** `/api/auth/login`

**请求体：**
```json
{
  "username": "testuser",
  "password": "password123"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "userId": 1,
    "username": "testuser",
    "role": "USER",
    "nickname": "测试用户"
  }
}
```

---

### 2.3 发送忘记密码验证码

- **POST** `/api/auth/forgot-password`

**请求体：**
```json
{
  "email": "test@example.com"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "验证码已发送，请查收邮件",
  "data": null
}
```

---

### 2.4 重置密码

- **POST** `/api/auth/reset-password`

**请求体：**
```json
{
  "email": "test@example.com",
  "code": "123456",
  "newPassword": "newpassword123"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": null
}
```

---

## 三、用户模块 `/api/user`

> 需要认证（USER / OPERATOR / ADMIN）

### 3.1 获取当前用户信息

- **GET** `/api/user/info`

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "phone": "13800138000",
    "email": "test@example.com",
    "role": "USER",
    "nickname": "测试用户",
    "avatar": null,
    "status": 1,
    "createTime": "2024-01-01 10:00:00"
  }
}
```

---

### 3.2 修改个人资料

- **PUT** `/api/user/profile`

**请求体：**
```json
{
  "nickname": "新昵称",
  "phone": "13900139000",
  "email": "new@example.com",
  "avatar": "/avatars/xxx.jpg"
}
```

---

### 3.3 修改密码

- **PUT** `/api/user/password`

**请求体：**
```json
{
  "oldPassword": "oldpassword123",
  "newPassword": "newpassword123"
}
```

---

### 3.4 获取车辆列表

- **GET** `/api/user/vehicles`

**响应：**
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "plateNo": "京A12345",
      "brand": "特斯拉",
      "model": "Model 3",
      "batteryCap": 75.0
    }
  ]
}
```

---

### 3.5 添加车辆

- **POST** `/api/user/vehicles`

**请求体：**
```json
{
  "plateNo": "京A12345",
  "brand": "特斯拉",
  "model": "Model 3",
  "batteryCap": 75.0
}
```

---

### 3.6 删除车辆

- **DELETE** `/api/user/vehicles/{id}`

---

### 3.7 查看钱包

- **GET** `/api/user/wallet`

**响应：**
```json
{
  "code": 200,
  "data": {
    "balance": 100.00,
    "totalRecharge": 500.00,
    "totalConsume": 400.00
  }
}
```

---

### 3.8 钱包充值

- **POST** `/api/user/wallet/recharge`

**请求体：**
```json
{
  "amount": 100.00
}
```

---

### 3.9 查看流水记录

- **GET** `/api/user/wallet/records?page=1&size=10&type=CONSUME`

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页数量，默认 10 |
| type | String | 否 | 类型筛选：RECHARGE/CONSUME/REFUND |

---

## 四、充电站/桩查询模块

> 部分接口公开，部分需要认证

### 4.1 充电站列表

- **GET** `/api/station/list?page=1&size=10&city=北京&keyword=充电`（公开）

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |
| city | String | 否 | 城市筛选 |
| keyword | String | 否 | 关键词搜索（站名/地址） |

**响应数据包含：** 站点基本信息 + 充电桩总数 + 空闲桩数量

---

### 4.2 充电站详情

- **GET** `/api/station/{id}`（公开）

**响应：**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "朝阳区充电站",
    "address": "北京市朝阳区xxx路1号",
    "city": "北京",
    "longitude": 116.4074,
    "latitude": 39.9042,
    "businessHours": "08:00-22:00",
    "parkingFee": "免费停车2小时",
    "status": "ONLINE",
    "pileCount": 10,
    "idleCount": 5
  }
}
```

---

### 4.3 站内充电桩状态

- **GET** `/api/station/{stationId}/piles`（需认证）

**响应：** 返回该站点所有充电桩列表，每个桩包含充电枪状态

---

### 4.4 查询计费规则

- **GET** `/api/billing/list/{stationId}`（公开）

**响应：**
```json
{
  "code": 200,
  "data": [
    {
      "periodType": "PEAK",
      "startHour": 8,
      "endHour": 12,
      "electricityPrice": 1.2000,
      "servicePrice": 0.8000,
      "effectiveDate": "2024-01-01"
    },
    {
      "periodType": "VALLEY",
      "startHour": 23,
      "endHour": 8,
      "electricityPrice": 0.4000,
      "servicePrice": 0.4000,
      "effectiveDate": "2024-01-01"
    }
  ]
}
```

---

## 五、预约模块 `/api/reservation`

> 需要认证（USER）

### 5.1 发起预约

- **POST** `/api/reservation`

**请求体：**
```json
{
  "pileId": 1,
  "reserveDate": "2024-12-01",
  "startTime": "2024-12-01 14:00:00",
  "endTime": "2024-12-01 16:00:00"
}
```

---

### 5.2 我的预约列表

- **GET** `/api/reservation/my?page=1&size=10&status=PENDING`

---

### 5.3 取消预约

- **PUT** `/api/reservation/{id}/cancel`

---

### 5.4 确认到场

- **PUT** `/api/reservation/{id}/confirm`

---

### 5.5 从预约发起充电

- **POST** `/api/reservation/{id}/start-charging`

**请求体：**
```json
{
  "gunId": 1,
  "vehicleId": 1
}
```

---

## 六、充电订单模块 `/api/order`

> 需要认证（USER）

### 6.1 发起充电

- **POST** `/api/order/start`

**请求体：**
```json
{
  "pileNo": "PILE-001",
  "gunId": 1,
  "vehicleId": 1
}
```

**响应：**
```json
{
  "code": 200,
  "message": "充电已开始",
  "data": {
    "orderId": 100,
    "orderNo": "ORD20241201140000001",
    "startTime": "2024-12-01 14:00:00",
    "status": "CHARGING"
  }
}
```

---

### 6.2 查询进行中订单

- **GET** `/api/order/charging`

**响应：**
```json
{
  "code": 200,
  "data": {
    "orderId": 100,
    "orderNo": "ORD20241201140000001",
    "startTime": "2024-12-01 14:00:00",
    "chargeKwh": 5.200,
    "estimatedFee": 12.50,
    "pileName": "PILE-001",
    "stationName": "朝阳区充电站"
  }
}
```

---

### 6.3 停止充电

- **PUT** `/api/order/{orderId}/stop`

**响应：**
```json
{
  "code": 200,
  "data": {
    "orderId": 100,
    "chargeKwh": 10.500,
    "chargeFee": 12.60,
    "serviceFee": 8.40,
    "totalFee": 21.00,
    "status": "FINISHED"
  }
}
```

---

### 6.4 订单详情

- **GET** `/api/order/{orderId}`

---

### 6.5 我的订单列表

- **GET** `/api/order/my?page=1&size=10&status=FINISHED`

---

### 6.6 订单支付

- **POST** `/api/order/{orderId}/pay`

**响应：**
```json
{
  "code": 200,
  "message": "支付成功",
  "data": {
    "payStatus": "PAID",
    "remainBalance": 79.00
  }
}
```

---

### 6.7 申请退款

- **POST** `/api/order/{orderId}/refund`

**请求体：**
```json
{
  "reason": "充电设备故障，充电未完成"
}
```

---

## 七、评价模块 `/api/evaluation`

> 需要认证

### 7.1 提交评价（USER）

- **POST** `/api/evaluation`

**请求体：**
```json
{
  "orderId": 100,
  "stationId": 1,
  "rating": 5,
  "content": "充电速度很快，设备干净整洁"
}
```

---

### 7.2 我的评价列表（USER）

- **GET** `/api/evaluation/my?page=1&size=10`

---

### 7.3 充电站评价列表（公开）

- **GET** `/api/evaluation/station/{stationId}?page=1&size=10`

---

## 八、故障模块 `/api/fault`

> 需要认证

### 8.1 上报故障（USER）

- **POST** `/api/fault`

**请求体：**
```json
{
  "pileId": 1,
  "description": "充电枪无法插入，接口损坏"
}
```

---

### 8.2 我的故障上报列表（USER）

- **GET** `/api/fault/my?page=1&size=10`

---

## 九、公告模块 `/api/announcement`

### 9.1 公告列表（公开）

- **GET** `/api/announcement/list?page=1&size=10&type=NOTICE`

---

### 9.2 公告详情（公开）

- **GET** `/api/announcement/{id}`

---

## 十、运营商模块 `/api/operator`

> 需要认证（OPERATOR / ADMIN）

### 10.1 充电站管理

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/operator/station/list` | 查询名下充电站列表（分页） |
| GET | `/api/operator/station/{id}` | 充电站详情 |
| POST | `/api/operator/station` | 创建充电站 |
| PUT | `/api/operator/station/{id}` | 修改充电站信息 |
| DELETE | `/api/operator/station/{id}` | 删除充电站 |

**创建充电站请求体：**
```json
{
  "name": "新建充电站",
  "address": "北京市海淀区xxx路",
  "city": "北京",
  "longitude": 116.3000,
  "latitude": 39.9000,
  "businessHours": "00:00-24:00",
  "parkingFee": "免费"
}
```

---

### 10.2 充电桩管理

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/operator/pile/list` | 查询充电桩列表（分页，支持按站点筛选） |
| POST | `/api/operator/pile` | 创建充电桩 |
| PUT | `/api/operator/pile/{id}` | 修改充电桩信息 |
| DELETE | `/api/operator/pile/{id}` | 删除充电桩 |
| PUT | `/api/operator/pile/{id}/status` | 修改充电桩状态 |

**创建充电桩请求体：**
```json
{
  "pileNo": "PILE-001",
  "stationId": 1,
  "pileType": "FAST",
  "power": 120.0
}
```

---

### 10.3 充电枪管理

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/operator/gun/list/{pileId}` | 查询充电桩下的充电枪 |
| POST | `/api/operator/gun` | 添加充电枪 |
| PUT | `/api/operator/gun/{id}` | 修改充电枪信息 |
| DELETE | `/api/operator/gun/{id}` | 删除充电枪 |

---

### 10.4 计费规则管理

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/operator/billing/list/{stationId}` | 查询站点计费规则 |
| POST | `/api/operator/billing` | 创建计费规则 |
| PUT | `/api/operator/billing/{id}` | 修改计费规则 |
| DELETE | `/api/operator/billing/{id}` | 删除计费规则 |

**创建计费规则请求体：**
```json
{
  "stationId": 1,
  "periodType": "PEAK",
  "startHour": 8,
  "endHour": 12,
  "electricityPrice": 1.2000,
  "servicePrice": 0.8000,
  "effectiveDate": "2024-01-01"
}
```

---

### 10.5 运营商订单管理

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/operator/order/list` | 查询名下订单列表（分页） |
| GET | `/api/operator/order/{orderId}` | 订单详情 |

**查询参数：** `page`, `size`, `stationId`, `status`, `startDate`, `endDate`

---

### 10.6 收益统计

- **GET** `/api/operator/income?type=day&startDate=2024-01-01&endDate=2024-01-31`

**响应：**
```json
{
  "code": 200,
  "data": {
    "totalIncome": 5000.00,
    "chargeFee": 3000.00,
    "serviceFee": 2000.00,
    "orderCount": 200,
    "totalKwh": 2500.000,
    "trend": [
      { "date": "2024-01-01", "income": 200.00, "orderCount": 8 }
    ]
  }
}
```

---

### 10.7 评价管理

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/operator/evaluation/list` | 查询名下站点评价列表 |
| PUT | `/api/operator/evaluation/{id}/reply` | 回复评价 |

---

### 10.8 故障管理

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/operator/fault/list` | 查询名下故障列表 |
| PUT | `/api/operator/fault/{id}/handle` | 处理故障（更新状态和备注） |

---

## 十一、管理员模块 `/api/admin`

> 需要认证（ADMIN）

### 11.1 用户管理

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/admin/user/list` | 用户列表（分页，支持关键词搜索） |
| PUT | `/api/admin/user/{id}/status` | 启用/禁用用户 |

---

### 11.2 运营商管理

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/admin/operator/list` | 运营商列表（分页） |

---

### 11.3 订单管理

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/admin/order/list` | 全平台订单列表（分页，多条件筛选） |
| GET | `/api/admin/order/refund/list` | 退款申请列表 |
| PUT | `/api/admin/order/{orderId}/refund/approve` | 审核通过退款 |
| PUT | `/api/admin/order/{orderId}/refund/reject` | 拒绝退款 |

---

### 11.4 评价管理

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/admin/evaluation/list` | 全平台评价列表 |
| PUT | `/api/admin/evaluation/{id}/hide` | 屏蔽评价 |
| PUT | `/api/admin/evaluation/{id}/show` | 取消屏蔽 |

---

### 11.5 公告管理

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/admin/announcement/list` | 公告列表（含下线公告） |
| POST | `/api/admin/announcement` | 创建公告 |
| PUT | `/api/admin/announcement/{id}` | 修改公告 |
| PUT | `/api/admin/announcement/{id}/online` | 上线公告 |
| PUT | `/api/admin/announcement/{id}/offline` | 下线公告 |

---

### 11.6 操作日志

- **GET** `/api/admin/log/list?page=1&size=20&operatorName=admin&startDate=2024-01-01`

---

### 11.7 仪表盘统计

- **GET** `/api/admin/dashboard`

**响应：**
```json
{
  "code": 200,
  "data": {
    "totalUsers": 1000,
    "totalOperators": 50,
    "totalStations": 200,
    "totalPiles": 1000,
    "todayOrders": 500,
    "todayIncome": 5000.00,
    "totalOrders": 50000,
    "totalIncome": 500000.00
  }
}
```

---

### 11.8 故障列表

- **GET** `/api/admin/fault/list?page=1&size=10&status=PENDING`

---

## 十二、报表模块 `/api/report`

> 需要认证（OPERATOR / ADMIN）

### 12.1 订单量趋势

- **GET** `/api/report/order-trend?type=day&startDate=2024-01-01&endDate=2024-01-31&stationId=1`

**响应：**
```json
{
  "code": 200,
  "data": [
    { "date": "2024-01-01", "orderCount": 50, "totalKwh": 500.000 },
    { "date": "2024-01-02", "orderCount": 60, "totalKwh": 600.000 }
  ]
}
```

---

### 12.2 收入汇总

- **GET** `/api/report/income?type=month&year=2024&stationId=1`

---

### 12.3 充电桩利用率

- **GET** `/api/report/pile-usage?stationId=1&startDate=2024-01-01&endDate=2024-01-31`

**响应：**
```json
{
  "code": 200,
  "data": [
    {
      "pileNo": "PILE-001",
      "pileType": "FAST",
      "totalHours": 744,
      "usedHours": 300,
      "usageRate": 40.32
    }
  ]
}
```

---

### 12.4 Excel 报表导出

- **GET** `/api/report/export?type=order&startDate=2024-01-01&endDate=2024-01-31&stationId=1`

**说明：** 返回 Excel 文件流，前端触发下载

---

### 12.5 用户增长分析（管理员）

- **GET** `/api/admin/report/user-growth?type=month&year=2024`

---

### 12.6 故障分析（管理员）

- **GET** `/api/admin/report/fault-analysis?startDate=2024-01-01&endDate=2024-12-31`

---

## 十三、接口汇总统计

| 模块 | 接口数量 |
|------|---------|
| 认证模块 | 4 |
| 用户模块 | 9 |
| 充电站/桩查询 | 4 |
| 预约模块 | 5 |
| 充电订单模块 | 7 |
| 评价模块 | 3 |
| 故障模块 | 2 |
| 公告模块（用户端） | 2 |
| 运营商-充电站管理 | 5 |
| 运营商-充电桩管理 | 5 |
| 运营商-充电枪管理 | 4 |
| 运营商-计费规则 | 4 |
| 运营商-订单管理 | 2 |
| 运营商-收益统计 | 1 |
| 运营商-评价管理 | 2 |
| 运营商-故障管理 | 2 |
| 管理员-用户管理 | 2 |
| 管理员-运营商管理 | 1 |
| 管理员-订单/退款 | 4 |
| 管理员-评价管理 | 3 |
| 管理员-公告管理 | 5 |
| 管理员-操作日志 | 1 |
| 管理员-仪表盘 | 1 |
| 管理员-故障列表 | 1 |
| 报表模块 | 6 |
| **合计** | **85** |
