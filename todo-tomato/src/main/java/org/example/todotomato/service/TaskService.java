package org.example.todotomato.service;

import org.example.todotomato.entity.PomodoroSession;
import org.example.todotomato.entity.Task;
import org.example.todotomato.mapper.PomodoroSessionMapper;
import org.example.todotomato.mapper.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务业务层 —— 处理任务的增删改查、番茄钟计数、统计
 */
@Service
public class TaskService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private PomodoroSessionMapper sessionMapper;

    // ==================== 任务 CRUD ====================

    /**
     * 获取用户的所有任务
     * @param userId 用户 ID
     * @return 任务列表（按创建时间倒序）
     */
    public List<Task> getTasks(Long userId) {
        return taskMapper.findByUserId(userId);
    }

    /**
     * 创建任务
     * @param userId 用户 ID
     * @param title  任务标题
     * @return 创建好的任务对象（含自增 ID）
     */
    public Task createTask(Long userId, String title) {
        Task task = new Task();
        task.setUserId(userId);
        task.setTitle(title);
        // pomodoroCount 和 done 在 XML 的 INSERT SQL 里设了默认值 0，这里不用设
        taskMapper.insert(task);
        return task;  // 插入后 task.id 已被 MyBatis 自动回填
    }

    /**
     * 更新任务（标题 / 是否完成）
     * @param userId 当前登录用户 ID（用于权限校验）
     * @param taskId 要更新的任务 ID
     * @param title  新标题（null 表示不改）
     * @param done   是否完成（null 表示不改）
     */
    public Task updateTask(Long userId, Long taskId, String title, Boolean done) {
        // 查出来校验权限：只能改自己的任务
        Task task = checkOwnership(userId, taskId);

        if (title != null) {
            task.setTitle(title);
        }
        if (done != null) {
            task.setDone(done);
        }

        taskMapper.update(task);
        return task;
    }

    /**
     * 删除任务
     * @param userId 当前登录用户 ID（用于权限校验）
     * @param taskId 要删除的任务 ID
     */
    public void deleteTask(Long userId, Long taskId) {
        checkOwnership(userId, taskId);  // 校验权限：只能删自己的任务
        taskMapper.deleteById(taskId);
    }

    /**
     * 完成一个番茄钟
     * 两件事：
     * 1. 任务的 pomodoro_count + 1
     * 2. 插入一条番茄钟记录（用于后续统计）
     *
     * @param userId   当前登录用户 ID
     * @param taskId   关联的任务 ID
     * @param type     类型：work = 工作，break = 休息
     * @param duration 专注时长（秒）
     * @return 更新后的任务对象
     */
    public Task completePomodoro(Long userId, Long taskId, String type, Integer duration) {
        // 先检验是不是自己的任务
        checkOwnership(userId, taskId);

        // 操作1：任务番茄数 +1
        taskMapper.incrementPomodoroCount(taskId);

        // 操作2：记录番茄钟完成记录
        PomodoroSession session = new PomodoroSession();
        session.setUserId(userId);
        session.setTaskId(taskId);
        session.setType(type != null ? type : "work");          // 默认工作番茄钟
        session.setDuration(duration != null ? duration : 1500); // 默认 25 分钟 = 1500 秒
        sessionMapper.insert(session);

        // 返回更新后的任务（重新查一次，拿到最新的 pomodoro_count）
        return taskMapper.findById(taskId);
    }

    /**
     * 获取统计数据
     * @param userId 用户 ID
     * @return Map，包含 totalPomodoros、totalMinutes、taskCount、doneCount
     */
    public Map<String, Object> getStats(Long userId) {
        List<Task> tasks = taskMapper.findByUserId(userId);
        int totalPomodoros = sessionMapper.countByUserId(userId);
        // sumDurationByUserId 返回的是 Integer（可为 null），用三元运算防 NPE
        Integer secondsObj = sessionMapper.sumDurationByUserId(userId);
        int totalSeconds = secondsObj != null ? secondsObj : 0;

        // 计算已完成任务数
        long doneCount = tasks.stream().filter(Task::getDone).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPomodoros", totalPomodoros);
        stats.put("totalMinutes", totalSeconds / 60);
        stats.put("taskCount", tasks.size());
        stats.put("doneCount", (int) doneCount);
        return stats;
    }

    // ==================== 内部工具方法 ====================

    /**
     * 校验任务是否属于当前用户
     * 防止用户 A 通过改 URL 里的任务 ID 来操作用户 B 的任务
     *
     * @param userId 当前登录用户 ID
     * @param taskId 任务 ID
     * @return 任务对象（顺便返回，调用方可以直接用）
     * @throws RuntimeException 任务不存在或不属于当前用户
     */
    private Task checkOwnership(Long userId, Long taskId) {
        Task task = taskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!task.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此任务");
        }
        return task;
    }
}
