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
      userId.value = user.userId || ''
      username.value = user.username || ''
      realName.value = user.realName || ''
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

  function hasRole(checkRole: string) {
    return role.value === checkRole
  }

  async function login(usernameVal: string, passwordVal: string) {
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
    userId.value = String(data.userId)
    username.value = data.username || usernameVal
    realName.value = data.realName || ''
    role.value = data.role || ''
    college.value = data.college || ''

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
        userId.value = String(data.id)
        username.value = data.username || ''
        realName.value = data.realName || ''
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
