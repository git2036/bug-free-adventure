import { createRouter, createWebHistory } from 'vue-router';
import Home from '../views/Home.vue';
import Login from '../views/Login.vue';
import Register from '../views/Register.vue'; // 导入注册页面

const routes = [
    {
        path: '/',
        name: 'Home',
        component: Home,
        meta: { requiresAuth: true }, // 需要登录才能访问
    },
    {
        path: '/login',
        name: 'Login',
        component: Login,
    },
    {
        path: '/register',
        name: 'Register',
        component: Register, // 注册页面
    },
    //增加Report.vue页面
    {
        path: '/report', // 报表页面路径
        name: 'Report', // 报表页面名称
        component: () => import('../components/Report.vue'), // 报告页面组件
        meta: { requiresAuth: true }, // 需要登录才能访问
    },
    //UserManagement.vue页面
    {
        path: '/Usermanagement',// 用户管理页面路径
        name: 'UserManagement',
        component: () => import('../components/UserManagement.vue'),
        meta: { 
            requiresAuth: true,
            requiresAdmin: true 
        }
    },
    // 数据源管理路由
    {
        path: '/datasource',
        name: 'DataSourceManagement',
        component: () => import('../components/DataSourceManagement.vue'),
        meta: {
            requiresAuth: true
        }
    },
    // 报表实例管理路由
    {
        path: '/report-instances',
        name: 'ReportInstanceManagement',
        component: () => import('../components/ReportInstanceManagement.vue'),
        meta: {
            requiresAuth: true
        }
    },
    {   path: '/permission-management', // 角色管理页面路径
        name: 'PermissionManagement',
        component: () => import('../components/RoleManagement.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }}, // 权限管理页面
    //报表管理路由
    {
        path: '/report-management',
        name: 'ReportManagement',
        component: () => import('../components/ReportManagement.vue'),
        meta: {
            requiresAuth: true
        }
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

// 路由守卫：检查用户是否登录
// 路由守卫，在每次路由跳转前执行
router.beforeEach((to, from, next) => {
    // 获取本地存储中的登录状态
    const isAuthenticated = localStorage.getItem('isAuthenticated');
    // 获取本地存储中的用户信息
    const user = JSON.parse(localStorage.getItem('user') || '{}');

    // 登录验证
    if (to.meta.requiresAuth && !isAuthenticated) {
        // 如果需要登录且未登录，跳转到登录页面
        next('/login');
    }
    // 管理员权限验证
    else if (to.meta.requiresAdmin) {
        // 根据用户信息判断管理员身份
        const isAdmin = user.username === 'admin';
        if (!isAdmin) {
            // 非管理员跳转首页
            next('/');
            return
        }
    }
    else {
        // 其他情况，直接放行
        next();
    }
});

export default router;
