package org.example.todotomato.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.todotomato.entity.Task;

import java.util.List;

/**
 * 任务 Mapper 接口 —— 对应 tasks 表的数据库操作
 */
@Mapper
public interface TaskMapper {

    /**
     * 查某个用户的所有任务
     * @param userId 用户 ID
     * @return 该用户的任务列表（按创建时间倒序，最新的在上面）
     */
    List<Task> findByUserId(@Param("userId") Long userId);

    /**
     * 根据任务 ID 查单个任务
     * @param id 任务 ID
     * @return 查到返回 Task，查不到返回 null
     */
    Task findById(@Param("id") Long id);

    /**
     * 新增任务
     * @param task 任务对象
     *             useGeneratedKeys="true" 表示插入后自动回填自增 ID 到 task.id
     */
    void insert(Task task);

    /**
     * 更新任务（标题 / 是否完成）
     * @param task 任务对象，只更新非空字段
     */
    void update(Task task);

    /**
     * 删除任务
     * @param id 任务 ID
     */
    void deleteById(@Param("id") Long id);

    /**
     * 番茄钟计数 +1
     * 为什么不直接 update set count = count + 1？
     * 因为这样是原子操作，不会出现并发问题（两个番茄钟同时完成不会少算）
     * @param taskId 任务 ID
     */
    void incrementPomodoroCount(@Param("taskId") Long taskId);
}
