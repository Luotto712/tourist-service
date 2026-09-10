import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getCurrentUser } from '@/api/auth'
import { getToken, setToken, removeToken, getUser, setUser, removeUser } from '@/utils/auth'
import { ROLE } from '@/constants/roles'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userId = ref('')
  const username = ref('')
  const realName = ref('')
  const role = ref('')
  const college = ref('')

  function initFromStorage() {
    const user = getUser()
    if (user) {
      userId.value = user.userId || user.id || ''
      username.value = user.username || ''
      realName.value = user.realName || user.name || ''
      role.value = user.role || ''
      college.value = user.college || ''
    }
  }

  initFromStorage()

  const isLoggedIn = computed(() => !!token.value)

  const isTourist = computed(() => role.value === ROLE.TOURIST)

  const isPlatformAdmin = computed(() => role.value === ROLE.PLATFORM_ADMIN)

  const isApprover = computed(() => role.value === ROLE.APPROVER)

  const isComplaintHandler = computed(() => role.value === ROLE.COMPLAINT_HANDLER)

  const isHotelAdmin = computed(() => role.value === ROLE.HOTEL_ADMIN)

  function hasRole(checkRole) {
    return role.value === checkRole
  }

  async function login(usernameVal, passwordVal) {
    const res = await loginApi({ username: usernameVal, password: passwordVal })
    const data = res.data || res

    // Check if login actually succeeded
    if (res.code && res.code !== 200) {
      throw new Error(res.message || '登录失败')
    }
    if (!data || !data.token) {
      throw new Error('登录失败，请检查用户名和密码')
    }

    token.value = data.token
    const userInfo = data.user || {
      userId: data.userId,
      id: data.id,
      username: data.username,
      realName: data.realName || data.name,
      role: data.role,
      college: data.college
    }
    userId.value = userInfo.userId || userInfo.id || ''
    username.value = userInfo.username || usernameVal
    realName.value = userInfo.realName || userInfo.name || ''
    role.value = userInfo.role || ''
    college.value = userInfo.college || ''

    setToken(token.value)
    setUser({
      userId: userId.value,
      username: username.value,
      realName: realName.value,
      role: role.value,
      college: college.value
    })
  }

  function logout() {
    token.value = ''
    userId.value = ''
    username.value = ''
    realName.value = ''
    role.value = ''
    college.value = ''
    removeToken()
    removeUser()
  }

  async function fetchProfile() {
    try {
      const res = await getCurrentUser()
      const data = res.data || res
      if (data) {
        userId.value = data.userId || data.id || ''
        username.value = data.username || ''
        realName.value = data.realName || data.name || ''
        role.value = data.role || ''
        college.value = data.college || ''
        setUser({
          userId: userId.value,
          username: username.value,
          realName: realName.value,
          role: role.value,
          college: college.value
        })
      }
    } catch {
      // Profile fetch failure is non-fatal
    }
  }

  return {
    token,
    userId,
    username,
    realName,
    role,
    college,
    isLoggedIn,
    isTourist,
    isPlatformAdmin,
    isApprover,
    isComplaintHandler,
    isHotelAdmin,
    hasRole,
    login,
    logout,
    fetchProfile
  }
})
