<template>
  <el-dialog
    :model-value="visible"
    title="提交成果"
    width="560px"
    @close="$emit('close')"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="关联计划" prop="planId">
        <el-select
          v-model="form.planId"
          placeholder="请选择已通过的计划"
          style="width: 100%"
          filterable
        >
          <el-option
            v-for="plan in approvedPlans"
            :key="plan.id"
            :label="`${plan.title}（${plan.planDate}）`"
            :value="plan.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="成果标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入标题" maxlength="200" show-word-limit />
      </el-form-item>

      <el-form-item label="成果内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="6"
          placeholder="请描述成果内容"
        />
      </el-form-item>

      <el-form-item label="审批领导" prop="approveLeaderId">
        <el-select v-model="form.approveLeaderId" placeholder="请选择审批领导" style="width: 100%">
          <el-option
            v-for="leader in leaders"
            :key="leader.id"
            :label="leader.name"
            :value="leader.id"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="$emit('close')">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSaveDraft">保存草稿</el-button>
      <el-button type="success" :loading="loading" @click="handleSubmit">提交审批</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createResult } from '../api/result'
import { getPlanList } from '../api/plan'
import { getLeaders } from '../api/user'

const props = defineProps({
  visible: Boolean,
})

const emit = defineEmits(['close', 'success'])

const formRef = ref(null)
const loading = ref(false)
const approvedPlans = ref([])
const leaders = ref([])

const form = reactive({
  planId: null,
  title: '',
  content: '',
  approveLeaderId: null,
})

const rules = {
  planId: [{ required: true, message: '请选择关联计划', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

watch(() => props.visible, async (val) => {
  if (val) {
    // 加载已通过的计划和领导列表
    const [planRes, leaderRes] = await Promise.all([
      getPlanList({ status: 2, size: 100 }),
      getLeaders(),
    ])
    approvedPlans.value = planRes.data.records
    leaders.value = leaderRes.data

    Object.assign(form, { planId: null, title: '', content: '', approveLeaderId: null })
  }
})

async function handleSaveDraft() {
  await doSave(false)
}

async function handleSubmit() {
  await doSave(true)
}

async function doSave(submitAfterSave) {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (submitAfterSave && !form.approveLeaderId) {
    ElMessage.warning('请选择审批领导')
    return
  }

  loading.value = true
  try {
    const payload = { ...form }
    if (!submitAfterSave) {
      payload.approveLeaderId = null
    }
    await createResult(payload)
    ElMessage.success(submitAfterSave ? '已提交审批' : '已保存草稿')
    emit('success')
    emit('close')
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}
</script>
