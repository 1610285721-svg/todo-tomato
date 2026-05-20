package org.example.todotomato.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.todotomato.entity.PomodoroSession;

/**
 * 番茄钟记录 Mapper 接口 —— 对应 pomodoro_sessions 表
 * <p>
 * 这张表只做"记录"和"统计查询"，不删不改，所以只有 insert 和统计方法。
 */
@Mapper
public interface PomodoroSessionMapper {

    /**
     * 插入一条番茄钟完成记录
     * @param session 记录对象
     */
    void insert(PomodoroSession session);

    /**
     * 统计某个用户总共完成了多少个番茄钟
     * @param userId 用户 ID
     * @return 总数
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 统计某个用户总共专注了多少秒
     * @param userId 用户 ID
     * @return 总秒数，没记录时返回 null（MyBatis 会返回 0 因为 int 默认值）
     */
    Integer sumDurationByUserId(@Param("userId") Long userId);
}
