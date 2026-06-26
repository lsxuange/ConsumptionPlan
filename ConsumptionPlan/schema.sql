-- ============================================================
-- ConsumptionPlan Database Schema
-- MySQL 8 + utf8mb4 + InnoDB
-- ============================================================

CREATE DATABASE IF NOT EXISTS ConsumptionPlan
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE ConsumptionPlan;

-- ============================================================
-- 1. user
-- ============================================================
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `username`    VARCHAR(50)  NOT NULL,
    `email`       VARCHAR(100) NOT NULL,
    `password`    VARCHAR(255) NOT NULL,
    `avatar`      VARCHAR(255) NULL,
    `status`      TINYINT      NOT NULL DEFAULT 1  COMMENT '1正常 0禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. category
-- ============================================================
CREATE TABLE `category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(50)  NOT NULL,
    `type`        TINYINT      NOT NULL DEFAULT 0  COMMENT '0支出 1收入',
    `icon`        VARCHAR(100) NULL,
    `sort_num`    INT          NOT NULL DEFAULT 0,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. expense_record
-- ============================================================
CREATE TABLE `expense_record` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT         NOT NULL,
    `category_id` BIGINT         NOT NULL,
    `type`        TINYINT        NOT NULL DEFAULT 0  COMMENT '0支出 1收入',
    `amount`      DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    `remark`      VARCHAR(255)   NULL,
    `record_date` DATE           NOT NULL,
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_date` (`user_id`, `record_date`),
    INDEX `idx_user_category` (`user_id`, `category_id`),
    INDEX `idx_user_type` (`user_id`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. budget
-- ============================================================
CREATE TABLE `budget` (
    `id`           BIGINT         NOT NULL AUTO_INCREMENT,
    `user_id`      BIGINT         NOT NULL,
    `budget_month` VARCHAR(7)     NOT NULL COMMENT '格式 2026-06',
    `amount`       DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    `create_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_month` (`user_id`, `budget_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. ai_report
-- ============================================================
CREATE TABLE `ai_report` (
    `id`             BIGINT         NOT NULL AUTO_INCREMENT,
    `user_id`        BIGINT         NOT NULL,
    `report_month`   VARCHAR(7)     NOT NULL,
    `total_income`   DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    `total_expense`  DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    `budget_amount`  DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    `report_content` TEXT           NOT NULL,
    `create_time`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 6. feedback
-- ============================================================
CREATE TABLE `feedback` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT        NOT NULL,
    `content`     VARCHAR(1000) NOT NULL,
    `status`      TINYINT       NOT NULL DEFAULT 0  COMMENT '0未处理 1已处理',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 7. notification
-- ============================================================
CREATE TABLE `notification` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT        NOT NULL,
    `title`       VARCHAR(100)  NOT NULL,
    `content`     VARCHAR(1000) NOT NULL,
    `is_read`     TINYINT       NOT NULL DEFAULT 0  COMMENT '0未读 1已读',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 8. check_in_record
-- ============================================================
CREATE TABLE `check_in_record` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT   NOT NULL,
    `check_date`  DATE     NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `check_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Initial category data
-- ============================================================
INSERT INTO `category` (`name`, `type`, `sort_num`) VALUES
('餐饮', 0, 1),
('购物', 0, 2),
('交通', 0, 3),
('娱乐', 0, 4),
('学习', 0, 5),
('医疗', 0, 6),
('住房', 0, 7),
('其他', 0, 8),
('工资', 1, 1),
('兼职', 1, 2),
('奖学金', 1, 3),
('投资', 1, 4),
('红包', 1, 5),
('其他', 1, 6);
