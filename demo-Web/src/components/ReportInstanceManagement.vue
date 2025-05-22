<template>
  <div class="container">
    <!-- 搜索框 -->
    <div class="header">
      <el-input
          v-model="searchQuery"
          placeholder="搜索报表实例名称"
          style="width: 300px; margin-right: 20px"
          clearable
          @keyup.enter="handleSearch"
      />
    </div>

    <!-- 数据表格 -->
    <el-table
        :data="currentPageData"
        border
        stripe
        style="width: 100%; table-layout: auto;"
        fit
        stretch
    >
      <el-table-column
          prop="instanceName"
          label="报表实例名称"
          min-width="200"
          flex="2"
      />
      <el-table-column
          prop="templateID"
          label="报表模板ID"
          width="120"
      />
      <el-table-column
          prop="createdBy"
          label="创建者ID"
          min-width="150"
          flex="1"
      />
      <el-table-column label="状态" width="150">
        <template #default="{ row }">
          <span :class="row.status === '已发布' ? 'active' : 'inactive'">
            {{ row.status }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button
              size="small"
              type="danger"
              @click="deleteInstance(row)"
          >
            删除
          </el-button>
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

    <!-- 编辑弹窗 -->
    <el-dialog
        title="编辑报表实例"
        v-model="editDialogVisible"
        width="40%"
        @close="resetForm"
    >
      <el-form
          :model="form"
          ref="formRef"
          label-width="120px"
          :rules="formRules"
      >
        <el-form-item label="报表实例名称" prop="instanceName">
          <el-input v-model="form.instanceName" autocomplete="off" />
        </el-form-item>
        <el-form-item label="报表模板ID" prop="templateID">
          <el-input v-model="form.templateID" autocomplete="off" />
        </el-form-item>
        <el-form-item label="创建者ID" prop="createdBy">
          <el-input v-model="form.createdBy" autocomplete="off" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
              v-model="form.status"
              placeholder="请选择状态"
              style="width: 100%"
          >
            <el-option label="已发布" value="已发布" />
            <el-option label="已归档" value="已归档" />
            <el-option label="已弃置" value="已弃置" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue';
import {ElMessage, ElMessageBox, ElForm} from 'element-plus';
import axios from 'axios';

// 表格数据
const tableData = ref([]);
const searchQuery = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const jumpPage = ref(1);

// 编辑弹窗数据
const editDialogVisible = ref(false);
const form = ref({
  instanceID: null,
  instanceName: '',
  templateID: '',
  createdBy: '',
  status: '已发布' // 修改默认状态
});
const formRef = ref(null);
const formRules = {
  instanceName: [{required: true, message: '请输入报表实例名称', trigger: 'blur'}],
  templateID: [{required: true, message: '请输入报表模板ID', trigger: 'blur'}],
  createdBy: [{required: true, message: '请输入创建者', trigger: 'blur'}],
  status: [{required: true, message: '请选择状态', trigger: 'change'}]
};

// 数据过滤与分页
const filteredData = computed(() => {
  return tableData.value.filter(item =>
      item.instanceName.toLowerCase().includes(searchQuery.value.toLowerCase())
  );
});

const currentPageData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredData.value.slice(start, end);
});

// 生命周期：加载数据
onMounted(async () => {
  await fetchInstances();
});

// 获取实例列表
const fetchInstances = async () => {
  try {
    const {data} = await axios.get('http://localhost:8080/reportinstances/getAll');
    if (data.code === 200) {
      tableData.value = data.data;
    } else {
      ElMessage.error(data.msg || '数据加载失败');
    }
  } catch (error) {
    console.error('获取实例失败:', error);
    ElMessage.error('网络错误，请稍后重试');
  }
};

// 打开编辑弹窗
const openEditDialog = (row) => {
  form.value = {...row};
  editDialogVisible.value = true;
};

// 提交编辑
const submitEdit = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await axios.put(
            `http://localhost:8080/reportinstances/update/${form.value.instanceID}`,
            form.value
        );
        if (response.data.code === 200) {
          ElMessage.success('更新成功');
          editDialogVisible.value = false;
          fetchInstances();
        } else {
          ElMessage.error(response.data.msg || '更新失败');
        }
      } catch (error) {
        ElMessage.error('网络错误，请稍后重试');
      }
    }
  });
};

// 重置表单
const resetForm = () => {
  form.value = {
    instanceID: null,
    instanceName: '',
    templateID: '',
    createdBy: '',
    status: '已发布' // 重置为默认状态
  };
  formRef.value?.resetFields();
};

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1; // 搜索后回到第一页
};

// 删除实例
const deleteInstance = async (row) => {
  await ElMessageBox.confirm('确定删除该报表实例吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const response = await axios.delete(
          `http://localhost:8080/reportinstances/delete/${row.instanceID}`
      );
      if (response.data.code === 200) {
        ElMessage.success('删除成功');
        await fetchInstances();
      }
    } catch (error) {
      ElMessage.error('删除失败');
    }
  });
};

// 分页相关方法
const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  currentPage.value = 1; // 改变每页数量后回到第一页
};

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage;
};

const handleJumpPage = () => {
  const totalPages = Math.ceil(filteredData.value.length / pageSize.value);
  if (jumpPage.value >= 1 && jumpPage.value <= totalPages) {
    currentPage.value = jumpPage.value;
  } else {
    ElMessage.error('输入的页码无效');
  }
};
</script>

<style scoped>
/* 保持原有样式不变 */
.container {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.el-table {
  margin-top: 15px;
  font-size: 0.9em;
}

.el-table td,
.el-table th {
  padding: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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

.active {
  color: #409eff;
  font-weight: 500;
}

.inactive {
  color: #666;
}

.el-form-item {
  margin-bottom: 15px;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
  }

  .el-input {
    width: 100% !important;
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>