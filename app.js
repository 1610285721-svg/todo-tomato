/**
 * 小程序入口文件 —— 相当于 Vue 的 main.js
 * <p>
 * App() 函数：注册整个小程序应用，只会执行一次。
 * 里面的 onLaunch 在小程序启动时自动调用。
 * globalData：全局共享数据，任何页面都可以通过 getApp().globalData 读写。
 */
App({
    /**
     * 小程序启动时执行（相当于 Vue 的 mounted，但只执行一次）
     * 这里用来检查登录状态
     */
    onLaunch() {
        // 检查本地存储里有没有 token（上次登录保存的）
        const token = wx.getStorageSync('token');
        if (token) {
            console.log('已有登录 token，跳过登录');
        } else {
            console.log('未登录，需要先登录');
        }
    },

    /**
     * 全局共享数据
     * 比如登录后在 globalData 里存 userId 和 nickname，所有页面都能读到
     * 用法：getApp().globalData.userId
     */
    globalData: {
        userId: null,      // 当前登录用户 ID
        nickname: null,    // 昵称
        phone: null        // 手机号
    }
});
