<template>
  <div class="login-container">
    <h1>报表设计系统登录</h1>
    <form @submit.prevent="handleLogin">
      <div class="form-group">
        <label for="username">用户名</label>
        <input
            type="text"
            id="username"
            v-model="username"
            placeholder="请输入用户名"
            required
        />
      </div>
      <div class="form-group">
        <label for="password">密码</label>
        <input
            type="password"
            id="password"
            v-model="password"
            placeholder="请输入密码"
            required
        />
      </div>
      <button type="submit">登录</button>
      <p v-if="error" class="error-message">{{ error }}</p>
    </form>
    <p class="register-link">
      没有账号？
      <router-link to="/register">去注册</router-link>
    </p>
  </div>
</template>

<script>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios'; // 引入 axios

export default {
  setup() {
    const username = ref('');
    const password = ref('');
    const error = ref('');
    const router = useRouter();

    const handleLogin = async () => {
      try {
        // 登录请求获取用户信息
        const loginResponse = await axios.post('/api/users/login', {
          username: username.value,
          password: password.value,
        }, {
          headers: {
            'Content-Type': 'application/json', // 显式设置请求头
          },
        });

        // 检查登录是否成功
        if (loginResponse.data.code !== 200) {
          error.value = loginResponse.data.msg || '登录失败';
          return;
        }

        // 从响应中获取用户名（假设后端返回的用户信息包含username）
        const userUsername = username.value; // 确保字段名与后端一致
        console.log(userUsername);
        // 通过用户名获取权限
        const permissionsResponse = await axios.get(`http://localhost:8080/users/${userUsername}/permissions`);

        if (permissionsResponse.data.code === 200) {
          const permissions = permissionsResponse.data.data;
          // 存储权限和用户信息（可选：根据需要存储用户信息）
          localStorage.setItem('permissions', permissions);
          localStorage.setItem('user', JSON.stringify(loginResponse.data.data)); // 存储用户信息
          localStorage.setItem('isAuthenticated', 'true');
          // 强制刷新主页（可选，确保首次登录时菜单正确生成）
          router.push({ path: '/', replace: true });
          // 跳转至主页
          // router.push('/home');
        } else {
          error.value = '获取权限失败，请重试';
          console.error('权限获取失败:', permissionsResponse.data.msg);
        }

      } catch (err) {
        console.error('登录过程出错:', err);
        error.value = '登录失败，请检查网络或联系管理员';
      }
    };

    return {
      username,
      password,
      error,
      handleLogin,
    };
  },
};
</script>

<style scoped>
/* 样式部分保持不变，无需修改 */
.login-container {
  max-width: 400px;
  margin: 100px auto;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  text-align: center;
}

h1 {
  text-align: center;
  margin-bottom: 20px;
}

.form-group {
  display: flex; /* 使用 Flexbox 布局 */
  align-items: center; /* 垂直居中 */
  margin-bottom: 15px;
}

label {
  flex: 0 0 100px; /* 固定标签宽度 */
  font-weight: bold;
  margin-right: 10px; /* 标签和输入框之间的间距 */
}

input {
  flex: 1; /* 输入框占据剩余空间 */
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 16px;
}

button {
  width: 100%;
  padding: 10px;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
}

button:hover {
  background-color: #369f6e;
}

.error-message {
  color: red;
  text-align: center;
  margin-top: 10px;
}

.register-link {
  margin-top: 15px;
  font-size: 14px;
}

.register-link a {
  color: #42b983;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>