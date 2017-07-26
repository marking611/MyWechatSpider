/*
Navicat MySQL Data Transfer

Source Server         : 本地MySQL
Source Server Version : 50636
Source Host           : localhost:3306
Source Database       : test

Target Server Type    : MYSQL
Target Server Version : 50636
File Encoding         : 65001

Date: 2017-07-26 23:02:44
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for whchat_spider
-- ----------------------------
DROP TABLE IF EXISTS `whchat_spider`;
CREATE TABLE `whchat_spider` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `wechat_id` varchar(255) DEFAULT NULL,
  `createtime` datetime DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `url` varchar(1000) DEFAULT NULL,
  `img` varchar(1000) DEFAULT NULL,
  `content` longtext,
  `publishtime` varchar(50) DEFAULT NULL,
  `author` varchar(255) DEFAULT NULL,
  `intro` longtext,
  `md5` varchar(255) DEFAULT NULL,
  `pt` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=851 DEFAULT CHARSET=utf8;
