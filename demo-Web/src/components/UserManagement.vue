<template>
  <div class="container">
    <!-- 搜索框和新增按钮 -->
    <div class="header">
      <el-input
          v-model="searchKey"
          placeholder="搜索用户名称"
          style="width: 300px; margin-right: 20px"
          clearable
          @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleAdd">新增用户</el-button>
    </div>
    <!-- 表格展示数据 -->
    <el-table :data="currentPageData" border stripe style="width: 100%">
      <el-table-column prop="username" label="用户名" width="180" />
      <el-table-column label="密码" width="120">
        <template #default="{ row }">
          {{ row.password.replace(/./g, '*') }}
        </template>
      </el-table-column>
      <el-table-column label="角色" min-width="250">
        <template #default="{ row }">
          {{ row.role }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.userId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件包裹元素 -->
    <div class="pagination-wrapper">
      <el-pagination
          v-if="filteredData.length > 10"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 30]"
          :page-size="pageSize"
          prev-text="上一页"
          next-text="下一页"
          :total="filteredData.length"
          layout="total, sizes, prev, pager, next, jumper"
      >
        <template #total>
          共 {{ filteredData.length }} 条数据
        </template>
        <template #sizes>
          每页显示
          <el-select v-model="pageSize" @change="handleSizeChange">
            <el-option
                v-for="size in [10, 20, 30]"
                :key="size"
                :label="size + ' 条'"
                :value="size"
            />
          </el-select>
        </template>
        <template #jumper>
          跳转到
          <el-input v-model="jumpPage" @keyup.enter="handleJumpPage" size="small" style="width: 50px" /> 页
        </template>
      </el-pagination>
    </div>
    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="showDialog" :title="dialogTitle" width="30%">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" placeholder="请选择角色">
            <el-option
                v-for="role in roles"
                :key="role"
                :label="role"
                :value="role"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 表格数据
const tableData = ref([])
const searchKey = ref('')

// 对话框相关状态
const showDialog = ref(false)
const isEditMode = ref(false)
const currentId = ref(null)

// 表单数据
const form = ref({
  username: '',
  password: '',
  role: ''
})

// 角色数据
const roles = ref([])

// 分页相关数据
const currentPage = ref(1)
const pageSize = ref(10)
const jumpPage = ref(1)

// 计算属性
const dialogTitle = computed(() => (isEditMode.value ? '编辑用户' : '新增用户'))

// 过滤后的数据
const filteredData = computed(() => {
  return tableData.value.filter(item => {
    if (item && item.username) {
      return item.username.toLowerCase().includes(searchKey.value.toLowerCase())
    }
    return false
  })
})

// 当前页要展示的数据
const currentPageData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredData.value.slice(start, end)
})

// 生命周期钩子
onMounted(async () => {
  await fetchData()
  await fetchRoles()
  console.log('tableData:', tableData.value)
  console.log('filteredData:', filteredData.value)
  console.log('roles:', roles.value)
})

// 获取用户数据
const fetchData = async () => {
  try {
    const { data } = await axios.get('http://localhost:8080/users/all')
    tableData.value = data.data.map(item => ({
      userId: item.userID,
      username: item.username,
      password: item.password,
      role: item.role // 去掉默认的 '普通用户'
    }))
    ElMessage.success('数据加载成功');
  } catch (error) {
    console.error('用户数据加载失败:', error)
    ElMessage.error('用户数据加载失败')
  }
}

// 获取角色数据
const fetchRoles = async () => {
  try {
    const { data } = await axios.get('http://localhost:8080/role/roles')
    if (data.code === 200) {
      roles.value = data.data.map(role => role.roleName)
    } else {
      ElMessage.error(data.msg)
    }
  } catch (error) {
    console.error('角色数据加载失败:', error)
    ElMessage.error('角色数据加载失败')
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1 // 搜索后回到第一页
}

// 处理新增
const handleAdd = () => {
  isEditMode.value = false
  form.value = { username: '', password: '', role: '' }
  showDialog.value = true
}

// 处理编辑
const handleEdit = (row) => {
  isEditMode.value = true
  currentId.value = row.userId
  form.value = { ...row }
  showDialog.value = true
}

// 处理删除
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // 发送删除请求到后端
    await axios.delete(`http://localhost:8080/users/${id}`)
    // 从本地数据中移除已删除的用户
    tableData.value = tableData.value.filter(item => item.userId !== id)
    ElMessage.success('删除成功')
    // 删除成功后刷新数据
    await fetchData()
  } catch (error) {
    ElMessage.info('取消删除')
  }
}

// 提交表单
const submitForm = async () => {
  try {
    const formData = { ...form.value };
    console.log('formData:', formData); // 打印请求数据

    if (isEditMode.value) {
      // 编辑用户，发送 PUT 请求
      const response = await axios.put(`http://localhost:8080/users/${currentId.value}/authority`, formData);
      if (response.data.code === 200) { // 假设成功的状态码是 200
        const index = tableData.value.findIndex(item => item.userId === currentId.value);
        tableData.value[index] = { ...formData, userId: currentId.value };
        ElMessage.success('用户更新成功');
        // 编辑成功后刷新数据
        await fetchData()
      } else {
        ElMessage.error(response.data.message);
      }
    } else {
      // 新增用户，发送 POST 请求
      const response = await axios.post('http://localhost:8080/users/adduser', formData);
      if (response.data.code === 200) {
        tableData.value.push(response.data.data);
        ElMessage.success('用户创建成功');
        // 新增成功后刷新数据
        await fetchData()
      } else {
        ElMessage.error(response.data.message);
      }
    }
    showDialog.value = false;
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

// 处理每页显示数量的变化
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1 // 改变每页数量后回到第一页
}

// 处理页码变化
const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
}

// 处理跳转页码
const handleJumpPage = () => {
  const totalPages = Math.ceil(filteredData.value.length / pageSize.value)
  if (jumpPage.value >= 1 && jumpPage.value <= totalPages) {
    currentPage.value = jumpPage.value
  } else {
    ElMessage.error('输入的页码无效')
  }
}
</script>

<style scoped>
.container {
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
.header {
  margin-bottom: 20px;
}
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
.inline-checkbox-group {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
}
.inline-checkbox-group .el-checkbox {
  margin-right: 10px; /* 可根据需要调整复选框之间的间距 */
}
/* 让表格自适应内容，消除右边空白 */
.el-table__header-wrapper,
.el-table__body-wrapper {
  width: auto !important;
}
</style>