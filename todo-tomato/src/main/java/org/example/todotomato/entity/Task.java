package org.example.todotomato.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务实体类 —— 对应数据库 tasks 表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    private Long id;            // 任务 ID，数据库自增主键
    private Long userId;        // 属于哪个用户（关联 users 表的 id）
    private String title;       // 任务标题
    private Integer pomodoroCount;  // 已完成的番茄钟数量，默认 0
    private Boolean done;       // 是否完成，默认 false
    private LocalDateTime createdAt;  // 创建时间
}
