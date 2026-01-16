import { createPinia } from 'pinia'

const pinia = createPinia()

export default pinia

// ==================== 统一导出所有 store ====================
export * from './user'
export * from './app'
