-- ============================================================
-- PlanCraft 数据库初始化脚本
-- 版本: v2.0
-- 日期: 2026-06-20
-- 说明: 包含全部业务表、索引、初始数据
-- ============================================================

CREATE DATABASE IF NOT EXISTS plancraft DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE plancraft;

-- ============================================================
-- 1. 部门表
-- ============================================================
CREATE TABLE IF NOT EXISTS `department` (
    `id`          BIGINT      NOT NULL COMMENT '主键(雪花ID)',
    `name`        VARCHAR(50) NOT NULL COMMENT '部门名称',
    `parent_id`   BIGINT      DEFAULT 0   COMMENT '上级部门ID, 0=顶级',
    `leader_id`   BIGINT      DEFAULT NULL COMMENT '部门负责人ID',
    `sort_order`  INT         NOT NULL DEFAULT 0 COMMENT '排序号(越小越前)',
    `status`      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-正常',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version`     INT         NOT NULL DEFAULT 1 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ============================================================
-- 2. 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS `user` (
    `id`              BIGINT       NOT NULL COMMENT '主键(雪花ID)',
    `username`        VARCHAR(50)  NOT NULL COMMENT '登录名',
    `password`        VARCHAR(255) NOT NULL COMMENT '加密密码(BCrypt)',
    `name`            VARCHAR(50)  NOT NULL COMMENT '真实姓名',
    `role`            VARCHAR(20)  NOT NULL COMMENT '角色: EMPLOYEE-员工, LEADER-领导',
    `department_id`   BIGINT       DEFAULT NULL COMMENT '所属部门ID',
    `phone`           VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `email`           VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar`          VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态: 0-停用, 1-正常',
    `last_login_time` DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删, 1-已删',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version`         INT          NOT NULL DEFAULT 1 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    INDEX `idx_department_id` (`department_id`),
    INDEX `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 3. 汇报关系表 (领导-下属映射, 用于数据权限)
-- ============================================================
CREATE TABLE IF NOT EXISTS `user_relation` (
    `id`          BIGINT   NOT NULL COMMENT '主键(雪花ID)',
    `leader_id`   BIGINT   NOT NULL COMMENT '领导ID',
    `user_id`     BIGINT   NOT NULL COMMENT '下属ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_leader_user` (`leader_id`, `user_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='汇报关系表';

-- ============================================================
-- 4. 计划表
-- ============================================================
CREATE TABLE IF NOT EXISTS `plan` (
    `id`                BIGINT       NOT NULL COMMENT '主键(雪花ID)',
    `user_id`           BIGINT       NOT NULL COMMENT '创建员工ID',
    `type`              TINYINT      NOT NULL COMMENT '类型: 1-日计划, 2-月计划',
    `title`             VARCHAR(200) NOT NULL COMMENT '计划标题',
    `content`           TEXT         DEFAULT NULL COMMENT '计划内容(支持富文本)',
    `plan_date`         DATE         NOT NULL COMMENT '计划日期(日计划=当天, 月计划=月份第一天)',
    `status`            TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-待审批, 2-已通过, 3-已驳回, 4-已撤回',
    `reject_count`      INT          NOT NULL DEFAULT 0 COMMENT '累计驳回次数',
    `submit_time`       DATETIME     DEFAULT NULL COMMENT '最近提交时间',
    `approve_leader_id` BIGINT       DEFAULT NULL COMMENT '最近审批领导ID',
    `approve_time`      DATETIME     DEFAULT NULL COMMENT '最近审批时间',
    `approve_comment`   VARCHAR(500) DEFAULT NULL COMMENT '最近审批意见',
    `deleted`           TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删, 1-已删',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version`           INT          NOT NULL DEFAULT 1 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_plan_date` (`plan_date`),
    INDEX `idx_status` (`status`),
    INDEX `idx_type_status` (`type`, `status`),
    INDEX `idx_user_date` (`user_id`, `plan_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划表';

-- ============================================================
-- 5. 计划审批日志表 (保留每次审批/驳回的历史记录)
-- ============================================================
CREATE TABLE IF NOT EXISTS `plan_approval_log` (
    `id`          BIGINT       NOT NULL COMMENT '主键(雪花ID)',
    `plan_id`     BIGINT       NOT NULL COMMENT '计划ID',
    `operator_id` BIGINT       NOT NULL COMMENT '操作人ID',
    `action`      VARCHAR(20)  NOT NULL COMMENT '动作: SUBMIT-提交, WITHDRAW-撤回, APPROVE-通过, REJECT-驳回',
    `from_status` TINYINT      NOT NULL COMMENT '操作前状态',
    `to_status`   TINYINT      NOT NULL COMMENT '操作后状态',
    `comment`     VARCHAR(500) DEFAULT NULL COMMENT '审批意见(驳回/通过时填写)',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    INDEX `idx_plan_id` (`plan_id`),
    INDEX `idx_operator_id` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划审批日志表';

-- ============================================================
-- 6. 成果表
-- ============================================================
CREATE TABLE IF NOT EXISTS `result` (
    `id`                BIGINT       NOT NULL COMMENT '主键(雪花ID)',
    `plan_id`           BIGINT       NOT NULL COMMENT '关联计划ID(必须是已通过的计划)',
    `user_id`           BIGINT       NOT NULL COMMENT '员工ID',
    `title`             VARCHAR(200) NOT NULL COMMENT '成果标题',
    `content`           TEXT         DEFAULT NULL COMMENT '成果描述(支持富文本)',
    `attachment_url`    VARCHAR(500) DEFAULT NULL COMMENT '附件URL(多个用逗号分隔)',
    `status`            TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-待审批, 2-已通过, 3-已驳回, 4-已撤回',
    `reject_count`      INT          NOT NULL DEFAULT 0 COMMENT '累计驳回次数',
    `submit_time`       DATETIME     DEFAULT NULL COMMENT '最近提交时间',
    `approve_leader_id` BIGINT       DEFAULT NULL COMMENT '最近审批领导ID',
    `approve_time`      DATETIME     DEFAULT NULL COMMENT '最近审批时间',
    `approve_comment`   VARCHAR(500) DEFAULT NULL COMMENT '最近审批意见',
    `deleted`           TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删, 1-已删',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version`           INT          NOT NULL DEFAULT 1 COMMENT '乐观锁版本',
    PRIMARY KEY (`id`),
    INDEX `idx_plan_id` (`plan_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成果表';

-- ============================================================
-- 7. 成果审批日志表
-- ============================================================
CREATE TABLE IF NOT EXISTS `result_approval_log` (
    `id`          BIGINT       NOT NULL COMMENT '主键(雪花ID)',
    `result_id`   BIGINT       NOT NULL COMMENT '成果ID',
    `operator_id` BIGINT       NOT NULL COMMENT '操作人ID',
    `action`      VARCHAR(20)  NOT NULL COMMENT '动作: SUBMIT-提交, WITHDRAW-撤回, APPROVE-通过, REJECT-驳回',
    `from_status` TINYINT      NOT NULL COMMENT '操作前状态',
    `to_status`   TINYINT      NOT NULL COMMENT '操作后状态',
    `comment`     VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    INDEX `idx_result_id` (`result_id`),
    INDEX `idx_operator_id` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成果审批日志表';

-- ============================================================
-- 8. 通知表
-- ============================================================
CREATE TABLE IF NOT EXISTS `notification` (
    `id`            BIGINT       NOT NULL COMMENT '主键(雪花ID)',
    `user_id`       BIGINT       NOT NULL COMMENT '接收人ID',
    `title`         VARCHAR(100) NOT NULL COMMENT '通知标题',
    `content`       VARCHAR(500) NOT NULL COMMENT '通知内容',
    `type`          VARCHAR(30)  NOT NULL COMMENT '通知类型: PLAN_SUBMITTED-计划待审, PLAN_APPROVED-计划通过, PLAN_REJECTED-计划驳回, RESULT_SUBMITTED-成果待审, RESULT_APPROVED-成果通过, RESULT_REJECTED-成果驳回',
    `related_id`    BIGINT       DEFAULT NULL COMMENT '关联业务ID(计划ID或成果ID)',
    `related_type`  VARCHAR(10)  DEFAULT NULL COMMENT '关联业务类型: PLAN-计划, RESULT-成果',
    `is_read`       TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    `read_time`     DATETIME     DEFAULT NULL COMMENT '阅读时间',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_read` (`user_id`, `is_read`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ============================================================
-- 初始数据
-- ============================================================

-- 默认部门
INSERT INTO `department` (`id`, `name`, `parent_id`, `sort_order`) VALUES
(1, '总经办', 0, 1),
(2, '技术部', 1, 2),
(3, '产品部', 1, 3),
(4, '运营部', 1, 4);

-- 默认管理员(密码: admin123, 项目启动时由 DataInitializer 自动 BCrypt 加密)
INSERT INTO `user` (`id`, `username`, `password`, `name`, `role`, `department_id`, `status`) VALUES
(1, 'admin', 'admin123', '系统管理员', 'LEADER', 1, 1);

-- 测试领导(密码: leader123)
INSERT INTO `user` (`id`, `username`, `password`, `name`, `role`, `department_id`, `status`) VALUES
(2, 'leader1', 'leader123', '张经理', 'LEADER', 2, 1);

-- 测试同事(密码: emp123)
INSERT INTO `user` (`id`, `username`, `password`, `name`, `role`, `department_id`, `status`) VALUES
(3, 'emp1', 'emp123', '李明', 'EMPLOYEE', 2, 1),
(4, 'emp2', 'emp123', '王华', 'EMPLOYEE', 2, 1);

-- 汇报关系: leader1 管 emp1, emp2
INSERT INTO `user_relation` (`id`, `leader_id`, `user_id`) VALUES
(1, 2, 3),
(2, 2, 4);
