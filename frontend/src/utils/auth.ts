const TOKEN_KEY = 'tourist_token'
const USER_KEY = 'tourist_user'

export interface User {
  userId: string
  username: string
  realName: string
  role: string
  college: string

  // 兼容旧数据（? = 可能没有）
  id?: string
  name?: string
}


export function getToken() {
  return sessionStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
  sessionStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  sessionStorage.removeItem(TOKEN_KEY)
}

export function getUser() {
  const userStr = sessionStorage.getItem(USER_KEY)
  if (!userStr) return null
  try {
    return JSON.parse(userStr) as User
  } catch {
    return null
  }
}

export function setUser(user: User) {
  sessionStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function removeUser() {
  sessionStorage.removeItem(USER_KEY)
}

export function getRole() {
  const user = getUser()
  return user ? user.role : null
}
