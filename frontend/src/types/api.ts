// src/types/api.ts

/**
 * 后端统一返回结构
 * 对照后端：com.tourist.common.Result
 */
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}
export interface PageResult<T> {
  // ✏️ 两个字段，照着上面的 Java 写
  //    total 是 long → TS 里是 ______
  total: number
  list: T[]
}
