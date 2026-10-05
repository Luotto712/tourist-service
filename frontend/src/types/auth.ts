// src/types/auth.ts

/** 存在 sessionStorage 里的用户信息 */
export interface User {
  userId: string
  username: string
  realName: string
  role: string
  college: string
}
/** 注册请求（对照 RegisterRequest.java） */
export interface RegisterParams {
  // ✏️ 4 个字段，都是字符串：username / phone / email / password
  username: string
  phone: string
  email: string
  password: string
}
/** 登录请求参数 */
export interface LoginParams {
  // ✏️ 两个字段：username 和 password，都是字符串
  username: string
  password: string
}

/** 登录接口返回（对照后端 LoginResponse.java） */
export interface LoginResponse {
  token: string
  // ✏️ 剩下 5 个字段，照着上面的 Java 写
  //    ⚠️ 注意 userId 是 Long，对应的 TS 类型是 ______
  userId: number
  username: string
  realName: string
  role: string
  college: string
}

/** 当前用户信息（对照后端 UserProfileResponse.java，只写前端用到的） */
export interface UserProfileResponse {
  // ✏️ 前端用到了 id / username / realName / role / college 这 5 个
  //    ⚠️ id 也是 Long
  id: number
  username: string
  realName: string
  role: string
  college: string
}
/** 修改密码请求（对照 ChangePasswordRequest.java） */
export interface ChangePasswordParams {
  // ✏️ 2 个字段
  oldPassword: string
  newPassword: string
}

/** 忘记密码请求（对照 ForgotPasswordRequest.java） */
export interface ForgotPasswordParams {
  // ✏️ 2 个字段
  username: string
  email: string
}