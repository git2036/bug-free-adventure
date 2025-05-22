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

        <!-- 设计容器 -->
        <div class="design-container" style="height: calc(100vh - 220px); overflow: auto">
          <el-tabs v-model="activeTab" type="card" style="height: 100%">

            <!-- 设计模式 -->
            <el-tab-pane label="设计模式" name="design">
              <!-- 设计模式内容 -->
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
                    :cell-style="{ whiteSpace: 'normal' }"
                >
                  <el-table-column
                      v-for="(col, index) in previewColumns"
                      :key="index"
                      :prop="col.prop"
                      :label="col.label"
                      :min-width="120"
                      :show-overflow-tooltip="true"
                  />
                </el-table>
                <div class="pagination-wrapper">
                  <el-pagination
                      @size-change="handleSizeChange"
                      @current-change="handleCurrentChange"
                      :current-page="currentPage"
                      :page-sizes="[5, 10, 20]"
                      :page-size="pageSize"
                      prev-text="上一页"
                      next-text="下一页"
                      :total="filteredPreviewData.length"
                      layout="total, sizes, prev, pager, next, jumper"
                  >
                    <template #total>
                      共 {{ filteredPreviewData.length }} 条数据
                    </template>
                    <template #sizes>
                      每页显示
                      <el-select v-model="pageSize" @change="handleSizeChange">
                        <el-option
                            v-for="size in [5, 10, 20]"
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
              </div>
            </el-tab-pane>

            <!-- 数据编辑 -->
            <el-tab-pane label="数据编辑" name="edit">
              <div style="height: 100%; display: flex; flex-direction: column; overflow: auto;">
                <el-table
                    :data="pagedEditData"
                    border
                    stripe
                    style="width: 100%; flex: 1;"
                >
<!--                  &lt;!&ndash; 筛选行（位于字段行下方，数据上方） &ndash;&gt;-->
<!--                  <template #header>-->
<!--                    <div class="filter-row" style="padding: 12px 20px; background: #f5f7fa; border-bottom: 1px solid #ebeef5; display: flex; align-items: center; gap: 10px;">-->
<!--                      &lt;!&ndash; 字段筛选输入框 &ndash;&gt;-->
<!--                      <div v-for="field in queryResult.fields" :key="field.columnName" style="flex: 1; max-width: 200px;">-->
<!--                        <el-input-->
<!--                            v-model="filters[field.columnName]"-->
<!--                            placeholder="筛选 {{ field.customLabel || field.columnName }}"-->
<!--                            style="width: 100%;"-->
<!--                        />-->
<!--                      </div>-->
<!--                      &lt;!&ndash; 操作按钮 &ndash;&gt;-->
<!--                      <div style="flex-shrink: 0;">-->
<!--                        <el-button type="primary" @click="handleFilter">筛选</el-button>-->
<!--                        <el-button type="primary" @click="addEmptyRow">添加</el-button>-->
<!--                      </div>-->
<!--                    </div>-->
<!--                  </template>-->

                  <!-- 自增ID列（保留，但编辑弹窗不显示） -->
                  <el-table-column label="ID" width="60">
                    <template #default="{ $index }">
                      {{ $index + 1 }}
                    </template>
                  </el-table-column>

                  <el-table-column
                      v-for="(field, index) in queryResult.fields.filter(field => field.columnName !== primaryKeyField)"
                      :key="index"
                      :prop="field.columnName"
                      :label="field.customLabel || field.columnName"
                      :width="field.columnSize || 150"
                  >
                    <template #default="{ row }">
                      <template v-if="field.columnName === primaryKeyField">
                        {{ row[field.columnName] }}
                      </template>
                      <template v-else>
                        <el-input
                            v-model="row[field.columnName]"
                            style="width: 100%; max-width: 180px;"
                        />
                      </template>
                    </template>
                  </el-table-column>

                  <el-table-column label="操作" width="180" fixed="right">
                    <template #default="{ row, $index }">
                      <el-button size="small" @click="handleEditData(row, $index)">编辑</el-button>
                      <el-button size="small" type="danger" @click="handleDeleteData($index)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>

                <!-- 编辑对话框（移除ID字段） -->
                <el-dialog
                    v-model="editDataDialogVisible"
                    title="编辑数据"
                    width="600px"
                    class="compact-dialog"
                >
                  <el-form :model="currentEditData">
                    <el-form-item
                        v-for="col in queryResult.fields.filter(col => col.columnName !== primaryKeyField)"
                        :key="col.columnName"
                        :label="col.customLabel || col.columnName"
                        :label-width="col.columnSize ? `${col.columnSize}px` : '100px'"
                    >
                      <el-input
                          v-model="currentEditData[col.columnName]"
                          :style="{ width: col.columnSize ? `${col.columnSize}px` : '240px' }"
                      />
                    </el-form-item>
                  </el-form>
                  <template #footer>
                    <div style="display: flex; justify-content: flex-end; gap: 10px;">
                      <el-button @click="editDataDialogVisible = false">取消</el-button>
                      <el-button type="primary" @click="confirmEditData">保存</el-button>
                    </div>
                  </template>
                </el-dialog>
              </div>
              <div class="pagination-wrapper">
                <el-pagination
                    @size-change="handleEditSizeChange"
                    @current-change="handleEditCurrentChange"
                    :current-page="editCurrentPage"
                    :page-sizes="[5, 10, 20]"
                    :page-size="editPageSize"
                    prev-text="上一页"
                    next-text="下一页"
                    :total="editTotal"
                    layout="total, sizes, prev, pager, next, jumper"
                >
                  <template #total>共 {{ editTotal }} 条数据</template>
                  <template #sizes>
                    每页显示
                    <el-select v-model="editPageSize" @change="handleEditSizeChange">
                      <el-option
                          v-for="size in [5, 10, 20]"
                          :key="size"
                          :label="size + ' 条'"
                          :value="size"
                      />
                    </el-select>
                  </template>
                  <template #jumper>
                    跳转到
                    <el-input
                        v-model.number="editJumpPage"
                        @keyup.enter="handleEditJumpPage"
                        size="small"
                        style="width: 50px"
                    /> 页
                  </template>
                </el-pagination>
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
import {ref, computed, watch, onMounted} from 'vue'
import axios from 'axios'
import {ElMessage, ElLoading, ElMessageBox} from 'element-plus'
import {Close} from '@element-plus/icons-vue'
import draggable from 'vuedraggable'

