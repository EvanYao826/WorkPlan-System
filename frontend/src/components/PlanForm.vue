<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑计划' : '新建计划'"
    width="560px"
    @close="$emit('close')"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="计划类型" prop="type">
        <el-radio-group v-model="form.type" :disabled="isEdit">
          <el-radio :value="1">日计划</el-radio>
          <el-radio :value="2">月计划</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="计划标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入标题" maxlength="200" show-word-limit />
      </el-form-item>

      <el-form-item label="计划日期" prop="planDate">
        <el-date-picker
          v-model="form.planDate"
          type="date"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="计划内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="6"
          placeholder="请输入计划内容"
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
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { createPlan } from '../api/plan'
import { getLeaders } from '../api/user'

const props = defineProps({
  visible: Boolean,
  editData: Object,
})

const emit = defineEmits(['close', 'success'])

const formRef = ref(null)
const loading = ref(false)
const isEdit = ref(false)
const leaders = ref([])

const form = reactive({
  type: 1,
  title: '',
  planDate: '',
  content: '',
  approveLeaderId: null,
})

const rules = {
  type: [{ required: true, message: '请选择计划类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  planDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

watch(() => props.visible, async (val) => {
  if (val) {
    // 加载领导列表
    const res = await getLeaders()
    leaders.value = res.data

    if (props.editData) {
      isEdit.value = true
      Object.assign(form, {
        type: props.editData.type,
        title: props.editData.title,
        planDate: props.editData.planDate,
        content: props.editData.content,
        approveLeaderId: props.editData.approveLeaderId ? String(props.editData.approveLeaderId) : null,
      })
    } else {
      isEdit.value = false
      Object.assign(form, { type: 1, title: '', planDate: '', content: '', approveLeaderId: null })
    }
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
    // 提交审批时带上 approveLeaderId，后端一个事务完成创建+提交
    const payload = { ...form }
    if (!submitAfterSave) {
      payload.approveLeaderId = null
    }
    await createPlan(payload)
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
