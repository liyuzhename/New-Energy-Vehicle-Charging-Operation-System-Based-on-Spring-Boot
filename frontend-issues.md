# 前端问题清单

以下问题属于前端逻辑/组件问题，后端接口无需变更，需在前端代码中修复。

---

## 问题1：登录界面"记住密码"功能缺失

**现象**：登录页面无"记住密码"选项，刷新或关闭标签页后需要重新登录。

**位置**：`Login.vue`（或对应的登录页面组件）

**修复方向**：
1. 在登录表单中添加"记住密码"复选框。
2. 勾选时将 token 存入 `localStorage`（持久化）；不勾选时存入 `sessionStorage`（临时）。
3. 页面初始化时优先从 `localStorage` 读取 token，若存在则自动登录（跳过登录页）。

---

## 问题2：个人信息页面绑定车辆时报"车牌号不能为空"

**现象**：车牌号格式正确但提交后提示 `Error: 车牌号不能为空`。

**报错位置**：`UserVehicles.vue:140 handleAdd`

**原因分析**：
后端 `BindVehicleRequest` 的车牌号字段名为 **`plateNo`**，前端发送的字段名与后端不一致（可能发送了 `licensePlate`、`plate_no` 或其他名称），导致后端收到空值触发 `@NotBlank` 校验失败。

**修复方向**：
1. 打开 `UserVehicles.vue`，找到 `handleAdd` 方法中构造请求体的代码。
2. 将车牌号字段名修改为 `plateNo`，确保与后端一致，例如：
   ```js
   const data = {
     plateNo: this.form.plateNo,   // 必须是 plateNo
     brand: this.form.brand,
     model: this.form.model,
     batteryCap: this.form.batteryCap
   }
   ```

---

## 问题3：后台用户管理进入时自动触发所有角色状态切换

**现象**：进入后台用户管理页面时，所有用户的角色状态开关自动被切换，需要手动点击所有取消按钮才能恢复正常显示。

**位置**：`AdminUsers.vue`

**原因分析**：
表格中的角色状态使用了 `el-switch` 等双向绑定组件，`v-model` 绑定的值在初始渲染或数据加载时触发了 `change` 事件，导致误调用了状态更新接口。

**修复方向**：
1. 找到 `AdminUsers.vue` 中 switch/toggle 组件的 `change` 事件监听。
2. 使用 `@change` 而非 `v-model` 的 setter 来触发接口调用，或在 `change` 回调中判断新旧值是否真实变化。
3. 确保数据加载完成后再渲染 switch，避免初始化时触发 change 事件：
   ```vue
   <!-- 推荐：手动绑定值，通过事件处理 -->
   <el-switch
     :value="row.status === 1"
     @change="(val) => handleStatusChange(row, val)"
   />
   ```
