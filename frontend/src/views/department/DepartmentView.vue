<template>
  <div class="department-page">
    <el-row :gutter="16">
      <!-- 左侧：部门树 -->
      <el-col :span="8">
        <el-card shadow="never" class="tree-card">
          <template #header>
            <div class="card-header">
              <span>部门结构</span>
              <el-button type="primary" size="small" @click="openCreate(null)">
                <el-icon><Plus /></el-icon>新增顶级部门
              </el-button>
            </div>
          </template>

          <el-tree
            :data="treeData"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            highlight-current
            default-expand-all
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <span>{{ data.name }}</span>
                <span class="tree-actions">
                  <el-button type="primary" link size="small" @click.stop="openCreate(data)">
                    <el-icon><Plus /></el-icon>
                  </el-button>
                  <el-button type="danger" link size="small" @click.stop="handleDelete(data)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </span>
              </div>
            </template>
          </el-tree>

          <el-empty v-if="treeData.length === 0" description="暂无部门数据" />
        </el-card>
      </el-col>

      <!-- 右侧：部门详情/编辑 -->
      <el-col :span="16">
        <el-card shadow="never" class="form-card">
          <template #header>
            <span>{{ selectedDept ? '编辑部门' : '部门详情' }}</span>
          </template>

          <template v-if="selectedDept">
            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-width="100px"
              style="max-width: 500px"
            >
              <el-form-item label="部门名称" prop="name">
                <el-input v-model="form.name" placeholder="请输入部门名称" />
              </el-form-item>
              <el-form-item label="上级部门" prop="parentId">
                <el-tree-select
                  v-model="form.parentId"
                  :data="treeData"
                  :props="{ label: 'name', children: 'children', value: 'id' }"
                  placeholder="选择上级部门（不选则为顶级）"
                  clearable
                  check-strictly
                />
              </el-form-item>
              <el-form-item label="部门负责人" prop="leaderId">
                <el-select v-model="form.leaderId" placeholder="选择负责人" clearable>
                  <el-option
                    v-for="leader in leaders"
                    :key="leader.id"
                    :label="leader.name"
                    :value="leader.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="排序号" prop="sortOrder">
                <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
              </el-form-item>
              <el-form-item label="状态" prop="status">
                <el-radio-group v-model="form.status">
                  <el-radio :value="1">正常</el-radio>
                  <el-radio :value="0">停用</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSave">保存</el-button>
                <el-button @click="selectedDept = null">取消</el-button>
              </el-form-item>
            </el-form>
          </template>

          <el-empty v-else description="请从左侧选择部门进行编辑" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增部门弹框 -->
    <el-dialog v-model="createVisible" title="新增部门" width="500px">
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="上级部门">
          <el-input :model-value="parentDeptName" disabled />
        </el-form-item>
        <el-form-item label="部门负责人" prop="leaderId">
          <el-select v-model="createForm.leaderId" placeholder="选择负责人" clearable>
            <el-option
              v-for="leader in leaders"
              :key="leader.id"
              :label="leader.name"
              :value="leader.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number v-model="createForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDepartmentTree, createDepartment, updateDepartment, deleteDepartment } from '../../api/department'
import { getLeaders } from '../../api/user'

const treeData = ref([])
const leaders = ref([])
const selectedDept = ref(null)
const createVisible = ref(false)
const parentDeptName = ref('')

const formRef = ref(null)
const createFormRef = ref(null)

const form = reactive({
  name: '',
  parentId: null,
  leaderId: null,
  sortOrder: 0,
  status: 1,
})

const createForm = reactive({
  name: '',
  parentId: null,
  leaderId: null,
  sortOrder: 0,
})

const rules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
}

// 获取部门树
async function fetchTree() {
  try {
    const res = await getDepartmentTree()
    treeData.value = res.data
  } catch (e) {
    // 错误已由拦截器处理
  }
}

// 获取领导列表
async function fetchLeaders() {
  try {
    const res = await getLeaders()
    leaders.value = res.data
  } catch (e) {
    // 错误已由拦截器处理
  }
}

// 点击树节点
function handleNodeClick(data) {
  selectedDept.value = data
  form.name = data.name
  form.parentId = data.parentId || null
  form.leaderId = data.leaderId
  form.sortOrder = data.sortOrder
  form.status = data.status
}

// 打开新增弹框
function openCreate(parent) {
  createForm.name = ''
  createForm.leaderId = null
  createForm.sortOrder = 0

  if (parent) {
    createForm.parentId = parent.id
    parentDeptName.value = parent.name
  } else {
    createForm.parentId = null
    parentDeptName.value = '无（顶级部门）'
  }

  createVisible.value = true
}

// 新增部门
async function handleCreate() {
  await createFormRef.value.validate()
  try {
    await createDepartment(createForm)
    ElMessage.success('新增成功')
    createVisible.value = false
    fetchTree()
  } catch (e) {
    // 错误已由拦截器处理
  }
}

// 保存编辑
async function handleSave() {
  await formRef.value.validate()
  try {
    await updateDepartment(selectedDept.value.id, form)
    ElMessage.success('保存成功')
    fetchTree()
  } catch (e) {
    // 错误已由拦截器处理
  }
}

// 删除部门
async function handleDelete(data) {
  await ElMessageBox.confirm(`确认删除部门「${data.name}」？`, '提示', { type: 'warning' })
  try {
    await deleteDepartment(data.id)
    ElMessage.success('删除成功')
    if (selectedDept.value?.id === data.id) {
      selectedDept.value = null
    }
    fetchTree()
  } catch (e) {
    // 错误已由拦截器处理
  }
}

onMounted(() => {
  fetchTree()
  fetchLeaders()
})
</script>

<style scoped>
.department-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tree-card,
.form-card {
  border: 1px solid #f0f0f0;
  min-height: calc(100vh - 150px);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 8px;
}

.tree-actions {
  display: none;
}

.tree-node:hover .tree-actions {
  display: inline-flex;
}
</style>
