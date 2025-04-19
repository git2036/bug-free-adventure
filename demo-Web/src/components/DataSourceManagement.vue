<template>
  <div class="container">
    <!-- 搜索框和新增按钮 -->
    <div class="header">
      <el-input
        v-model="searchKey"
        placeholder="搜索数据源名称"
        style="width: 300px; margin-right: 20px"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleAdd">新增数据源</el-button>
    </div>
    <!-- 表格展示数据 -->
    <el-table :data="currentPageData" border stripe style="width: 100%">
      <el-table-column prop="dataSourceName" label="名称" width="180" />
      <el-table-column prop="dataSourceType" label="类型" width="120">
        <template #default="{ row }">
          {{ typeMap[row.dataSourceType] || row.dataSourceType }}
        </template>
      </el-table-column>
      <el-table-column label="连接地址">
        <template #default="{ row }">
          <el-tooltip effect="dark" :content="row.connectionInfo" placement="top">
            <span class="ellipsis">{{ row.connectionInfo }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="dataSourceUsername" label="用户名" />
      <el-table-column prop="dataSourcePassword" label="密码" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.dataSourceID)">删除</el-button>
          <el-button size="small" type="success" @click="testConnection(row)">测试连接</el-button>
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
        <el-form-item label="数据源名称">
          <el-input v-model="form.dataSourceName" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.dataSourceType" placeholder="请选择">
            <el-option
                v-for="item in typeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="连接信息">
          <el-input v-model="form.connectionInfo" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="form.dataSourceUsername" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.dataSourcePassword" type="password" show-password />
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
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import axios from 'axios';

// 表格数据
const tableData = ref([]);
const searchKey = ref('');

// 对话框相关状态
const showDialog = ref(false);
const isEditMode = ref(false);
const currentId = ref(null);

// 表单数据
const form = ref({
  dataSourceName: '',
  dataSourceType: '',
  connectionInfo: '',
  dataSourceUsername: '',
  dataSourcePassword: ''
});

// 类型映射
const typeMap = {
  mysql: 'MySQL'
};

const typeOptions = Object.entries(typeMap).map(([value, label]) => ({ value, label }));

// 分页相关数据
const currentPage = ref(1);
const pageSize = ref(10);
const jumpPage = ref(1);

// 计算属性
const dialogTitle = computed(() => (isEditMode.value ? '编辑数据源' : '新增数据源'));

// 过滤后的数据
const filteredData = computed(() => {
  return tableData.value.filter((item) => {
    return (
      item.dataSourceName &&
      typeof item.dataSourceName === 'string' &&
      item.dataSourceName.toLowerCase().includes(searchKey.value.toLowerCase())
    );
  });
});

// 当前页要展示的数据
const currentPageData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredData.value.slice(start, end);
});

// 生命周期钩子，页面挂载时获取数据
onMounted(async () => {
  await fetchData();
});

// 获取数据
const fetchData = async () => {
  try {
    const { data } = await axios.get('http://localhost:8080/datasources/getAllDatasource');
    if (data.code === 200 && Array.isArray(data.data)) {
      tableData.value = data.data;
      ElMessage.success('数据加载成功');
    } else {
      ElMessage.error(`获取失败：${data.msg || '服务器返回异常状态码或数据格式错误'}`);
    }
  } catch (error) {
    let errorMsg = '网络请求异常';
    if (error.response) {
      errorMsg = `服务器响应异常：${error.response.status}`;
    } else if (error.request) {
      errorMsg = '无法连接到服务器';
    }
    ElMessage.error(`请求失败：${errorMsg}`);
  }
};

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1; // 搜索后回到第一页
};

// 处理新增
const handleAdd = () => {
  isEditMode.value = false;
  form.value = {
    dataSourceName: '',
    dataSourceType: '',
    connectionInfo: '',
    dataSourceUsername: '',
    dataSourcePassword: ''
  };
  showDialog.value = true;
};

// 处理编辑
const handleEdit = (row) => {
  isEditMode.value = true;
  currentId.value = row.dataSourceID;
  form.value = { ...row };
  showDialog.value = true;
};

// 处理删除
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该数据源吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    const { data } = await axios.delete(`http://localhost:8080/datasources/deleteDataSource/${id}`);
    if (data.code === 200) {
      tableData.value = tableData.value.filter((item) => item.dataSourceID !== id);
      ElMessage.success('删除成功');
      await fetchData(); // 删除成功后刷新数据
    } else {
      ElMessage.error(`删除失败：${data.msg || '服务器返回异常状态码'}`);
    }
  } catch (error) {
    ElMessage.info('取消删除');
  }
};

// 测试连接
const testConnection = async (row) => {
  try {
    const { data } = await axios.post('http://localhost:8080/datasources/testDataSource', {
      connectionInfo: row.connectionInfo,
      username: row.dataSourceUsername,
      password: row.dataSourcePassword
    });
    if (data.code === 200) {
      ElMessage.success(data.data || '连接成功');
    } else {
      ElMessage.error(data.msg || '连接失败');
    }
  } catch (error) {
    ElMessage.error('连接失败');
  }
};

// 提交表单
const submitForm = async () => {
  try {
    if (isEditMode.value) {
      const { data } = await axios.put(
        `http://localhost:8080/datasources/updateDataSource/${currentId.value}`,
        form.value
      );
      if (data.code === 200) {
        const index = tableData.value.findIndex((item) => item.dataSourceID === currentId.value);
        tableData.value[index] = { ...form.value, dataSourceID: currentId.value };
        ElMessage.success('编辑成功');
        await fetchData(); // 编辑成功后刷新数据
      } else {
        ElMessage.error(`编辑失败：${data.msg || '服务器返回异常状态码'}`);
      }
    } else {
      const { data } = await axios.post('http://localhost:8080/datasources/addDataSource', form.value);
      if (data.code === 200) {
        tableData.value.push(data.data);
        ElMessage.success('新增成功');
        await fetchData(); // 新增成功后刷新数据
      } else {
        ElMessage.error(`新增失败：${data.msg || '服务器返回异常状态码'}`);
      }
    }
    showDialog.value = false;
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

// 处理每页显示数量的变化
const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  currentPage.value = 1; // 改变每页数量后回到第一页
};

// 处理页码变化
const handleCurrentChange = (newPage) => {
  currentPage.value = newPage;
};

// 处理跳转页码
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
.container {
  padding: 20px;
}
.header {
  margin-bottom: 20px;
}
.ellipsis {
  display: inline-block;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>