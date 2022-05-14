/*
 Navicat Premium Data Transfer

 Source Server         : 本地数据库
 Source Server Type    : MySQL
 Source Server Version : 80025
 Source Host           : localhost:3306
 Source Schema         : simplebot

 Target Server Type    : MySQL
 Target Server Version : 80025
 File Encoding         : 65001

 Date: 14/05/2022 13:08:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for group_boot_state
-- ----------------------------
DROP TABLE IF EXISTS `group_boot_state`;
CREATE TABLE `group_boot_state`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `group_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '群号',
  `state` tinyint(1) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
