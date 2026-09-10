-- ============================================================
-- 武侯祠游客服务中心 数据库初始化脚本
-- Wuhou Shrine Tourist Service Center DB Init
-- Engine: InnoDB | Charset: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS tourist_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tourist_service;

-- 幂等重建：先关闭外键检查，避免 DROP 表受外键引用阻塞
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------
-- 1. sys_user - 系统用户表（角色: TOURIST/PLATFORM_ADMIN/APPROVER/COMPLAINT_HANDLER/HOTEL_ADMIN）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `role` VARCHAR(20) NOT NULL COMMENT '角色: TOURIST/PLATFORM_ADMIN/APPROVER/COMPLAINT_HANDLER/HOTEL_ADMIN',
    `college` VARCHAR(100) DEFAULT NULL COMMENT '所属学院',
    `student_no` VARCHAR(50) DEFAULT NULL COMMENT '学号',
    `teacher_no` VARCHAR(50) DEFAULT NULL COMMENT '教师工号',
    `gpa` DECIMAL(4,2) DEFAULT NULL COMMENT '绩点GPA',
    `grade_score` DECIMAL(5,2) DEFAULT NULL COMMENT '综合成绩',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `department_id` BIGINT DEFAULT NULL COMMENT '所属部门ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role` (`role`),
    KEY `idx_college` (`college`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- -----------------------------------------------------------
-- 2. approval_flow - 审批流程表（主体泛化为 subject_type/subject_id）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `approval_flow`;
CREATE TABLE `approval_flow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流程ID',
    `subject_type` VARCHAR(30) NOT NULL COMMENT '主体类型: COMPLAINT',
    `subject_id` BIGINT NOT NULL COMMENT '主体ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_flow_subject` (`subject_type`, `subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批流程表';

-- -----------------------------------------------------------
-- 3. approval_node - 审批节点表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `approval_node`;
CREATE TABLE `approval_node` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '节点ID',
    `flow_id` BIGINT NOT NULL COMMENT '流程ID',
    `node_name` VARCHAR(100) NOT NULL COMMENT '节点名称',
    `approver_role` VARCHAR(20) NOT NULL COMMENT '审批人角色: APPROVER',
    `sort_order` INT NOT NULL COMMENT '审批顺序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_node_order` (`flow_id`, `sort_order`),
    CONSTRAINT `fk_node_flow` FOREIGN KEY (`flow_id`) REFERENCES `approval_flow` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批节点表';

-- -----------------------------------------------------------
-- 4. approval_record - 审批记录表（主体泛化，无硬外键到主表）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `approval_record`;
CREATE TABLE `approval_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `subject_type` VARCHAR(30) NOT NULL COMMENT '主体类型: COMPLAINT',
    `subject_id` BIGINT NOT NULL COMMENT '主体ID',
    `node_id` BIGINT DEFAULT NULL COMMENT '审批节点ID',
    `approver_id` BIGINT NOT NULL COMMENT '审批人用户ID',
    `action` VARCHAR(20) NOT NULL COMMENT '操作: APPROVE/REJECT',
    `comment` TEXT COMMENT '审批意见',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_ar_subject` (`subject_type`, `subject_id`),
    CONSTRAINT `fk_ar_node` FOREIGN KEY (`node_id`) REFERENCES `approval_node` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_ar_approver` FOREIGN KEY (`approver_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批记录表';

-- -----------------------------------------------------------
-- 5. notification - 通知消息表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
    `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content` TEXT COMMENT '通知内容',
    `type` VARCHAR(50) DEFAULT 'SYSTEM' COMMENT '类型',
    `related_id` BIGINT COMMENT '关联业务ID',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_read` (`user_id`, `is_read`),
    INDEX `idx_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知消息表';

-- -----------------------------------------------------------
-- 6. file_attachment - 文件附件表（多态：related_type/related_id）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `file_attachment`;
CREATE TABLE `file_attachment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '附件ID',
    `related_type` VARCHAR(30) DEFAULT '' COMMENT '关联类型: COMPLAINT/GENERAL',
    `related_id` BIGINT DEFAULT NULL COMMENT '关联ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
    `file_size` BIGINT NOT NULL COMMENT '文件大小(字节)',
    `file_ext` VARCHAR(20) DEFAULT NULL COMMENT '文件扩展名',
    `uploader_id` BIGINT NOT NULL COMMENT '上传者用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_fa_related` (`related_type`, `related_id`),
    CONSTRAINT `fk_fa_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件附件表';

-- -----------------------------------------------------------
-- 7. operation_log - 操作日志表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT COMMENT '操作用户ID',
    `username` VARCHAR(50) COMMENT '操作用户名',
    `module` VARCHAR(50) COMMENT '模块',
    `action` VARCHAR(50) COMMENT '操作',
    `target_id` BIGINT COMMENT '目标对象ID',
    `detail` VARCHAR(500) COMMENT '操作描述',
    `ip` VARCHAR(50) COMMENT '操作IP',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_module_time` (`module`, `create_time`),
    INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- -----------------------------------------------------------
-- 8. tourist_complaint - 游客投诉主表
--    状态: PENDING/APPROVED/REJECTED/PROCESSING/RESOLVED/CONFIRMED/CLOSED
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `tourist_complaint`;
CREATE TABLE `tourist_complaint` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '投诉ID',
    `user_id` BIGINT NOT NULL COMMENT '投诉用户ID',
    `content` TEXT NOT NULL COMMENT '投诉内容',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/APPROVED/REJECTED/PROCESSING/RESOLVED/CONFIRMED/CLOSED',
    `is_published` TINYINT DEFAULT 0 COMMENT '是否对外发布: 1是 0否',
    `handler_id` BIGINT DEFAULT NULL COMMENT '处理人员ID',
    `result` TEXT COMMENT '处理结果',
    `rating` INT DEFAULT NULL COMMENT '评价打分 1-5',
    `current_node_id` BIGINT DEFAULT NULL COMMENT '当前审批节点ID',
    `assign_time` DATETIME DEFAULT NULL COMMENT '分派时间',
    `process_time` DATETIME DEFAULT NULL COMMENT '处理完成时间',
    `confirm_time` DATETIME DEFAULT NULL COMMENT '游客确认时间',
    `close_time` DATETIME DEFAULT NULL COMMENT '结案时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 1是 0否',
    PRIMARY KEY (`id`),
    KEY `idx_c_status` (`status`),
    KEY `idx_c_user` (`user_id`),
    KEY `idx_c_handler` (`handler_id`),
    CONSTRAINT `fk_c_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_c_handler` FOREIGN KEY (`handler_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_c_node` FOREIGN KEY (`current_node_id`) REFERENCES `approval_node` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游客投诉主表';

-- -----------------------------------------------------------
-- 9. complaint_reply - 投诉回复表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `complaint_reply`;
CREATE TABLE `complaint_reply` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '回复ID',
    `complaint_id` BIGINT NOT NULL COMMENT '投诉ID',
    `user_id` BIGINT NOT NULL COMMENT '回复用户ID',
    `content` TEXT NOT NULL COMMENT '回复内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_reply_complaint` (`complaint_id`),
    CONSTRAINT `fk_reply_complaint` FOREIGN KEY (`complaint_id`) REFERENCES `tourist_complaint` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_reply_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投诉回复表';

-- -----------------------------------------------------------
-- 10. emergency_info - 旅游应急信息表
--    状态: PENDING/APPROVED/REJECTED
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `emergency_info`;
CREATE TABLE `emergency_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '信息ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT COMMENT '内容',
    `valid_from` DATE DEFAULT NULL COMMENT '有效开始日期',
    `valid_to` DATE DEFAULT NULL COMMENT '有效结束日期',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/APPROVED/REJECTED',
    `publisher_id` BIGINT NOT NULL COMMENT '发布人员ID',
    `publish_time` DATETIME DEFAULT NULL COMMENT '审批通过发布时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 1是 0否',
    PRIMARY KEY (`id`),
    KEY `idx_ei_status` (`status`),
    KEY `idx_ei_valid` (`valid_from`, `valid_to`),
    CONSTRAINT `fk_ei_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='旅游应急信息表';

-- -----------------------------------------------------------
-- 11. attraction - 旅游景区（点）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `attraction`;
CREATE TABLE `attraction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '景点ID',
    `name` VARCHAR(200) NOT NULL COMMENT '名称',
    `type` VARCHAR(50) DEFAULT NULL COMMENT '类型',
    `address` VARCHAR(200) DEFAULT NULL COMMENT '地址',
    `thumb_id` BIGINT DEFAULT NULL COMMENT '封面附件ID',
    `intro` TEXT COMMENT '简介',
    `open_time` VARCHAR(100) DEFAULT NULL COMMENT '开放时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_at_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='旅游景区（点）表';

-- -----------------------------------------------------------
-- 12. tour_route - 旅游线路
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `tour_route`;
CREATE TABLE `tour_route` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '线路ID',
    `name` VARCHAR(200) NOT NULL COMMENT '线路名称',
    `attraction_ids` VARCHAR(500) DEFAULT NULL COMMENT '包含景点ID（逗号分隔）',
    `duration` VARCHAR(50) DEFAULT NULL COMMENT '建议时长',
    `intro` TEXT COMMENT '简介',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_route_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='旅游线路表';

-- -----------------------------------------------------------
-- 13. catering - 餐饮娱乐信息
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `catering`;
CREATE TABLE `catering` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '餐饮ID',
    `name` VARCHAR(200) NOT NULL COMMENT '名称',
    `type` VARCHAR(50) DEFAULT NULL COMMENT '类型(餐饮/娱乐)',
    `address` VARCHAR(200) DEFAULT NULL COMMENT '地址',
    `price` VARCHAR(50) DEFAULT NULL COMMENT '参考价格',
    `thumb_id` BIGINT DEFAULT NULL COMMENT '封面附件ID',
    `intro` TEXT COMMENT '简介',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_catering_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='餐饮娱乐信息表';

-- -----------------------------------------------------------
-- 14. performance_group - 营业性演出团体
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `performance_group`;
CREATE TABLE `performance_group` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '团体ID',
    `name` VARCHAR(200) NOT NULL COMMENT '团体名称',
    `type` VARCHAR(50) DEFAULT NULL COMMENT '类型',
    `address` VARCHAR(200) DEFAULT NULL COMMENT '地址',
    `intro` TEXT COMMENT '简介',
    `contact` VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_pg_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='营业性演出团体表';

-- -----------------------------------------------------------
-- 15. transport_info - 景区交通
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `transport_info`;
CREATE TABLE `transport_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '交通ID',
    `name` VARCHAR(200) NOT NULL COMMENT '名称',
    `type` VARCHAR(50) DEFAULT NULL COMMENT '类型(观光车/交通车/停车场/接驳站)',
    `area` VARCHAR(100) DEFAULT NULL COMMENT '区域',
    `start` VARCHAR(100) DEFAULT NULL COMMENT '起点',
    `end` VARCHAR(100) DEFAULT NULL COMMENT '终点',
    `schedule` VARCHAR(200) DEFAULT NULL COMMENT '班次/时刻',
    `price` VARCHAR(50) DEFAULT NULL COMMENT '价格',
    `intro` TEXT COMMENT '简介',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_tr_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='景区交通信息表';

-- -----------------------------------------------------------
-- 16. star_hotel - 星级酒店
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `star_hotel`;
CREATE TABLE `star_hotel` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '酒店ID',
    `name` VARCHAR(200) NOT NULL COMMENT '酒店名称',
    `level` VARCHAR(20) DEFAULT NULL COMMENT '星级',
    `address` VARCHAR(200) DEFAULT NULL COMMENT '地址',
    `tel` VARCHAR(50) DEFAULT NULL COMMENT '电话',
    `thumb_id` BIGINT DEFAULT NULL COMMENT '封面附件ID',
    `intro` TEXT COMMENT '简介',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_sh_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='星级酒店表';

-- -----------------------------------------------------------
-- 17. nonstar_hotel - 非星级/乡村旅游酒店
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `nonstar_hotel`;
CREATE TABLE `nonstar_hotel` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '酒店ID',
    `name` VARCHAR(200) NOT NULL COMMENT '酒店名称',
    `type` VARCHAR(50) DEFAULT NULL COMMENT '类型(非星级/乡村旅游)',
    `address` VARCHAR(200) DEFAULT NULL COMMENT '地址',
    `tel` VARCHAR(50) DEFAULT NULL COMMENT '电话',
    `intro` TEXT COMMENT '简介',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_nh_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='非星级/乡村旅游酒店表';

-- -----------------------------------------------------------
-- 18. hotel_room - 酒店房间预订实时信息
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `hotel_room`;
CREATE TABLE `hotel_room` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '房间ID',
    `hotel_id` BIGINT NOT NULL COMMENT '酒店ID',
    `hotel_type` VARCHAR(20) NOT NULL COMMENT '酒店类型(STAR/NONSTAR)',
    `room_type` VARCHAR(100) NOT NULL COMMENT '房型',
    `total` INT DEFAULT 0 COMMENT '总房量',
    `booked` INT DEFAULT 0 COMMENT '已预订量',
    `price` DECIMAL(10,2) DEFAULT NULL COMMENT '价格',
    `date` DATE DEFAULT NULL COMMENT '房间日期',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_hr_hotel` (`hotel_type`, `hotel_id`),
    KEY `idx_hr_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='酒店房间预订实时信息表';

-- -----------------------------------------------------------
-- 19. hotel_marketing - 酒店营销推荐记录
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `hotel_marketing`;
CREATE TABLE `hotel_marketing` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '营销ID',
    `hotel_id` BIGINT NOT NULL COMMENT '酒店ID',
    `hotel_type` VARCHAR(20) NOT NULL COMMENT '酒店类型(STAR/NONSTAR)',
    `content` TEXT COMMENT '营销内容',
    `diversion` VARCHAR(200) DEFAULT NULL COMMENT '导流情况',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_hm_hotel` (`hotel_type`, `hotel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='酒店营销推荐记录表';

-- ============================================================
-- SEED DATA - 种子数据
-- ============================================================

-- 密码均为 "123456" 的BCrypt加密值（已用 spring-security-crypto 的 BCryptPasswordEncoder 校验通过）
SET @bcrypt_pwd = '$2a$10$1Z6ukp1XbrgFS3QI4BR9.OkERXcxKQXp4zZo3Eo2ZH78Yc28eFVVS';

-- 五角色种子用户
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role`, `college`, `student_no`, `teacher_no`, `gpa`, `grade_score`, `phone`, `email`, `department_id`, `status`) VALUES
(1, 'platform1', @bcrypt_pwd, '平台管理员',   'PLATFORM_ADMIN',    NULL, NULL, NULL, NULL, NULL, '13800000001', 'platform@wuhou.cn', NULL, 1),
(2, 'approver1',  @bcrypt_pwd, '审批人员',     'APPROVER',          NULL, NULL, NULL, NULL, NULL, '13800000002', 'approver@wuhou.cn', NULL, 1),
(3, 'handler1',   @bcrypt_pwd, '投诉处理员一', 'COMPLAINT_HANDLER', NULL, NULL, NULL, NULL, NULL, '13800000003', 'handler1@wuhou.cn', NULL, 1),
(4, 'handler2',   @bcrypt_pwd, '投诉处理员二', 'COMPLAINT_HANDLER', NULL, NULL, NULL, NULL, NULL, '13800000004', 'handler2@wuhou.cn', NULL, 1),
(5, 'hotel1',     @bcrypt_pwd, '酒店管理员',   'HOTEL_ADMIN',       NULL, NULL, NULL, NULL, NULL, '13800000005', 'hotel@wuhou.cn',    NULL, 1),
(6, 'tourist1',   @bcrypt_pwd, '游客小张',     'TOURIST',           NULL, NULL, NULL, NULL, NULL, '13800000006', 'tourist@wuhou.cn',  NULL, 1);

-- 示例景区（点）
INSERT INTO `attraction` (`name`, `type`, `address`, `intro`, `open_time`) VALUES
('武侯祠', '历史古迹', '成都市武侯祠大街231号', '纪念诸葛亮与蜀汉名臣的祠庙，全国重点文物保护单位。', '09:00-18:00'),
('锦里古街', '民俗街区', '武侯祠大街231号旁', '西蜀风情古街，集小吃、民俗、非遗于一体。', '全天开放'),
('三国文化研习馆', '展馆', '武侯祠内', '展示三国文化与蜀汉历史的专题展馆。', '09:00-17:30');

-- 示例旅游线路
INSERT INTO `tour_route` (`name`, `attraction_ids`, `duration`, `intro`) VALUES
('三国文化经典游', '1,2', '约3小时', '武侯祠—锦里，串联三国文化与民俗体验。'),
('武侯祠精华半日游', '1', '约2小时', '重点游览武侯祠主体建筑与文物。');

-- 示例餐饮娱乐
INSERT INTO `catering` (`name`, `type`, `address`, `price`, `intro`) VALUES
('蜀风园', '餐饮', '武侯祠大街', '人均80元', '地道川菜，靠近武侯祠。'),
('锦里小吃街', '餐饮', '锦里古街', '人均40元', '汇集四川各色名小吃。'),
('三国演艺厅', '娱乐', '锦里古街', '人均120元', '三国主题演艺演出。');

-- 示例演出团体
INSERT INTO `performance_group` (`name`, `type`, `address`, `intro`, `contact`) VALUES
('蜀韵演出团', '戏曲', '锦里古街', '川剧变脸、蜀韵文化演出。', '028-88888888'),
('三国鼓舞团', '歌舞', '武侯祠广场', '三国主题鼓舞表演。', '028-66666666');

-- 示例景区交通
INSERT INTO `transport_info` (`name`, `type`, `area`, `start`, `end`, `schedule`, `price`) VALUES
('景区摆渡车', '观光车', '武侯祠片区', '北门', '锦里', '每15分钟一班', '10元'),
('武侯祠停车场', '停车场', '武侯祠片区', '武侯祠东侧', NULL, '全天开放', '按次收费');

-- 示例星级酒店
INSERT INTO `star_hotel` (`name`, `level`, `address`, `tel`, `intro`, `status`) VALUES
('武侯祠锦华酒店', '五星', '武侯区武侯祠大街100号', '028-81234567', '毗邻武侯祠，设施完善。', 1),
('蜀都国际酒店', '四星', '武侯区一环路南三段', '028-85556666', '舒适商务酒店。', 1);

-- 示例非星级酒店
INSERT INTO `nonstar_hotel` (`name`, `type`, `address`, `tel`, `intro`) VALUES
('锦里客栈', '乡村旅游', '锦里古街', '028-8555888', '民俗客栈，近锦里。'),
('川西农家乐', '乡村旅游', '武侯区簇桥街道', '028-8555777', '乡村民宿，田园体验。');

-- 示例房间实时信息（hotel1=hotel_id 1 星级）
INSERT INTO `hotel_room` (`hotel_id`, `hotel_type`, `room_type`, `total`, `booked`, `price`, `date`, `update_user_id`) VALUES
(1, 'STAR', '标准双床房', 30, 12, 480.00, '2026-09-15', 5),
(1, 'STAR', '豪华大床房', 20, 8, 780.00, '2026-09-15', 5);

-- 示例酒店营销
INSERT INTO `hotel_marketing` (`hotel_id`, `hotel_type`, `content`, `diversion`, `create_by`) VALUES
(1, 'STAR', '入住锦华酒店送武侯祠联票', '假期游客导流', 1),
(3, 'NONSTAR', '锦里客栈住宿礼遇', '锦里客流导流', 1);


-- -----------------------------------------------------------
-- 20. room_type - 房型基准（每酒店每房型一行）
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `room_type`;
CREATE TABLE `room_type` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '房型ID',
    `hotel_type` VARCHAR(20) NOT NULL COMMENT '酒店类型(STAR/NONSTAR)',
    `hotel_id` BIGINT NOT NULL COMMENT '酒店ID',
    `room_type` VARCHAR(100) NOT NULL COMMENT '房型名称',
    `total` INT DEFAULT 0 COMMENT '总房量',
    `base_booked` INT DEFAULT 0 COMMENT '基准已预定',
    `price` DECIMAL(10,2) DEFAULT NULL COMMENT '单价',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_roomtype` (`hotel_type`, `hotel_id`, `room_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房型基准表';

-- -----------------------------------------------------------
-- 21. hotel_booking - 游客预订记录
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `hotel_booking`;
CREATE TABLE `hotel_booking` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预订ID',
    `user_id` BIGINT NOT NULL COMMENT '游客ID',
    `hotel_type` VARCHAR(20) NOT NULL COMMENT '酒店类型(STAR/NONSTAR)',
    `hotel_id` BIGINT NOT NULL COMMENT '酒店ID',
    `hotel_name` VARCHAR(200) DEFAULT NULL COMMENT '酒店名称快照',
    `room_type` VARCHAR(100) NOT NULL COMMENT '房型名称',
    `price` DECIMAL(10,2) DEFAULT NULL COMMENT '单价快照',
    `check_in` DATE NOT NULL COMMENT '入住日期',
    `check_out` DATE NOT NULL COMMENT '离开日期',
    `guests` INT DEFAULT 1 COMMENT '入住人数',
    `total_price` DECIMAL(10,2) DEFAULT NULL COMMENT '总价',
    `status` VARCHAR(20) DEFAULT 'BOOKED' COMMENT '状态: BOOKED/CANCELLED',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_bk_hotel` (`hotel_type`, `hotel_id`, `room_type`),
    KEY `idx_bk_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='酒店预订记录表';

-- 房型基准种子（取自 hotel_room 存量：基准已预定=原 booked）
INSERT INTO `room_type` (`hotel_type`,`hotel_id`,`room_type`,`total`,`base_booked`,`price`) VALUES
('STAR',1,'标准双床房',30,12,480.00),
('STAR',1,'豪华大床房',20,8,780.00),
('STAR',1,'商务大床房',12,2,520.00),
('STAR',1,'亲子房',10,3,888.00);

SET FOREIGN_KEY_CHECKS = 1;
