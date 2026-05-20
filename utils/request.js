/**
 * 网络请求封装 —— 小程序里的"axios"
 * <p>
 * 为什么封装 wx.request？
 * 1. 每次调后端都要写一堆重复代码（method、header、success、fail）
 * 2. 每次都要手动带 Authorization: Bearer <token>
 * 3. 统一处理错误（比如 token 过期自动跳到登录页）
 * <p>
 * 封装后调用方式：request.get('/api/tasks').then(res => { ... })
 * 和你在 Vue 里用 axios.get('/api/tasks') 几乎一样
 */

// 后端地址（开发环境用 localhost，微信开发者工具模拟器可以直接访问）
const BASE_URL = 'http://localhost:8888';

/**
 * 核心请求方法
 * @param {string} url    接口路径，如 '/api/tasks'
 * @param {string} method 请求方法：GET / POST / PUT / DELETE
 * @param {object} data   请求体数据（GET 请求传 null）
 * @returns {Promise}     返回 Promise，用 .then(res => ...) 拿结果
 */
function request(url, method, data) {
    // 从本地存储取出登录时保存的 token
    const token = wx.getStorageSync('token');

    // 返回一个 Promise（和 axios 一样，用 .then / .catch 处理结果）
    return new Promise((resolve, reject) => {
        wx.request({
            url: BASE_URL + url,
            method: method,
            // 请求头：带上 JSON 格式声明 + JWT token
            header: {
                'Content-Type': 'application/json',
                // 如果有 token 就带上，没有的话后端会返回 401
                'Authorization': token ? 'Bearer ' + token : ''
            },
            // GET 请求不传 data，POST/PUT 请求把 data 转成 JSON 字符串
            data: method === 'GET' ? undefined : data,

            // 请求成功
            success(res) {
                // res.statusCode：HTTP 状态码（200、401、500 等）
                // res.data：后端返回的 JSON（即 ApiResponse 对象）
                if (res.statusCode === 200) {
                    // 后端返回 { code: 200, message: "...", data: ... }
                    if (res.data.code === 200) {
                        resolve(res.data);  // 成功，把整个响应对象传出去
                    } else {
                        // 后端业务逻辑失败（如参数校验不通过）
                        wx.showToast({
                            title: res.data.message || '请求失败',
                            icon: 'none'  // 'none' = 只显示文字，不带图标
                        });
                        reject(res.data);
                    }
                } else if (res.statusCode === 401) {
                    // 未登录或 token 过期，清除旧 token，跳到登录页
                    wx.removeStorageSync('token');
                    wx.showToast({ title: '请重新登录', icon: 'none' });
                    // 跳转到登录页（redirectTo 会关闭当前页，防止返回来）
                    wx.redirectTo({ url: '/pages/login/login' });
                    reject(res.data);
                } else {
                    wx.showToast({ title: '服务器错误', icon: 'none' });
                    reject(res.data);
                }
            },

            // 网络错误或请求超时
            fail(err) {
                wx.showToast({ title: '网络请求失败', icon: 'none' });
                reject(err);
            }
        });
    });
}

// ===== 导出 4 个快捷方法，和 axios 用法一样 =====

// GET 请求：request.get('/api/tasks').then(res => { ... })
const get = (url) => request(url, 'GET', null);

// POST 请求：request.post('/api/tasks', { title: 'xxx' })
const post = (url, data) => request(url, 'POST', data);

// PUT 请求：request.put('/api/tasks/1', { done: true })
const put = (url, data) => request(url, 'PUT', data);

// DELETE 请求：request.del('/api/tasks/1')
const del = (url) => request(url, 'DELETE', null);

module.exports = { get, post, put, del };
