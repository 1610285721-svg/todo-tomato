/**
 * 番茄钟页 —— 逻辑层
 * <p>
 * 核心逻辑：倒计时 → 结束震动 → 回写后端
 * <p>
 * 两种模式：
 * - work（工作）：默认 25 分钟 = 1500 秒
 * - break（休息）：默认 5 分钟 = 300 秒
 * <p>
 * 页面生命周期要点：
 * - onUnload：页面销毁时 MUST 清除 setInterval，不然定时器一直跑
 * - onHide：页面隐藏时不暂停（用户可以切出去回微信，番茄钟继续）
 */
const request = require('../../utils/request');

// 常量配置（调试时可以改短，比如 10 秒确认流程）
const WORK_SECONDS = 25 * 60;    // 工作番茄钟：25 分钟
const BREAK_SECONDS = 5 * 60;    // 休息番茄钟：5 分钟

// 格式化时间显示
function formatTime(seconds) {
    const min = Math.floor(seconds / 60);
    const sec = seconds % 60;
    // padStart(2, '0')：补零，比如 5:03 而不是 5:3
    return min + ':' + String(sec).padStart(2, '0');
}

Page({
    data: {
        taskId: null,           // 从上一页传来的任务 ID
        taskTitle: '',          // 从上一页传来的任务标题

        // 倒计时相关
        mode: 'work',           // 当前模式：work 或 break
        totalSeconds: WORK_SECONDS,   // 当前模式总秒数
        remainingSeconds: WORK_SECONDS, // 剩余秒数
        displayTime: formatTime(WORK_SECONDS), // 显示用的 "25:00"
        progress: 1,            // 进度条比例（1 = 满，0 = 空）

        // 按钮状态
        status: 'idle',         // idle（初始）、running（计时中）、paused（暂停）

        // 内部数据（不渲染，放 data 外拿不到但可以放 this 上）
        timerId: null           // setInterval 返回的 ID，清除时用
    },

    /**
     * 页面加载时执行（只执行一次）
     * 接收从任务列表页传来的 taskId 和 title
     * @param {object} options URL 上的参数，即 ?taskId=xxx&title=xxx
     */
    onLoad(options) {
        const taskId = parseInt(options.taskId);  // URL 参数都是字符串，转成数字
        const title = decodeURIComponent(options.title || ''); // 解码回中文
        this.setData({
            taskId: taskId,
            taskTitle: title
        });

        // 设置导航栏标题为任务名
        wx.setNavigationBarTitle({ title: title || '番茄钟' });
    },

    /**
     * 页面销毁时执行（跳走或返回）
     * 必须清除定时器！否则 setInterval 会一直在后台跑
     */
    onUnload() {
        this.clearTimer();
    },

    // ==================== 计时器控制 ====================

    /**
     * 开始计时
     * setInterval：每隔 1000ms（1 秒）执行一次 tick 方法
     */
    startTimer() {
        // 如果已经在运行，不重复创建
        if (this.timerId) return;

        this.setData({ status: 'running' });

        // setInterval 返回一个数字 ID，存起来方便后面清除
        this.timerId = setInterval(() => {
            this.tick();
        }, 1000);
    },

    /**
     * 暂停计时
     * clearInterval：停止 setInterval 创建的定时器
     */
    pauseTimer() {
        this.clearTimer();  // 清除定时器
        this.setData({ status: 'paused' });
    },

    /**
     * 重置计时器
     */
    resetTimer() {
        this.clearTimer();
        const seconds = this.data.mode === 'work' ? WORK_SECONDS : BREAK_SECONDS;
        this.setData({
            status: 'idle',
            remainingSeconds: seconds,
            totalSeconds: seconds,
            displayTime: formatTime(seconds),
            progress: 1
        });
    },

    /**
     * 清除定时器（内部方法，不被 wxml 直接调用）
     */
    clearTimer() {
        if (this.timerId) {
            clearInterval(this.timerId);
            this.timerId = null;
        }
    },

    /**
     * 每秒执行一次
     * 减少 1 秒，更新时间显示和进度条
     */
    tick() {
        let remaining = this.data.remainingSeconds - 1;

        if (remaining <= 0) {
            // 倒计时结束！
            this.clearTimer();
            this.setData({
                remainingSeconds: 0,
                displayTime: '0:00',
                progress: 0,
                status: 'idle'
            });

            this.onPomodoroFinished();
            return;
        }

        // 还剩时间，更新显示
        const total = this.data.totalSeconds;
        this.setData({
            remainingSeconds: remaining,
            displayTime: formatTime(remaining),
            progress: (remaining / total).toFixed(2)  // 进度条比例，保留 2 位小数
        });
    },

    // ==================== 番茄钟完成 ====================

    /**
     * 番茄钟倒计时结束
     * 两件事：1.震一下提醒  2.如果是工作番茄钟，调后端 API
     */
    onPomodoroFinished() {
        // 第一步：震动提醒（700ms，够明显但不至于太猛）
        wx.vibrateLong();

        // 第二步：弹窗告知用户
        const modeText = this.data.mode === 'work' ? '工作' : '休息';
        wx.showModal({
            title: modeText + '时间结束！',
            content: this.data.mode === 'work'
                ? '太棒了！休息一下吧～'
                : '休息完了，开始干活吧！',
            showCancel: false,       // 只显示确定按钮
            confirmText: '好的',
            success: () => {
                // 第三步：如果是工作番茄钟，向后台记录
                if (this.data.mode === 'work') {
                    this.savePomodoro();
                }

                // 第四步：切换到另一种模式
                this.switchMode();
            }
        });
    },

    /**
     * 调后端接口：番茄钟 +1
     */
    savePomodoro() {
        request.post('/api/tasks/' + this.data.taskId + '/pomodoro', {
            type: this.data.mode,                 // "work"
            duration: this.data.totalSeconds      // 实际专注秒数
        }).then((res) => {
            console.log('番茄钟已记录，当前任务番茄数:', res.data.pomodoroCount);
        }).catch((err) => {
            console.error('记录番茄钟失败:', err);
        });
    },

    /**
     * 切换模式：工作 → 休息，休息 → 工作
     */
    switchMode() {
        const newMode = this.data.mode === 'work' ? 'break' : 'work';
        const seconds = newMode === 'work' ? WORK_SECONDS : BREAK_SECONDS;
        this.setData({
            mode: newMode,
            totalSeconds: seconds,
            remainingSeconds: seconds,
            displayTime: formatTime(seconds),
            progress: 1,
            status: 'idle'
        });
    }
});
