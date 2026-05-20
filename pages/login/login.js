/**
 * 登录页 —— 逻辑层（相当于 Vue 的 <script>）
 * <p>
 * Page() 函数：注册一个页面，和 App() 类似但每个页面有自己的实例。
 * data 里的数据可以在 wxml 里用 {{ }} 绑定，用 this.setData() 更新。
 */
const request = require('../../utils/request');

Page({
    /**
     * data：页面的响应式数据
     * 和 Vue 的 data() 类似，但小程序必须通过 this.setData() 来修改，
     * 不能像 Vue 那样直接 this.xxx = yyy
     */
    data: {
        loading: false   // 登录按钮 loading 状态，防止重复点击
    },

    /**
     * 微信登录按钮点击事件
     * <p>
     * wx.login()：微信提供的登录 API，弹出"获取你的昵称头像"授权弹窗，
     * 用户同意后拿到一个临时 code（有效期 5 分钟）。
     * 注意：这个 code 只能用一次，而且必须由后端去换 openid。
     */
    handleLogin() {
        // 防止重复点击
        if (this.data.loading) return;
        this.setData({ loading: true });

        // 第一步：调用微信 API 获取临时 code
        wx.login({
            success: (res) => {
                if (!res.code) {
                    wx.showToast({ title: '微信登录失败', icon: 'none' });
                    this.setData({ loading: false });
                    return;
                }

                console.log('拿到微信 code:', res.code);

                // 第二步：把 code 发给后端换取 JWT token
                request.post('/api/auth/wechat-login', {
                    code: res.code
                }).then((apiRes) => {
                    // apiRes = { code: 200, message: "登录成功", data: { token, userId, nickname, phone } }
                    const { token, userId, nickname, phone } = apiRes.data;

                    // 第三步：token 存入本地存储（类似浏览器的 localStorage）
                    wx.setStorageSync('token', token);

                    // 第四步：用户信息存入全局数据
                    const app = getApp();  // getApp() 获取全局 App 实例
                    app.globalData.userId = userId;
                    app.globalData.nickname = nickname;
                    app.globalData.phone = phone;

                    wx.showToast({ title: '登录成功', icon: 'success' });

                    // 第五步：跳到任务列表页
                    // reLaunch：关闭所有页面，打开新页面（登录后不能返回到登录页）
                    wx.reLaunch({ url: '/pages/index/index' });

                }).catch((err) => {
                    console.error('登录失败:', err);
                    this.setData({ loading: false });
                });
            },

            // wx.login 本身失败（极少见，一般是微信服务异常）
            fail: (err) => {
                console.error('wx.login 调用失败:', err);
                wx.showToast({ title: '微信登录失败', icon: 'none' });
                this.setData({ loading: false });
            }
        });
    }
});
