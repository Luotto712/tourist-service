import { createRouter, createWebHistory } from 'vue-router'
import { getToken, getRole } from '@/utils/auth'
import { ROLE } from '@/constants/roles'

const AppLayout = () => import('@/components/layout/AppLayout.vue')

// 查询类目录：游客只读 + 平台管理员维护（走通用 Catalog 组件）
const CATALOG_RESOURCES = [
  'attractions',
  'routes',
  'catering',
  'performance-groups',
  'transport',
  'hotels/star',
  'hotels/nonstar'
]
const catalogRoutes = CATALOG_RESOURCES.flatMap(resource => [
  {
    path: `/catalog/${resource}`,
    component: AppLayout,
    meta: { roles: [ROLE.TOURIST] },
    children: [
      {
        path: '',
        name: `Catalog-${resource.replace('/', '-')}`,
        component: () => import('@/views/shared/CatalogListView.vue'),
        props: { resource }
      }
    ]
  },
  {
    path: `/manage/${resource}`,
    component: AppLayout,
    meta: { roles: [ROLE.PLATFORM_ADMIN] },
    children: [
      {
        path: '',
        name: `Manage-${resource.replace('/', '-')}`,
        component: () => import('@/views/platform_admin/CatalogManageView.vue'),
        props: { resource }
      }
    ]
  }
])

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/RegisterView.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/login/ForgotPasswordView.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('@/views/login/ResetPasswordView.vue'),
    meta: { noAuth: true }
  },

  // 首页（所有登录用户）
  {
    path: '/',
    component: AppLayout,
    children: [{ path: '', name: 'Home', component: () => import('@/views/shared/HomeView.vue') }]
  },

  // 游客：我的投诉 + 应急信息
  {
    path: '/complaints',
    component: AppLayout,
    meta: { roles: [ROLE.TOURIST] },
    children: [
      {
        path: '',
        name: 'MyComplaints',
        component: () => import('@/views/shared/MyComplaintsView.vue')
      }
    ]
  },
  {
    path: '/complaints/:id',
    component: AppLayout,
    meta: { activeMenu: '/complaints' },
    children: [
      {
        path: '',
        name: 'ComplaintDetail',
        component: () => import('@/views/shared/ComplaintDetailView.vue'),
        props: true
      }
    ]
  },
  {
    path: '/my-bookings',
    component: AppLayout,
    meta: { roles: [ROLE.TOURIST] },
    children: [
      { path: '', name: 'MyBookings', component: () => import('@/views/shared/MyBookingsView.vue') }
    ]
  },
  {
    path: '/emergency-info/list',
    component: AppLayout,
    meta: { roles: [ROLE.TOURIST] },
    children: [
      {
        path: '',
        name: 'EmergencyInfoList',
        component: () => import('@/views/shared/EmergencyInfoListView.vue')
      }
    ]
  },

  // 审批人员：投诉审批 + 应急信息审批
  {
    path: '/complaints/approval',
    component: AppLayout,
    meta: { roles: [ROLE.APPROVER] },
    children: [
      {
        path: '',
        name: 'ComplaintApproval',
        component: () => import('@/views/approver/ComplaintApprovalView.vue')
      }
    ]
  },
  {
    path: '/emergency-info/approval',
    component: AppLayout,
    meta: { roles: [ROLE.APPROVER] },
    children: [
      {
        path: '',
        name: 'EmergencyApproval',
        component: () => import('@/views/approver/EmergencyApprovalView.vue')
      }
    ]
  },

  // 平台管理员：投诉分派/结案、应急信息管理、用户管理
  {
    path: '/complaints/assign',
    component: AppLayout,
    meta: { roles: [ROLE.PLATFORM_ADMIN] },
    children: [
      {
        path: '',
        name: 'ComplaintAssign',
        component: () => import('@/views/platform_admin/ComplaintAssignView.vue')
      }
    ]
  },
  {
    path: '/complaints/close',
    component: AppLayout,
    meta: { roles: [ROLE.PLATFORM_ADMIN] },
    children: [
      {
        path: '',
        name: 'ComplaintClose',
        component: () => import('@/views/platform_admin/ComplaintCloseView.vue')
      }
    ]
  },
  {
    path: '/emergency-info',
    component: AppLayout,
    meta: { roles: [ROLE.PLATFORM_ADMIN] },
    children: [
      {
        path: '',
        name: 'EmergencyInfoManage',
        component: () => import('@/views/platform_admin/EmergencyInfoManageView.vue')
      }
    ]
  },
  {
    path: '/users',
    component: AppLayout,
    meta: { roles: [ROLE.PLATFORM_ADMIN] },
    children: [
      {
        path: '',
        name: 'UserManage',
        component: () => import('@/views/platform_admin/UserManageView.vue')
      }
    ]
  },

  // 投诉处理人员
  {
    path: '/complaints/handler',
    component: AppLayout,
    meta: { roles: [ROLE.COMPLAINT_HANDLER] },
    children: [
      {
        path: '',
        name: 'ComplaintProcess',
        component: () => import('@/views/handler/ComplaintProcessView.vue')
      }
    ]
  },

  // 酒店管理员
  {
    path: '/hotel/rooms',
    component: AppLayout,
    meta: { roles: [ROLE.HOTEL_ADMIN] },
    children: [
      {
        path: '',
        name: 'HotelRoomEntry',
        component: () => import('@/views/hotel_admin/HotelRoomView.vue')
      }
    ]
  },

  // 查询类目录（游客只读）与维护（平台管理员）
  ...catalogRoutes,

  // 天气及出行（游客）
  {
    path: '/weather-conditions',
    component: AppLayout,
    meta: { roles: [ROLE.TOURIST] },
    children: [
      {
        path: '',
        name: 'WeatherRoad',
        component: () => import('@/views/shared/WeatherRoadView.vue')
      }
    ]
  },

  // 平台管理员：数据看板 + 酒店营销
  {
    path: '/dashboard',
    component: AppLayout,
    meta: { roles: [ROLE.PLATFORM_ADMIN] },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/platform_admin/DashboardView.vue')
      }
    ]
  },
  {
    path: '/hotel-marketing',
    component: AppLayout,
    meta: { roles: [ROLE.PLATFORM_ADMIN] },
    children: [
      {
        path: '',
        name: 'HotelMarketing',
        component: () => import('@/views/platform_admin/HotelMarketingView.vue')
      }
    ]
  },

  {
    path: '/profile',
    component: AppLayout,
    children: [
      {
        path: '',
        name: 'UserProfile',
        component: () => import('@/views/shared/UserProfileView.vue')
      }
    ]
  },

  {
    path: '/change-password',
    component: AppLayout,
    children: [
      {
        path: '',
        name: 'ChangePassword',
        component: () => import('@/views/login/ChangePasswordView.vue')
      }
    ]
  },

  // Notifications (all authenticated users)
  {
    path: '/notifications',
    component: AppLayout,
    children: [
      {
        path: '',
        name: 'Notifications',
        component: () => import('@/views/shared/NotificationListView.vue')
      }
    ]
  },

  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  const token = getToken()

  if (to.meta.noAuth) {
    next()
    return
  }

  if (!token) {
    next({ name: 'Login', query: { redirect: to.path !== '/' ? to.fullPath : '' } })
    return
  }

  if (to.meta.roles) {
    const userRole = getRole()
    if (!userRole || !to.meta.roles.includes(userRole)) {
      next({ name: 'Home' })
      return
    }
  }

  next()
})

export default router
