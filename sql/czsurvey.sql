/*
 Navicat Premium Data Transfer

 Source Server         : ubuntu 虚拟机
 Source Server Type    : MySQL
 Source Server Version : 80027
 Source Host           : 192.168.113.129:3306
 Source Schema         : czsurvey

 Target Server Type    : MySQL
 Target Server Version : 80027
 File Encoding         : 65001

 Date: 22/02/2023 22:20:07
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_contact
-- ----------------------------
DROP TABLE IF EXISTS `t_contact`;
CREATE TABLE `t_contact`  (
  `id` bigint NOT NULL COMMENT '主键',
  `owner_user_id` bigint NULL DEFAULT NULL COMMENT '所属者ID',
  `group_id` bigint NULL DEFAULT NULL COMMENT '用户组ID',
  `real_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '姓名',
  `phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `gender` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '性别',
  `wx_openid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信openid',
  `wx_nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信昵称',
  `wx_avatar` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信头像',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_contact_group
-- ----------------------------
DROP TABLE IF EXISTS `t_contact_group`;
CREATE TABLE `t_contact_group`  (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组名',
  `owner_user_id` bigint NULL DEFAULT NULL COMMENT '所属者ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_project
-- ----------------------------
DROP TABLE IF EXISTS `t_project`;
CREATE TABLE `t_project`  (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目名',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '目录id',
  `owner_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目类型',
  `owner_id` bigint NULL DEFAULT NULL COMMENT '关联项目ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_role
-- ----------------------------
DROP TABLE IF EXISTS `t_role`;
CREATE TABLE `t_role`  (
  `user_id` bigint NOT NULL COMMENT '用户id',
  `role` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色',
  PRIMARY KEY (`user_id`, `role`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_survey
-- ----------------------------
DROP TABLE IF EXISTS `t_survey`;
CREATE TABLE `t_survey`  (
  `id` bigint NOT NULL COMMENT '主键',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '问卷标题',
  `instruction` json NULL COMMENT '问卷描述',
  `conclusion` json NULL COMMENT '问卷结束页描述',
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '问卷状态',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_survey_answer
-- ----------------------------
DROP TABLE IF EXISTS `t_survey_answer`;
CREATE TABLE `t_survey_answer`  (
  `id` bigint NOT NULL,
  `survey_id` bigint NULL DEFAULT NULL COMMENT '问卷ID',
  `answer` json NULL COMMENT '问卷回答',
  `ua` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ua',
  `duration` int NULL DEFAULT NULL COMMENT '耗时',
  `is_anonymously` tinyint(1) NULL DEFAULT NULL COMMENT '是否匿名',
  `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `ip_city` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP所在的城市',
  `ip_province` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ip所在的省',
  `browser` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '浏览器',
  `os` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作系统',
  `platform` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '平台',
  `is_valid` tinyint(1) NOT NULL COMMENT '是否有效',
  `answerer_id` bigint NULL DEFAULT NULL COMMENT '回答人ID',
  `started_at` datetime NULL DEFAULT NULL COMMENT '回答开始时间',
  `ended_at` datetime NULL DEFAULT NULL COMMENT '回答结束时间',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_survey_logic
-- ----------------------------
DROP TABLE IF EXISTS `t_survey_logic`;
CREATE TABLE `t_survey_logic`  (
  `id` bigint NOT NULL COMMENT '主键',
  `survey_id` bigint NULL DEFAULT NULL COMMENT '问卷ID',
  `expression` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '表达式',
  `conditions` json NULL COMMENT '条件列表',
  `question_keys` json NULL COMMENT '需要显示的问题key列表',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_survey_page
-- ----------------------------
DROP TABLE IF EXISTS `t_survey_page`;
CREATE TABLE `t_survey_page`  (
  `survey_id` bigint NOT NULL COMMENT '问卷ID',
  `page_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分页key',
  `order_num` int NULL DEFAULT NULL COMMENT '排序号',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`survey_id`, `page_key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_survey_question
-- ----------------------------
DROP TABLE IF EXISTS `t_survey_question`;
CREATE TABLE `t_survey_question`  (
  `id` bigint NOT NULL COMMENT '主键',
  `survey_id` bigint NULL DEFAULT NULL COMMENT '问卷ID',
  `question_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '问题Key',
  `page_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分页key',
  `title` json NULL COMMENT '问题标题',
  `description` json NULL COMMENT '问题描述',
  `type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '问题类型',
  `required` tinyint(1) NULL DEFAULT NULL COMMENT '是否必填',
  `additional_info` json NULL COMMENT '问题详情',
  `order_num` int NULL DEFAULT NULL COMMENT '排序号',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_survey_setting
-- ----------------------------
DROP TABLE IF EXISTS `t_survey_setting`;
CREATE TABLE `t_survey_setting`  (
  `survey_id` bigint NOT NULL COMMENT '问卷id',
  `is_display_question_no` tinyint(1) NULL DEFAULT NULL COMMENT '显示问题编号',
  `is_allow_rollback` tinyint(1) NULL DEFAULT NULL COMMENT '允许回退',
  `is_login_required` tinyint(1) NULL DEFAULT NULL COMMENT '回答需要登录验证',
  `answerer_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '谁可以回答该问卷',
  `is_enable_user_answer_limit` tinyint(1) NULL DEFAULT NULL COMMENT '是否开启用户回答次数限制',
  `user_limit_freq` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户回答频率',
  `user_limit_num` int NULL DEFAULT NULL COMMENT '用户回答次数',
  `is_enable_ip_answer_limit` tinyint(1) NULL DEFAULT NULL COMMENT '是否开启IP回答次数限制',
  `ip_limit_freq` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP回答频率',
  `ip_limit_num` int NULL DEFAULT NULL COMMENT 'IP回答次数',
  `begin_time` datetime NULL DEFAULT NULL COMMENT '问卷开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '问卷结束时间',
  `max_answers` int NULL DEFAULT NULL COMMENT '回收数量限制，为0表示不限制',
  `is_add_to_contact` tinyint(1) NULL DEFAULT NULL COMMENT '是否将将答题者保存为联系人',
  `contact_group_id` bigint NULL DEFAULT NULL COMMENT '答题者保存到的联系人分组ID',
  `is_enable_change` tinyint(1) NULL DEFAULT NULL COMMENT '是否允许修改',
  `is_anonymously` tinyint(1) NULL DEFAULT NULL COMMENT '是否匿名',
  `is_breakpoint_resume` tinyint(1) NULL DEFAULT NULL COMMENT '是否允许断点续传',
  `is_save_last_answer` tinyint(1) NULL DEFAULT NULL COMMENT '是否保存上一次的回答',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`survey_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`  (
  `id` bigint NOT NULL COMMENT '主键',
  `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `real_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '姓名',
  `phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密码',
  `wx_openid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信openid',
  `is_enabled` tinyint(1) NULL DEFAULT NULL COMMENT '是否启用',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
