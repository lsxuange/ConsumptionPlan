<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { getUnreadCount } from '@/api/notification'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const unreadCount = ref(0)

// 登录/注册页不显示侧边栏和顶部栏
const isAuthPage = computed(() => {
  return route.path === '/login' || route.path === '/register'
})

// 导航菜单
const menuItems = [
  { path: '/dashboard',  label: '首页',     icon: 'House',       iconActive: 'HomeFilled'    },
  { path: '/records',    label: '消费记录', icon: 'Document',    iconActive: 'Tickets'       },
  { path: '/statistics', label: '统计分析', icon: 'TrendCharts', iconActive: 'DataAnalysis'  },
  { path: '/budget',     label: '预算管理', icon: 'Wallet',      iconActive: 'CreditCard'    },
  { path: '/report',     label: 'AI报告',   icon: 'MagicStick',  iconActive: 'DataBoard'     },
  { path: '/profile',    label: '我的',     icon: 'User',        iconActive: 'UserFilled'    },
]

// 当前页面标题
const pageTitleMap = {
  '/dashboard': '首页总览',
  '/records': '消费记录',
  '/statistics': '统计分析',
  '/budget': '预算管理',
  '/report': 'AI报告',
  '/profile': '我的',
}
const currentPageTitle = computed(() => {
  return pageTitleMap[route.path] || ''
})

async function loadUnreadCount() {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res || 0
  } catch (err) {
    // 静默失败
  }
}

onMounted(() => {
  if (!isAuthPage.value) {
    loadUnreadCount()
  }
})
</script>

<template>
  <!-- 登录/注册页：纯净页面 -->
  <template v-if="isAuthPage">
    <router-view />
  </template>

  <!-- 认证页面：侧边栏 + 顶部栏 + 内容 -->
  <div v-else class="app-container">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <!-- Logo区 -->
      <div class="sidebar-logo">
        <span class="logo-icon">💰</span>
        <span class="logo-text">消费计划</span>
      </div>
      <!-- 导航菜单 -->
      <nav class="sidebar-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          active-class="nav-item--active"
          exact-active-class="nav-item--active"
        >
          <el-icon :size="18">
            <component :is="route.path === item.path ? item.iconActive : item.icon" />
          </el-icon>
          <span class="nav-label">{{ item.label }}</span>
        </router-link>
      </nav>
      <!-- 底部用户信息 -->
      <div class="sidebar-footer">
        <el-avatar :size="32" :src="userStore.avatar">
          {{ userStore.username?.charAt(0)?.toUpperCase() }}
        </el-avatar>
        <span class="footer-username">{{ userStore.username }}</span>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 顶部栏 -->
      <header class="topbar">
        <h2 class="page-title">{{ currentPageTitle }}</h2>
        <div class="topbar-right">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0">
            <el-button circle @click="router.push('/profile')">🔔</el-button>
          </el-badge>
          <span class="user-greeting">你好，{{ userStore.username || '用户' }}</span>
        </div>
      </header>
      <!-- 页面内容 -->
      <div class="page-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<style>
/* ===== CSS 变量 ===== */
:root {
  --color-primary: #00b96b;
  --color-primary-dark: #00a35c;
  --color-primary-light: #e8f9f2;
  --color-sidebar-bg: #ffffff;
  --color-sidebar-text: #606266;
  --color-sidebar-active-bg: #e8f9f2;
  --color-sidebar-active-text: #00b96b;
  --color-bg: #f5f7fa;
  --color-border: #e4e7ed;
}

/* ===== 全局重置 ===== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  width: 100%;
  background: #f5f7fa;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ===== 整体布局 ===== */
.app-container {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

/* ===== 侧边栏 ===== */
.sidebar {
  width: 220px;
  min-height: 100vh;
  background: #ffffff;
  border-right: 1px solid #e4e7ed;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
}

.sidebar-logo {
  padding: 28px 20px 24px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 15px;
  font-weight: 700;
  color: #1d1d1f;
  letter-spacing: 0.5px;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 16px;
  border-radius: 10px;
  color: #606266;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s;
  position: relative;
}

.nav-item:hover {
  background: #f0faf5;
  color: #00b96b;
}

.nav-item--active {
  background: #e8f9f2;
  color: #00b96b;
  font-weight: 600;
  box-shadow: none;
}

.nav-item--active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: #00b96b;
  border-radius: 0 2px 2px 0;
}

.nav-item .el-icon {
  margin-right: 8px;
  vertical-align: middle;
  flex-shrink: 0;
}

.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.footer-username {
  color: #909399;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== 主内容区 ===== */
.main-content {
  margin-left: 220px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* ===== 顶部栏 ===== */
.topbar {
  height: 60px;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 99;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-greeting {
  font-size: 14px;
  color: #606266;
}

/* ===== 页面内容 ===== */
.page-content {
  flex: 1;
  padding: 24px 28px;
  overflow-y: auto;
  background: #f5f7fa;
}
</style>
