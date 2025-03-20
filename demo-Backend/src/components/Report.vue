<template>
  <div class="report-container">
    <!-- 顶部操作栏 -->
    <div class="top-panel">
      <div class="top-left" style="display: flex; align-items: start; gap: 10px; width: 100%;">
        <div style="display: flex; gap: 20px; align-items: flex-start;">
          <el-button type="primary" @click="handleImportFields" style="width: 100px; margin-right: 20px;">
            导入字段
          </el-button>
          <div class="field-selector" style="width: 100%;">
            <h3 style="margin: 0 0 8px 0; font-size: 14px;">可选字段</h3>
            <draggable
              v-model="availableFields"
              item-key="prop"
              :group="{ name: 'fields', pull: 'clone', put: false }"
              class="draggable-fields"
              @start="dragStart"
              @end="dragEnd"
            >
              <template #item="{ element }">
                <div class="field-item">
                  {{ element.label }}
                </div>
              </template>
            </draggable>
          </div>
        </div>
      </div>
    </div>

    <!-- 设计预览区域 -->
    <div class="design-preview">
      <div class="design-area">
        <div class="toolbar">
          <el-button-group>
            <el-button type="primary" @click="resetLayout">重置布局</el-button>
          </el-button-group>
          <div class="view-switch">
            <el-radio-group v-model="activeTab" size="small">
              <el-radio-button label="design">设计模式</el-radio-button>
              <el-radio-button label="preview">预览模式</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </div>

      <!-- 设计容器 -->
      <div class="design-container" style="height: calc(100vh - 220px); overflow: auto">
        <el-tabs v-model="activeTab" type="card" style="height: 100%">
          <!-- 设计模式 -->
          <el-tab-pane label="设计模式" name="design">
            <el-table 
              :data="reportLayout" 
              border 
              style="width: 100%; height: 100%;"
              :row-class-name="tableRowClassName"
            >
              <el-table-column
                v-for="col in tableColumns"
                :key="col.prop"
                :prop="col.prop"
                :label="col.label"
                :width="col.width"
                :resizable="true"
              >
                <template #default="{ row, column }">
                  <draggable
                    v-model="row[column.property]"
                    item-key="prop"
                    :group="{ name: 'fields', put: true }"
                    class="draggable-container"
                    style="min-height: 50px; padding: 8px;"
                    @change="handleLayoutChange"
                  >
                    <template #item="{ element }">
                      <div class="field-item">
                        <span>{{ element.label }}</span>
                        <el-icon 
                          class="delete-icon" 
                          @click.stop="removeField(row, column.property, element)"
                        >
                          <Close />
                        </el-icon>
                      </div>
                    </template>
                  </draggable>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- 预览模式 -->
          <el-tab-pane label="预览模式" name="preview">
            <el-table
              :data="previewData"
              border
              stripe
              style="width: 100%; height: 100%;"
            >
              <el-table-column
                v-for="(col, index) in previewColumns"
                :key="index"
                :prop="col.prop"
                :label="col.label"
                :width="col.width"
              />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 操作按钮 -->
      <div class="save-button">
        <el-button type="success" @click="previewReport">预览报表</el-button>
        <el-button type="success" @click="showSaveDialog = true">保存表单</el-button>
      </div>
    </div>

    <!-- 数据源选择弹窗 -->
    <el-dialog
      v-model="showDataSourceDialog"
      title="选择数据源并执行 SQL"
      width="60%"
    >
      <div class="data-source-and-sql">
        <div class="data-source-select">
          <el-select 
            v-model="selectedDataSource" 
            placeholder="请选择数据源"
            style="width: 100%; margin-bottom: 20px;"
          >
            <el-option
              v-for="source in dataSources"
              :key="source.dataSourceID"
              :label="source.dataSourceName"
              :value="source.dataSourceID"
            />
          </el-select>
        </div>
        <div class="sql-editor">
          <el-input
            v-model="sqlQuery"
            type="textarea"
            :rows="5"
            placeholder="请输入 SQL 查询语句"
            clearable
            style="margin-bottom: 20px;"
          />
        </div>
        <div class="action-buttons">
          <el-button type="primary" @click="executeQuery">执行 SQL</el-button>
        </div>
        <el-table
          :data="queryResult.fields"
          v-if="queryResult.fields.length > 0"
          @selection-change="handleSelectionChange"
          style="margin-top: 20px;"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="columnName" label="列名" />
          <el-table-column label="标题">
            <template #default="{ row }">
              <el-input 
                v-model="row.customLabel" 
                :placeholder="row.columnName"
              />
            </template>
          </el-table-column>
          <el-table-column prop="columnType" label="类型" />
          <el-table-column prop="dataType" label="数据类型" />
          <el-table-column prop="columnSize" label="宽度" />
        </el-table>
      </div>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 10px;">
          <el-button @click="showDataSourceDialog = false">关闭</el-button>
          <el-button type="primary" @click="handleDone">完成</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 预览弹窗 -->
    <el-dialog
      v-model="showPreviewDialog"
      title="报表预览"
      width="90%"
      top="5vh"
    >
      <div class="preview-container">
        <el-table
          :data="previewData"
          border
          stripe
          height="70vh"
          style="width: 100%"
        >
          <el-table-column
            v-for="(col, index) in previewColumns"
            :key="index"
            :prop="col.prop"
            :label="col.label"
            :width="col.width"
          />
        </el-table>
        <el-pagination
          :current-page.sync="pagination.currentPage"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          style="margin-top: 20px;"
          @size-change="handlePaginationChange"
          @current-change="handlePaginationChange"
        />
      </div>
      <template #footer>
        <el-button @click="exportPreviewData">导出数据</el-button>
        <el-button type="primary" @click="showPreviewDialog = false">关闭预览</el-button>
      </template>
    </el-dialog>

    <!-- 保存表单弹窗 -->
    <el-dialog
      v-model="showSaveDialog"
      title="保存报表"
      width="40%"
    >
      <div class="save-form">
        <el-form :model="saveForm">
          <el-form-item label="报表名称" :label-width="formLabelWidth">
            <el-input 
              v-model="saveForm.templateName" 
              autocomplete="off"
              placeholder="请输入报表名称"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 10px;">
          <el-button @click="showSaveDialog = false">取消</el-button>
          <el-button type="primary" @click="saveReport">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import draggable from "vuedraggable";
