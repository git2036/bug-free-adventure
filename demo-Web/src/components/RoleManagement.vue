<template>
  <div class="grid-item">
    <!-- 搜索框与新增按钮容器 -->
    <div class="search-add-container">
      <el-input
          v-model="roleSearchQuery"
          placeholder="搜索角色名称"
          clearable
          @keyup.enter="handleRoleSearch"
          style="width: 250px; flex-shrink: 1; min-width: 200px"
      />

      <el-button
          type="primary"
          @click="openAddDialog"
          style="margin-left: 0"
      >
        新增角色
      </el-button>
    </div>

    <el-table :data="currentPageRoles" border stripe style="width: 100%">
      <el-table-column prop="roleID" label="角色ID" width="120" />
      <el-table-column prop="roleName" label="角色名称" width="180" />
      <el-table-column label="角色权限" min-width="350">
        <template #default="{ row }">
          <el-checkbox-group v-model="row.permissions" class="inline-checkbox-group">
            <el-checkbox label="design">报表设计</el-checkbox>
            <el-checkbox label="report manager">报表管理</el-checkbox>
            <el-checkbox label="data source">数据源管理</el-checkbox>
            <el-checkbox label="report example">报表实例管理</el-checkbox>
            <el-checkbox label="role">角色管理</el-checkbox>
            <el-checkbox label="user">用户管理</el-checkbox>
          </el-checkbox-group>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <div class="button-group">
            <el-button size="small" @click="openEditDialog(row)" :disabled="row.roleID === 1">
              编辑
            </el-button>
            <el-button
                size="small"
                type="danger"
                @click="deleteRole(row)"
                :disabled="row.roleID === 1"
            >
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件（居中显示） -->
    <div class="pagination-container" v-if="total > 10">
      <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
          layout="prev, pager, next"
          class="centered-pagination"
      />
    </div>

    <!-- 编辑角色弹窗 -->
    <el-dialog
        v-model="editDialogVisible"
        title="编辑角色"
        @close="resetEditForm"
        :before-close="handleBeforeClose"
        width="400px"
    >
      <el-form
          :model="editForm"
          :rules="editRules"
          ref="editFormRef"
          label-width="80px"
          style="width: 350px"
      >
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="editForm.roleName" />
        </el-form-item>
        <el-form-item label="角色权限" prop="permissions">
          <el-checkbox-group v-model="editForm.permissions" class="inline-checkbox-group">
            <el-checkbox label="design">报表设计</el-checkbox>
            <el-checkbox label="report manager">报表管理</el-checkbox>
            <el-checkbox label="data source">数据源管理</el-checkbox>
            <el-checkbox label="report example">报表实例管理</el-checkbox>
            <el-checkbox label="role">角色管理</el-checkbox>
            <el-checkbox label="user">用户管理</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditRole">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新增角色弹窗 -->
    <el-dialog
        v-model="addDialogVisible"
        title="新增角色"
        @close="resetAddForm"
        width="400px"
    >
      <el-form
          :model="addForm"
          :rules="addRules"
          ref="addFormRef"
          label-width="80px"
          style="width: 350px"
      >
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="addForm.roleName" />
        </el-form-item>
        <el-form-item label="角色权限" prop="permissions">
          <el-checkbox-group v-model="addForm.permissions" class="inline-checkbox-group">
            <el-checkbox label="design">报表设计</el-checkbox>
            <el-checkbox label="report manager">报表管理</el-checkbox>
            <el-checkbox label="data source">数据源管理</el-checkbox>
            <el-checkbox label="report example">报表实例管理</el-checkbox>
            <el-checkbox label="role">角色管理</el-checkbox>
            <el-checkbox label="user">用户管理</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddRole">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox, ElForm } from 'element-plus';
import axios from 'axios';

// 角色表数据
const roles = ref([]);
const roleSearchQuery = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 计算属性：过滤并分页数据
const filteredRoles = computed(() => {
  return roles.value.filter((role) =>
      role.roleName.toLowerCase().includes(roleSearchQuery.value.toLowerCase())
  );
});

const currentPageRoles = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredRoles.value.slice(start, end);
});

// 编辑弹窗状态
const editDialogVisible = ref(false);
const editForm = ref({
  roleID: null,
  roleName: '',
  permissions: [],
});
const editFormRef = ref();

// 新增弹窗状态
const addDialogVisible = ref(false);
const addForm = ref({
  roleName: '',
  permissions: [],
});
const addFormRef = ref();
const addRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
  ],
  permissions: [
    { required: true, message: '请选择角色权限', trigger: 'change' },
  ],
};

