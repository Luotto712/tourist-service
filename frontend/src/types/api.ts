// src/types/api.ts

/**
 * 后端统一返回结构
 * 对照后端：com.tourist.common.Result
 */
export interface ApiResult<T = unknown> {
  // ✏️ 三个字段，照着上面 Java 那三个写
  //    code    —— 数字
  //    message —— 字符串
  //    data    —— 泛型 T
  code: number
  message: string
  data: T
}