import axios from "axios";
import { ElMessage, ElLoading } from "element-plus";
import { Close } from '@element-plus/icons-vue'

// 数据源相关
const dataSources = ref([]);
const selectedDataSource = ref(null);
const sqlQuery = ref("");
const queryResult = ref({ fields: [], data: [] });
const selectedFields = ref([]);

// 字段管理
const availableFields = ref([]);
const reportLayout = ref([{}]);
const tableColumns = ref([]);

// 用户信息
const userInfo = ref({
  userID: null,
  username: "",
  password: "",
  permissions: ""
});

// 从 localStorage 获取用户信息
const loadUserInfo = () => {
  const user = localStorage.getItem("user"); // 从 localStorage 中获取用户信息
  if (user) {
    try {
      const parsedUser = JSON.parse(user); // 解析 JSON 字符串
      userInfo.value = {
        userID: parsedUser.userID,
        username: parsedUser.username,
        password: parsedUser.password,
        permissions: parsedUser.permissions
      };
    } catch (error) {
      console.error("解析用户信息失败:", error);
      ElMessage.error("加载用户信息失败，请重新登录");
    }
  } else {
    ElMessage.warning("未找到用户信息，请登录");
  }
};


// 弹窗控制
const showDataSourceDialog = ref(false);
const showPreviewDialog = ref(false);
const showSaveDialog = ref(false);
const activeTab = ref('design');
const drag = ref(false);

// 分页配置
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 保存表单
const saveForm = ref({ templateName: "" });
const formLabelWidth = "120px";

// 初始化布局
const initLayout = () => {
  tableColumns.value = Array.from({ length: 8 }, (_, index) => ({
    prop: `col${index + 1}`,
    label: `列${index + 1}`,
    width: 150
  }));

  reportLayout.value = [Object.fromEntries(
    tableColumns.value.map(col => [col.prop, []])
  )];
};

// 获取数据源
const fetchDataSources = async () => {
  try {
    const response = await axios.get("http://localhost:8080/datasources/getAllDatasource");
    if (response.data.code === 200) {
      dataSources.value = response.data.data;
    } else {
      ElMessage.error("数据源获取失败");
    }
  } catch (error) {
    ElMessage.error("数据源请求异常");
  }
};

