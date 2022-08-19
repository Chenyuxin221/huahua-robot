/*
 可选

 Navicat Premium Data Transfer

 Source Server         : 本地数据库
 Source Server Type    : MySQL
 Source Server Version : 80025
 Source Host           : localhost:3306
 Source Schema         : simplebot

 Target Server Type    : MySQL
 Target Server Version : 80025
 File Encoding         : 65001

 Date: 19/08/2022 20:47:44
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for porn
-- ----------------------------
DROP TABLE IF EXISTS `porn`;
CREATE TABLE `porn`
(
    `id`   int                                                           NOT NULL AUTO_INCREMENT,
    `url`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '自己偷偷看',
    `md5`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `type` int                                                           NOT NULL COMMENT '1: 正常  2: 疑似  3:色情',
    `tips` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
