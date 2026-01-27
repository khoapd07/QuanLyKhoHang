import { createApp } from 'vue'
import './style.css' // CSS riêng của bạn (nếu có)
import App from './App.vue'
import router from './router/index'

// 1. Import CSS của AdminLTE và Bootstrap Icons
import 'admin-lte/dist/css/adminlte.min.css'
import 'bootstrap-icons/font/bootstrap-icons.min.css'

// 2. Import JS của AdminLTE (để chạy các hiệu ứng toggle menu mặc định)
import 'admin-lte/dist/js/adminlte.min.js'

const app = createApp(App)
app.use(router)
app.mount('#app')