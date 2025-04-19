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
              <el-radio-button label="edit">数据编辑</el-radio-button>
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
                  v-for="(col, colIndex) in tableColumns"
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
                      <div class="field-item" style="cursor: move;">
                        <span>{{ element.label }}</span>
                        <el-radio v-model="primaryKeyField" :label="element.prop">
                          <el-icon
                              class="delete-icon"
                              @click.stop="removeField(row, column.property, element)"
                          >
                            <Close />
                          </el-icon>
                        </el-radio>
                      </div>
                    </template>
                  </draggable>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- 预览模式 -->
          <el-tab-pane label="预览模式" name="preview">
            <div style="height: 100%; display: flex; flex-direction: column;">
              <el-table
                  :data="pagedData"
                  border
                  stripe
                  style="width: 100%; flex: 1;"
              >
                <el-table-column
                    v-for="(col, index) in previewColumns"
                    :key="index"
                    :prop="col.prop"
                    :label="col.label"
                    :width="col.width"
                />
              </el-table>
              <div class="pagination-container" style="text-align: left; margin-top: 12px; padding-left: 20px;">
                <el-pagination
                    :current-page="pagination.currentPage"
                    :page-size="5"
                    :total="pagination.total"
                    layout="total, prev, pager, next, jumper"
                    @current-change="handlePageChange"
                    style="width: fit-content;"
                />
              </div>
            </div>
          </el-tab-pane>
          <!-- 数据编辑 -->
          <el-tab-pane label="数据编辑" name="edit">
            <div style="height: 100%; display: flex; flex-direction: column;">
              <el-table
                  :data="editData"
                  border
                  stripe
                  style="width: 100%; flex: 1;"
              >
                <el-table-column
                    v-for="(col, index) in previewColumns"
                    :key="index"
                    :prop="col.prop"
                    :label="col.label"
                    :width="col.width"
                >
                  <template #default="{ row, $index }">
                    <!-- 空白行的输入框 -->
                    <template v-if="$index === 0 && col.prop !== primaryKeyField.value">
                      <el-input v-model="newDataRow[col.prop]" />
                    </template>
                    <!-- 其他行的数据显示 -->
                    <template v-else>
                      {{ row[col.prop] }}
                    </template>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150" fixed="right">
                  <template #default="{ row, $index }">
                    <!-- 空白行的查询和添加按钮 -->
                    <template v-if="$index === 0">
                      <el-button size="small" @click="handleQuery">查询</el-button>
                      <el-button size="small" type="primary" @click="handleAdd">添加</el-button>
                    </template>
                    <!-- 其他行的编辑和删除按钮 -->
                    <template v-else>
                      <el-button size="small" @click="handleEditData(row, $index - 1)">编辑</el-button>
                      <el-button size="small" type="danger" @click="handleDeleteData($index - 1)">删除</el-button>
                    </template>
                  </template>
                </el-table-column>
              </el-table>

              <!-- 编辑对话框 -->
              <el-dialog v-model="editDataDialogVisible" title="编辑数据">
                <el-form :model="currentEditData">
                  <el-form-item
                      v-for="col in previewColumns"
                      :key="col.prop"
                      :label="col.label"
                      :label-width="col.width"
                  >
                    <el-input v-model="currentEditData[col.prop]" />
                  </el-form-item>
                </el-form>
                <template #footer>
                  <el-button @click="editDataDialogVisible = false">取消</el-button>
                  <el-button type="primary" @click="confirmEditData">确认</el-button>
                </template>
              </el-dialog>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
    <!-- 操作按钮 -->
    <div class="save-button">
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
        <el-table-column label="是否为主键">
          <template #default="{ row }">
            <el-radio v-model="primaryKeyField" :label="row.columnName"></el-radio>
          </template>
        </el-table-column>
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
</template>

<script setup>
import { ref, onMounted, watch, computed } from "vue";
import draggable from "vuedraggable";
import axios from "axios";
import { ElMessage, ElLoading, ElMessageBox } from "element-plus";
import { Close } from '@element-plus/icons-vue';

// 数据源相关
const dataSources = ref([]);
const selectedDataSource = ref(null);
const selectedDataSourceName = ref(''); // 新增：保存选中数据源的名称
const sqlQuery = ref("");
const queryResult = ref({ fields: [], data: { rows: [] } });
const selectedFields = ref([]);

// 字段管理
const availableFields = ref([]);
const reportLayout = ref([{}]);
const tableColumns = ref([]);

// 新增数据编辑相关状态
const editDataDialogVisible = ref(false);
const currentEditData = ref({});
const currentEditIndex = ref(-1);

// 用户信息
const userInfo = ref({
  userID: null,
  username: "",
  password: "",
  permissions: ""
});

// 弹窗控制
const showDataSourceDialog = ref(false);
const showSaveDialog = ref(false);
const activeTab = ref('design');

// 分页配置
const pagination = ref({
  currentPage: 1,
  pageSize: 5,
  total: 0
});

