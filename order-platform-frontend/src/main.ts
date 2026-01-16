import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import pinia from './stores'

// ==================== 全局样式 ====================
import './styles/index.scss'

const app = createApp(App)

// ==================== 注册插件 ====================
// 重要：pinia 必须在 router 之前注册
// 因为 router 的 beforeEach 守卫会使用 userStore
app.use(pinia)
app.use(router)
app.use(ElementPlus)

// ==================== 按需注册 Element Plus 图标 ====================
// 只注册实际使用的图标，减少 bundle size
import {
  HomeFilled,
  Document,
  Van,
  User,
  UserFilled,
  Lock,
  DataAnalysis,
  Connection,
  Warning
} from '@element-plus/icons-vue'

app.component('HomeFilled', HomeFilled)
app.component('Document', Document)
app.component('Van', Van)
app.component('User', User)
app.component('UserFilled', UserFilled)
app.component('Lock', Lock)
app.component('DataAnalysis', DataAnalysis)
app.component('Connection', Connection)
app.component('Warning', Warning)

// ==================== 挂载应用 ====================
app.mount('#app')
