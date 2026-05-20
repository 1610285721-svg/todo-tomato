package org.example.todotomato.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 番茄钟记录实体类 —— 对应数据库 pomodoro_sessions 表
 * <p>
 * 每完成一个番茄钟（无论工作还是休息），就插入一条记录。
 * 用于后续统计分析（比如"本周总共专注了多少小时"）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PomodoroSession {

    private Long id;            // 记录 ID，数据库自增主键
    private Long userId;        // 属于哪个用户
    private Long taskId;        // 关联哪个任务
    private String type;        // 类型：work = 工作番茄钟，break = 休息
    private Integer duration;   // 时长（秒），比如 25 分钟 = 1500 秒
    private LocalDateTime createdAt;  // 完成时间
}
