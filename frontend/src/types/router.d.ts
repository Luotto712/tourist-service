// src/types/router.d.ts
import 'vue-router'
import type { Role } from '@/constants/roles'

declare module 'vue-router' {
  interface RouteMeta {
    // ✏️ 三个字段（都写可选，因为不是每个路由都有）
    //    noAuth     —— boolean，"不需要登录"
    //    roles      —— Role[]，允许访问的角色
    //    activeMenu —— string，侧边栏高亮的路径
    noAuth?: boolean
    roles?: Role[]
    activeMenu?: string
  }
}
