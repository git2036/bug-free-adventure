<template>
  <div class="register-container">
    <h1>用户注册</h1>
    <form @submit.prevent="handleRegister">
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
      <div class="form-group">
        <label for="confirmPassword">确认密码</label>
        <input
            type="password"
            id="confirmPassword"
            v-model="confirmPassword"
            placeholder="请再次输入密码"
            required
        />
      </div>
      <button type="submit">注册</button>
      <p v-if="error" class="error-message">{{ error }}</p>
      <p v-if="success" class="success-message">{{ success }}</p>
    </form>
    <p class="login-link">
      已有账号？<router-link to="/login">去登录</router-link>
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
    const confirmPassword = ref('');
    const error = ref('');
    const success = ref('');
    const router = useRouter();

    const handleRegister = async () => {
      // console.log('用户名:', username.value); // 打印用户名
      // console.log('密码:', password.value); // 打印密码
      // console.log('确认密码:', confirmPassword.value); // 打印确认密码

      if (!username.value || !password.value || !confirmPassword.value) {
        error.value = '用户名和密码不能为空';
        return;
      }
      // 检查两次密码是否一致
      if (password.value !== confirmPassword.value) {
        error.value = '两次输入的密码不一致';
        return;
      }

      try {
        // 发送注册请求
        const response = await axios.post('/api/users/register', null, {
          params: {
            username: username.value,
            password: password.value,
          },
        });

        // 根据接口返回结果处理
        if (response.data.code === 200) {
          success.value = '注册成功！正在跳转到登录页面...';
          setTimeout(() => {
            router.push('/login'); // 注册成功后跳转到登录页面
          }, 2000);
        } else {
          error.value = response.data.msg || '注册失败，请重试';
        }
      } catch (err) {
        // 处理请求错误
        error.value = '请求失败，请检查网络或稍后重试';
        console.error('注册请求失败:', err);
      }
    };

    return {
      username,
      password,
      confirmPassword,
      error,
      success,
      handleRegister,
    };
  },
};
</script>

<style scoped>
.register-container {
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

.success-message {
  color: green;
  text-align: center;
  margin-top: 10px;
}

.login-link {
  margin-top: 15px;
  font-size: 14px;
}

.login-link a {
  color: #42b983;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>