<template>
  <div class="container">
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
                              <Close/>
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
              <div style="height: 100%; display: flex; flex-direction: column; overflow: auto;">
                <el-table
                    :data="editData"
                    border
                    stripe
                    style="width: 100%; flex: 1;"
                >
                  <el-table-column
                      v-for="(field, index) in queryResult.fields"
                      :key="index"
                      :prop="field.columnName"
                      :label="field.customLabel || field.columnName"
                      :width="field.columnSize || 150"
                  >
                    <template #default="{ row }">
                      <template v-if="field.columnName !== primaryKeyField">
                        <el-input v-model="row[field.columnName]"/>
                      </template>
                      <template v-else>
                        {{ row[field.columnName] }}
                      </template>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="150" fixed="right">
                    <template #default="{ row }">
                      <el-button size="small" @click="handleEditData(row)">编辑</el-button>
                      <el-button size="small" type="danger" @click="handleDeleteData(row)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>

                <!-- 编辑对话框 -->
                <el-dialog v-model="editDataDialogVisible" title="编辑数据">
                  <el-form :model="currentEditData">
                    <el-form-item
                        v-for="col in queryResult.fields"
                        :key="col.columnName"
                        :label="col.customLabel || col.columnName"
                        :label-width="col.columnSize ? `${col.columnSize}px` : '120px'"
                    >
                      <el-input v-model="currentEditData[col.columnName]"/>
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
          <el-table-column type="selection" width="55"/>
          <el-table-column prop="columnName" label="列名"/>
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
          <el-table-column prop="columnType" label="类型"/>
          <el-table-column prop="dataType" label="数据类型"/>
          <el-table-column prop="columnSize" label="宽度"/>
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
        class="save-form-dialog"
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
import { ref, computed, watch, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElLoading, ElMessageBox } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'

// 数据源相关
const dataSources = ref([])
const selectedDataSource = ref(null)
const selectedDataSourceName = ref('')
const sqlQuery = ref('')
const queryResult = ref({ fields: [], data: { rows: [] } })
const selectedFields = ref([])

// 字段管理
const availableFields = ref([])
const reportLayout = ref([{}])
const tableColumns = ref([])

// 数据编辑相关
const editDataDialogVisible = ref(false)
const currentEditData = ref({})
const currentEditIndex = ref(-1)
const dataSourceName = ref('')
const targetTable = ref('')
const primaryKeyField = ref('')
const editData = ref([])
const newDataRow = ref({})

// 用户信息
const userInfo = ref({
  userID: null,
  username: '',
  password: '',
  permissions: ''
})

// 视图控制
const activeTab = ref('design')
const showDataSourceDialog = ref(false)
const showSaveDialog = ref(false)

// 分页相关
const pagination = ref({
  currentPage: 1,
  pageSize: 5,
  total: 0
})

// 保存表单
const saveForm = ref({ templateName: '' })
const formLabelWidth = '120px'

// 计算属性 - 预览模式字段
const previewColumns = computed(() => {
  const layoutFields = []
  reportLayout.value[0] && tableColumns.value.forEach(col => {
    layoutFields.push(...(reportLayout.value[0][col.prop] || []))
  })
  return layoutFields.map(field => ({
    prop: field.prop,
    label: field.label,
    width: 150
  }))
})

// 计算属性 - 分页数据
const pagedData = computed(() => {
  const start = (pagination.value.currentPage - 1) * pagination.value.pageSize
  const end = start + pagination.value.pageSize
  return previewData.value.slice(start, end)
})

const previewData = computed(() => {
  const rawData = queryResult.value.data.rows || []
  const layoutFields = previewColumns.value
  return rawData.map(row => {
    return layoutFields.reduce((acc, field) => {
      acc[field.prop] = row[field.prop] !== undefined ? row[field.prop] : ''
      return acc
    }, {})
  })
})

// 初始化布局
const initLayout = () => {
  tableColumns.value = Array.from({ length: 8 }, (_, index) => ({
    prop: `col${index + 1}`,
    label: `列${index + 1}`,
    width: 150
  }))

  reportLayout.value = [
    tableColumns.value.reduce((acc, col) => {
      acc[col.prop] = []
      return acc
    }, {})
  ]
}

