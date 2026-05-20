-- 番茄土豆 数据库初始化脚本
-- 在 MySQL 中创建数据库后执行

CREATE DATABASE IF NOT EXISTS todo_tomato CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE todo_tomato;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    openid VARCHAR(64) NOT NULL UNIQUE COMMENT '微信openid',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    nickname VARCHAR(64) DEFAULT '微信用户' COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 任务表
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(200) NOT NULL COMMENT '任务标题',
    pomodoro_count INT DEFAULT 0 COMMENT '已完成番茄数',
    done TINYINT DEFAULT 0 COMMENT '是否完成 0否1是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 番茄钟记录表
CREATE TABLE IF NOT EXISTS pomodoro_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    type VARCHAR(10) DEFAULT 'work' COMMENT 'work/break',
    duration INT DEFAULT 1500 COMMENT '时长(秒)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '完成时间',
    INDEX idx_user_id (user_id),
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='番茄钟记录表';