// 分页数据计算
const pagedData = computed(() => {
  const start = (pagination.value.currentPage - 1) * pagination.value.pageSize;
  const end = start + pagination.value.pageSize;
  return previewData.value.slice(start, end);
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

  reportLayout.value = [
    tableColumns.value.reduce((acc, col) => {
      acc[col.prop] = [];
      return acc;
    }, {})
  ];
};

// 预览数据
const previewData = ref([]);
const previewColumns = ref([]);

// 编辑数据，包含空白行
const newDataRow = ref({});
const editData = computed(() => {
  const blankRow = previewColumns.value.reduce((acc, col) => {
    acc[col.prop] = newDataRow.value[col.prop] || '';
    return acc;
  }, {});
  return [blankRow, ...previewData.value];
});

// 监听预览模式切换
watch(
    activeTab,
    async (newTab) => {
      if (newTab === 'preview') {
        generatePreviewData();
      }
      if (newTab === 'edit') {
        console.log('切换到数据编辑模式，当前 queryResult:', queryResult.value);
        if (queryResult.value.data.rows.length === 0) {
          await executeQuery();
        }
        generatePreviewData();
      }
    }
);

// 生成预览数据
const generatePreviewData = () => {
  console.log('开始生成预览数据，queryResult:', queryResult.value);
  if (queryResult.value.data.rows.length === 0) {
    ElMessage.warning("请先执行 SQL 查询获取数据");
    activeTab.value = 'design';
    return;
  }

  const layoutFields = [];
  const row = reportLayout.value[0];

  tableColumns.value.forEach(col => {
    layoutFields.push(...(row[col.prop] || []));
  });

  previewColumns.value = layoutFields.map(field => ({
    prop: field.prop,
    label: field.label,
    width: 150
  }));

  previewData.value = queryResult.value.data.rows.map(rowData => {
    const item = {};
    layoutFields.forEach(field => {
      item[field.prop] = rowData[field.prop] || '';
    });
    return item;
  });

  pagination.value.total = previewData.value.length;
  pagination.value.currentPage = 1;
  console.log('生成预览数据完成，previewData:', previewData.value);
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

  // 保存选中数据源的名称
  const selectedSource = dataSources.value.find(source => source.dataSourceID === selectedDataSource.value);
  if (selectedSource) {
    selectedDataSourceName.value = selectedSource.dataSourceName;
  }

  const loading = ElLoading.service({ fullscreen: true });
  try {
    const response = await axios.post("http://localhost:8080/queries/executeQuery", {
      dataSourceId: selectedDataSource.value,
      query: sqlQuery.value
    });
    console.log('执行 SQL 查询，响应数据:', response.data);
    if (response.data.code === 200) {
      queryResult.value = {
        fields: response.data.data.columnsInfo.map(f => ({
          ...f,
          customLabel: f.columnName
        })),
        data: {
          rows: response.data.data.rows,
          columnsInfo: response.data.data.columnsInfo
        }
      };
    }
  } catch (error) {
    ElMessage.error("SQL执行失败");
    console.error('执行 SQL 查询失败:', error);
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
  const fieldsToSave = [];
  const layoutRow = reportLayout.value[0];

  tableColumns.value.forEach(col => {
    layoutRow[col.prop].forEach(field => {
      fieldsToSave.push({ prop: field.prop, label: field.label });
    });
  });

  const uniqueFields = [...new Map(fieldsToSave.map(item => [item.prop, item])).values()];

  const reportTemplate = {
    templateName: saveForm.value.templateName,
    dataSourceID: selectedDataSource.value,
    dataSourceName: selectedDataSourceName.value, // 新增：传递数据源名称
    templateCreator: userInfo.value.username,
    querySql: sqlQuery.value,
    templateConfig: JSON.stringify(uniqueFields),
    templateState: 1,
    TemplateKey: primaryKeyField.value, // 确保 primaryKeyField 有值
    primaryKey: primaryKeyField.value // 新增：将主键信息传递到 primaryKey 字段
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

const handlePageChange = (currentPage) => {
  pagination.value.currentPage = currentPage;
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

const handleImportFields = () => {
  showDataSourceDialog.value = true;
};

// 加载用户信息
const loadUserInfo = () => {
  const user = localStorage.getItem("user");
  if (user) {
    try {
      userInfo.value = JSON.parse(user);
    } catch (error) {
      console.error("解析用户信息失败:", error);
      ElMessage.error("加载用户信息失败，请重新登录");
    }
  } else {
    ElMessage.warning("未找到用户信息，请登录");
  }
};

// 处理数据编辑
const handleEditData = (row, index) => {
  currentEditData.value = { ...row };
  currentEditIndex.value = index;
  editDataDialogVisible.value = true;
};

// 确认编辑
const confirmEditData = () => {
  if (currentEditIndex.value >= 0) {
    previewData.value.splice(currentEditIndex.value, 1, currentEditData.value);
    ElMessage.success('数据修改成功');
    editDataDialogVisible.value = false;
  }
};

// 处理数据删除
const handleDeleteData = (index) => {
  ElMessageBox.confirm('确认删除该条数据吗？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    previewData.value.splice(index, 1);
    ElMessage.success('数据删除成功');
    pagination.value.total -= 1;
  });
};

// 新增：存储主键字段
const primaryKeyField = ref(null);

// 查询操作
const handleQuery = () => {
  // 这里可以实现查询逻辑，例如重新执行 SQL 查询
  executeQuery();
};

// 添加操作
const handleAdd = () => {
  const newRow = { ...newDataRow.value };
  previewData.value.unshift(newRow);
  pagination.value.total += 1;
  // 清空空白行
  Object.keys(newDataRow.value).forEach(key => {
    newDataRow.value[key] = '';
  });
  ElMessage.success('数据添加成功');
};

onMounted(() => {
  loadUserInfo();
  initLayout();
  fetchDataSources();
});
</script>

<style scoped>
/* 新增编辑对话框样式 */
.edit-dialog-form .el-form-item {
  margin-bottom: 18px;
}
.edit-dialog-form .el-input {
  width: 80%;
}
.report-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 20px;
  background: #fff;
}

/* 移除分页居中样式，改为靠左 */
.pagination-container {
  text-align: left;
  padding: 12px 0 0 20px;
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

/* 重置表格行高 */
:deep(.el-table__row) {
  height: auto !important;
}
</style>