import axios from 'axios';

// 1. Tạo instance Axios riêng
const api = axios.create({
    baseURL: 'http://103.56.163.55:8080/api', // URL gốc của Backend
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
});

// 2. Interceptor cho Request: Tự động gắn Token trước khi gửi
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            // Quan trọng: Phải đúng chuẩn "Bearer <token>"
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 3. Interceptor cho Response: Xử lý khi Token hết hạn (Lỗi 401/403)
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
            // Nếu bị từ chối quyền -> Xóa token và đá về trang Login
            console.warn('Phiên đăng nhập hết hạn hoặc không có quyền.');
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            
            
            // Chuyển hướng về Login (dùng window.location để refresh sạch sẽ)
            // if (window.location.pathname !== '/login') {
            //     window.location.href = '/login';
            // }

            alert('Phiên đăng nhập của bạn đã hết hạn hoặc bạn không có quyền truy cập.');
        }
        return Promise.reject(error);
    }
);

export default api;