<template>
  <div class="container">
    <!-- 搜索和过滤 -->
    <div class="header">
      <el-input v-model="searchQuery" placeholder="搜索报表..." style="width: 300px; margin-right: 20px" clearable @keyup.enter="handleSearch" />
      <el-select v-model="filterStatus" placeholder="选择状态" style="width: 120px">
        <el-option value="">全部状态</el-option>
        <el-option value="1">启用</el-option>
        <el-option value="0">停用</el-option>
      </el-select>
    </div>

    <!-- 报表列表 -->
    <el-table :data="currentPageData" border stripe style="width: 100%">
      <el-table-column prop="templateName" label="报表名称" />
      <el-table-column prop="dataSourceName" label="数据来源" />
      <el-table-column prop="templateCreator" label="创建者" />
      <el-table-column label="状态">
        <template #default="{ row }">
          <span :class="row.templateState === '1' ? 'active' : 'inactive'">
            {{ row.templateState ===  1 ? '启用' : '停用' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button size="small" @click="editReport(row)">编辑</el-button>
          <el-button size="small" color="green" @click="viewReportData(row)">查看</el-button>
          <el-button size="small" type="danger" @click="deleteReport(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 实例化弹窗 -->
    <el-dialog v-model="instanceDialogVisible" title="创建报表实例" width="30%">
      <el-form ref="instanceFormRef" :model="instanceForm" :rules="instanceRules" label-width="100px">
        <el-form-item label="实例名称" prop="instanceName">
          <el-input v-model="instanceForm.instanceName" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeInstanceDialog">取消</el-button>
          <el-button type="primary" @click="confirmCreateInstance">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑报表" @close="closeEditDialog" width="30%">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="报表名称">
          <el-input v-model="editForm.templateName" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.templateState">
            <el-option value="1">启用</el-option>
            <el-option value="0">停用</el-option>
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

      <!-- 查看弹窗底部按钮 -->
      <template #footer>
        <span class="dialog-footer">
          <!-- 实例化按钮：样式与关闭按钮一致 -->
          <el-button type="primary" @click="exportFromViewDialog">实例化</el-button>
          <el-button @click="closeViewDialog">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 分页组件包裹元素 -->
    <div class="pagination-wrapper">
      <el-pagination v-if="filteredReports.length > 10"
                     @size-change="handleSizeChange"
                     @current-change="handleCurrentChange"
                     :current-page="currentPage"
                     :page-sizes="[10, 20, 30]"
                     :page-size="pageSize"
                     prev-text="上一页"
                     next-text="下一页"
                     :total="filteredReports.length"
                     layout="total, sizes, prev, pager, next, jumper">
        <template #total>共 {{ filteredReports.length }} 条数据</template>
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
import { ElMessage, ElMessageBox } from 'element-plus';
import axios from 'axios';

const API_BASE = 'http://localhost:8080';

export default {
  setup() {
    const searchQuery = ref('');
    const filterStatus = ref('');
    const reports = ref([]);
    const currentPage = ref(1);
    const pageSize = ref(10);
    const jumpPage = ref(1);

    // 实例化相关
    const instanceDialogVisible = ref(false);
    const instanceForm = ref({
      instanceName: '',
      templateID: null,
      reportData: [],
      createdBy: '',
      status: '已生成'
    });
    const instanceFormRef = ref(null);
    const instanceRules = {
      instanceName: [
        { required: true, message: '请输入实例名称', trigger: 'blur' }
      ]
    };

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
    const templateInfoMap = ref({});

    // 编辑数据弹窗相关
    const editDataDialogVisible = ref(false);
    const editDataForm = ref({});

    // 过滤后的数据
    const filteredReports = computed(() => {
      return reports.value.filter((report) => {
        const reportState = String(report.templateState);
        const selectedStatus = String(filterStatus.value);
        const matchesSearch = report.templateName.toLowerCase().includes(searchQuery.value.toLowerCase());
        const matchesStatus = !selectedStatus || reportState === selectedStatus;
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
      currentPage.value = 1;
    };

    // 处理每页显示数量的变化
    const handleSizeChange = (newSize) => {
      pageSize.value = newSize;
      currentPage.value = 1;
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

    // 获取所有报表数据
    const fetchReports = async () => {
      try {
        const response = await axios.get(`${API_BASE}/reporttemplates/getAll`);
        if (response.data.code === 200) {
          const allReports = response.data.data.map(report => {
            if (!report.dataSourceName) {
              report.dataSourceName = '未知数据源';
            }
            templateInfoMap.value[report.templateID] = {
              templateKey: report.templateKey || report.primaryKey || 'defaultTemplateKey',
              dataSourceName: report.dataSourceName,
              templateCreator: report.templateCreator
            };
            return report;
          });
          reports.value = allReports;
        } else {
          ElMessage.error('获取报表数据失败');
        }
      } catch (error) {
        console.error('获取报表数据时出错:', error);
        ElMessage.error('获取报表数据失败');
      }
    };

    // 从查看弹窗调用的实例化方法
    const exportFromViewDialog = () => {
      const templateInfo = templateInfoMap.value[currentTemplateId.value];
      if (!templateInfo) {
        ElMessage.error('无法获取模板信息');
        return;
      }

      instanceForm.value = {
        instanceName: '',
        templateID: currentTemplateId.value,
        reportData: [...reportDataList.value],
        createdBy: templateInfo.templateCreator || templateInfo.dataSourceName || '未知用户',
        status: '已生成'
      };

      instanceDialogVisible.value = true;
    };

    // 提交创建实例
    const confirmCreateInstance = async () => {
      try {
        await instanceFormRef.value.validate();

        const requestData = {
          instanceName: instanceForm.value.instanceName,
          templateID: instanceForm.value.templateID,
          reportData: JSON.stringify(instanceForm.value.reportData),
          createdBy: instanceForm.value.createdBy,
          status: instanceForm.value.status
        };
        console.log('创建实例请求数据:', requestData);
        const response = await axios.post(`${API_BASE}/reporttemplates/instance`, requestData);

        if (response.data.code === 200) {
          ElMessage.success('报表实例创建成功');
          closeInstanceDialog();
        } else {
          ElMessage.error('报表实例创建失败');
        }
      } catch (error) {
        console.error('创建报表实例时出错:', error);
        ElMessage.error('创建报表实例失败');
      }
    };

    // 关闭实例化弹窗
    const closeInstanceDialog = () => {
      instanceDialogVisible.value = false;
      instanceForm.value = {
        instanceName: '',
        templateID: null,
        reportData: [],
        createdBy: '',
        status: '已生成'
      };
    };

    // 编辑报表
    const editReport = (report) => {
      editForm.value = {
        templateID: report.templateID,
        templateName: report.templateName,
        templateState: report.templateState
      };
      editDialogVisible.value = true;
    };

    // 关闭编辑弹窗
    const closeEditDialog = () => {
      editDialogVisible.value = false;
    };

    // 保存编辑后的报表
    const saveEditReport = async () => {
      try {
        const status = editForm.value.templateState;
        const templateName = editForm.value.templateName;

        const response = await axios.put(
            `${API_BASE}/reporttemplates/update/${editForm.value.templateID}`,
            null,
            {
              params: {
                status: status,
                templateName: templateName
              }
            }
        );

        if (response.data.code === 200) {
          ElMessage.success('报表模板状态更新成功');
          editDialogVisible.value = false;
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
        await ElMessageBox.confirm('确定要删除该报表吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });

        const response = await axios.delete(`${API_BASE}/reporttemplates/deleteById/${report.templateID}`);

        if (response.data.code === 200) {
          ElMessage.success('报表模板删除成功');
          await fetchReports();
        } else {
          ElMessage.error('报表模板删除失败');
        }
      } catch (error) {
        if (error === 'cancel') return;
        console.error('删除报表时出错:', error);
        ElMessage.error('报表模板删除失败');
      }
    };

    // 查看报表数据
    const viewReportData = async (report) => {
      try {
        currentTemplateId.value = report.templateID;
        const templateResponse = await axios.get(`${API_BASE}/reporttemplates/getById/${report.templateID}`);
        if (templateResponse.data.code === 200) {
          const template = templateResponse.data.data;
          tableColumns.value = JSON.parse(template.templateConfig);

          const dataResponse = await axios.get(`${API_BASE}/dynamic-data/${report.templateID}`);
          if (dataResponse.data.code === 200) {
            reportDataList.value = dataResponse.data.data;
            viewDialogVisible.value = true;
          } else {
            ElMessage.error('获取报表数据失败');
          }
        } else {
          ElMessage.error('获取报表模板信息失败');
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

    // 编辑报表数据
    const editReportData = (data) => {
      editDataForm.value = {...data};
      editDataDialogVisible.value = true;
    };

    // 关闭编辑数据弹窗
    const closeEditDataDialog = () => {
      editDataDialogVisible.value = false;
    };

    // 保存编辑后的报表数据
    const saveEditReportData = async () => {
      try {
        const { templateKey, dataSourceName } = templateInfoMap.value[currentTemplateId.value];
        const encodedDataSourceName = encodeURIComponent(dataSourceName);
        const encodedTemplateKey = encodeURIComponent(templateKey);
        const config = {
          headers: {
            'dataSourceName': encodedDataSourceName,
            'templateKey': encodedTemplateKey
          }
        };

        const response = await axios.put(
            `${API_BASE}/dynamic-data/${currentTemplateId.value}`,
            editDataForm.value,
            config
        );

        if (response.data.code === 200) {
          ElMessage.success('数据更新成功');
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
        await ElMessageBox.confirm('确定要删除该报表数据吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });

        const id = data.id;
        const { templateKey, dataSourceName } = templateInfoMap.value[currentTemplateId.value];
        const encodedDataSourceName = encodeURIComponent(dataSourceName);
        const encodedTemplateKey = encodeURIComponent(templateKey);
        const config = {
          headers: {
            'dataSourceName': encodedDataSourceName,
            'templateKey': encodedTemplateKey
          }
        };

        const response = await axios.delete(
            `${API_BASE}/dynamic-data/${currentTemplateId.value}/${id}`,
            config
        );

        if (response.data.code === 200) {
          ElMessage.success('数据删除成功');
          await viewReportData({ templateID: currentTemplateId.value });
        } else {
          ElMessage.error('数据删除失败');
        }
      } catch (error) {
        if (error === 'cancel') return;
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
      editReportData,
      editDataDialogVisible,
      editDataForm,
      closeEditDataDialog,
      saveEditReportData,
      deleteReportData,
      closeViewDialog,
      exportFromViewDialog,
      instanceDialogVisible,
      instanceForm,
      instanceFormRef,
      instanceRules,
      confirmCreateInstance,
      closeInstanceDialog
    };
  }
};
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>