/**
 * 任务列表页 —— 逻辑层
 * <p>
 * 页面的生命周期：
 * - onLoad：页面第一次加载时调用，只调一次（相当于 Vue 的 created + mounted）
 * - onShow：页面显示时调用（每次从其他页面切回来都会触发）
 * - onHide：页面隐藏时调用
 * <p>
 * 我们选 onShow 而不是 onLoad 来加载数据，因为：
 * 番茄钟完成回来后页面重新显示，onShow 会再执行，自动刷新任务列表。
 */
const request = require('../../utils/request');

Page({
    data: {
        tasks: [],           // 任务列表
        inputValue: '',      // 输入框内容（新增任务用）
        loading: true        // 页面加载状态
    },

    /**
     * 页面显示时触发（每次切回这个页面都会执行）
     * 在这里调后端获取最新任务列表
     */
    onShow() {
        this.loadTasks();
    },

    /**
     * 从后端加载任务列表
     */
    loadTasks() {
        this.setData({ loading: true });

        request.get('/api/tasks').then((res) => {
            // res.data 就是后端返回的任务数组 [{id, title, pomodoroCount, done, ...}, ...]
            this.setData({
                tasks: res.data,
                loading: false
            });
        }).catch(() => {
            this.setData({ loading: false });
        });
    },

    /**
     * 输入框内容变化时
     * @param {object} e 事件对象，e.detail.value 是输入框当前值
     */
    onInputChange(e) {
        this.setData({ inputValue: e.detail.value });
    },

    /**
     * 新增任务
     * 按回车或点击按钮触发
     */
    addTask() {
        const title = this.data.inputValue.trim();
        if (!title) {
            wx.showToast({ title: '请输入任务名称', icon: 'none' });
            return;
        }

        request.post('/api/tasks', { title: title }).then(() => {
            // 成功后清空输入框并刷新列表
            this.setData({ inputValue: '' });
            this.loadTasks();
        });
    },

    /**
     * 切换任务完成状态
     * @param {object} e 事件对象
     */
    toggleDone(e) {
        const taskId = e.currentTarget.dataset.id;
        // dataset：wxml 里用 data-id="xxx" 传的值，在 js 里通过 e.currentTarget.dataset.id 获取
        const task = this.data.tasks.find(t => t.id === taskId);
        if (!task) return;

        request.put('/api/tasks/' + taskId, {
            done: !task.done  // 取反：原来 true 改 false，原来 false 改 true
        }).then(() => {
            this.loadTasks();
        });
    },

    /**
     * 删除任务
     * @param {object} e 事件对象
     */
    deleteTask(e) {
        const taskId = e.currentTarget.dataset.id;

        // wx.showModal：弹出确认对话框
        wx.showModal({
            title: '删除任务',
            content: '确定要删除这个任务吗？',
            success: (res) => {
                if (res.confirm) {
                    // 用户点了"确定"
                    request.del('/api/tasks/' + taskId).then(() => {
                        wx.showToast({ title: '已删除', icon: 'success' });
                        this.loadTasks();
                    });
                }
            }
        });
    },

    /**
     * 点击任务 → 进入番茄钟页
     * @param {object} e 事件对象
     */
    goToPomodoro(e) {
        const { id, title } = e.currentTarget.dataset;
        // wx.navigateTo：保留当前页，打开新页面。可以返回。
        // url 后面的 ? 是传参，和网页 GET 请求一样
        wx.navigateTo({
            url: '/pages/pomodoro/pomodoro?taskId=' + id + '&title=' + encodeURIComponent(title)
            // encodeURIComponent()：防止标题里有特殊字符（如 & = ?）导致解析错误
        });
    }
});
