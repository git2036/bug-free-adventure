<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <h1>报表设计系统</h1>
      <div class="user-info">
        <span>欢迎，{{ user.username }}</span>
        <el-button @click="dialogVisible = true">个人中心</el-button>
        <el-button type="danger" @click="handleLogout">退出登录</el-button>

        <el-dialog v-model="dialogVisible" title="个人信息" width="30%">
          <el-form label-width="80px">
            <el-form-item label="用户名">
              <el-input v-model="user.username" :disabled="!editable" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input
                  v-model="user.password"
                  type="password"
                  :disabled="!editable"
                  show-password
              />
            </el-form-item>
            <el-form-item label="权限">
              <el-tag type="success">{{ user.Permissions }}</el-tag>
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button v-if="!editable" type="primary" @click="editable = true">编辑</el-button>
            <el-button v-if="editable" type="success" @click="handleSave">保存</el-button>
            <el-button type="danger" @click="confirmDeleteAccount">注销账户</el-button>
          </template>
        </el-dialog>
      </div>
    </el-header>

    <!-- 侧边栏和主内容区 -->
    <el-container class="main-content">
      <!-- 侧边栏 -->
      <el-aside width="200px" class="sidebar">
        <el-menu :default-active="activeMenu" @select="handleMenuSelect">
          <el-menu-item v-for="item in menuItems" :key="item.name" :index="item.name">
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.label }}</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="content">
        <component :is="activeComponent" />
      </el-main>
    </el-container>
  </div>
</template>

<script>
import {ref, onMounted, computed} from "vue";
import {useRouter} from "vue-router";
import axios from "axios";
import {ElMessage, ElMessageBox} from "element-plus";

import {User, Document, Edit, Setting} from "@element-plus/icons-vue";
import Report from "../components/Report.vue";
import UserManagement from "../components/UserManagement.vue";
import ReportManagement from "../components/ReportManagement.vue";
import DataSourceManagement from "../components/DataSourceManagement.vue";
import ReportInstanceManagement from "../components/ReportInstanceManagement.vue";
import PermissionManagement from "../components/RoleManagement.vue"; // 导入权限管理组件

export default {
  components: {
    Report,
    UserManagement,
    ReportManagement,
    DataSourceManagement,
    ReportInstanceManagement,
    PermissionManagement // 注册权限管理组件
  },
  setup() {
    const router = useRouter();
    const user = ref({});
    const activeMenu = ref("report");
    const menuItems = ref([]);
    const dialogVisible = ref(false);
    const editable = ref(false);

    const activeComponent = computed(() => {
      switch (activeMenu.value) {
        case "report":
          return "Report";
        case "role":
          return "UserManagement";
        case "report-management":
          return "ReportManagement";
        case "data-source":
          return "DataSourceManagement";
        case "report-instance":
          return "ReportInstanceManagement";
        case "permission-management": // 权限管理组件映射
          return "PermissionManagement";
        default:
          return "Report";
      }
    });

    onMounted(() => {
      const userData = localStorage.getItem("user");
      if (userData) {
        user.value = JSON.parse(userData);
        if (!user.value.Permissions) {
          user.value.Permissions = user.value.username === "admin" ? "Create,Edit,Delete,View" : "View";
        }
        generateMenuItems(user.value);
      } else {
        router.push("/login");
      }
    });

    const generateMenuItems = user => {
      const items = [
        {name: "report", label: "报表设计", icon: Document},
        {name: "report-management", label: "报表管理", icon: Document},
        {name: "data-source", label: "数据源管理", icon: Edit},
        {name: "report-instance", label: "报表实例管理", icon: Document},
        {name: "permission-management", label: "角色管理", icon: Setting} // 新增菜单项
      ];
      if (user.username === "admin") {
        items.push({name: "role", label: "用户管理", icon: Setting});
      }
      menuItems.value = items;
    };

    const handleMenuSelect = index => {
      activeMenu.value = index;
    };

    const handleLogout = () => {
      localStorage.removeItem("isAuthenticated");
      localStorage.removeItem("user");
      router.push("/login");
    };

    const handleDeleteAccount = async () => {
      try {
        const response = await axios.delete(`http://localhost:8080/users/${user.value.userID}`);
        if (response.data.code === 200) {
          ElMessage.success("账户注销成功");
          localStorage.removeItem("user");
          router.push("/login");
        } else {
          ElMessage.error("账户注销失败");
        }
      } catch (error) {
        ElMessage.error("网络错误，请稍后重试");
      }
    };

    const confirmDeleteAccount = () => {
      ElMessageBox.confirm(
          '确定要注销账户吗？此操作不可逆！',
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
      ).then(() => {
        handleDeleteAccount();
      }).catch(() => {
        ElMessage.info('已取消注销操作');
      });
    };

    const handleSave = async () => {
      console.log('Sending user data:', user.value);
      try {
        const response = await axios.put(`http://localhost:8080/users/${user.value.userID}`, user.value);
        if (response.data.code === 200) {
          ElMessage.success("用户信息更新成功");
          editable.value = false;
        } else {
          ElMessage.error("用户信息更新失败");
        }
      } catch (error) {
        ElMessage.error("网络错误，请稍后重试");
      }
    };

    return {
      user,
      activeMenu,
      menuItems,
      activeComponent,
      dialogVisible,
      editable,
      handleMenuSelect,
      handleLogout,
      handleDeleteAccount,
      handleSave,
      confirmDeleteAccount
    };
  }
};
</script>

<style scoped>
/* 样式保持不变，无需修改 */
.home-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background-color: #42b983;
  color: white;
}

.header h1 {
  margin: 0;
  font-size: 24px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-info span {
  margin-right: 10px;
}

.main-content {
  flex: 1;
  flex-direction: row;
}

.sidebar {
  background-color: #ffffff;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.el-menu {
  border-right: none !important;
  padding: 8px 0;
}

.el-menu-item {
  margin: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.el-menu-item:hover {
  background-color: #f5f7fa;
}

.el-menu-item.is-active {
  background-color: #42b983;
  color: white;
}

.el-menu-item.is-active:hover {
  background-color: #3aa876;
}

.el-icon {
  margin-right: 8px;
}

.content {
  padding: 20px;
  background-color: white;
}
</style>