// 数据源相关
const dataSources = ref([])
const selectedDataSource = ref(null)
const selectedDataSourceName = ref('')
const sqlQuery = ref('')
const queryResult = ref({fields: [], data: {rows: []}})
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
const editData = ref([]) // 初始化为空数组
const newDataRow = ref({})
const reportTemplateId = ref(null)

// 筛选条件
const filters = ref({})
const filteredEditData = ref(editData.value) // 改为响应式引用

// 用户信息
const userInfo = ref({
  userID: null,
  username: 'admin',
  password: '',
  permissions: ''
})

// 视图控制
const activeTab = ref('design')
const showDataSourceDialog = ref(false)
const showSaveDialog = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(5)
const jumpPage = ref(1)

// 数据编辑分页相关（新增）
const editCurrentPage = ref(1) // 当前页
const editPageSize = ref(5) // 每页显示条数
const editTotal = ref(0) // 总数据量
const editFilteredData = ref([]) // 过滤后的数据集

// 保存表单
const saveForm = ref({templateName: ''})
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

// 计算分页数据（新增）（数据编辑模式）
const pagedEditData = computed(() => {
  const start = (editCurrentPage.value - 1) * editPageSize.value
  const end = start + editPageSize.value
  return editFilteredData.value.slice(start, end)
})

// 过滤后的预览数据
const filteredPreviewData = computed(() => {
  return previewData.value.filter(item => {
    const searchKey = Object.values(filters.value).find(key => key)
    if (!searchKey) return true

    return Object.values(item).some(val =>
        String(val).toLowerCase().includes(searchKey.toLowerCase())
    )
  })
})

// 分页后的预览数据
const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredPreviewData.value.slice(start, end)
})

// 预览数据
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

// 筛选处理函数
const handleFilter = () => {
  if (Object.keys(filters.value).length === 0) {
    editFilteredData.value = editData.value
  } else {
    editFilteredData.value = editData.value.filter(row => {
      return Object.entries(filters.value).every(([key, value]) => {
        if (!value) return true
        return String(row[key]).toLowerCase().includes(value.toLowerCase())
      })
    })
  }
  editTotal.value = editFilteredData.value.length // 更新总数据量
}

