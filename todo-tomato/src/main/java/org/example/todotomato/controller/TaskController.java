package org.example.todotomato.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.todotomato.dto.ApiResponse;
import org.example.todotomato.entity.Task;
import org.example.todotomato.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务控制器 —— 处理任务的增删改查、番茄钟、统计
 * <p>
 * 所有接口都在 /api/tasks 下，全部需要登录（被 AuthInterceptor 拦截保护）。
 * 通过拦截器后，可以从 request.getAttribute("userId") 拿到当前用户 ID。
 */
@RestController
@RequestMapping("/api")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 工具方法：从 request 中取出当前登录用户的 ID
     * 这个 userId 是 AuthInterceptor 校验完 token 后放进去的
     */
    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    // ==================== 任务 CRUD ====================

    /**
     * 获取任务列表
     * <p>
     * @GetMapping：只接受 GET 请求。
     * 前端：wx.request({ method: 'GET', url: '/api/tasks' })
     */
    @GetMapping("/tasks")
    public ApiResponse<List<Task>> getTasks(HttpServletRequest request) {
        Long userId = getUserId(request);
        List<Task> tasks = taskService.getTasks(userId);
        return ApiResponse.success(tasks);
    }

    /**
     * 创建任务
     * <p>
     * @RequestBody Map<String, String> body：
     * 用 Map 来接收 JSON，更灵活，不用为每个简单参数单独建 DTO。
     * 前端传过来 {"title": "复习面试题"}，Spring 自动转成 Map。
     */
    @PostMapping("/tasks")
    public ApiResponse<Task> createTask(HttpServletRequest request,
                                         @RequestBody Map<String, String> body) {
        String title = body.get("title");
        // 参数校验
        if (title == null || title.trim().isEmpty()) {
            return ApiResponse.error(400, "任务标题不能为空");
        }

        Long userId = getUserId(request);
        Task task = taskService.createTask(userId, title.trim());
        return ApiResponse.success("创建成功", task);
    }

    /**
     * 更新任务
     * <p>
     * @PathVariable：从 URL 路径中取参数。
     * 比如请求 PUT /api/tasks/5，则 taskId = 5。
     */
    @PutMapping("/tasks/{id}")
    public ApiResponse<Task> updateTask(HttpServletRequest request,
                                         @PathVariable("id") Long taskId,
                                         @RequestBody Map<String, Object> body) {
        Long userId = getUserId(request);
        String title = (String) body.get("title");
        Boolean done = body.get("done") != null ? (Boolean) body.get("done") : null;

        try {
            Task task = taskService.updateTask(userId, taskId, title, done);
            return ApiResponse.success("更新成功", task);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 删除任务
     * <p>
     * @DeleteMapping：只接受 DELETE 请求。
     */
    @DeleteMapping("/tasks/{id}")
    public ApiResponse<Void> deleteTask(HttpServletRequest request,
                                         @PathVariable("id") Long taskId) {
        Long userId = getUserId(request);
        try {
            taskService.deleteTask(userId, taskId);
            return ApiResponse.success("删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 完成一个番茄钟
     * <p>
     * 前端在番茄钟倒计时结束后调用这个接口。
     * 请求体：{"duration": 1500, "type": "work"}
     * - duration: 专注秒数（可选，默认 1500 = 25 分钟）
     * - type: 类型（可选，默认 "work"）
     */
    @PostMapping("/tasks/{id}/pomodoro")
    public ApiResponse<Task> completePomodoro(HttpServletRequest request,
                                               @PathVariable("id") Long taskId,
                                               @RequestBody Map<String, Object> body) {
        Long userId = getUserId(request);
        String type = (String) body.getOrDefault("type", "work");
        Integer duration = body.get("duration") != null
                ? ((Number) body.get("duration")).intValue()
                : 1500;  // 默认 25 分钟

        try {
            Task task = taskService.completePomodoro(userId, taskId, type, duration);
            return ApiResponse.success("番茄钟 +1", task);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats(HttpServletRequest request) {
        Long userId = getUserId(request);
        Map<String, Object> stats = taskService.getStats(userId);
        return ApiResponse.success(stats);
    }
}
