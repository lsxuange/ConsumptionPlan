<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度为 2-20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度为 6-30 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const { token, username, avatar } = await login(form)
    userStore.setUser(token, username, avatar)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (err) {
    ElMessage.error(err.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-wrapper">

    <!-- Logo 区 -->
    <div class="login-logo">
      <!-- SVG 圆形徽章 -->
      <svg class="logo-badge" width="264" height="264" viewBox="0 0 160 160" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <path id="circle-text-path" d="M 80,80 m -60,0 a 60,60 0 1,1 120,0 a 60,60 0 1,1 -120,0"/>
        </defs>
        <!-- 外圆环 -->
        <circle cx="80" cy="80" r="72" fill="#e8f9f2" stroke="#00b96b" stroke-width="2.5" opacity="0.6"/>
        <!-- 内虚线圆环 -->
        <circle cx="80" cy="80" r="64" fill="none" stroke="#00b96b" stroke-width="0.6" stroke-dasharray="3 5" opacity="0.35"/>
        <!-- 环绕文字 -->
        <text font-family="Arial, sans-serif" font-size="9.5" fill="#00b96b" letter-spacing="7.2">
          <textPath href="#circle-text-path" startOffset="5%">
            CONSUMPTION · PLAN · CONSUMPTION · PLAN ·
          </textPath>
        </text>
        <!-- 中心文字 CP -->
        <text x="80" y="88" text-anchor="middle" font-family="'Playfair Display', serif" font-size="52" font-weight="bold" fill="#00a35c" letter-spacing="4">CP</text>
      </svg>
      <!-- slogan -->
      <p class="login-logo__slogan">智能记账 · 科学消费</p>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 底部注册引导 -->
    <div class="login-footer">
      还没有账号？
      <router-link to="/register" class="login-footer__link">立即注册</router-link>
    </div>
  </div>
</template>

<style scoped>
.login-wrapper {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f7f8f5;
  overflow: hidden;
}

/* ==== Logo 区 ==== */
.login-logo {
  text-align: center;
  margin-bottom: 32px;
}

.logo-badge {
  display: block;
  margin: 0 auto;
  transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  cursor: default;
}

.logo-badge:hover {
  transform: scale(1.12);
}

.logo-badge:hover text[font-weight="bold"] {
  font-weight: 900;
  filter: drop-shadow(0 0 6px rgba(0, 163, 92, 0.5));
}

.logo-badge:hover circle:first-of-type {
  stroke-width: 3.5;
  opacity: 1;
  transition: all 0.35s ease;
}

.login-logo__slogan {
  font-size: 13px;
  color: #606266;
  letter-spacing: 0.15em;
  margin: 28px 0 0;
}

/* ==== 登录卡片 ==== */
.login-card {
  width: 360px;
  padding: 36px 40px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

/* Element Plus 表单穿透样式 */
.login-card :deep(.el-form-item) {
  margin-bottom: 22px;
}

.login-card :deep(.el-form-item__label) {
  color: #1d1d1f;
}

.login-card :deep(.el-input__wrapper) {
  background: #ffffff;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  box-shadow: none;
}

.login-card :deep(.el-input__wrapper:hover) {
  border-color: #c0c4cc;
}

.login-card :deep(.el-input__wrapper.is-focus) {
  border-color: #00b96b;
}

.login-card :deep(.el-input__inner) {
  color: #1d1d1f;
}

.login-card :deep(.el-input__inner::placeholder) {
  color: #c0c4cc;
}

.login-card :deep(.el-input .el-input__prefix) {
  color: #c0c4cc;
}

/* 登录按钮 */
.login-card :deep(.el-button--primary) {
  width: 100%;
  height: 42px;
  border-radius: 8px;
  --el-button-bg-color: #00b96b;
  --el-button-border-color: #00b96b;
  --el-button-hover-bg-color: #00a35c;
  --el-button-hover-border-color: #00a35c;
  font-size: 15px;
  letter-spacing: 0.05em;
}

/* 密码可见切换图标 */
.login-card :deep(.el-input__suffix) {
  color: #c0c4cc;
}

/* 清除按钮 */
.login-card :deep(.el-input .el-input__clear) {
  color: #c0c4cc;
}

/* ==== 底部注册引导 ==== */
.login-footer {
  margin-top: 16px;
  font-size: 14px;
  color: #606266;
}

.login-footer__link {
  color: #00b96b;
  text-decoration: none;
  margin-left: 4px;
}

.login-footer__link:hover {
  text-decoration: underline;
}
</style>

