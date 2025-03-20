<template>
  <div class="container">
    <!-- 搜索和过滤 -->
    <div class="header">
      <el-input v-model="searchQuery" placeholder="搜索报表..." style="width: 300px; margin-right: 20px" clearable
        @keyup.enter="handleSearch" />
      <el-select v-model="filterStatus" placeholder="选择状态" style="width: 120px">
        <el-option value="">全部状态</el-option>
        <el-option value="启用">启用</el-option>
        <el-option value="停用">停用</el-option>
      </el-select>
    </div>

    <!-- 报表列表 -->
    <el-table :data="currentPageData" border stripe style="width: 100%">
      <el-table-column prop="templateName" label="报表名称" />
      <el-table-column prop="dataSourceID" label="数据来源" />
      <el-table-column prop="templateCreator" label="创建者" />
      <el-table-column label="状态">
        <template #default="{ row }">
          <span :class="row.templateState === '启用' ? 'active' : 'inactive'">
            {{ row.templateState }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="editReport(row)">编辑</el-button>
          <el-button size="small" color="green" @click="viewReportData(row)">查看</el-button>
          <el-button size="small" type="danger" @click="deleteReport(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑报表" @close="closeEditDialog">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="报表名称">
          <el-input v-model="editForm.templateName" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.templateState">
            <el-option value="启用">启用</el-option>
            <el-option value="停用">停用</el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeEditDialog">取消</el-button>
          <el-button type="primary" @click="saveEditReport">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看数据列表弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="报表数据列表" @close="closeViewDialog">
      <el-table :data="reportDataList" border stripe style="width: 100%">
        <!-- 动态生成列 -->
        <el-table-column v-for="column in tableColumns" :key="column.prop" :prop="column.prop" :label="column.label" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button size="small" @click="editReportData(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteReportData(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

       <!-- 添加数据表单 -->
       <el-form :model="addForm" label-width="80px">
      <template v-for="column in tableColumns" :key="column.prop">
        <el-form-item :label="column.label">
          <el-input v-model="addForm[column.prop]" />
        </el-form-item>
      </template>
      <el-button type="primary" @click="addReportData">添加</el-button>
    </el-form>

    <!-- 编辑数据表单 -->
    <el-dialog v-model="editDataDialogVisible" title="编辑报表数据" @close="closeEditDataDialog">
      <el-form :model="editDataForm" label-width="80px">
        <template v-for="column in tableColumns" :key="column.prop">
          <el-form-item :label="column.label">
            <el-input v-model="editDataForm[column.prop]" />
          </el-form-item>
        </template>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeEditDataDialog">取消</el-button>
          <el-button type="primary" @click="saveEditReportData">保存</el-button>
        </span>
      </template>
    </el-dialog>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeViewDialog">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 分页组件包裹元素 -->
    <div class="pagination-wrapper">
      <el-pagination v-if="filteredReports.length > 10" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" :current-page="currentPage" :page-sizes="[10, 20, 30]"
        :page-size="pageSize" prev-text="上一页" next-text="下一页" :total="filteredReports.length"
        layout="total, sizes, prev, pager, next, jumper">
        <template #total>
          共 {{ filteredReports.length }} 条数据
        </template>
        <template #sizes>
          每页显示
          <el-select v-model="pageSize" @change="handleSizeChange">
            <el-option v-for="size in [10, 20, 30]" :key="size" :label="size + ' 条'" :value="size" />
          </el-select>
        </template>
        <template #jumper>
          跳转到
          <el-input v-model="jumpPage" @keyup.enter="handleJumpPage" size="small" style="width: 50px" /> 页
        </template>
      </el-pagination>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';

export default {
  setup() {
    const searchQuery = ref('');
    const filterStatus = ref('');
    const reports = ref([]);
    const currentPage = ref(1);
    const pageSize = ref(10);
    const jumpPage = ref(1);

    // 编辑弹窗相关
    const editDialogVisible = ref(false);
    const editForm = ref({
      templateID: null,
      templateName: '',
      templateState: ''
    });

    // 查看数据列表弹窗相关
    const viewDialogVisible = ref(false);
    const reportDataList = ref([]);
    const currentTemplateId = ref(null);
    const tableColumns = ref([]);

    // 添加数据表单
    const addForm = ref({});

    // 编辑数据弹窗相关
    const editDataDialogVisible = ref(false);
    const editDataForm = ref({});

    // 过滤后的数据
    const filteredReports = computed(() => {
      return reports.value.filter((report) => {
        const matchesSearch = report.templateName.toLowerCase().includes(searchQuery.value.toLowerCase());
        const matchesStatus = (!filterStatus.value || report.templateState === filterStatus.value);
        return matchesSearch && matchesStatus;
      });
    });

    // 当前页要展示的数据
    const currentPageData = computed(() => {
      const start = (currentPage.value - 1) * pageSize.value;
      const end = start + pageSize.value;
      return filteredReports.value.slice(start, end);
    });

    // 处理搜索
    const handleSearch = () => {
      currentPage.value = 1; // 搜索后回到第一页
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
      const totalPages = Math.ceil(filteredReports.value.length / pageSize.value);
      if (jumpPage.value >= 1 && jumpPage.value <= totalPages) {
        currentPage.value = jumpPage.value;
      } else {
        ElMessage.error('输入的页码无效');
      }
    };

    // 调用API获取报表数据
    const fetchReports = async () => {
      try {
        const response = await axios.get('http://localhost:8080/reporttemplates/getAll');
        if (response.data.code === 200) {
          reports.value = response.data.data;
          console.log('获取到的报表数据：', reports.value);
        } else {
          ElMessage.error('获取报表数据失败');
        }
      } catch (error) {
        console.error('获取报表数据时出错:', error);
        ElMessage.error('获取报表数据失败');
      }
    };

    // 编辑报表
    const editReport = (report) => {
      console.log('编辑按钮被点击，当前报表信息：', report);
      editForm.value = {
        templateID: report.templateID,
        templateName: report.templateName,
        templateState: report.templateState
      };
      editDialogVisible.value = true;
      console.log('编辑弹窗是否显示：', editDialogVisible.value);
    };

    // 关闭编辑弹窗
    const closeEditDialog = () => {
      editDialogVisible.value = false;
    };

    // 保存编辑后的报表
    const saveEditReport = async () => {
      try {
        const status = editForm.value.templateState === '启用';
        const response = await axios.put(
          `http://localhost:8080/reporttemplates/updateStatus/${editForm.value.templateID}`,
          {
            status: status
          }
        );
        if (response.data.code === 200) {
          ElMessage.success('报表模板状态更新成功');
          closeEditDialog();
          await fetchReports();
        } else {
          ElMessage.error('报表模板状态更新失败');
        }
      } catch (error) {
        console.error('更新报表时出错:', error);
        ElMessage.error('报表模板状态更新失败');
      }
    };

    // 删除报表
    const deleteReport = async (report) => {
      try {
        const response = await axios.delete(`http://localhost:8080/reporttemplates/deleteById/${report.templateID}`);
        if (response.data.code === 200) {
          ElMessage.success('报表模板删除成功');
          // 重新获取报表数据
          await fetchReports();
        } else {
          ElMessage.error('报表模板删除失败');
        }
      } catch (error) {
        console.error('删除报表时出错:', error);
        ElMessage.error('报表模板删除失败');
      }
    };

    // 查看报表数据
    const viewReportData = async (report) => {
      try {
        currentTemplateId.value = report.templateID;
        // 获取报表模板信息
        const templateResponse = await axios.get(`http://localhost:8080/reporttemplates/getById/${report.templateID}`);
        if (templateResponse.data.code === 200) {
          const template = templateResponse.data.data;
          // 解析 TemplateConfig
          tableColumns.value = JSON.parse(template.templateConfig);
          // 初始化添加表单
          addForm.value = {};
          tableColumns.value.forEach(column => {
            addForm.value[column.prop] = '';
          });
        } else {
          ElMessage.error('获取报表模板信息失败');
          return;
        }

        // 获取报表数据
        const dataResponse = await axios.get(`http://localhost:8080/reportgenerator/generate/${report.templateID}`);
        if (dataResponse.data.code === 200) {
          reportDataList.value = dataResponse.data.data;
          viewDialogVisible.value = true;
        } else {
          ElMessage.error('获取报表数据失败');
        }
      } catch (error) {
        console.error('获取报表数据时出错:', error);
        ElMessage.error('获取报表数据失败');
      }
    };

    // 关闭查看数据列表弹窗
    const closeViewDialog = () => {
      viewDialogVisible.value = false;
    };

    // 添加报表数据
    const addReportData = async () => {
      try {
        const response = await axios.post('http://localhost:8080/reportdata/add', {
          templateId: currentTemplateId.value,
          data: addForm.value // 将数据包裹在data字段中
        });
        if (response.data.code === 200) {
          ElMessage.success('数据添加成功');
          // 重新获取报表数据
          await viewReportData({ templateID: currentTemplateId.value });
          // 清空添加表单
          tableColumns.value.forEach(column => {
            addForm.value[column.prop] = '';
          });
        } else {
          ElMessage.error('数据添加失败');
        }
      } catch (error) {
        console.error('添加数据时出错:', error);
        ElMessage.error('数据添加失败');
      }
    };

    // 编辑报表数据
    const editReportData = (data) => {
      editDataForm.value = { ...data };
      editDataDialogVisible.value = true;
    };

    // 关闭编辑数据弹窗
    const closeEditDataDialog = () => {
      editDataDialogVisible.value = false;
    };

    // 保存编辑后的报表数据
    const saveEditReportData = async () => {
      try {
        const response = await axios.put('http://localhost:8080/reportdata/update', {
          templateId: currentTemplateId.value,
          data: editDataForm.value // 数据包裹在data字段
        });
        if (response.data.code === 200) {
          ElMessage.success('数据更新成功');
          // 重新获取报表数据
          await viewReportData({ templateID: currentTemplateId.value });
          closeEditDataDialog();
        } else {
          ElMessage.error('数据更新失败');
        }
      } catch (error) {
        console.error('更新数据时出错:', error);
        ElMessage.error('数据更新失败');
      }
    };

    // 删除报表数据
    const deleteReportData = async (data) => {
      try {
        // 构造condition（假设根据唯一键"id"删除）
        const condition = { id: data.id };
        const response = await axios.delete(
          `http://localhost:8080/reportdata/delete/${currentTemplateId.value}`,
          { data: condition } // 请求体传递condition
        );
        if (response.data.code === 200) {
          ElMessage.success('数据删除成功');
          // 重新获取报表数据
          await viewReportData({ templateID: currentTemplateId.value });
        } else {
          ElMessage.error('数据删除失败');
        }
      } catch (error) {
        console.error('删除数据时出错:', error);
        ElMessage.error('数据删除失败');
      }
    };

    onMounted(async () => {
      await fetchReports();
    });

    return {
      searchQuery,
      filterStatus,
      reports,
      currentPage,
      pageSize,
      jumpPage,
      filteredReports,
      currentPageData,
      handleSearch,
      handleSizeChange,
      handleCurrentChange,
      handleJumpPage,
      editReport,
      deleteReport,
      editDialogVisible,
      editForm,
      closeEditDialog,
      saveEditReport,
      viewReportData,
      viewDialogVisible,
      reportDataList,
      tableColumns,
      addReportData,
      addForm,
      editReportData,
      editDataDialogVisible,
      editDataForm,
      closeEditDataDialog,
      saveEditReportData,
      deleteReportData
    };
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

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
