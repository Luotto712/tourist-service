// src/types/shims-vue.d.ts
// 告诉 TS：所有 .vue 文件都是一个 Vue 组件，默认导出组件对象
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  // Vue 官方 shim 写法，这里的 {} 是刻意的
  // eslint-disable-next-line @typescript-eslint/no-empty-object-type
  const component: DefineComponent<{}, {}, any>
  export default component
}