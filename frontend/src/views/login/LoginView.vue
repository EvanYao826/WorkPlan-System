<template>
  <div class="login-page">
    <!-- 左侧 -->
    <div class="left-panel">
      <div class="left-content">
        <div class="brand">📋 PlanCraft</div>
        <h1>日/月计划<br />与成果验收系统</h1>
        <p class="desc">规范化的计划提交与审批流程，让团队协作更高效</p>
        <div class="features">
          <div class="feature-item">
            <span class="feat-icon">📝</span>
            <span>日计划 / 月计划在线提交</span>
          </div>
          <div class="feature-item">
            <span class="feat-icon">✅</span>
            <span>领导实时审批，意见留存可追溯</span>
          </div>
          <div class="feature-item">
            <span class="feat-icon">📊</span>
            <span>数据统计，团队进度一目了然</span>
          </div>
        </div>
      </div>
      <!-- 装饰圆 -->
      <div class="deco-circle deco-1"></div>
      <div class="deco-circle deco-2"></div>
      <div class="deco-circle deco-3"></div>
    </div>

    <!-- 右侧 -->
    <div class="right-panel">
      <div class="form-wrapper">
        <div class="form-header">
          <h2>欢迎登录</h2>
          <p>请输入您的账号密码</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              prefix-icon="User"
              size="large"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              placeholder="密码"
              prefix-icon="Lock"
              type="password"
              show-password
              size="large"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="test-accounts">
          <el-divider>
            <span style="color: #c0c4cc; font-size: 12px">测试账号</span>
          </el-divider>
          <div class="account-list">
            <el-button
              v-for="item in accounts"
              :key="item.user"
              round
              @click="fillAccount(item)"
            >
              {{ item.label }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/auth'
import { setToken, setUserInfo } from '../../utils/auth'
import { useUserStore } from '../../store/index'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const accounts = [
  { label: '同事', user: 'emp1', pwd: 'emp123' },
  { label: '领导', user: 'leader1', pwd: 'leader123' },
]

function fillAccount(item) {
  form.username = item.user
  form.password = item.pwd
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login(form)
    setToken(res.data.token)
    setUserInfo(res.data)
    userStore.setUser(res.data)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    // 错误已在 request.js 拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* 左侧 */
.left-panel {
  flex: 1;
  background: linear-gradient(160deg, #2b3a67 0%, #409eff 60%, #36d1dc 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;
}

.left-content {
  color: #fff;
  max-width: 420px;
  position: relative;
  z-index: 2;
}

.brand {
  font-size: 15px;
  letter-spacing: 3px;
  opacity: 0.5;
  margin-bottom: 40px;
  text-transform: uppercase;
}

.left-content h1 {
  font-size: 38px;
  font-weight: 700;
  line-height: 1.35;
  margin: 0 0 20px;
}

.desc {
  font-size: 15px;
  opacity: 0.7;
  line-height: 1.7;
  margin: 0 0 48px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  opacity: 0.85;
}

.feat-icon {
  font-size: 18px;
}

/* 装饰圆 */
.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
  z-index: 1;
}
.deco-1 {
  width: 300px;
  height: 300px;
  bottom: -80px;
  left: -60px;
}
.deco-2 {
  width: 180px;
  height: 180px;
  top: -40px;
  right: -30px;
}
.deco-3 {
  width: 100px;
  height: 100px;
  top: 40%;
  right: 15%;
  background: rgba(255, 255, 255, 0.04);
}

/* 右侧 */
.right-panel {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #c3d9f0 0%, #a8c8f0 40%, #b8e0e8 100%);
}

.form-wrapper {
  width: 360px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 44px 36px 36px;
  box-shadow: 0 8px 32px rgba(43, 58, 103, 0.12);
}

.form-header {
  margin-bottom: 32px;
}

.form-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #2b3a67;
  margin: 0 0 8px;
}

.form-header p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.test-accounts {
  margin-top: 4px;
}

.account-list {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.login-btn {
  width: 100%;
  background: linear-gradient(135deg, #409eff 0%, #2b6cb0 100%);
  border: none;
}
.login-btn:hover {
  background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
}
</style>