// 获取数据源
const fetchDataSources = async () => {
  try {
    const response = await axios.get('http://localhost:8080/datasources/getAllDatasource')
    if (response.data.code === 200) {
      dataSources.value = response.data.data
    } else {
      ElMessage.error('数据源获取失败')
    }
  } catch (error) {
    ElMessage.error('数据源请求异常')
  }
}

// 执行SQL查询
const executeQuery = async () => {
  if (!selectedDataSource.value) {
    ElMessage.error('请选择数据源')
    return
  }

  const selectedSource = dataSources.value.find(
      source => source.dataSourceID === selectedDataSource.value
  )
  if (selectedSource) {
    selectedDataSourceName.value = selectedSource.dataSourceName
    dataSourceName.value = selectedSource.dataSourceName
  }

  const loading = ElLoading.service({ fullscreen: true })
  try {
    const response = await axios.post('http://localhost:8080/queries/executeQuery', {
      dataSourceId: selectedDataSource.value,
      query: sqlQuery.value
    })
    if (response.data.code === 200) {
      queryResult.value = {
        fields: response.data.data.columnsInfo.map(f => ({
          ...f,
          customLabel: f.customLabel || f.columnName
        })),
        data: {
          rows: response.data.data.rows,
          columnsInfo: response.data.data.columnsInfo
        }
      }
      targetTable.value = extractTableName(sqlQuery.value)
      editData.value = queryResult.value.data.rows
    }
  } catch (error) {
    ElMessage.error('SQL执行失败')
  } finally {
    loading.close()
  }
}

// 提取表名
const extractTableName = (sql) => {
  const matches = sql.match(/FROM\s+([^\s,;]+)/i)
  return matches ? matches[1] : ''
}

// 完成字段选择
const handleDone = () => {
  availableFields.value = selectedFields.value.map(f => ({
    prop: f.columnName,
    label: f.customLabel || f.columnName
  }))
  showDataSourceDialog.value = false
  initLayout()
}

// 保存报表
const saveReport = async () => {
  const fieldsToSave = []
  const layoutRow = reportLayout.value[0]

  tableColumns.value.forEach(col => {
    layoutRow[col.prop].forEach(field => {
      fieldsToSave.push({ prop: field.prop, label: field.label })
    })
  })

  // 新增主键校验逻辑
  if (!primaryKeyField.value) {
    ElMessage.error('请勾选一个主键字段');
    return;
  }

  const uniqueFields = [...new Map(fieldsToSave.map(item => [item.prop, item])).values()]

  const reportTemplate = {
    templateName: saveForm.value.templateName,
    dataSourceID: selectedDataSource.value,
    dataSourceName: selectedDataSourceName.value,
    templateCreator: userInfo.value.username,
    querySql: sqlQuery.value,
    templateConfig: JSON.stringify(uniqueFields),
    templateState: 1,
    TemplateKey: primaryKeyField.value,
    primaryKey: primaryKeyField.value
  }

  try {
    const response = await axios.post('http://localhost:8080/reporttemplates/save', reportTemplate)
    if (response.data.code === 200) {
      ElMessage.success('报表保存成功')
      showSaveDialog.value = false
    } else {
      ElMessage.error('保存失败: ' + response.data.msg)
    }
  } catch (error) {
    ElMessage.error('保存失败: ' + error.message)
  }
}

// 数据操作
const handleQuery = async () => {
  try {
    const response = await axios.post('http://localhost:8080/queries/executeQuery', {
      dataSourceId: selectedDataSource.value,
      query: sqlQuery.value
    })
    if (response.data.code === 200) {
      editData.value = response.data.data.rows
      generatePreviewData()
    }
  } catch (error) {
    ElMessage.error('查询失败')
  }
}

const handleAdd = async () => {
  try {
    await axios.post('/data-edition', newDataRow.value, {
      headers: {
        dataSourceName: dataSourceName.value,
        targetTable: targetTable.value,
        primaryKey: primaryKeyField.value
      }
    })
    ElMessage.success('添加成功')
    newDataRow.value = {}
    await handleQuery()
  } catch (error) {
    ElMessage.error('添加失败: ' + error.message)
  }
}

const handleEditData = (row) => {
  currentEditData.value = { ...row }
  editDataDialogVisible.value = true
}

