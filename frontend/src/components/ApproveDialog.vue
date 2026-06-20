<template>
  <el-dialog
    :model-value="visible"
    :title="type === 'result' ? '审批成果' : '审批计划'"
    width="560px"
    @close="$emit('close')"
  >
    <template v-if="plan">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="标题" :span="2">{{ plan.title }}</el-descriptions-item>
        <el-descriptions-item v-if="type !== 'result'" label="类型">{{ plan.type === 1 ? '日计划' : '月计划' }}</el-descriptions-item>
        <el-descriptions-item v-if="type !== 'result'" label="计划日期">{{ plan.planDate }}</el-descriptions-item>
        <el-descriptions-item label="提交时间" :span="2">{{ plan.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">
          <div style="white-space: pre-wrap; line-height: 1.8">{{ plan.content }}</div>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <el-form label-width="80px">
        <el-form-item label="审批意见">
          <el-input
            v-model="comment"
            type="textarea"
            :rows="3"
            placeholder="请输入审批意见（驳回时必填）"
          />
        </el-form-item>
      </el-form>
    </template>

    <template #footer>
      <el-button @click="$emit('close')">取消</el-button>
      <el-button type="danger" :loading="loading" @click="handleReject">驳回</el-button>
      <el-button type="success" :loading="loading" @click="handleApprove">通过</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { approvePlan, rejectPlan } from '../api/plan'
import { approveResult, rejectResult } from '../api/result'

const props = defineProps({
  visible: Boolean,
  plan: Object,
  type: { type: String, default: 'plan' }, // 'plan' 或 'result'
})

const emit = defineEmits(['close', 'success'])

const comment = ref('')
const loading = ref(false)

watch(() => props.visible, (val) => {
  if (val) comment.value = ''
})

async function handleApprove() {
  loading.value = true
  try {
    if (props.type === 'result') {
      await approveResult(props.plan.id, comment.value || null)
    } else {
      await approvePlan(props.plan.id, comment.value || null)
    }
    ElMessage.success('审批通过')
    emit('success')
    emit('close')
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

async function handleReject() {
  if (!comment.value.trim()) {
    ElMessage.warning('驳回时请填写审批意见')
    return
  }
  loading.value = true
  try {
    if (props.type === 'result') {
      await rejectResult(props.plan.id, comment.value)
    } else {
      await rejectPlan(props.plan.id, comment.value)
    }
    ElMessage.success('已驳回')
    emit('success')
    emit('close')
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}
</script>
