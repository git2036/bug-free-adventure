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
import {ref} from 'vue';
import {useRouter} from 'vue-router';
import axios from 'axios'; // 引入 axios

export default {
  setup() {
    const username = ref('');
    const password = ref('');
    const error = ref('');
    const router = useRouter();

    const handleLogin = async () => {

      try {
        const response = await axios.post('/api/users/login', {
          username: username.value,
          password: password.value,
        }, {
          headers: {
            'Content-Type': 'application/json', // 显式设置请求头
          },
        });


        if (response.data.code === 200) {
          localStorage.setItem('isAuthenticated', 'true'); // 存储登录状态
          localStorage.setItem('user', JSON.stringify(response.data.data)); // 存储用户信息
          await router.push('/'); // 跳转到首页
        } else {
          error.value = response.data.msg || '用户名或密码错误';
        }
      } catch (err) {
        console.error('登录请求失败:', err); // 打印错误信息
        error.value = '请求失败，请检查网络或稍后重试';
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