const confirmEditData = async () => {
  try {
    await axios.put('/data-edition', currentEditData.value, {
      headers: {
        dataSourceName: dataSourceName.value,
        targetTable: targetTable.value,
        primaryKey: primaryKeyField.value
      }
    })
    ElMessage.success('更新成功')
    editDataDialogVisible.value = false
    await handleQuery()
  } catch (error) {
    ElMessage.error('更新失败: ' + error.message)
  }
}

const handleDeleteData = async (row) => {
  try {
    const primaryKeyValue = row[primaryKeyField.value]
    await axios.delete(`/data-edition/${primaryKeyValue}`, {
      headers: {
        dataSourceName: dataSourceName.value,
        targetTable: targetTable.value,
        primaryKey: primaryKeyField.value
      }
    })
    ElMessage.success('删除成功')
    await handleQuery()
  } catch (error) {
    ElMessage.error('删除失败: ' + error.message)
  }
}

// 生成预览数据
const generatePreviewData = () => {
  // 自动触发 computed 重新计算
}

const handleImportFields = () => {
  showDataSourceDialog.value = true
}

const handleSelectionChange = (selection) => {
  selectedFields.value = selection
}

const removeField = (row, col, field) => {
  row[col] = row[col].filter(f => f.prop !== field.prop)
}

const resetLayout = () => {
  initLayout()
  ElMessage.success('布局已重置')
}

const handlePageChange = (currentPage) => {
  pagination.value.currentPage = currentPage
}

const tableRowClassName = ({ rowIndex }) => {
  return rowIndex % 2 === 0 ? 'even-row' : 'odd-row'
}

const dragStart = (e) => {
  e.item.style.opacity = '0.5'
}

const dragEnd = (e) => {
  e.item.style.opacity = '1'
}

const handleLayoutChange = () => {
  if (activeTab.value === 'preview') {
    generatePreviewData()
  }
}

// 生命周期
onMounted(() => {
  initLayout()
  fetchDataSources()
})

// 监听器
watch(activeTab, (newTab) => {
  if (newTab === 'preview') {
    generatePreviewData()
  }
})
</script>

<style scoped>
.container {
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.report-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
  background: #fff;
}

.top-panel {
  margin-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 16px;
}

.design-preview {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.design-container {
  flex: 1;
  border: 1px solid #ebeef5;
  border-radius: 4px;
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
  min-width: 300px;
}

.field-item {
  padding: 6px 12px;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: move;
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
  padding: 8px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  background: #fafafa;
}

.delete-icon {
  cursor: pointer;
  color: #f56c6c;
  margin-left: auto;
  font-size: 14px;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.save-button {
  margin-top: 20px;
  text-align: right;
}

.pagination-container {
  text-align: left;
  padding: 12px 0 0 20px;
}

.even-row {
  background-color: #fafafa;
}

.odd-row {
  background-color: #ffffff;
}

.data-source-and-sql {
  padding: 20px;
}

.sql-editor {
  margin-bottom: 20px;
}

.action-buttons {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.save-form {
  padding: 20px;
}

:deep(.el-table__row) {
  height: auto !important;
}

:deep(.el-dialog) {
  width: 60%;
  max-width: 1000px;
  margin: 0 auto;
  position: fixed;
  top: 50%;
  left: 0;
  right: 0;
  transform: translateY(-50%);
  box-sizing: border-box;
}


/* 移除原有复杂选择器，改用以下样式 */
:deep(.save-form-dialog) {
  width: 40% !important;
  max-width: 500px !important; /* 根据需求调整最大宽度 */
}


:deep(.el-dialog__header) {
  //background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  //padding: 16px 20px;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
}

:deep(.el-dialog__body) {
  //padding: 20px;

}

:deep(.el-dialog__footer) {
  //background: #f5f7fa;
  border-top: 1px solid #ebeef5;
  //padding: 16px 20px;
  //display: flex;
  //justify-content: flex-end;
  //gap: 10px;
}

:deep(.el-form-item) {
  margin-bottom: 16px;
}

:deep(.el-form-item__label) {
  color: #606266;
  font-size: 14px;
}

:deep(.el-input) {
  width: 100%;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-pagination) {
  padding: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}
</style>