// 执行SQL查询
const executeQuery = async () => {
  if (!selectedDataSource.value) {
    ElMessage.error("请选择数据源");
    return;
  }

  const loading = ElLoading.service({ fullscreen: true });
  try {
    const response = await axios.post("http://localhost:8080/queries/executeQuery", {
      dataSourceId: selectedDataSource.value,
      query: sqlQuery.value
    });

    if (response.data.code === 200) {
      queryResult.value = {
        fields: response.data.data.columnsInfo.map(f => ({
          ...f,
          customLabel: f.columnName
        })),
        data: response.data.data.rows
      };
    }
  } catch (error) {
    ElMessage.error("SQL执行失败");
  } finally {
    loading.close();
  }
};

// 完成字段选择
const handleDone = () => {
  availableFields.value = selectedFields.value.map(f => ({
    prop: f.columnName,
    label: f.customLabel || f.columnName
  }));
  showDataSourceDialog.value = false;
  initLayout();
};

// 保存报表
const saveReport = async () => {
  // 收集布局中的字段
  const fieldsToSave = [];
  const layoutRow = reportLayout.value[0];
  
  for (const colKey in layoutRow) {
    layoutRow[colKey].forEach(field => {
      fieldsToSave.push({
        prop: field.prop,
        label: field.label
      });
    });
  }

  // 去重处理
  const uniqueFields = [...new Map(fieldsToSave.map(item => [item.prop, item])).values()];

  // 构造完整的 ReportTemplate 对象
  const reportTemplate = {
    templateName: saveForm.value.templateName, // 报表名称
    dataSourceID: selectedDataSource.value,    // 数据源 ID
    templateCreator: userInfo.value.username,  // 用户名
    querySql: sqlQuery.value,                 // SQL 查询语句
    templateConfig: JSON.stringify(uniqueFields), // 字段配置
    templateState: 1                          // 报表状态（假设 1 表示启用）
  };

  try {
    const response = await axios.post('http://localhost:8080/reporttemplates/save', reportTemplate);
    if (response.data.code === 200) {
      ElMessage.success('报表保存成功');
      showSaveDialog.value = false;
    } else {
      ElMessage.error('保存失败: ' + response.data.msg);
    }
  } catch (error) {
    ElMessage.error('保存失败: ' + error.message);
  }
};

// 预览数据生成
const previewData = ref([]);
const previewColumns = ref([]);
const previewReport = () => {
  previewColumns.value = availableFields.value.map(f => ({
    prop: f.prop,
    label: f.label,
    width: 150
  }));

  previewData.value = queryResult.value.data.map(row => {
    const item = {};
    availableFields.value.forEach(f => {
      item[f.prop] = row[f.prop] || '';
    });
    return item;
  });

  pagination.value.total = previewData.value.length;
  showPreviewDialog.value = true;
};

// 其他方法
const handleSelectionChange = (selection) => {
  selectedFields.value = selection;
};

const removeField = (row, col, field) => {
  row[col] = row[col].filter(f => f.prop !== field.prop);
};

const resetLayout = () => {
  initLayout();
  ElMessage.success("布局已重置");
};

const handlePaginationChange = () => {
  // 分页处理逻辑
};

const exportPreviewData = () => {
  // 导出数据逻辑
};

const tableRowClassName = ({ rowIndex }) => {
  return rowIndex % 2 === 0 ? 'even-row' : 'odd-row';
};

const dragStart = (e) => {
  e.item.style.opacity = '0.5';
};

const dragEnd = (e) => {
  e.item.style.opacity = '1';
};

// 导入字段处理
const handleImportFields = () => {
  showDataSourceDialog.value = true;
};

// 初始化
onMounted(() => {
  loadUserInfo(); // 加载用户信息
  initLayout();
  fetchDataSources();
});
</script>

<style scoped>
.report-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 20px;
  background: #fff;
}

.draggable-fields {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 4px;
  min-height: 60px;
  border: 1px solid #ebeef5;
}

.field-item {
  flex: 0 0 auto;
  padding: 6px 12px;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: move;
  transition: all 0.3s;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 8px;
}

.field-item:hover {
  border-color: #409eff;
  background: #ecf5ff;
}

.draggable-container {
  min-height: 50px;
  padding: 5px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  background: #fafafa;
}

.delete-icon {
  cursor: pointer;
  color: #f56c6c;
  margin-left: auto;
}

:deep(.el-table__row) {
  height: 80px !important;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.save-button {
  margin-top: 20px;
  text-align: right;
}

.even-row {
  background-color: #fafafa;
}

.odd-row {
  background-color: #ffffff;
}

.preview-container {
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}
</style>