// 表单验证规则
const editRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
  ],
  permissions: [
    { required: true, message: '请选择角色权限', trigger: 'change' },
  ],
};

// 生命周期：加载数据
onMounted(async () => {
  await fetchRoles();
});

// 获取角色列表
const fetchRoles = async () => {
  try {
    const { data } = await axios.get('http://localhost:8080/role/roles');
    if (data.code === 200) {
      roles.value = data.data.map((item) => ({
        ...item,
        permissions: item.permissions ? item.permissions.split(',') : [],
      }));
      total.value = data.data.length; // 更新总数据量
    } else {
      ElMessage.error(data.msg || '角色数据加载失败');
      roles.value = [];
      total.value = 0;
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试');
    roles.value = [];
    total.value = 0;
  }
};

// 分页相关方法
const handlePageSizeChange = (newSize) => {
  pageSize.value = newSize;
  currentPage.value = 1; // 切换页大小后回到第一页
};

const handlePageChange = (newPage) => {
  currentPage.value = newPage;
};

// 打开编辑弹窗
const openEditDialog = (row) => {
  editForm.value = {
    roleID: row.roleID,
    roleName: row.roleName,
    permissions: [...row.permissions],
  };
  editDialogVisible.value = true;
};

// 重置编辑表单
const resetEditForm = () => {
  editForm.value = {
    roleID: null,
    roleName: '',
    permissions: [],
  };
  editFormRef.value?.resetFields();
};

// 编辑角色逻辑
const handleEditRole = async () => {
  if (!editFormRef.value) return;

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const formData = {
          ...editForm.value,
          permissions: editForm.value.permissions.join(','),
        };
        const response = await axios.put(
            `http://localhost:8080/role/roles/${editForm.value.roleID}`,
            formData
        );
        if (response.data.code === 200) {
          ElMessage.success('角色编辑成功');
          editDialogVisible.value = false;
          fetchRoles();
        } else {
          ElMessage.error(response.data.msg || '编辑失败');
        }
      } catch (error) {
        ElMessage.error('网络错误，请稍后重试');
      }
    }
  });
};

// 阻止编辑默认角色
const handleBeforeClose = (done) => {
  if (editForm.value.roleID === 1) {
    ElMessage.warning('管理员角色不可编辑');
    editDialogVisible.value = false;
  } else {
    done();
  }
};

// 角色删除操作
const deleteRole = async (row) => {
  if (row.roleID === 1) {
    return ElMessage.warning('管理员角色不可删除');
  }
  await ElMessageBox.confirm('确认删除此角色？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      const response = await axios.delete(
          `http://localhost:8080/role/roles/${row.roleID}`
      );
      if (response.data.code === 200) {
        ElMessage.success('角色删除成功');
        await fetchRoles();
      }
    } catch (error) {
      ElMessage.error('删除失败');
    }
  });
};

// 新增角色相关逻辑
const openAddDialog = () => {
  resetAddForm();
  addDialogVisible.value = true;
};

const resetAddForm = () => {
  addForm.value = {
    roleName: '',
    permissions: [],
  };
  addFormRef.value?.resetFields();
};

const handleAddRole = async () => {
  if (!addFormRef.value) return;

  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const formData = {
          ...addForm.value,
          permissions: addForm.value.permissions.join(','),
        };
        const response = await axios.post(
            'http://localhost:8080/role/roles',
            formData
        );
        if (response.data.code === 200) {
          ElMessage.success('角色新增成功');
          addDialogVisible.value = false;
          fetchRoles();
        } else {
          ElMessage.error(response.data.msg || '新增失败');
        }
      } catch (error) {
        ElMessage.error('网络错误，请稍后重试');
      }
    }
  });
};

// 搜索函数（更新分页到第一页）
const handleRoleSearch = () => {
  currentPage.value = 1; // 搜索后回到第一页
};
</script>

<style scoped>
.grid-item {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.search-add-container {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  gap: 8px; /* 搜索框与按钮间距 */
}

.button-group {
  display: flex;
  gap: 4px;
}

.el-table {
  margin-top: 15px;
}

.el-dialog__body {
  padding: 20px;
}

/* 分页组件样式 */
.pagination-container {
  display: flex;
  justify-content: center; /* 居中对齐 */
  margin-top: 20px;
}

.centered-pagination {
  /* 可选：调整分页组件内边距 */
  padding: 10px;
}

.inline-checkbox-group {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.inline-checkbox-group .el-checkbox {
  margin-right: 10px; /* 可根据需要调整复选框之间的间距 */
  margin-bottom: 5px;
}
</style>