// 初始化布局
const initLayout = () => {
  tableColumns.value = Array.from({length: 8}, (_, index) => ({
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

const validateSQL = (sql) => {
  if (!sql) return false

  // 标准化处理
  const standardizedSQL = sql
      .toLowerCase()
      .replace(/[\n\r]/g, ' ')  // 替换换行符
      .replace(/\s+/g, ' ')     // 合并连续空格
      .replace(/;\s*$/, '')     // 去除末尾分号
      .trim()

  // 校验必要结构
  const hasSelect = standardizedSQL.startsWith('select')
  const hasFrom = standardizedSQL.includes(' from ')
  const fromPosition = standardizedSQL.indexOf(' from ')

  if (!hasSelect || !hasFrom) return false

  // 校验FROM后内容
  const afterFrom = standardizedSQL.slice(fromPosition + 6).trim()
  if (!afterFrom || afterFrom.split(' ')[0] === '') return false

  return true
}

// 执行SQL查询
const executeQuery = async () => {
  // 校验数据源选择
  if (!selectedDataSource.value) {
    ElMessage.error('请先选择数据源')
    return
  }

  // 执行SQL校验
  if (!validateSQL(sqlQuery.value)) {
    ElMessage.error({
      message: 'sql语句输入错误',
      duration: 5000
    })
    return
  }

  const loading = ElLoading.service({
    fullscreen: true,
    text: '正在执行查询，请稍候...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

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
      editData.value = response.data.data.rows
      handleFilter()
      ElMessage.success('查询执行成功')
    } else {
      ElMessage.error(`查询失败：${response.data.msg}`)
    }
  } catch (error) {
    const errorMessage = error.response?.data?.message ||
        error.message ||
        '未知错误'
    ElMessage.error(`请求失败：${errorMessage}`)
  } finally {
    loading.close()
  }
}

// 数据编辑模式 - 每页显示条数改变时触发
const handleEditSizeChange = (newSize) => {
  editPageSize.value = newSize;
  editCurrentPage.value = 1; // 切换每页数量后回到第一页
};

// 数据编辑模式 - 当前页改变时触发
const handleEditCurrentChange = (newPage) => {
  editCurrentPage.value = newPage;
};

// 数据编辑模式 - 跳转页码时触发
const handleEditJumpPage = () => {
  // 确保页码为正整数
  if (!Number.isInteger(editJumpPage.value) || editJumpPage.value < 1) {
    ElMessage.error('页码必须为正整数');
    editJumpPage.value = 1;
    return;
  }

  const totalPages = Math.ceil(editTotal.value / editPageSize.value);
  if (editJumpPage.value > totalPages && totalPages > 0) {
    editJumpPage.value = totalPages; // 超出总页数时自动跳转至最后一页
  }

  editCurrentPage.value = editJumpPage.value;
};

// 表名提取方法优化
const extractTableName = (sql) => {
  const fromRegex = /from\s+([^({\s]+)/i
  const matches = sql.match(fromRegex)
  if (!matches) return ''

  let tableName = matches[1]
  // 处理可能存在的模式前缀
  if (tableName.includes('.')) {
    tableName = tableName.split('.').pop()
  }
  // 去除可能存在的特殊字符
  return tableName.replace(/[`"'[\]]/g, '')
}

// 完成字段选择
const handleDone = () => {
  if (!primaryKeyField.value) {
    ElMessage.error('请选择一个主键字段')
    return // 阻止关闭弹窗并返回
  }
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
      fieldsToSave.push({prop: field.prop, label: field.label})
    })
  })

  if (!primaryKeyField.value) {
    ElMessage.error('请勾选一个主键字段')
    return
  }

  const uniqueFields = [...new Map(fieldsToSave.map(item => [item.prop, item])).values()]



  // 数据编辑分页处理（新增）
  const handleEditSizeChange = (newSize) => {
    editPageSize.value = newSize
    editCurrentPage.value = 1
  }

  const handleEditCurrentChange = (newPage) => {
    editCurrentPage.value = newPage
  }

  const handleEditJumpPage = () => {
    const totalPages = Math.ceil(editFilteredData.value.length / editPageSize.value)
    if (editJumpPage.value >= 1 && editJumpPage.value <= totalPages) {
      editCurrentPage.value = editJumpPage.value
    } else {
      ElMessage.error('输入的页码无效')
    }
  }
  // 数据编辑分页处理（新增）结束


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
      reportTemplateId.value = response.data.data.templateId
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

// 修改数据编辑函数
const handleEditData = (row, index) => {
  currentEditData.value = {...row}
  currentEditIndex.value = index
  editDataDialogVisible.value = true
}

// 修改数据编辑确认函数
const confirmEditData = async () => {
  if (currentEditIndex.value === -1) return

  // 确保包含主键字段
  if (!currentEditData.value[primaryKeyField.value]) {
    ElMessage.error('主键字段值不可为空')
    return
  }

  try {
    const response = await axios.put(
        'http://localhost:8080/data-edition',
        currentEditData.value,
        {
          headers: {
            dataSourceName: selectedDataSourceName.value,
            targetTable: targetTable.value,
            primaryKey: primaryKeyField.value
          }
        }
    )

    if (response.data.code === 200) {
      editData.value[currentEditIndex.value] = {...currentEditData.value}
      editDataDialogVisible.value = false
      currentEditIndex.value = -1
      ElMessage.success('编辑成功')
      handleFilter()
    } else {
      ElMessage.error(`更新失败: ${response.data.msg}`)
    }
  } catch (error) {
    ElMessage.error(`更新失败: ${error.message}`)
  }
}

// 修改数据删除函数
const handleDeleteData = async (index) => {
  const row = editData.value[index]
  if (!row[primaryKeyField.value]) {
    ElMessage.error('无法获取删除记录的主键值')
    return
  }

  await ElMessageBox.confirm('确定删除此记录？')
      .then(async () => {
        try {
          const response = await axios.delete(
              `http://localhost:8080/data-edition/${row[primaryKeyField.value]}`,
              {
                headers: {
                  dataSourceName: selectedDataSourceName.value,
                  targetTable: targetTable.value,
                  primaryKey: primaryKeyField.value
                },
                // 传递完整条件（可选，根据后端需求）
                data: {...row}
              }
          )

          if (response.data.code === 200) {
            editData.value.splice(index, 1)
            ElMessage.success('删除成功')
            handleFilter()
          } else {
            ElMessage.error(`删除失败: ${response.data.msg}`)
          }
        } catch (error) {
          ElMessage.error(`删除失败: ${error.message}`)
        }
      })
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

const tableRowClassName = ({rowIndex}) => {
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

// 新增空白行
const addEmptyRow = () => {
  newDataRow.value = Object.fromEntries(
      queryResult.value.fields
          .filter(f => f.columnName !== primaryKeyField.value)
          .map(f => [f.columnName, ''])
  )
  currentEditData.value = {...newDataRow.value}
  currentEditIndex.value = -1
  editDataDialogVisible.value = true
}

// 新增保存方法
const saveNewRow = async () => {
  if (Object.keys(currentEditData.value).length === 0) {
    ElMessage.warning('请先填写数据')
    return
  }

  const reportDataItem = {
    templateId: reportTemplateId.value,
    data: currentEditData.value
  }

  try {
    const response = await axios.post('http://localhost:8080/reportdata/add', reportDataItem)
    if (response.data.code === 200) {
      editData.value.unshift({...currentEditData.value})
      editDataDialogVisible.value = false
      ElMessage.success('添加成功')
      handleFilter()
    } else {
      ElMessage.error('添加失败: ' + response.data.msg)
    }
  } catch (error) {
    ElMessage.error('添加失败: ' + error.message)
  }
}

// 分页处理函数
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1 // 改变每页数量后回到第一页
}

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
}

const handleJumpPage = () => {
  const totalPages = Math.ceil(filteredPreviewData.value.length / pageSize.value)
  if (jumpPage.value >= 1 && jumpPage.value <= totalPages) {
    currentPage.value = jumpPage.value
  } else {
    ElMessage.error('输入的页码无效')
  }
}

// 新增数据源名称监听
watch(selectedDataSource, (newValue) => {
  if (newValue) {
    const source = dataSources.value.find(s => s.dataSourceID === newValue)
    selectedDataSourceName.value = source?.dataSourceName || ''
  }
}, {immediate: true})

// 生命周期
onMounted(() => {
  initLayout()
  fetchDataSources()
})

// 监听器
watch(queryResult, () => {
  editData.value = queryResult.value.data.rows.map(row => ({...row}))
  handleFilter() // 数据源变化时重新筛选
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

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
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

:deep(.save-form-dialog) {
  width: 40% !important;
  max-width: 500px !important;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 20px;
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

:deep(.el-pagination .el-select .el-input) {
  width: 80px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 8px;
}

.add-row {
  display: flex;
  justify-content: flex-start;
  padding: 12px 20px;
  background: #f5f7fa;
  border-top: 1px solid #ebeef5;
}

.el-input {
  width: 100% !important;
  max-width: 180px;
}

.el-table .el-table__column {
  flex-shrink: 0; /* 禁止列宽收缩 */
  min-width: 120px; /* 最小宽度 */
}

.el-table__cell {
  white-space: normal !important; /* 单元格内容换行 */
  word-break: break-all; /* 允许换行断字 */
}

.el-table-column--fixed-right {
  background-color: #f8f9fa;
}
:deep(.compact-dialog) {
  width: 600px !important;
  max-width: 600px !important;
}
</style>
