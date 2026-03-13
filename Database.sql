-- phpMyAdmin SQL Dump
-- version 4.9.2
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Server version: 10.4.10-MariaDB
-- PHP Version: 7.3.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `gtanks`
--

-- --------------------------------------------------------

--
-- Table structure for table `black_ips`
--

DROP TABLE IF EXISTS `black_ips`;
CREATE TABLE IF NOT EXISTS `black_ips` (
  `idblack_ips` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip` varchar(255) NOT NULL,
  PRIMARY KEY (`idblack_ips`),
  UNIQUE KEY `idblack_ips` (`idblack_ips`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Структура таблицы `payment`
--

DROP TABLE IF EXISTS `payment`;
CREATE TABLE IF NOT EXISTS `payment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` datetime NOT NULL,
  `id_payment` bigint(20) NOT NULL,
  `nickname` varchar(255) NOT NULL,
  `status` tinyint(4) NOT NULL,
  `summ` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `date` (`date`),
  UNIQUE KEY `id_payment` (`id_payment`),
  UNIQUE KEY `nickname` (`nickname`),
  UNIQUE KEY `status` (`status`),
  UNIQUE KEY `summ` (`summ`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `garages`
--

DROP TABLE IF EXISTS `garages`;
CREATE TABLE IF NOT EXISTS `garages` (
  `uid` bigint(20) NOT NULL AUTO_INCREMENT,
  `colormaps` longtext NOT NULL,
  `hulls` longtext NOT NULL,
  `inventory` longtext NOT NULL,
  `turrets` longtext NOT NULL,
  `userid` varchar(255) NOT NULL,
  `effects` longtext NOT NULL,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `uid` (`uid`),
  UNIQUE KEY `userid` (`userid`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `karma`
--

DROP TABLE IF EXISTS `karma`;
CREATE TABLE IF NOT EXISTS `karma` (
  `idkarma` bigint(20) NOT NULL AUTO_INCREMENT,
  `chat_banned` bit(1) DEFAULT NULL,
  `chat_banned_before` datetime DEFAULT NULL,
  `game_banned` bit(1) DEFAULT NULL,
  `game_banned_before` datetime DEFAULT NULL,
  `reason_for_chat_ban` varchar(255) DEFAULT NULL,
  `reason_for_game_ban` varchar(255) DEFAULT NULL,
  `userid` varchar(255) NOT NULL,
  `banner_chat_user_id` varchar(255) DEFAULT NULL,
  `banner_game_user_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`idkarma`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `logs`
--

DROP TABLE IF EXISTS `logs`;
CREATE TABLE IF NOT EXISTS `logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` datetime NOT NULL,
  `message` longtext NOT NULL,
  `type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Новости хранятся в src/main/resources/data/json/news.json
-- и не создаются в БД отдельной таблицей.
--

-- Структура таблицы `users_viewed_news_info`
--

CREATE TABLE `users_viewed_news_info` (
  `id` bigint(20) NOT NULL,
  `viewed_ids` varchar(255) NOT NULL,
  `nickname` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------
--
-- Table structure for table `users`
--

--
-- Индексы таблицы `users_viewed_news_info`
--
ALTER TABLE `users_viewed_news_info`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `id` (`id`);

--
-- AUTO_INCREMENT для таблицы `users_viewed_news_info`
--
ALTER TABLE `users_viewed_news_info`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=1;
COMMIT;

DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `uid` bigint(20) NOT NULL AUTO_INCREMENT,
  `crystalls` bigint(11) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `last_ip` varchar(255) NOT NULL,
  `last_issue_bonus` datetime DEFAULT NULL,
  `next_score` bigint(11) NOT NULL,
  `nickname` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `place` bigint(11) NOT NULL,
  `rank` bigint(11) NOT NULL,
  `rating` bigint(11) NOT NULL,
  `score` bigint(11) NOT NULL,
  `user_type` bigint(11) NOT NULL,
  `purchasedFirstThing` bigint(11) NOT NULL,
  `kills` bigint(11) NOT NULL,
  `deaths` bigint(11) NOT NULL,
  `kd` DOUBLE NOT NULL,
  `wealth` bigint(11) NOT NULL,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `uid` (`uid`),
  UNIQUE KEY `nickname` (`nickname`),
  UNIQUE KEY `email` (`email`),
  KEY `uid_2` (`uid`),
  KEY `password` (`password`) USING BTREE,
  KEY `next_score` (`next_score`) USING BTREE,
  KEY `score` (`score`) USING BTREE,
  KEY `crystalls` (`crystalls`) USING BTREE,
  KEY `user_type` (`user_type`) USING BTREE,
  KEY `place` (`place`) USING BTREE,
  KEY `rating` (`rating`) USING BTREE,
  KEY `rank` (`rank`) USING BTREE
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;