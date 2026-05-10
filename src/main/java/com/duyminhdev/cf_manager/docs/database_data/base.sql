-- MySQL dump 10.13  Distrib 8.0.38, for Win64 (x86_64)
--
-- Host: localhost    Database: cf_crm_nckh_v02
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account` (
  `id` int NOT NULL AUTO_INCREMENT,
  `account_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` longtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `photo` longtext COLLATE utf8mb4_unicode_ci,
  `date_of_birth` datetime NOT NULL,
  `phone_number` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `role_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_account_role_id` (`role_id`),
  CONSTRAINT `fk_account_role_id` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=72 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` VALUES (1,'AD-1001','admin','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Huy Khôi AD','huykhoivn@gmail.com','images/avatar/307ce493-b254-4b2d-8ba4-d12c080d6651.jpg','2025-05-24 00:00:00','0387052612','2025-03-21 09:26:55',1,1),(2,'AD_10204','pnqa1','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Phạm Nhật Quân Anh','huykho286@gmail.com',NULL,'2025-04-07 00:00:00','0353023534','2025-03-29 17:44:17',0,1),(3,'AD-9788','huykhoi2188','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Trịnh Huy Khôi','huykho02a02@gmail.com','images/avatar/ed316367-c2b4-4c78-a177-f871b76b21ec.jpg','2025-05-24 00:00:00','0330698471','2025-04-11 16:57:25',1,1),(4,'ADMIN-04','huykhoi29124','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Trịnh Huy Khôi','huykho302@gmail.com','images/userdefault.jpg','2001-10-02 00:00:00','0430698471','2025-04-12 20:24:03',0,1),(5,'AD-1305','nhatdang49a3','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Nguyễn Nhật Đăng','lamq4543@gmail.com','images/avatar/73814e7e-0f20-4feb-9f1a-5369fe321bd3.jpg','2025-05-24 00:00:00','0353882388','2025-04-12 20:50:45',1,8),(6,'PV-0249','ngoctam2041','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Ngô Minh Ngọc Tâm','tamngoc9582@gmail.com','images/avatar/700969e7-8765-4374-8e01-cba6a77ffe19.png','2002-04-12 00:00:00','0386324566','2025-04-13 22:36:46',1,6),(7,'PV-0249','trungdao12','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Đào Thanh Trung','lam3quey@gmail.com','images/avatar/649da190-d313-4e24-8907-0c448e4f5fe1.jpg','2025-05-10 00:00:00','0325454968','2025-05-10 09:32:43',1,8),(8,'PV-0325','namtrng685','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Đỗ Nam Trung','tamn93582@gmail.com','images/avatar/ce7684b1-c8e9-4635-9b71-9c71143019d4.jpg','2025-05-10 00:00:00','0358495077','2025-05-10 09:38:21',1,8),(9,'PV-0277','phapdqua2','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Lê Đức Pháp','lamquey443@gmail.com','images/avatar/37fd4271-f2df-4f45-9ec9-e0e2d97aca42.png','2025-05-22 00:00:00','0354252177','2025-05-22 14:12:42',1,8),(10,'PC-3859','phache1','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Nguyễn Công Tắc','phache34@gmail.com','images/avatar/bf4f28ba-c301-4a4a-b785-6bd9022de1c6.jpg','2025-05-24 00:00:00','0353089377','2025-05-24 16:33:58',1,6),(11,'PV-2394','phucvu01','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Cao Đăng Chiếm','phucvua43@gmail.com','images/avatar/bd937940-6bae-4109-90c6-ffcd96f80127.jpg','2025-05-24 00:00:00','0358592847','2025-05-24 16:57:36',1,8),(12,'PV-0359','minhtv01','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Trần Văn Minh','minhtran2403@gmail.com','images/userdefault.jpg','2006-05-12 00:00:00','0334895777','2025-05-24 17:05:41',1,8),(13,'PV-6976','linhvh2548','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Hoàng Văn Linh','linhvh2548@gmail.com','images/userdefault.jpg','1995-10-05 00:00:00','0385283354','2025-01-15 01:08:55',1,8),(14,'PV-5413','trangtb2409','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Bùi Trọng Trang','trangtb2409@gmail.com','images/userdefault.jpg','1993-09-18 00:00:00','0395042335','2025-01-16 18:08:19',1,8),(15,'PV-8831','linhvl2700','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Lê Văn Linh','linhvl2700@gmail.com','images/userdefault.jpg','2000-10-20 00:00:00','0365717293','2025-01-05 23:39:28',1,8),(16,'PV-2344','hunggv8305','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Vũ Gia Hùng','hunggv8305@gmail.com','images/userdefault.jpg','1993-12-25 00:00:00','0396089491','2025-01-21 19:02:48',1,8),(17,'PV-3563','quanxb8865','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Bùi Xuân Quân','quanxb8865@gmail.com','images/userdefault.jpg','2025-05-24 00:00:00','0326419784','2025-01-15 14:22:53',1,8),(18,'PV-4915','huongt5087','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Lê Thị Hương','huongt5087@gmail.com','images/userdefault.jpg','1998-11-13 00:00:00','0968455326','2025-01-19 18:01:01',1,8),(19,'PV-1464','lant7822','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Hoàng Thị Lan','lant7822@gmail.com','images/userdefault.jpg','2003-06-18 00:00:00','0918550090','2025-02-04 18:29:14',1,8),(20,'PV-5780','dungn1776','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Đỗ Ngọc Dũng','dungn1776@gmail.com','images/userdefault.jpg','1988-04-15 00:00:00','0910928221','2025-01-22 07:50:34',1,8),(21,'PV-3885','quânt4815','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Bùi Thanh Quân','quânt4815@gmail.com','images/userdefault.jpg','1978-11-20 00:00:00','0936208163','2025-01-30 21:03:22',1,8),(22,'PV-1630','lanx7372','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Đặng Xuân Lan','lanx7372@gmail.com','images/userdefault.jpg','1989-02-20 00:00:00','0938689349','2025-01-24 11:23:25',1,8),(23,'PV-9805','hùngx7201','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Lê Xuân Hùng','hùngx7201@gmail.com','images/userdefault.jpg','1994-08-15 00:00:00','0972932483','2025-01-31 07:34:54',1,8),(24,'PV-4123','dungx8567','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Đỗ Xuân Dũng','dungx8567@gmail.com','images/userdefault.jpg','1984-03-13 00:00:00','0905230484','2025-02-10 03:22:54',1,8),(25,'PV-3317','sonv1533','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Đỗ Văn Sơn','sonv1533@gmail.com','images/userdefault.jpg','1999-07-16 00:00:00','0978160027','2025-02-04 15:33:33',1,8),(26,'PV-2767','hùnga2335','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Vũ Anh Hùng','hùnga2335@gmail.com','images/userdefault.jpg','2005-08-10 00:00:00','0996054720','2025-01-08 05:59:10',1,8),(27,'PV-2085','hàa4780','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Phạm Anh Hà','hàa4780@gmail.com','images/userdefault.jpg','1999-02-19 00:00:00','0960120192','2025-02-05 06:20:52',1,8),(28,'PV-8910','quânt3964','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Phạm Thị Quân','quânt3964@gmail.com','images/userdefault.jpg','1975-03-14 00:00:00','0988522255','2025-01-29 20:17:58',1,8),(29,'PV-9710','namt7969','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Phạm Thị Nam','namt7969@gmail.com','images/userdefault.jpg','1995-01-22 00:00:00','0985268232','2025-02-15 20:19:41',1,8),(30,'PV-1886','hàa4477','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Đỗ Anh Hà','hàa4477@gmail.com','images/userdefault.jpg','1986-09-06 00:00:00','0902228972','2025-01-04 15:50:37',1,8),(31,'PV-1263','sonn5587','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Hồ Ngọc Sơn','sonn5587@gmail.com','images/userdefault.jpg','1977-07-22 00:00:00','0996825044','2025-01-25 10:39:25',1,8),(32,'PV-2423','hùngt6527','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Đặng Tuấn Hùng','hùngt6527@gmail.com','images/userdefault.jpg','1995-09-21 00:00:00','0984796929','2025-01-19 13:28:09',1,8),(33,'PV-4158','huonga7091','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Trần Anh Hương','huonga7091@gmail.com','images/userdefault.jpg','1981-02-05 00:00:00','0965468051','2025-02-14 10:54:16',1,8),(34,'PV-3993','maiv3031','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Lê Văn Mai','maiv3031@gmail.com','images/userdefault.jpg','1983-10-13 00:00:00','0960779188','2025-01-06 20:03:30',1,8),(35,'PV-7969','dungg9880','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Đỗ Gia Dũng','dungg9880@gmail.com','images/userdefault.jpg','1976-03-19 00:00:00','0904210914','2025-01-30 22:55:21',1,8),(36,'PV-4716','maia9348','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Bùi Anh Mai','maia9348@gmail.com','images/userdefault.jpg','1992-08-17 00:00:00','0921008505','2025-01-19 06:02:36',1,8),(37,'PV-8797','dungv7540','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Lê Văn Dũng','dungv7540@gmail.com','images/userdefault.jpg','1986-12-24 00:00:00','0959284248','2025-02-14 16:11:03',1,8),(38,'PV-5277','lant5193','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Trần Tuấn Lan','lant5193@gmail.com','images/userdefault.jpg','1970-04-12 00:00:00','0964744260','2025-01-04 09:15:31',1,8),(39,'PV-3705','hùngx6510','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Nguyễn Xuân Hùng','hùngx6510@gmail.com','images/userdefault.jpg','1993-01-25 00:00:00','0932039515','2025-02-21 20:49:15',1,8),(40,'PV-1286','hàx1507','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Phạm Xuân Hà','hàx1507@gmail.com','images/userdefault.jpg','1992-04-20 00:00:00','0921207118','2025-02-05 14:38:14',1,8),(41,'PV-1969','hùngx5947','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Phạm Xuân Hùng','hùngx5947@gmail.com','images/userdefault.jpg','1973-07-24 00:00:00','0977742121','2025-02-21 12:41:55',1,8),(42,'PV-9494','trangg1723','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Phạm Gia Trang','trangg1723@gmail.com','images/userdefault.jpg','2003-09-11 00:00:00','0936042458','2025-01-04 16:04:36',1,8),(43,'PV-8391','dungh7407','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Hoàng Hữu Dũng','dungh7407@gmail.com','images/userdefault.jpg','1987-11-06 00:00:00','0986169804','2025-02-14 20:09:48',1,8),(44,'PV-8229','sont5517','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Đỗ Thanh Sơn','sont5517@gmail.com','images/userdefault.jpg','1994-01-26 00:00:00','0988958320','2025-01-09 19:57:45',1,8),(45,'PV-7763','lant5344','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Lê Thị Lan','lant5344@gmail.com','images/userdefault.jpg','1978-07-23 00:00:00','0993029955','2025-02-15 17:23:01',1,8),(46,'PV-6043','namn8979','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Đỗ Ngọc Nam','namn8979@gmail.com','images/userdefault.jpg','1978-10-15 00:00:00','0915831085','2025-01-11 13:12:11',1,8),(47,'PV-1191','namt5928','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Bùi Thanh Nam','namt5928@gmail.com','images/userdefault.jpg','1979-04-16 00:00:00','0940102189','2025-01-01 22:46:25',1,8),(48,'PV-8878','quânt6364','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Lê Thị Quân','quânt6364@gmail.com','images/userdefault.jpg','1976-11-21 00:00:00','0925815896','2025-01-02 17:55:01',1,8),(49,'PV-5304','mait5144','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Đỗ Tuấn Mai','mait5144@gmail.com','images/userdefault.jpg','2005-06-27 00:00:00','0951571405','2025-01-16 20:39:41',1,8),(50,'PV-4442','lant4768','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Nguyễn Thị Lan','lant4768@gmail.com','images/userdefault.jpg','1978-11-28 00:00:00','0961318026','2025-01-03 05:03:25',1,8),(51,'PV-9533','maig3205','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Đặng Gia Mai','maig3205@gmail.com','images/userdefault.jpg','1976-05-10 00:00:00','0932520298','2025-01-23 03:39:23',1,8),(52,'PV-5133','huongm8408','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Đặng Minh Hương','huongm8408@gmail.com','images/userdefault.jpg','1977-08-12 00:00:00','0912319356','2025-01-27 19:11:38',1,8),(53,'PV-9846','main8399','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Trần Ngọc Mai','main8399@gmail.com','images/userdefault.jpg','1986-04-14 00:00:00','0968108565','2025-01-29 07:04:56',1,8),(54,'PV-4681','mait6927','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Phạm Tuấn Mai','mait6927@gmail.com','images/userdefault.jpg','1994-09-28 00:00:00','0915367169','2025-02-12 16:29:39',1,8),(55,'PV-4407','hùngt7560','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Đỗ Thị Hùng','hùngt7560@gmail.com','images/userdefault.jpg','2002-09-18 00:00:00','0908804424','2025-02-23 13:11:58',1,8),(56,'PV-2178','hàg9782','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Đỗ Gia Hà','hàg9782@gmail.com','images/userdefault.jpg','2004-12-26 00:00:00','0977587084','2025-02-24 16:05:07',0,8),(57,'PV-1817','sonx1096','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Hoàng Xuân Sơn','sonx1096@gmail.com','images/userdefault.jpg','1997-06-22 00:00:00','0901370988','2025-01-28 23:04:00',0,8),(58,'PV-4666','namx3204','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Đỗ Xuân Nam','namx3204@gmail.com','images/userdefault.jpg','1990-08-01 00:00:00','0940072306','2025-02-16 06:13:32',0,8),(59,'PV-4950','sonm4281','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Vũ Minh Sơn','sonm4281@gmail.com','images/userdefault.jpg','1996-01-11 00:00:00','0931898858','2025-02-10 19:20:31',0,8),(60,'PV-4207','hàa9105','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Hồ Anh Hà','hàa9105@gmail.com','images/userdefault.jpg','1977-12-24 00:00:00','0999398030','2025-01-27 10:56:30',0,8),(61,'PV-4260','huongt6715','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Vũ Thị Hương','huongt6715@gmail.com','images/userdefault.jpg','1993-09-08 00:00:00','0932725715','2025-01-12 02:19:53',0,8),(62,'PV-5285','trangm7737','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Phạm Minh Trang','trangm7737@gmail.com','images/userdefault.jpg','1976-08-03 00:00:00','0917705455','2025-01-06 01:38:17',0,8),(63,'PV-3877','hàt3892','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Đặng Thị Hà','hàt3892@gmail.com','images/userdefault.jpg','1989-11-28 00:00:00','0960639915','2025-02-26 16:16:25',0,8),(64,'PV-5189','namg8920','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Đỗ Gia Nam','namg8920@gmail.com','images/userdefault.jpg','2001-09-13 00:00:00','0952367163','2025-02-02 15:34:07',0,8),(65,'PV-8915','mait5116','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Vũ Thị Mai','mait5116@gmail.com','images/userdefault.jpg','1982-08-10 00:00:00','0907463810','2025-02-07 06:08:47',0,8),(66,'PV-6566','huongt9095','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Lê Thanh Hương','huongt9095@gmail.com','images/userdefault.jpg','1987-06-16 00:00:00','0981748048','2025-02-25 21:56:41',0,8),(67,'PV-4066','hàt6560','$2a$12$5e313bTcWq.M5HBVFS6KUugYrmNzDT.8c.M47Bsl6FGRgwOkm.eaO','Lê Tuấn Hà','hàt6560@gmail.com','images/userdefault.jpg','1976-08-16 00:00:00','0954593820','2025-02-10 18:37:10',0,8),(68,NULL,'testuser1','$2a$10$FAg5utXncdn1hA516ZNDR.svIDWROqsbjB8YHOUtWl6hi6.Ybyf9y','Nguyễn Duy Minh',NULL,'images/userdefault.jpg','1999-12-31 17:00:00',NULL,'2026-03-28 14:37:15',1,8),(69,NULL,'admin1','$2a$10$lnrRzsioKO6E1HGGA2rkkO68N2OIIsQHoYHzdZtzOYNtQZv7L92pi','Quản Trị Viên',NULL,'images/userdefault.jpg','1999-12-31 17:00:00',NULL,'2026-03-28 14:37:20',1,1),(70,NULL,'testuser12','$2a$10$UrSPWVkIyyi6PBUYUoP3TuuBujKRzKeUT2HmROxZJezvJoCwZA5JO','Nguyễn Duy Minh',NULL,'images/userdefault.jpg','1999-12-31 17:00:00',NULL,'2026-03-28 14:47:27',1,8),(71,NULL,'admin12','$2a$10$bhb0rdv0jlvsROlxUxSaz.ESy9aSQahh/Hik9jW5XSGyh986NQwey','Quản Trị Viên',NULL,'images/userdefault.jpg','1999-12-31 17:00:00',NULL,'2026-03-28 14:47:30',1,1);
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `account_activity`
--

DROP TABLE IF EXISTS `account_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account_activity` (
  `id` int NOT NULL AUTO_INCREMENT,
  `activity_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `account_id` int DEFAULT NULL,
  `activity_description` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `activity_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_account_activity_account_id` (`account_id`),
  CONSTRAINT `fk_account_activity_account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_activity`
--

LOCK TABLES `account_activity` WRITE;
/*!40000 ALTER TABLE `account_activity` DISABLE KEYS */;
INSERT INTO `account_activity` VALUES (1,'1','2025-05-02 18:15:21',1,1,'Tạo đơn nhập hàng','purchase_order'),(2,'2','2025-05-02 22:53:58',1,1,'Tạo đơn nhập hàng','purchase_order'),(3,'3','2025-05-02 22:54:59',1,1,'Tạo đơn nhập hàng','purchase_order'),(4,'4','2025-05-02 23:04:28',1,1,'Tạo đơn nhập hàng','purchase_order'),(5,'5','2025-05-02 23:07:40',1,1,'Tạo đơn nhập hàng','purchase_order'),(6,'2','2025-05-03 00:19:20',1,1,'Cập nhật đơn nhập hàng','purchase_order'),(7,'1','2025-05-03 14:19:31',1,1,'Đơn nhập hàng đã được duyệt','purchase_order'),(8,'3','2025-05-03 14:19:59',1,1,'Đơn nhập hàng đã bị hủy','purchase_order'),(9,'4','2025-05-03 14:27:10',1,1,'Đơn nhập hàng đã hoàn thành','purchase_order'),(10,'1','2025-05-03 14:30:20',1,1,'Đơn nhập hàng đã hoàn thành','purchase_order'),(11,'6','2025-05-03 14:56:15',1,1,'Tạo đơn nhập hàng','purchase_order'),(12,'7','2025-05-03 15:01:11',1,5,'Tạo đơn nhập hàng','purchase_order'),(13,'8','2025-05-03 15:01:35',1,5,'Tạo đơn nhập hàng','purchase_order'),(14,'9','2025-05-08 11:25:25',1,1,'Tạo đơn nhập hàng','purchase_order'),(15,'9','2025-05-08 11:25:44',1,1,'Đơn nhập hàng đã hoàn thành','purchase_order'),(16,'10','2025-05-08 13:53:45',1,1,'Tạo đơn nhập hàng','purchase_order'),(17,'10','2025-05-08 14:06:15',1,1,'Đơn hàng được gửi','purchase_order'),(18,'10','2025-05-08 14:06:23',1,1,'Đơn nhập hàng đã được duyệt','purchase_order'),(19,'10','2025-05-08 14:06:29',1,1,'Đơn nhập hàng đã hoàn thành','purchase_order'),(20,'11','2025-05-08 14:08:09',1,1,'Tạo đơn nhập hàng','purchase_order'),(21,'11','2025-05-08 14:08:25',1,1,'Đơn nhập hàng đã hoàn thành','purchase_order'),(22,'12','2025-05-08 16:22:20',1,1,'Tạo đơn nhập hàng','purchase_order'),(23,'12','2025-05-08 16:23:49',1,1,'Đơn hàng được gửi','purchase_order'),(24,'12','2025-05-08 16:24:05',1,1,'Đơn nhập hàng đã được duyệt','purchase_order'),(25,'12','2025-05-08 16:24:32',1,1,'Đơn nhập hàng đã hoàn thành','purchase_order'),(26,'13','2025-05-08 16:26:10',1,1,'Tạo đơn nhập hàng','purchase_order'),(27,'13','2025-05-08 16:26:22',1,1,'Đơn nhập hàng đã hoàn thành','purchase_order'),(28,'14','2025-05-13 19:53:38',1,1,'Tạo đơn nhập hàng','purchase_order'),(29,'15','2025-05-28 00:13:48',1,1,'Tạo đơn nhập hàng','purchase_order'),(30,'15','2025-05-28 00:14:00',1,1,'Đơn hàng được gửi','purchase_order'),(31,'15','2025-05-28 00:14:07',1,1,'Đơn nhập hàng đã được duyệt','purchase_order'),(32,'15','2025-05-28 00:14:23',1,1,'Đơn nhập hàng đã hoàn thành','purchase_order');
/*!40000 ALTER TABLE `account_activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `account_token`
--

DROP TABLE IF EXISTS `account_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account_token` (
  `id` int NOT NULL AUTO_INCREMENT,
  `account_id` int NOT NULL,
  `refresh_token` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `refresh_token_expires_at` datetime DEFAULT NULL,
  `access_token_jti` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_revoked` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_token_refresh_token` (`refresh_token`),
  UNIQUE KEY `uk_account_token_access_token_jti` (`access_token_jti`),
  KEY `idx_account_token_account_id` (`account_id`),
  KEY `idx_account_token_refresh_token_expires_at` (`refresh_token_expires_at`),
  KEY `idx_account_token_is_revoked` (`is_revoked`),
  CONSTRAINT `fk_account_token_account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_token`
--

LOCK TABLES `account_token` WRITE;
/*!40000 ALTER TABLE `account_token` DISABLE KEYS */;
INSERT INTO `account_token` VALUES (1,1,'jtFmmcBG5enPyOUCSLpIPw7st6aiGKAeT2AcZkvYPpr5SWeD6PTv0yofDk0rPNhLSq2K5EqR1RCaxbklvI4i8A','2026-04-04 09:43:33','cd8c5f94-a5e8-417d-8878-91a88be08967',1,'2026-03-28 09:43:33','2026-03-28 09:54:20'),(2,1,'XZYWh3wplZMvZPWXXM44Ub9qbn73H-9qmuJXj0K-eMdwXdhLyjVDrlQwqTZeP_8DKA0BIGcgzztARBYvxikY4w','2026-04-04 09:54:20','1d2eee61-ba13-482a-9d86-345279d6fea9',1,'2026-03-28 09:54:20','2026-03-28 13:34:28'),(3,1,'rbfQvkAyljS7HWGSTI9jW8K8zoqhkvNJs3AEWR5MEZtLFl-0h_6z-ZHfKApposxG80ceiQv9bGoZsMQ-fZJWjQ','2026-04-04 13:34:28','57785193-d401-476b-a011-e40a151f5f1d',1,'2026-03-28 13:34:28','2026-03-28 13:34:29'),(4,1,'wlmgHeYTWUs6w7xBssRp2iAw3PyPfL2hMOvUtnAMO7kDZ0g1lMpZAuKc8rTgKdu6zYOscC-yuwEtOWolSsWtYQ','2026-04-04 13:34:29','023e087d-01c2-48c3-8401-f62bab903b8a',1,'2026-03-28 13:34:29','2026-03-28 14:37:11'),(5,1,'hNOGWejruHO5N9g4A1pnQ8rjJqQOR7ehFGvJvU7l2XQT1eRetP-16_q7t9PuWZBSKaolP97lnk4Biv-fY6kTOA','2026-04-04 14:37:11','40ea8dd2-7d67-4afa-afe3-2c0f06ad2e0b',1,'2026-03-28 14:37:11','2026-03-28 14:37:56'),(6,68,'jozvoW75azd9LlkkOSvvDscb978cXtZGxzbpXbDP2AyseQyifWfXpwlTvRNephP2RWypIRl8w-Ov02dCQRI1Dg','2026-04-04 14:37:15','73b25ab5-3158-4774-8807-ea47e62107ba',0,'2026-03-28 14:37:15','2026-03-28 14:37:15'),(7,69,'XlGPhVPQS575EeAhMuBt_oMQOYfSM7r3Jua_zYZzdy0-V9FqZYWj9VRF4rWUEr_WCqGq8FyAmaY3oU_YZFAvFA','2026-04-04 14:37:20','f91b1f80-fb64-4d5a-89ea-13932e67c467',0,'2026-03-28 14:37:20','2026-03-28 14:37:20'),(8,1,'RHpw9JrtelLcoLkaNIYaHT0eFJa3ihyB4fbHXTqozELhfyECnqXSQaz4x0vy3ZFTx_ZG2cqpxMLqKvFKhxvN5w','2026-04-04 14:37:56','57756601-e5d8-4ceb-88b3-b722f234bc67',1,'2026-03-28 14:37:56','2026-03-28 14:38:05'),(9,1,'2gxNnwGXZuLzqAAfK4orF1VBcjUx31Zjmmt3ys8BW1YRlPex9TkNPjcpWlp578gNk9Te4L1G-Yp7AZ6_jAy0Fw','2026-04-04 14:38:05','eafc31a8-533d-4d73-89d0-362679d256b3',1,'2026-03-28 14:38:05','2026-03-28 14:47:12'),(10,1,'WuPaHVzitxd8w3WRsYcMXmevWpityQ_CPQNlM-qEp5G5ySX_59mo2Pi68_4IFIZGS75x1C_ZWxbi56RfU5TSDg','2026-04-04 14:47:12','652a2a13-96da-40c4-b569-983840a5cb9a',1,'2026-03-28 14:47:12','2026-03-28 16:13:54'),(11,70,'nox6OgXsyg2K7AKtjbQnF8_KYfFeq93AFwqLSq456dY9ms73y5V3A42l206nKey1X6A2q2ejLsJdT4RVG9hyoA','2026-04-04 14:47:27','b34f4727-63f8-4879-a026-a2ec74b834a9',0,'2026-03-28 14:47:27','2026-03-28 14:47:27'),(12,71,'UK6hd33C6bLgDaO-Pj71nzzb--lh0wuq9qok1mGoX0gwj1gMWdvmFaa-79LjtqLtGbzoOn4zyuEaxFN6RbhbGA','2026-04-04 14:47:30','7e624a88-651a-4da1-bcf7-0d7608ff2f68',1,'2026-03-28 14:47:30','2026-03-28 14:47:45'),(13,71,'sgpgmFCCyGux0_XNVkhE63h3uffvPRaRGmbmCLxVhhNtD8MNxMk6uYahMGTdYaYbcn7wOX7jVSKRjvnyaeRkIw','2026-04-04 14:47:45','903d8e3f-ba8b-46eb-80cc-d012d351ceee',0,'2026-03-28 14:47:45','2026-03-28 14:47:45'),(14,1,'gw2XFd-6o0lE7see5y_qBqcjXt4_MPy0lm98etwTueh5QOPiJhP2j7CiIv9OGQWcWbTIMk5Egf8pZ_IycxoBAQ','2026-04-04 16:13:54','34469b01-6dfb-4493-8257-a67e33e929b0',0,'2026-03-28 16:13:54','2026-03-28 16:13:54');
/*!40000 ALTER TABLE `account_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attendance`
--

DROP TABLE IF EXISTS `attendance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance` (
  `id` int NOT NULL AUTO_INCREMENT,
  `check_in_at` datetime NOT NULL,
  `check_out_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `account_id` int DEFAULT NULL,
  `work_hours` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_attendance_account_id` (`account_id`),
  CONSTRAINT `fk_attendance_account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attendance`
--

LOCK TABLES `attendance` WRITE;
/*!40000 ALTER TABLE `attendance` DISABLE KEYS */;
/*!40000 ALTER TABLE `attendance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cash_flow`
--

DROP TABLE IF EXISTS `cash_flow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cash_flow` (
  `id` int NOT NULL AUTO_INCREMENT,
  `total_amount` decimal(18,0) NOT NULL,
  `flow_type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `account_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_cash_flow_account_id` (`account_id`),
  CONSTRAINT `fk_cash_flow_account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cash_flow`
--

LOCK TABLES `cash_flow` WRITE;
/*!40000 ALTER TABLE `cash_flow` DISABLE KEYS */;
INSERT INTO `cash_flow` VALUES (1,50000,'INCOME','Hóa đơn bán hàng','2025-04-22 13:52:11',1,1),(2,55000,'INCOME','Hóa đơn bán hàng','2025-04-23 09:44:28',1,1),(3,39000,'INCOME','Hóa đơn bán hàng','2025-04-23 09:44:34',1,1),(4,89000,'INCOME','Hóa đơn bán hàng','2025-04-23 09:45:19',1,1),(5,94000,'INCOME','Hóa đơn bán hàng','2025-04-24 08:55:41',1,1),(6,94000,'INCOME','Hóa đơn bán hàng','2025-04-24 08:55:46',1,1),(7,50000,'INCOME','Hóa đơn bán hàng','2025-04-25 09:11:41',1,1),(8,215000,'INCOME','Hóa đơn bán hàng','2025-04-28 20:17:27',1,1),(9,55000,'INCOME','Hóa đơn bán hàng','2025-04-28 20:27:00',1,1),(10,199000,'INCOME','Hóa đơn bán hàng','2025-04-28 20:35:16',1,1),(11,94000,'INCOME','Hóa đơn bán hàng','2025-04-30 23:35:12',1,1),(12,50000,'INCOME','Hóa đơn bán hàng','2025-04-30 23:35:56',1,1),(14,150000,'DEBT','Nợ nhập hàng PO-000004-03/05/2025','2025-05-03 14:26:20',1,1),(15,500000,'DEBT','Nợ nhập hàng PO-000001-03/05/2025','2025-05-03 14:30:20',1,1),(16,105000,'INCOME','Hóa đơn bán hàng','2025-05-07 22:33:49',1,1),(17,550000,'DEBT','Nợ nhập hàng PO-000009-08/05/2025','2025-05-08 11:25:44',1,1),(18,9500000,'DEBT','Nợ nhập hàng PO-000010-08/05/2025','2025-05-08 14:06:29',1,1),(19,2500000,'DEBT','Nợ nhập hàng PO-000011-08/05/2025','2025-05-08 14:08:25',1,1),(20,2400000,'DEBT','Nợ nhập hàng PO-000012-08/05/2025','2025-05-08 16:24:32',1,1),(21,3400000,'DEBT','Nợ nhập hàng PO-000013-08/05/2025','2025-05-08 16:26:22',1,1),(22,210000,'INCOME','Hóa đơn bán hàng','2025-05-09 14:04:58',1,1),(23,55000,'INCOME','Hóa đơn bán hàng','2025-05-09 14:06:16',1,1),(24,55000,'INCOME','Hóa đơn bán hàng','2025-05-09 14:22:28',1,1),(25,500000,'DEBT_PAYMENT','Trả nợ cho DEBT_PO-000001','2025-05-09 17:51:01',1,1),(26,3400000,'DEBT_PAYMENT','Trả nợ cho DEBT_PO-000013','2025-05-09 20:56:52',1,1),(27,55000,'INCOME','Hóa đơn bán hàng','2025-05-13 21:30:27',1,1),(28,215000,'INCOME','Hóa đơn bán hàng','2025-05-13 21:30:32',1,1),(29,275000,'INCOME','Hóa đơn bán hàng','2025-05-13 21:30:56',1,1),(30,183000,'INCOME','Hóa đơn bán hàng','2025-05-14 00:27:14',1,1),(31,144000,'INCOME','Hóa đơn bán hàng','2025-05-14 01:24:52',1,1),(32,94000,'INCOME','Hóa đơn bán hàng','2025-05-17 00:43:25',1,1),(33,298000,'INCOME','Hóa đơn bán hàng','2025-05-17 00:49:11',1,1),(34,464000,'INCOME','Hóa đơn bán hàng','2025-05-20 06:33:30',1,1),(35,144000,'INCOME','Hóa đơn bán hàng','2025-05-20 06:42:07',1,1),(36,110000,'INCOME','Hóa đơn bán hàng','2025-05-20 06:46:16',1,1),(37,179000,'INCOME','Hóa đơn bán hàng','2025-05-23 11:05:17',1,1),(38,257000,'INCOME','Hóa đơn bán hàng','2025-05-24 16:36:18',1,1),(39,69000,'INCOME','Hóa đơn bán hàng','2025-05-24 16:53:21',1,10),(40,183000,'INCOME','Hóa đơn bán hàng','2025-05-24 16:58:58',1,1),(41,188000,'INCOME','Hóa đơn bán hàng','2025-05-24 17:34:16',1,1),(42,78000,'INCOME','Hóa đơn bán hàng','2025-05-24 17:34:21',1,1),(43,149000,'INCOME','Hóa đơn bán hàng','2025-05-24 17:34:26',1,1),(44,172000,'INCOME','Hóa đơn bán hàng','2025-05-24 17:34:31',1,1),(45,128000,'INCOME','Hóa đơn bán hàng','2025-05-24 17:34:36',1,1),(46,241000,'INCOME','Hóa đơn bán hàng','2025-05-26 21:06:41',1,1),(47,177000,'INCOME','Hóa đơn bán hàng','2025-05-27 20:37:22',1,1),(48,138000,'INCOME','Hóa đơn bán hàng','2025-05-27 21:11:34',1,1),(49,55000,'INCOME','Hóa đơn bán hàng','2025-05-27 21:38:02',1,1),(50,55000,'INCOME','Hóa đơn bán hàng','2025-05-27 22:33:26',1,1),(51,55000,'INCOME','Hóa đơn bán hàng','2025-05-27 23:37:57',1,1),(52,55000,'INCOME','Hóa đơn bán hàng','2025-05-27 23:38:02',1,1),(53,138000,'INCOME','Hóa đơn bán hàng','2025-05-27 23:38:05',1,1),(54,94000,'INCOME','Hóa đơn bán hàng','2025-05-27 23:38:10',1,1),(55,119000,'INCOME','Hóa đơn bán hàng','2025-05-27 23:38:14',1,1),(56,55000,'INCOME','Hóa đơn bán hàng','2025-05-27 23:40:51',1,1),(57,55000,'INCOME','Hóa đơn bán hàng','2025-05-28 00:05:56',1,1),(58,55000,'INCOME','Hóa đơn bán hàng','2025-05-28 00:09:50',1,1),(59,55000,'INCOME','Hóa đơn bán hàng','2025-05-28 00:11:04',1,1),(60,55000,'INCOME','Hóa đơn bán hàng','2025-05-28 00:13:24',1,1),(61,600000,'DEBT','Nợ nhập hàng PO-000015-28/05/2025','2025-05-28 00:14:23',1,1),(62,2500000,'DEBT_PAYMENT','Trả nợ cho DEBT_PO-000011','2025-05-28 00:18:28',1,1),(63,138000,'INCOME','Hóa đơn bán hàng','2025-05-28 06:19:46',1,1),(64,39000,'INCOME','Hóa đơn bán hàng','2025-05-28 06:34:31',1,1);
/*!40000 ALTER TABLE `cash_flow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `debt`
--

DROP TABLE IF EXISTS `debt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `debt` (
  `id` int NOT NULL AUTO_INCREMENT,
  `debt_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `debt_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `total_amount` decimal(18,0) NOT NULL,
  `is_paid` tinyint(1) NOT NULL,
  `paid_at` datetime DEFAULT NULL,
  `note` longtext COLLATE utf8mb4_unicode_ci,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `supplier_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_debt_supplier_id` (`supplier_id`),
  CONSTRAINT `fk_debt_supplier_id` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `debt`
--

LOCK TABLES `debt` WRITE;
/*!40000 ALTER TABLE `debt` DISABLE KEYS */;
INSERT INTO `debt` VALUES (2,'DEBT_PO-000004','Nợ nhập hàng PO-000004-03/05/2025',150000,0,NULL,'Nợ nhập hàng 03/05/2025','2025-05-03 14:26:18',1,2),(3,'DEBT_PO-000001','Nợ nhập hàng PO-000001-03/05/2025',500000,1,'2025-05-09 17:51:01','Nợ nhập hàng 03/05/2025','2025-05-03 14:30:20',1,1),(4,'DEBT_PO-000009','Nợ nhập hàng PO-000009-08/05/2025',550000,0,NULL,'Nợ nhập hàng 08/05/2025','2025-05-08 11:25:44',1,1),(5,'DEBT_PO-000010','Nợ nhập hàng PO-000010-08/05/2025',9500000,0,NULL,'Nợ nhập hàng 08/05/2025','2025-05-08 14:06:29',1,2),(6,'DEBT_PO-000011','Nợ nhập hàng PO-000011-08/05/2025',2500000,1,'2025-05-28 00:18:28','Nợ nhập hàng 08/05/2025','2025-05-08 14:08:25',1,2),(7,'DEBT_PO-000012','Nợ nhập hàng PO-000012-08/05/2025',2400000,0,NULL,'Nợ nhập hàng 08/05/2025','2025-05-08 16:24:32',1,2),(8,'DEBT_PO-000013','Nợ nhập hàng PO-000013-08/05/2025',3400000,1,'2025-05-09 20:56:52','Nợ nhập hàng 08/05/2025','2025-05-08 16:26:22',1,1),(9,'DEBT_PO-000015','Nợ nhập hàng PO-000015-28/05/2025',600000,0,NULL,'Nợ nhập hàng 28/05/2025','2025-05-28 00:14:23',1,2);
/*!40000 ALTER TABLE `debt` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dining_table`
--

DROP TABLE IF EXISTS `dining_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dining_table` (
  `id` int NOT NULL AUTO_INCREMENT,
  `table_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `floor` int DEFAULT '1',
  `table_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `table_status` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL,
  `slot` tinyint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dining_table`
--

LOCK TABLES `dining_table` WRITE;
/*!40000 ALTER TABLE `dining_table` DISABLE KEYS */;
INSERT INTO `dining_table` VALUES (1,'TB-101-6',1,'Bàn A1','occupied','2025-04-07 16:02:00',1,4),(2,'TB-102-6',1,'Bàn A2','available','2025-04-07 16:02:29',1,4),(3,'TB-103-6',1,'Bàn A3','available','2025-04-07 23:40:45',1,4),(4,'TB-104-6',1,'Bàn A4','booked','2025-04-07 23:42:54',1,4),(5,'TB-105-4',1,'Bàn A4','available','2025-04-07 23:43:11',1,4),(6,'TB-106-6',1,'Bàn A6','available','2025-04-07 23:43:29',1,4),(7,'TB-107-6',1,'Bàn A7','available','2025-04-07 23:47:05',1,4),(8,'TB-108-6',1,'Bàn A8','available','2025-04-07 23:47:09',1,4),(9,'TB-109-6',1,'Bàn A9','available','2025-04-07 23:47:50',1,4),(10,'TB-201-6',1,'Bàn B1','available','2025-04-07 23:48:16',1,4);
/*!40000 ALTER TABLE `dining_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dish`
--

DROP TABLE IF EXISTS `dish`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dish` (
  `id` int NOT NULL AUTO_INCREMENT,
  `dish_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dish_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `price` decimal(18,0) NOT NULL,
  `photo` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `dish_category_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_dish_dish_category_id` (`dish_category_id`),
  CONSTRAINT `fk_dish_dish_category_id` FOREIGN KEY (`dish_category_id`) REFERENCES `dish_category` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dish`
--

LOCK TABLES `dish` WRITE;
/*!40000 ALTER TABLE `dish` DISABLE KEYS */;
INSERT INTO `dish` VALUES (1,'traquamonganhđao','Trà quả mọng anh đào',50000,'images/dish/3ae9bb68-fbc4-4bba-9ff1-fefae7ec4674.png','2025-04-10 18:03:12',1,1),(2,'trasenvang','Trà sen vàng',55000,'images/dish/7bd5ef82-0693-4f7f-97d1-67f26c67b03a.jpg','2025-04-14 00:25:54',1,1),(3,'trathachđao','Trà thạch đào',55000,'images/dish/9102bbc2-5273-41ba-895e-e01302b3a73b.png','2025-04-14 21:26:30',1,1),(4,'phinsuađa','Phin sữa đá',39000,'images/dish/4112ee03-0b77-48ec-93d3-fcad497a8a65.png','2025-04-14 21:32:38',1,2),(5,'bacxiu','Bạc xỉu',39000,'images/dish/76570edc-696b-4032-b028-720d0b2684c2.jpg','2025-05-22 14:17:13',1,2),(6,'phinđenđa','Phin đen đá',39000,'images/dish/a22f58ba-510a-4248-af1c-618c79ccf46c.jpg','2025-05-22 14:19:21',1,2),(7,'phindihanhnhan','Phindi hạnh nhân',55000,'images/dish/f08f190d-911b-4b7f-9f9a-efcc35a474f4.jpg','2025-05-22 14:22:23',1,3),(8,'phindikemsua','Phindi kem sữa',55000,'images/dish/1616fda8-f94b-41ce-a055-5fe51d726c41.jpg','2025-05-22 14:22:48',1,3),(9,'phindichoco','Phindi choco',55000,'images/dish/c0273eb3-edf6-4a40-b737-e09f40516e39.jpg','2025-05-22 14:23:03',1,3),(10,'freezetraxanh','Freeze trà xanh',69000,'images/dish/ea7262b4-d191-4e16-813e-77f80483a696.jpeg','2025-05-22 14:28:30',1,4),(11,'caramelphinfreeze','Caramel phin freeze',69000,'images/dish/9768691a-aa8f-4338-a8a6-0a0bdc7e97f8.jpg','2025-05-22 14:34:44',1,4),(12,'đaulung','Đau lưng',60000,'images/dish/59225af5-6ac4-49bf-b88d-0733fdb929b5.png','2025-05-24 15:19:51',0,3),(13,'đaulung','Đau lưng',60000,'images/dish/8636b881-eea4-4250-955d-1a95e1705772.jpg','2025-05-28 00:17:38',1,1);
/*!40000 ALTER TABLE `dish` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dish_category`
--

DROP TABLE IF EXISTS `dish_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dish_category` (
  `id` int NOT NULL AUTO_INCREMENT,
  `dish_category_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dish_category_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dish_category`
--

LOCK TABLES `dish_category` WRITE;
/*!40000 ALTER TABLE `dish_category` DISABLE KEYS */;
INSERT INTO `dish_category` VALUES (1,'TRA-001','Trà hoa quả','2025-04-10 17:56:59',1),(2,'CAFE-001','Cà phê pha phin','2025-04-14 21:30:56',1),(3,'PHIN-DI','Phindi','2025-05-22 14:20:14',1),(4,'FREEZE-001','Freeze','2025-05-22 14:20:36',1),(5,'NM000001','Nhóm mới','2025-05-24 15:27:20',0),(6,'NM-02','Nhóm mới 2','2025-05-24 15:28:16',0),(7,'NM000001','CTE-03','2025-05-24 15:32:30',0);
/*!40000 ALTER TABLE `dish_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dish_order`
--

DROP TABLE IF EXISTS `dish_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dish_order` (
  `id` int NOT NULL AUTO_INCREMENT,
  `note` longtext COLLATE utf8mb4_unicode_ci,
  `dish_order_status_id` int NOT NULL,
  `dining_table_id` int NOT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `account_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_dish_order_dish_order_status_id` (`dish_order_status_id`),
  KEY `fk_dish_order_dining_table_id` (`dining_table_id`),
  KEY `fk_dish_order_account_id` (`account_id`),
  CONSTRAINT `fk_dish_order_account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `fk_dish_order_dining_table_id` FOREIGN KEY (`dining_table_id`) REFERENCES `dining_table` (`id`),
  CONSTRAINT `fk_dish_order_dish_order_status_id` FOREIGN KEY (`dish_order_status_id`) REFERENCES `dish_order_status` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=108 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dish_order`
--

LOCK TABLES `dish_order` WRITE;
/*!40000 ALTER TABLE `dish_order` DISABLE KEYS */;
INSERT INTO `dish_order` VALUES (7,NULL,4,1,'2025-04-13 19:33:35',1,1),(8,NULL,4,1,'2025-04-13 23:06:50',1,1),(9,NULL,4,1,'2025-04-14 20:13:58',1,1),(10,NULL,2,1,'2025-04-14 21:59:00',0,5),(11,NULL,4,1,'2025-04-16 17:42:47',1,1),(12,NULL,4,3,'2025-04-16 17:46:20',1,1),(13,NULL,4,1,'2025-04-16 20:33:54',1,1),(14,NULL,4,2,'2025-04-17 06:05:09',1,1),(15,NULL,4,1,'2025-04-17 06:50:46',1,1),(16,NULL,4,4,'2025-04-19 15:24:08',1,1),(17,NULL,4,1,'2025-04-22 09:37:08',1,1),(18,NULL,4,3,'2025-04-22 11:20:48',1,1),(19,NULL,4,3,'2025-04-22 11:20:53',1,1),(20,NULL,4,1,'2025-04-22 11:23:08',1,1),(21,NULL,4,6,'2025-04-22 11:25:19',1,1),(22,NULL,4,1,'2025-04-22 12:05:33',1,1),(23,NULL,4,8,'2025-04-22 12:40:49',1,1),(24,NULL,4,8,'2025-04-22 12:48:22',1,1),(25,NULL,4,1,'2025-04-22 12:51:19',1,1),(26,NULL,4,1,'2025-04-22 12:51:59',1,1),(27,NULL,4,1,'2025-04-22 12:59:20',1,1),(28,NULL,2,5,'2025-04-22 13:04:05',1,1),(29,NULL,4,1,'2025-04-22 13:51:54',1,1),(30,NULL,4,2,'2025-04-23 09:44:07',1,1),(31,NULL,4,3,'2025-04-23 09:44:17',1,1),(32,NULL,4,4,'2025-04-23 09:44:42',1,1),(33,NULL,4,1,'2025-04-24 08:47:56',1,1),(34,NULL,4,9,'2025-04-24 08:55:34',1,1),(35,NULL,4,3,'2025-04-25 09:11:34',1,1),(36,NULL,4,1,'2025-04-28 20:15:28',1,1),(37,NULL,4,1,'2025-04-28 20:26:39',1,1),(38,NULL,4,1,'2025-04-28 20:26:52',1,1),(39,NULL,4,2,'2025-04-28 20:35:05',1,1),(40,NULL,4,7,'2025-04-30 23:35:02',1,1),(41,NULL,4,9,'2025-04-30 23:35:36',1,1),(42,NULL,4,2,'2025-05-07 22:33:41',1,1),(43,NULL,4,1,'2025-05-09 14:03:14',1,1),(44,NULL,4,1,'2025-05-09 14:03:36',1,1),(45,NULL,4,1,'2025-05-09 14:05:53',1,1),(46,NULL,4,1,'2025-05-09 14:06:08',1,1),(47,NULL,4,1,'2025-05-09 14:15:11',1,1),(48,NULL,4,1,'2025-05-13 19:49:58',1,1),(49,NULL,4,1,'2025-05-13 19:50:48',1,1),(50,NULL,4,2,'2025-05-13 21:30:21',1,1),(51,NULL,4,3,'2025-05-13 21:30:49',1,1),(52,NULL,4,1,'2025-05-14 00:26:54',1,1),(53,NULL,4,1,'2025-05-14 00:27:08',1,1),(54,NULL,4,1,'2025-05-14 01:24:46',1,1),(55,NULL,4,3,'2025-05-17 00:48:34',1,1),(56,NULL,4,3,'2025-05-17 00:49:00',1,1),(57,NULL,4,1,'2025-05-20 00:25:02',1,1),(58,NULL,4,1,'2025-05-20 00:25:06',1,1),(59,NULL,4,1,'2025-05-20 06:41:14',1,1),(60,NULL,4,6,'2025-05-20 06:46:10',1,1),(61,NULL,4,1,'2025-05-23 11:04:06',1,1),(62,NULL,4,1,'2025-05-24 16:34:52',1,1),(63,NULL,4,1,'2025-05-24 16:35:42',1,1),(64,NULL,4,1,'2025-05-24 16:53:06',1,10),(65,NULL,4,1,'2025-05-24 17:31:44',1,1),(66,NULL,4,4,'2025-05-24 17:33:08',1,1),(67,NULL,4,8,'2025-05-24 17:33:16',1,1),(68,NULL,4,3,'2025-05-24 17:33:25',1,1),(69,NULL,4,7,'2025-05-24 17:33:33',1,1),(70,NULL,4,8,'2025-05-24 17:33:41',1,1),(71,NULL,4,1,'2025-05-26 21:05:30',1,1),(72,NULL,4,1,'2025-05-26 21:06:06',1,1),(73,NULL,4,1,'2025-05-27 20:36:56',1,1),(74,NULL,4,1,'2025-05-27 20:37:46',1,1),(75,NULL,4,1,'2025-05-27 21:30:58',1,1),(76,NULL,4,1,'2025-05-27 21:37:37',1,1),(77,NULL,4,1,'2025-05-27 21:38:14',1,1),(78,NULL,4,1,'2025-05-27 21:39:47',1,1),(79,NULL,4,1,'2025-05-27 21:40:05',1,1),(83,NULL,4,1,'2025-05-27 21:52:18',1,1),(84,NULL,4,1,'2025-05-27 22:10:14',1,1),(85,NULL,4,1,'2025-05-27 22:33:20',1,1),(86,NULL,4,1,'2025-05-27 22:33:31',1,1),(87,NULL,4,1,'2025-05-27 22:35:58',1,1),(88,NULL,4,1,'2025-05-27 23:32:48',1,1),(89,NULL,4,1,'2025-05-27 23:34:29',1,1),(90,NULL,4,1,'2025-05-27 23:36:43',1,1),(91,NULL,4,1,'2025-05-27 23:38:46',1,1),(92,NULL,4,1,'2025-05-27 23:38:53',1,1),(93,NULL,4,1,'2025-05-27 23:40:44',1,1),(94,NULL,4,2,'2025-05-27 23:41:05',1,1),(95,NULL,4,2,'2025-05-27 23:43:30',1,1),(96,NULL,4,1,'2025-05-27 23:51:40',1,1),(97,NULL,4,1,'2025-05-27 23:57:51',1,1),(98,NULL,4,1,'2025-05-28 00:00:05',1,1),(99,NULL,4,1,'2025-05-28 00:02:05',1,1),(100,NULL,4,3,'2025-05-28 00:03:39',1,1),(101,NULL,4,2,'2025-05-28 00:04:56',1,1),(102,NULL,4,2,'2025-05-28 00:09:14',1,1),(103,NULL,4,1,'2025-05-28 00:10:45',1,1),(104,NULL,4,1,'2025-05-28 06:17:45',1,1),(105,NULL,4,1,'2025-05-28 06:18:03',1,1),(106,NULL,4,2,'2025-05-28 06:18:19',1,1),(107,NULL,2,1,'2025-05-28 08:04:49',1,1);
/*!40000 ALTER TABLE `dish_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dish_order_detail`
--

DROP TABLE IF EXISTS `dish_order_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dish_order_detail` (
  `id` int NOT NULL AUTO_INCREMENT,
  `quantity` int NOT NULL,
  `note` longtext COLLATE utf8mb4_unicode_ci,
  `dish_order_id` int NOT NULL,
  `dish_id` int NOT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `price` decimal(18,0) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_dish_order_detail_dish_id` (`dish_id`),
  KEY `fk_dish_order_detail_dish_order_id` (`dish_order_id`),
  CONSTRAINT `fk_dish_order_detail_dish_id` FOREIGN KEY (`dish_id`) REFERENCES `dish` (`id`),
  CONSTRAINT `fk_dish_order_detail_dish_order_id` FOREIGN KEY (`dish_order_id`) REFERENCES `dish_order` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=146 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dish_order_detail`
--

LOCK TABLES `dish_order_detail` WRITE;
/*!40000 ALTER TABLE `dish_order_detail` DISABLE KEYS */;
INSERT INTO `dish_order_detail` VALUES (1,1,NULL,20,4,'2025-04-22 11:23:08',1,0),(2,1,NULL,20,2,'2025-04-22 11:23:08',1,0),(3,1,NULL,21,3,'2025-04-22 11:25:19',1,0),(4,1,NULL,21,1,'2025-04-22 11:25:19',1,0),(5,1,NULL,22,2,'2025-04-22 12:05:34',1,0),(6,2,NULL,22,4,'2025-04-22 12:05:34',1,0),(7,1,NULL,22,1,'2025-04-22 12:05:34',1,0),(8,1,NULL,23,2,'2025-04-22 12:40:49',1,0),(9,1,NULL,23,1,'2025-04-22 12:40:49',1,0),(10,1,NULL,24,2,'2025-04-22 12:48:22',1,0),(11,1,NULL,25,1,'2025-04-22 12:51:19',1,0),(12,1,NULL,26,3,'2025-04-22 12:52:10',1,0),(13,1,NULL,26,4,'2025-04-22 12:53:46',1,0),(14,1,NULL,27,3,'2025-04-22 12:59:20',1,0),(15,1,NULL,28,2,'2025-04-22 13:04:05',1,0),(16,1,NULL,29,1,'2025-04-22 13:51:54',1,0),(17,1,NULL,30,2,'2025-04-23 09:44:07',1,0),(18,1,NULL,31,4,'2025-04-23 09:44:17',1,0),(19,1,NULL,32,4,'2025-04-23 09:44:42',1,0),(20,1,NULL,32,1,'2025-04-23 09:44:42',1,0),(21,1,NULL,33,3,'2025-04-24 08:47:56',1,0),(22,1,NULL,33,4,'2025-04-24 08:47:56',1,0),(23,1,NULL,34,3,'2025-04-24 08:55:34',1,0),(24,1,NULL,34,4,'2025-04-24 08:55:34',1,0),(25,1,NULL,35,1,'2025-04-25 09:11:34',1,0),(26,1,NULL,36,2,'2025-04-28 20:15:28',1,0),(27,2,NULL,36,3,'2025-04-28 20:17:08',1,0),(28,1,NULL,36,1,'2025-04-28 20:17:18',1,0),(29,1,NULL,37,3,'2025-04-28 20:26:39',1,0),(30,1,NULL,37,4,'2025-04-28 20:26:39',1,0),(31,1,NULL,38,3,'2025-04-28 20:26:52',1,0),(32,1,NULL,39,1,'2025-04-28 20:35:05',1,0),(33,1,NULL,39,2,'2025-04-28 20:35:05',1,0),(34,1,NULL,39,3,'2025-04-28 20:35:05',1,0),(35,1,NULL,39,4,'2025-04-28 20:35:05',1,0),(36,1,NULL,40,2,'2025-04-30 23:35:02',1,0),(37,1,NULL,40,4,'2025-04-30 23:35:02',1,0),(38,1,NULL,41,1,'2025-04-30 23:35:36',1,0),(39,1,NULL,42,3,'2025-05-07 22:33:41',1,0),(40,1,NULL,42,1,'2025-05-07 22:33:41',1,0),(41,2,NULL,43,1,'2025-05-09 14:03:15',1,0),(42,1,NULL,43,3,'2025-05-09 14:03:15',1,0),(43,1,NULL,44,3,'2025-05-09 14:03:36',1,0),(44,1,NULL,45,1,'2025-05-09 14:05:53',1,0),(45,1,NULL,46,3,'2025-05-09 14:06:08',1,0),(46,1,NULL,47,2,'2025-05-09 14:15:11',1,0),(47,1,NULL,48,2,'2025-05-13 19:49:58',1,0),(48,1,NULL,48,3,'2025-05-13 19:49:58',1,0),(49,1,NULL,49,2,'2025-05-13 19:50:48',1,0),(50,1,NULL,49,1,'2025-05-13 19:50:48',1,0),(51,1,NULL,50,3,'2025-05-13 21:30:21',1,0),(52,2,NULL,51,2,'2025-05-13 21:30:49',1,0),(53,3,NULL,51,3,'2025-05-13 21:30:49',1,0),(54,1,NULL,52,3,'2025-05-14 00:26:55',1,0),(55,1,NULL,52,4,'2025-05-14 00:26:55',1,0),(56,1,NULL,53,4,'2025-05-14 00:27:08',1,0),(57,1,NULL,53,1,'2025-05-14 00:27:08',1,0),(58,1,NULL,54,4,'2025-05-14 01:24:46',1,0),(59,1,NULL,54,3,'2025-05-14 01:24:46',1,0),(60,1,NULL,54,1,'2025-05-14 01:24:46',1,0),(61,1,NULL,55,3,'2025-05-17 00:48:35',1,0),(62,1,NULL,55,4,'2025-05-17 00:48:35',1,0),(63,1,NULL,55,2,'2025-05-17 00:48:41',1,0),(64,1,NULL,56,2,'2025-05-17 00:49:00',1,0),(65,1,NULL,56,3,'2025-05-17 00:49:00',1,0),(66,1,NULL,56,4,'2025-05-17 00:49:00',1,0),(67,2,NULL,57,3,'2025-05-20 00:25:02',1,0),(68,2,NULL,57,2,'2025-05-20 00:25:02',1,0),(69,3,NULL,57,1,'2025-05-20 00:25:02',1,0),(70,1,NULL,58,2,'2025-05-20 00:25:06',1,0),(71,1,NULL,58,4,'2025-05-20 00:25:06',1,0),(72,1,NULL,59,3,'2025-05-20 06:41:14',1,0),(73,1,NULL,59,4,'2025-05-20 06:41:14',1,0),(74,1,NULL,59,1,'2025-05-20 06:41:14',1,0),(75,1,NULL,60,3,'2025-05-20 06:46:10',1,0),(76,1,NULL,60,2,'2025-05-20 06:46:10',1,0),(77,1,NULL,61,10,'2025-05-23 11:04:06',1,0),(78,1,NULL,61,9,'2025-05-23 11:04:06',1,0),(79,1,NULL,61,8,'2025-05-23 11:04:06',1,0),(80,1,NULL,62,9,'2025-05-24 16:34:52',1,0),(81,1,NULL,62,10,'2025-05-24 16:34:52',1,0),(82,1,NULL,63,9,'2025-05-24 16:35:42',1,0),(83,1,NULL,63,4,'2025-05-24 16:35:42',1,0),(84,1,NULL,63,5,'2025-05-24 16:35:42',1,0),(85,1,NULL,64,10,'2025-05-24 16:53:06',1,0),(86,1,NULL,65,10,'2025-05-24 17:31:44',1,0),(87,1,NULL,65,11,'2025-05-24 17:31:44',1,0),(88,1,NULL,65,1,'2025-05-24 17:31:44',1,0),(89,1,NULL,66,4,'2025-05-24 17:33:08',1,0),(90,1,NULL,66,5,'2025-05-24 17:33:08',1,0),(91,1,NULL,67,3,'2025-05-24 17:33:16',1,0),(92,1,NULL,67,4,'2025-05-24 17:33:16',1,0),(93,1,NULL,68,4,'2025-05-24 17:33:25',1,0),(94,1,NULL,68,2,'2025-05-24 17:33:25',1,0),(95,1,NULL,68,3,'2025-05-24 17:33:25',1,0),(96,1,NULL,69,4,'2025-05-24 17:33:33',1,0),(97,1,NULL,69,1,'2025-05-24 17:33:33',1,0),(98,1,NULL,69,6,'2025-05-24 17:33:33',1,0),(99,2,NULL,70,4,'2025-05-24 17:33:41',1,0),(100,1,NULL,71,9,'2025-05-26 21:05:30',1,0),(101,1,NULL,71,10,'2025-05-26 21:05:30',1,0),(102,1,NULL,71,6,'2025-05-26 21:05:30',1,0),(103,1,NULL,71,5,'2025-05-26 21:05:30',1,0),(104,1,NULL,72,6,'2025-05-26 21:06:06',1,0),(105,1,NULL,73,10,'2025-05-27 20:36:56',1,0),(106,1,NULL,73,11,'2025-05-27 20:36:56',1,0),(107,1,NULL,74,11,'2025-05-27 20:37:46',1,0),(108,1,NULL,74,10,'2025-05-27 20:37:46',1,0),(109,1,NULL,75,8,'2025-05-27 21:30:59',1,0),(110,1,NULL,76,8,'2025-05-27 21:37:37',1,0),(111,1,NULL,77,8,'2025-05-27 21:38:14',1,0),(112,1,NULL,78,9,'2025-05-27 21:39:47',1,0),(113,1,NULL,79,9,'2025-05-27 21:40:05',1,0),(114,1,NULL,83,9,'2025-05-27 21:52:19',1,0),(115,1,NULL,84,8,'2025-05-27 22:10:14',1,0),(116,1,NULL,85,9,'2025-05-27 22:33:20',1,0),(117,1,NULL,86,9,'2025-05-27 22:33:31',1,0),(118,1,NULL,87,9,'2025-05-27 22:35:58',1,0),(119,1,NULL,88,10,'2025-05-27 23:32:48',1,0),(120,1,NULL,88,1,'2025-05-27 23:32:48',1,0),(121,1,NULL,89,8,'2025-05-27 23:34:29',1,0),(122,1,NULL,89,4,'2025-05-27 23:34:29',1,0),(123,1,NULL,90,10,'2025-05-27 23:36:43',1,0),(124,1,NULL,90,11,'2025-05-27 23:36:43',1,0),(125,1,NULL,91,9,'2025-05-27 23:38:46',1,0),(126,1,NULL,91,10,'2025-05-27 23:38:46',1,0),(127,1,NULL,92,9,'2025-05-27 23:38:53',1,0),(128,1,NULL,93,8,'2025-05-27 23:40:44',1,0),(129,1,NULL,94,8,'2025-05-27 23:41:05',1,0),(130,1,NULL,94,10,'2025-05-27 23:41:05',1,0),(131,1,NULL,95,1,'2025-05-27 23:43:30',1,0),(132,1,NULL,96,8,'2025-05-27 23:51:40',1,0),(133,1,NULL,97,9,'2025-05-27 23:57:51',1,0),(134,2,NULL,97,10,'2025-05-27 23:57:51',1,0),(135,1,NULL,98,8,'2025-05-28 00:00:05',1,0),(136,1,NULL,99,8,'2025-05-28 00:02:06',1,0),(137,1,NULL,100,8,'2025-05-28 00:03:39',1,0),(138,1,NULL,101,8,'2025-05-28 00:04:56',1,0),(139,1,NULL,102,8,'2025-05-28 00:09:15',1,0),(140,1,NULL,103,8,'2025-05-28 00:10:45',1,0),(141,1,NULL,104,10,'2025-05-28 06:17:45',1,0),(142,1,NULL,105,10,'2025-05-28 06:18:03',1,0),(143,1,NULL,106,5,'2025-05-28 06:18:19',1,0),(144,1,NULL,107,10,'2025-05-28 08:04:49',1,0),(145,1,NULL,107,9,'2025-05-28 08:04:49',1,0);
/*!40000 ALTER TABLE `dish_order_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dish_order_status`
--

DROP TABLE IF EXISTS `dish_order_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dish_order_status` (
  `id` int NOT NULL AUTO_INCREMENT,
  `dish_order_status_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dish_order_status_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dish_order_status`
--

LOCK TABLES `dish_order_status` WRITE;
/*!40000 ALTER TABLE `dish_order_status` DISABLE KEYS */;
INSERT INTO `dish_order_status` VALUES (1,'PROCESSING','Đang chế biến','2025-04-10 22:45:45',1),(2,'DONE','Hoàn thành','2025-04-10 22:46:13',1),(3,'CANCEL','Đã hủy','2025-04-10 22:49:23',1),(4,'PAID','Đã thanh toán','2025-04-10 22:49:40',1);
/*!40000 ALTER TABLE `dish_order_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `financial_target`
--

DROP TABLE IF EXISTS `financial_target`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `financial_target` (
  `id` int NOT NULL AUTO_INCREMENT,
  `target_revenue` decimal(18,0) NOT NULL,
  `target_profit` decimal(18,0) DEFAULT NULL,
  `period` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `start_date` datetime NOT NULL,
  `end_date` datetime NOT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `financial_target`
--

LOCK TABLES `financial_target` WRITE;
/*!40000 ALTER TABLE `financial_target` DISABLE KEYS */;
/*!40000 ALTER TABLE `financial_target` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ingredient`
--

DROP TABLE IF EXISTS `ingredient`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ingredient` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ingredient_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ingredient_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `shelf_life` int NOT NULL,
  `average_price` decimal(18,0) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `ingredient_category_id` int NOT NULL,
  `supplier_id` int NOT NULL,
  `unit_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_ingredient_ingredient_category_id` (`ingredient_category_id`),
  KEY `fk_ingredient_supplier_id` (`supplier_id`),
  KEY `fk_ingredient_unit_id` (`unit_id`),
  CONSTRAINT `fk_ingredient_ingredient_category_id` FOREIGN KEY (`ingredient_category_id`) REFERENCES `ingredient_category` (`id`),
  CONSTRAINT `fk_ingredient_supplier_id` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`),
  CONSTRAINT `fk_ingredient_unit_id` FOREIGN KEY (`unit_id`) REFERENCES `unit` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ingredient`
--

LOCK TABLES `ingredient` WRITE;
/*!40000 ALTER TABLE `ingredient` DISABLE KEYS */;
INSERT INTO `ingredient` VALUES (1,'capheenloaii','Cà phê đen loại I',180,50000,'2025-04-26 15:31:27',1,1,2,7),(2,'capheentrunghai','Cà phê đen Trung Hải',80,55000,'2025-04-26 15:58:57',1,2,1,7),(3,'kem','Kem',15,50000,'2025-04-26 15:59:54',1,1,2,2),(4,'matchanhat','Matcha Nhật',365,80000,'2025-04-26 21:34:29',1,8,2,7),(5,'uongtrang','Đường trắng',120,25000,'2025-04-26 21:35:25',1,3,1,5),(6,'trathaomoc','Trà thảo mộc',1000,2500,'2025-04-26 22:34:03',1,7,1,2),(7,'caphehatrobusta','Cà phê hạt Robusta',90,150000,'2025-04-27 10:33:11',1,1,2,1),(8,'botcapherangxay','Bột cà phê rang xay',365,200000,'2025-04-27 10:33:41',1,1,2,1),(9,'traen','Trà đen',90,45000,'2025-04-27 10:34:16',1,7,1,7),(10,'traolong','Trà ô long',90,55000,'2025-04-27 10:34:56',1,7,1,7),(11,'trahoahong','Trà hoa hồng',80,68000,'2025-04-27 10:35:24',1,7,1,7),(12,'tralai','Trà lài',80,80000,'2025-04-27 10:35:47',1,7,1,7);
/*!40000 ALTER TABLE `ingredient` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ingredient_category`
--

DROP TABLE IF EXISTS `ingredient_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ingredient_category` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ingredient_category_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ingredient_category_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `parent_category_id` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ingredient_category`
--

LOCK TABLES `ingredient_category` WRITE;
/*!40000 ALTER TABLE `ingredient_category` DISABLE KEYS */;
INSERT INTO `ingredient_category` VALUES (1,'caphe','Cà phê','2025-04-25 09:13:27',1,NULL),(2,'caphearab','Cà phê Arab','2025-04-25 09:16:19',1,1),(3,'kemsua','Kem Sữa','2025-04-25 09:17:15',1,NULL),(4,'traxanh','Trà xanh','2025-04-26 10:04:10',0,NULL),(5,'trađa','Trà đá','2025-04-26 10:27:16',0,NULL),(6,'tempcate','temp cate','2025-04-26 10:31:13',0,NULL),(7,'traxanh','Trà xanh','2025-04-26 10:31:26',1,NULL),(8,'matcha','Matcha','2025-04-26 10:31:39',1,7),(9,'kemmuoi','Kem muối','2025-04-26 10:32:40',1,3),(10,'caphehat','Cà phê hạt','2025-04-27 13:53:28',1,1);
/*!40000 ALTER TABLE `ingredient_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory_audit`
--

DROP TABLE IF EXISTS `inventory_audit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory_audit` (
  `id` int NOT NULL AUTO_INCREMENT,
  `audit_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `audit_at` datetime NOT NULL,
  `auditor` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `note` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `warehouse_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_inventory_audit_warehouse_id` (`warehouse_id`),
  CONSTRAINT `fk_inventory_audit_warehouse_id` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_audit`
--

LOCK TABLES `inventory_audit` WRITE;
/*!40000 ALTER TABLE `inventory_audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `inventory_audit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory_discrepancy`
--

DROP TABLE IF EXISTS `inventory_discrepancy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory_discrepancy` (
  `id` int NOT NULL AUTO_INCREMENT,
  `inventory_audit_id` int NOT NULL,
  `stock_level_id` int NOT NULL,
  `expected_quantity` int NOT NULL,
  `actual_quantity` int NOT NULL,
  `discrepancy_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `fk_inventory_discrepancy_inventory_audit_id` (`inventory_audit_id`),
  KEY `fk_inventory_discrepancy_stock_level_id` (`stock_level_id`),
  CONSTRAINT `fk_inventory_discrepancy_inventory_audit_id` FOREIGN KEY (`inventory_audit_id`) REFERENCES `inventory_audit` (`id`),
  CONSTRAINT `fk_inventory_discrepancy_stock_level_id` FOREIGN KEY (`stock_level_id`) REFERENCES `stock_level` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_discrepancy`
--

LOCK TABLES `inventory_discrepancy` WRITE;
/*!40000 ALTER TABLE `inventory_discrepancy` DISABLE KEYS */;
/*!40000 ALTER TABLE `inventory_discrepancy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `invoice`
--

DROP TABLE IF EXISTS `invoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invoice` (
  `id` int NOT NULL AUTO_INCREMENT,
  `invoice_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_amount` decimal(18,0) NOT NULL,
  `payment_status` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `payment_method` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `account_id` int NOT NULL,
  `dining_table_id` int NOT NULL,
  `guest_count` int DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `fk_invoice_account_id` (`account_id`),
  KEY `fk_invoice_dining_table_id` (`dining_table_id`),
  CONSTRAINT `fk_invoice_account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `fk_invoice_dining_table_id` FOREIGN KEY (`dining_table_id`) REFERENCES `dining_table` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=74 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoice`
--

LOCK TABLES `invoice` WRITE;
/*!40000 ALTER TABLE `invoice` DISABLE KEYS */;
INSERT INTO `invoice` VALUES (1,'HD-2025-1',743000,'Chờ thanh toán','2025-04-22 11:05:10',1,'Tiền mặt',1,1,1),(2,'HD-2025-2',39000,'Chờ thanh toán','2025-04-22 11:20:19',1,'Tiền mặt',1,1,1),(3,'HD-2025-3',160000,'Chờ thanh toán','2025-04-22 11:21:17',1,'Chuyển khoản',1,3,1),(4,'HD-2025-4',94000,'Đã thanh toán','2025-04-22 11:23:16',1,'Tiền mặt',1,1,1),(5,'HD-2025-5',105000,'Chờ thanh toán','2025-04-22 11:25:36',1,'Tiền mặt',1,6,1),(6,'HD-2025-6',183000,'Đã thanh toán','2025-04-22 12:05:42',1,'Chuyển khoản',1,1,1),(7,'HD-2025-7',105000,'Đã hủy','2025-04-22 12:47:07',1,'Tiền mặt',1,8,1),(8,'HD-2025-8',55000,'Ðã thanh toán','2025-04-22 12:48:34',0,'Tiền mặt',1,8,1),(9,'HD-2025-9',144000,'Ðã thanh toán','2025-04-22 12:54:14',0,'Tiền mặt',1,1,1),(10,'HD-2025-10',55000,'Chờ thanh toán','2025-04-22 13:03:12',1,'Tiền mặt',1,1,1),(11,'HD-2025-11',50000,'Đã thanh toán','2025-04-22 13:52:01',1,'Tiền mặt',1,1,1),(12,'HD-2025-12',55000,'Đã thanh toán','2025-04-23 09:44:23',1,'Chuyển khoản',1,2,1),(13,'HD-2025-13',39000,'Đã thanh toán','2025-04-23 09:44:33',1,'Chuyển khoản',1,3,1),(14,'HD-2025-14',89000,'Đã thanh toán','2025-04-23 09:45:17',1,'Tiền mặt',1,4,1),(15,'HD-2025-15',94000,'Đã thanh toán','2025-04-24 08:55:40',1,'Tiền mặt',1,1,1),(16,'HD-2025-16',94000,'Đã thanh toán','2025-04-24 08:55:45',1,'Tiền mặt',1,9,1),(17,'HD-2025-17',50000,'Đã thanh toán','2025-04-25 09:11:39',1,'Tiền mặt',1,3,1),(18,'HD-2025-18',215000,'Đã thanh toán','2025-04-28 20:17:24',1,'Tiền mặt',1,1,1),(19,'HD-2025-19',55000,'Đã thanh toán','2025-04-28 20:26:59',1,'Tiền mặt',1,1,1),(20,'HD-2025-20',199000,'Đã thanh toán','2025-04-28 20:35:14',1,'Tiền mặt',1,2,1),(21,'HD-2025-21',94000,'Đã thanh toán','2025-04-30 23:35:10',1,'Tiền mặt',1,7,1),(22,'HD-2025-22',50000,'Đã thanh toán','2025-04-30 23:35:50',1,'Tiền mặt',1,9,1),(23,'HD-2025-23',105000,'Đã thanh toán','2025-05-07 22:33:47',1,'Tiền mặt',1,2,1),(24,'HD-2025-24',210000,'Đã thanh toán','2025-05-09 14:03:43',1,'Tiền mặt',1,1,1),(25,'HD-2025-25',55000,'Đã thanh toán','2025-05-09 14:06:14',1,'Tiền mặt',1,1,1),(26,'HD-2025-26',55000,'Đã thanh toán','2025-05-09 14:22:26',1,'Tiền mặt',1,1,1),(27,'HD-2025-27',55000,'Đã thanh toán','2025-05-13 21:30:26',1,'Tiền mặt',1,2,1),(28,'HD-2025-28',215000,'Đã thanh toán','2025-05-13 21:30:31',1,'Tiền mặt',1,1,1),(29,'HD-2025-29',275000,'Đã thanh toán','2025-05-13 21:30:55',1,'Tiền mặt',1,3,1),(30,'HD-2025-30',183000,'Đã thanh toán','2025-05-14 00:27:13',1,'Tiền mặt',1,1,1),(31,'HD-2025-31',144000,'Đã thanh toán','2025-05-14 01:24:51',1,'Tiền mặt',1,1,1),(32,'HD-2025-32',298000,'Đã thanh toán','2025-05-17 00:49:07',1,'Tiền mặt',1,3,1),(33,'HD-2025-33',464000,'Đã thanh toán','2025-05-20 06:33:28',1,'Tiền mặt',1,1,1),(34,'HD-2025-34',144000,'Đã thanh toán','2025-05-20 06:42:02',1,'Tiền mặt',1,1,1),(35,'HD-2025-35',110000,'Đã thanh toán','2025-05-20 06:46:15',1,'Tiền mặt',1,6,1),(36,'HD-2025-36',179000,'Đã thanh toán','2025-05-23 11:04:11',1,'Tiền mặt',1,1,1),(37,'HD-2025-37',257000,'Đã thanh toán','2025-05-24 16:36:16',1,'Tiền mặt',1,1,1),(38,'HD-2025-38',69000,'Đã thanh toán','2025-05-24 16:53:19',1,'Tiền mặt',10,1,1),(39,'HD-2025-39',188000,'Đã thanh toán','2025-05-24 17:34:15',1,'Tiền mặt',1,1,1),(40,'HD-2025-40',78000,'Đã thanh toán','2025-05-24 17:34:20',1,'Tiền mặt',1,4,1),(41,'HD-2025-41',149000,'Đã thanh toán','2025-05-24 17:34:25',1,'Tiền mặt',1,3,1),(42,'HD-2025-42',172000,'Đã thanh toán','2025-05-24 17:34:30',1,'Tiền mặt',1,8,1),(43,'HD-2025-43',128000,'Đã thanh toán','2025-05-24 17:34:35',1,'Tiền mặt',1,7,1),(44,'HD-2025-44',241000,'Đã thanh toán','2025-05-26 21:06:38',1,'Tiền mặt',1,1,1),(45,'HD-2025-45',177000,'Đã thanh toán','2025-05-27 20:37:19',1,'Chuyển khoản',1,1,1),(46,'HD-2025-46',138000,'Đã thanh toán','2025-05-27 21:10:48',1,'Tiền mặt',1,1,1),(47,'HD-2025-47',55000,'Chờ thanh toán','2025-05-27 21:36:23',1,'Tiền mặt',1,1,1),(48,'HD-2025-48',55000,'Đã thanh toán','2025-05-27 21:37:42',1,'Tiền mặt',1,1,1),(49,'HD-2025-49',55000,'Chờ thanh toán','2025-05-27 21:38:19',1,'Chuyển khoản',1,1,1),(50,'HD-2025-50',55000,'Chờ thanh toán','2025-05-27 21:39:51',1,'Tiền mặt',1,1,1),(51,'HD-2025-51',55000,'Chờ thanh toán','2025-05-27 21:40:09',1,'Chuyển khoản',1,1,1),(52,'HD-2025-52',55000,'Chờ thanh toán','2025-05-27 21:52:23',1,'Chuyển khoản',1,1,1),(53,'HD-2025-53',55000,'Chờ thanh toán','2025-05-27 22:10:26',1,'Chuyển khoản',1,1,1),(54,'HD-2025-54',55000,'Đã thanh toán','2025-05-27 22:33:24',1,'Tiền mặt',1,1,1),(55,'HD-2025-55',55000,'Đã thanh toán','2025-05-27 22:33:35',1,'Chuyển khoản',1,1,1),(56,'HD-2025-56',55000,'Đã thanh toán','2025-05-27 22:36:05',1,'Chuyển khoản',1,1,1),(57,'HD-2025-57',119000,'Đã thanh toán','2025-05-27 23:32:57',1,'Chuyển khoản',1,1,1),(58,'HD-2025-58',94000,'Đã thanh toán','2025-05-27 23:34:36',1,'Chuyển khoản',1,1,1),(59,'HD-2025-59',138000,'Đã thanh toán','2025-05-27 23:36:51',1,'Chuyển khoản',1,1,1),(60,'HD-2025-60',179000,'Chờ thanh toán','2025-05-27 23:39:13',1,'Chuyển khoản',1,1,1),(61,'HD-2025-61',55000,'Đã thanh toán','2025-05-27 23:40:49',1,'Tiền mặt',1,1,1),(62,'HD-2025-62',124000,'Chờ thanh toán','2025-05-27 23:41:15',1,'Chuyển khoản',1,2,1),(63,'HD-2025-63',50000,'Chờ thanh toán','2025-05-27 23:43:37',1,'Chuyển khoản',1,2,1),(64,'HD-2025-64',55000,'Chờ thanh toán','2025-05-27 23:51:49',1,'Chuyển khoản',1,1,1),(65,'HD-2025-65',193000,'Chờ thanh toán','2025-05-27 23:58:02',1,'Chuyển khoản',1,1,1),(66,'HD-2025-66',55000,'Chờ thanh toán','2025-05-28 00:00:12',1,'Chuyển khoản',1,1,1),(67,'HD-2025-67',55000,'Chờ thanh toán','2025-05-28 00:02:13',1,'Chuyển khoản',1,1,1),(68,'HD-2025-68',55000,'Đã thanh toán','2025-05-28 00:03:44',1,'Tiền mặt',1,3,1),(69,'638839875043283750',55000,'Đã thanh toán','2025-05-28 00:05:04',1,'Chuyển khoản',1,2,1),(70,'638839877632321303',55000,'Đã thanh toán','2025-05-28 00:09:23',1,'Chuyển khoản',1,2,1),(71,'HD-2025-1',55000,'Đã thanh toán','2025-05-28 00:11:03',1,'Tiền mặt',1,1,1),(72,'638840099232400461',138000,'Đã thanh toán','2025-05-28 06:18:43',1,'Chuyển khoản',1,1,1),(73,'638840108431908007',39000,'Đã thanh toán','2025-05-28 06:34:02',1,'Chuyển khoản',1,2,1);
/*!40000 ALTER TABLE `invoice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `invoice_detail`
--

DROP TABLE IF EXISTS `invoice_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invoice_detail` (
  `id` int NOT NULL AUTO_INCREMENT,
  `quantity` int NOT NULL,
  `unit_price` decimal(18,0) NOT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL,
  `invoice_id` int NOT NULL,
  `dish_id` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=234 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoice_detail`
--

LOCK TABLES `invoice_detail` WRITE;
/*!40000 ALTER TABLE `invoice_detail` DISABLE KEYS */;
INSERT INTO `invoice_detail` VALUES (1,10,50000,'2025-04-22 10:26:48',0,3,1),(2,3,55000,'2025-04-22 10:26:59',0,3,2),(3,2,39000,'2025-04-22 10:27:03',0,3,4),(4,10,50000,'2025-04-22 10:31:43',0,1,1),(5,3,55000,'2025-04-22 10:31:43',0,1,2),(6,2,39000,'2025-04-22 10:31:43',0,1,4),(7,10,50000,'2025-04-22 10:35:26',0,1,1),(8,3,55000,'2025-04-22 10:35:26',0,1,2),(9,2,39000,'2025-04-22 10:35:26',0,1,4),(10,10,50000,'2025-04-22 11:05:10',1,1,1),(11,3,55000,'2025-04-22 11:05:10',1,1,2),(12,2,39000,'2025-04-22 11:05:10',1,1,4),(13,1,39000,'2025-04-22 11:20:19',1,2,4),(14,1,50000,'2025-04-22 11:21:17',1,3,1),(15,1,55000,'2025-04-22 11:21:17',1,3,2),(16,1,55000,'2025-04-22 11:21:17',1,3,3),(17,1,55000,'2025-04-22 11:23:16',1,4,2),(18,1,39000,'2025-04-22 11:23:16',1,4,4),(19,1,50000,'2025-04-22 11:25:36',1,5,1),(20,1,55000,'2025-04-22 11:25:36',1,5,3),(21,1,50000,'2025-04-22 12:05:42',1,6,1),(22,1,55000,'2025-04-22 12:05:42',1,6,2),(23,2,39000,'2025-04-22 12:05:42',1,6,4),(24,1,50000,'2025-04-22 12:47:07',1,7,1),(25,1,55000,'2025-04-22 12:47:07',1,7,2),(26,1,55000,'2025-04-22 12:48:34',1,8,2),(27,1,55000,'2025-04-22 12:48:39',1,8,2),(28,1,50000,'2025-04-22 12:54:14',1,9,1),(29,1,55000,'2025-04-22 12:54:14',1,9,3),(30,1,39000,'2025-04-22 12:54:14',1,9,4),(31,1,50000,'2025-04-22 12:56:37',1,9,1),(32,1,55000,'2025-04-22 12:56:37',1,9,3),(33,1,39000,'2025-04-22 12:56:37',1,9,4),(34,1,55000,'2025-04-22 13:03:12',1,10,3),(35,1,50000,'2025-04-22 13:52:01',1,11,1),(36,1,50000,'2025-04-22 13:52:11',1,11,1),(37,1,55000,'2025-04-23 09:44:23',1,12,2),(38,1,55000,'2025-04-23 09:44:28',1,12,2),(39,1,39000,'2025-04-23 09:44:33',1,13,4),(40,1,39000,'2025-04-23 09:44:34',1,13,4),(41,1,50000,'2025-04-23 09:45:17',1,14,1),(42,1,39000,'2025-04-23 09:45:17',1,14,4),(43,1,50000,'2025-04-23 09:45:19',1,14,1),(44,1,39000,'2025-04-23 09:45:19',1,14,4),(45,1,55000,'2025-04-24 08:55:40',1,15,3),(46,1,39000,'2025-04-24 08:55:40',1,15,4),(47,1,55000,'2025-04-24 08:55:41',1,15,3),(48,1,39000,'2025-04-24 08:55:41',1,15,4),(49,1,55000,'2025-04-24 08:55:45',1,16,3),(50,1,39000,'2025-04-24 08:55:45',1,16,4),(51,1,55000,'2025-04-24 08:55:46',1,16,3),(52,1,39000,'2025-04-24 08:55:46',1,16,4),(53,1,50000,'2025-04-25 09:11:39',1,17,1),(54,1,50000,'2025-04-25 09:11:41',1,17,1),(55,1,50000,'2025-04-28 20:17:24',1,18,1),(56,1,55000,'2025-04-28 20:17:24',1,18,2),(57,2,55000,'2025-04-28 20:17:24',1,18,3),(58,1,50000,'2025-04-28 20:17:27',1,18,1),(59,1,55000,'2025-04-28 20:17:27',1,18,2),(60,2,55000,'2025-04-28 20:17:27',1,18,3),(61,1,55000,'2025-04-28 20:26:59',1,19,3),(62,1,55000,'2025-04-28 20:27:00',1,19,3),(63,1,50000,'2025-04-28 20:35:14',1,20,1),(64,1,55000,'2025-04-28 20:35:14',1,20,2),(65,1,55000,'2025-04-28 20:35:14',1,20,3),(66,1,39000,'2025-04-28 20:35:14',1,20,4),(67,1,50000,'2025-04-28 20:35:16',1,20,1),(68,1,55000,'2025-04-28 20:35:16',1,20,2),(69,1,55000,'2025-04-28 20:35:16',1,20,3),(70,1,39000,'2025-04-28 20:35:16',1,20,4),(71,1,55000,'2025-04-30 23:35:10',1,21,2),(72,1,39000,'2025-04-30 23:35:10',1,21,4),(73,1,55000,'2025-04-30 23:35:12',1,21,2),(74,1,39000,'2025-04-30 23:35:12',1,21,4),(75,1,50000,'2025-04-30 23:35:50',1,22,1),(76,1,50000,'2025-04-30 23:35:56',1,22,1),(77,1,50000,'2025-05-07 22:33:47',1,23,1),(78,1,55000,'2025-05-07 22:33:48',1,23,3),(79,1,50000,'2025-05-07 22:33:49',1,23,1),(80,1,55000,'2025-05-07 22:33:49',1,23,3),(81,2,50000,'2025-05-09 14:03:43',1,24,1),(82,2,55000,'2025-05-09 14:03:43',1,24,3),(83,2,50000,'2025-05-09 14:04:58',1,24,1),(84,2,55000,'2025-05-09 14:04:58',1,24,3),(85,1,55000,'2025-05-09 14:06:14',1,25,3),(86,1,55000,'2025-05-09 14:06:16',1,25,3),(87,1,55000,'2025-05-09 14:22:26',1,26,2),(88,1,55000,'2025-05-09 14:22:28',1,26,2),(89,1,55000,'2025-05-13 21:30:26',1,27,3),(90,1,55000,'2025-05-13 21:30:27',1,27,3),(91,1,50000,'2025-05-13 21:30:31',1,28,1),(92,2,55000,'2025-05-13 21:30:31',1,28,2),(93,1,55000,'2025-05-13 21:30:31',1,28,3),(94,1,50000,'2025-05-13 21:30:32',1,28,1),(95,2,55000,'2025-05-13 21:30:32',1,28,2),(96,1,55000,'2025-05-13 21:30:32',1,28,3),(97,2,55000,'2025-05-13 21:30:55',1,29,2),(98,3,55000,'2025-05-13 21:30:55',1,29,3),(99,2,55000,'2025-05-13 21:30:56',1,29,2),(100,3,55000,'2025-05-13 21:30:56',1,29,3),(101,1,50000,'2025-05-14 00:27:13',1,30,1),(102,1,55000,'2025-05-14 00:27:13',1,30,3),(103,2,39000,'2025-05-14 00:27:13',1,30,4),(104,1,50000,'2025-05-14 00:27:14',1,30,1),(105,1,55000,'2025-05-14 00:27:14',1,30,3),(106,2,39000,'2025-05-14 00:27:14',1,30,4),(107,1,50000,'2025-05-14 01:24:51',1,31,1),(108,1,55000,'2025-05-14 01:24:51',1,31,3),(109,1,39000,'2025-05-14 01:24:51',1,31,4),(110,1,50000,'2025-05-14 01:24:52',1,31,1),(111,1,55000,'2025-05-14 01:24:52',1,31,3),(112,1,39000,'2025-05-14 01:24:52',1,31,4),(113,2,55000,'2025-05-17 00:49:07',1,32,2),(114,2,55000,'2025-05-17 00:49:07',1,32,3),(115,2,39000,'2025-05-17 00:49:07',1,32,4),(116,2,55000,'2025-05-17 00:49:11',1,32,2),(117,2,55000,'2025-05-17 00:49:11',1,32,3),(118,2,39000,'2025-05-17 00:49:11',1,32,4),(119,3,50000,'2025-05-20 06:33:28',1,33,1),(120,3,55000,'2025-05-20 06:33:28',1,33,2),(121,2,55000,'2025-05-20 06:33:28',1,33,3),(122,1,39000,'2025-05-20 06:33:28',1,33,4),(123,3,50000,'2025-05-20 06:33:30',1,33,1),(124,3,55000,'2025-05-20 06:33:30',1,33,2),(125,2,55000,'2025-05-20 06:33:30',1,33,3),(126,1,39000,'2025-05-20 06:33:30',1,33,4),(127,1,50000,'2025-05-20 06:42:02',1,34,1),(128,1,55000,'2025-05-20 06:42:02',1,34,3),(129,1,39000,'2025-05-20 06:42:02',1,34,4),(130,1,50000,'2025-05-20 06:42:07',1,34,1),(131,1,55000,'2025-05-20 06:42:07',1,34,3),(132,1,39000,'2025-05-20 06:42:07',1,34,4),(133,1,55000,'2025-05-20 06:46:15',1,35,2),(134,1,55000,'2025-05-20 06:46:15',1,35,3),(135,1,55000,'2025-05-20 06:46:16',1,35,2),(136,1,55000,'2025-05-20 06:46:16',1,35,3),(137,1,55000,'2025-05-23 11:04:11',1,36,8),(138,1,55000,'2025-05-23 11:04:11',1,36,9),(139,1,69000,'2025-05-23 11:04:11',1,36,10),(140,1,55000,'2025-05-23 11:05:17',1,36,8),(141,1,55000,'2025-05-23 11:05:17',1,36,9),(142,1,69000,'2025-05-23 11:05:17',1,36,10),(143,1,39000,'2025-05-24 16:36:16',1,37,4),(144,1,39000,'2025-05-24 16:36:16',1,37,5),(145,2,55000,'2025-05-24 16:36:16',1,37,9),(146,1,69000,'2025-05-24 16:36:16',1,37,10),(147,1,39000,'2025-05-24 16:36:18',1,37,4),(148,1,39000,'2025-05-24 16:36:18',1,37,5),(149,2,55000,'2025-05-24 16:36:18',1,37,9),(150,1,69000,'2025-05-24 16:36:18',1,37,10),(151,1,69000,'2025-05-24 16:53:20',1,38,10),(152,1,69000,'2025-05-24 16:53:21',1,38,10),(153,1,50000,'2025-05-24 17:34:15',1,39,1),(154,1,69000,'2025-05-24 17:34:15',1,39,10),(155,1,69000,'2025-05-24 17:34:15',1,39,11),(156,1,50000,'2025-05-24 17:34:16',1,39,1),(157,1,69000,'2025-05-24 17:34:16',1,39,10),(158,1,69000,'2025-05-24 17:34:16',1,39,11),(159,1,39000,'2025-05-24 17:34:20',1,40,4),(160,1,39000,'2025-05-24 17:34:20',1,40,5),(161,1,39000,'2025-05-24 17:34:21',1,40,4),(162,1,39000,'2025-05-24 17:34:21',1,40,5),(163,1,55000,'2025-05-24 17:34:25',1,41,2),(164,1,55000,'2025-05-24 17:34:25',1,41,3),(165,1,39000,'2025-05-24 17:34:25',1,41,4),(166,1,55000,'2025-05-24 17:34:26',1,41,2),(167,1,55000,'2025-05-24 17:34:26',1,41,3),(168,1,39000,'2025-05-24 17:34:26',1,41,4),(169,1,55000,'2025-05-24 17:34:30',1,42,3),(170,3,39000,'2025-05-24 17:34:30',1,42,4),(171,1,55000,'2025-05-24 17:34:31',1,42,3),(172,3,39000,'2025-05-24 17:34:31',1,42,4),(173,1,50000,'2025-05-24 17:34:35',1,43,1),(174,1,39000,'2025-05-24 17:34:35',1,43,4),(175,1,39000,'2025-05-24 17:34:35',1,43,6),(176,1,50000,'2025-05-24 17:34:36',1,43,1),(177,1,39000,'2025-05-24 17:34:36',1,43,4),(178,1,39000,'2025-05-24 17:34:36',1,43,6),(179,1,39000,'2025-05-26 21:06:38',1,44,5),(180,2,39000,'2025-05-26 21:06:38',1,44,6),(181,1,55000,'2025-05-26 21:06:38',1,44,9),(182,1,69000,'2025-05-26 21:06:38',1,44,10),(183,1,39000,'2025-05-26 21:06:41',1,44,5),(184,2,39000,'2025-05-26 21:06:41',1,44,6),(185,1,55000,'2025-05-26 21:06:41',1,44,9),(186,1,69000,'2025-05-26 21:06:41',1,44,10),(187,1,39000,'2025-05-27 20:37:19',1,45,6),(188,1,69000,'2025-05-27 20:37:19',1,45,10),(189,1,69000,'2025-05-27 20:37:19',1,45,11),(190,1,39000,'2025-05-27 20:37:22',1,45,6),(191,1,69000,'2025-05-27 20:37:22',1,45,10),(192,1,69000,'2025-05-27 20:37:22',1,45,11),(193,1,69000,'2025-05-27 21:10:48',1,46,10),(194,1,69000,'2025-05-27 21:10:48',1,46,11),(195,1,69000,'2025-05-27 21:11:34',1,46,10),(196,1,69000,'2025-05-27 21:11:34',1,46,11),(197,1,55000,'2025-05-27 21:36:23',1,47,8),(198,1,55000,'2025-05-27 21:37:42',1,48,8),(199,1,55000,'2025-05-27 21:38:02',1,48,8),(200,1,55000,'2025-05-27 21:38:19',1,49,8),(201,1,55000,'2025-05-27 21:39:51',1,50,9),(202,1,55000,'2025-05-27 21:40:09',1,51,9),(203,1,55000,'2025-05-27 21:52:23',1,52,9),(204,1,55000,'2025-05-27 22:10:26',1,53,8),(205,1,55000,'2025-05-27 22:33:24',1,54,9),(206,1,55000,'2025-05-27 22:33:26',1,54,9),(207,1,55000,'2025-05-27 22:33:35',1,55,9),(208,1,55000,'2025-05-27 22:36:05',1,56,9),(209,1,50000,'2025-05-27 23:32:57',1,57,1),(210,1,69000,'2025-05-27 23:32:57',1,57,10),(211,1,39000,'2025-05-27 23:34:36',1,58,4),(212,1,55000,'2025-05-27 23:34:36',1,58,8),(213,1,69000,'2025-05-27 23:36:51',1,59,10),(214,1,69000,'2025-05-27 23:36:51',1,59,11),(215,2,55000,'2025-05-27 23:39:13',1,60,9),(216,1,69000,'2025-05-27 23:39:13',1,60,10),(217,1,55000,'2025-05-27 23:40:49',1,61,8),(218,1,55000,'2025-05-27 23:40:51',1,61,8),(219,1,55000,'2025-05-27 23:41:15',1,62,8),(220,1,69000,'2025-05-27 23:41:15',1,62,10),(221,1,50000,'2025-05-27 23:43:37',1,63,1),(222,1,55000,'2025-05-27 23:51:49',1,64,8),(223,1,55000,'2025-05-27 23:58:02',1,65,9),(224,2,69000,'2025-05-27 23:58:02',1,65,10),(225,1,55000,'2025-05-28 00:00:12',1,66,8),(226,1,55000,'2025-05-28 00:02:13',1,67,8),(227,1,55000,'2025-05-28 00:03:44',1,68,8),(228,1,55000,'2025-05-28 00:05:04',1,69,8),(229,1,55000,'2025-05-28 00:09:23',1,70,8),(230,1,55000,'2025-05-28 00:11:03',1,71,8),(231,1,55000,'2025-05-28 00:11:04',1,71,8),(232,2,69000,'2025-05-28 06:18:43',1,72,10),(233,1,39000,'2025-05-28 06:34:03',1,73,5);
/*!40000 ALTER TABLE `invoice_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_active` tinyint(1) DEFAULT NULL,
  `account_id` int NOT NULL,
  `notification_status_id` int DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sender_role_id` int NOT NULL,
  `description` longtext COLLATE utf8mb4_unicode_ci,
  `created_at` datetime DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `url` longtext COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `fk_notification_account_id` (`account_id`),
  KEY `fk_notification_notification_status_id` (`notification_status_id`),
  KEY `fk_notification_sender_role_id` (`sender_role_id`),
  CONSTRAINT `fk_notification_account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `fk_notification_notification_status_id` FOREIGN KEY (`notification_status_id`) REFERENCES `notification_status` (`id`),
  CONSTRAINT `fk_notification_sender_role_id` FOREIGN KEY (`sender_role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
INSERT INTO `notification` VALUES (5,1,1,1,'Đơn đặt món bàn a3 đã hoàn thành',6,'Done','2025-04-19 16:22:44',NULL,''),(6,1,1,1,'Đơn đặt món bàn a4 đã hoàn thành',6,'Done','2025-04-19 16:27:28',NULL,''),(7,1,1,1,'Đơn đặt món bàn a2 đã hoàn thành',6,'Done','2025-04-20 21:02:29',NULL,''),(8,1,1,1,'Đơn đặt món bàn 1 đã hoàn thành',6,'Done','2025-04-20 21:02:36',NULL,''),(9,1,1,1,'Đơn đặt món bàn a4 đã hoàn thành',6,'Done','2025-05-13 19:19:53',NULL,''),(10,1,1,1,'Đơn đặt món bàn a1 đã hoàn thành',6,'Done','2025-05-24 16:35:00',NULL,''),(11,1,1,1,'Đơn đặt món bàn a1 đã hoàn thành',6,'Done','2025-05-24 16:35:49',NULL,''),(12,1,10,2,'Đơn đặt món bàn a1 đã hoàn thành',6,'Done','2025-05-24 16:53:12',NULL,''),(13,1,1,1,'Đơn đặt món bàn a8 đã hoàn thành',6,'Done','2025-05-24 17:33:48',NULL,''),(14,1,1,1,'Đơn đặt món bàn a7 đã hoàn thành',6,'Done','2025-05-24 17:33:51',NULL,''),(15,1,1,1,'Đơn đặt món bàn a1 đã hoàn thành',6,'Done','2025-05-24 17:33:55',NULL,''),(16,1,1,1,'Đơn đặt món bàn a4 đã hoàn thành',6,'Done','2025-05-24 17:33:59',NULL,''),(17,1,1,1,'Đơn đặt món bàn a8 đã hoàn thành',6,'Done','2025-05-24 17:34:03',NULL,''),(18,1,1,1,'Đơn đặt món bàn a3 đã hoàn thành',6,'Done','2025-05-24 17:34:06',NULL,''),(19,1,1,1,'Đơn đặt món bàn a1 đã hoàn thành',6,'Done','2025-05-26 21:06:09',NULL,''),(20,1,1,1,'Đơn đặt món bàn a1 đã hoàn thành',6,'Done','2025-05-26 21:06:15',NULL,''),(21,1,1,1,'Đơn đặt món bàn a1 đã hoàn thành',6,'Done','2025-05-26 21:35:54',NULL,''),(22,1,1,1,'Đơn đặt món bàn a1 đã hoàn thành',6,'Done','2025-05-28 00:10:49',NULL,''),(23,1,1,1,'Đơn đặt món bàn a1 đã hoàn thành',6,'Done','2025-05-28 08:05:02',NULL,'');
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_status`
--

DROP TABLE IF EXISTS `notification_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_status` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_active` tinyint(1) DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` longtext COLLATE utf8mb4_unicode_ci,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_status`
--

LOCK TABLES `notification_status` WRITE;
/*!40000 ALTER TABLE `notification_status` DISABLE KEYS */;
INSERT INTO `notification_status` VALUES (1,1,'Đã xác nhận','Đã xác nhận','2025-04-19 16:22:19'),(2,1,'Chưa xác nhận','Chưa xác nhận','2025-04-19 16:22:27');
/*!40000 ALTER TABLE `notification_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `purchase_order`
--

DROP TABLE IF EXISTS `purchase_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_order` (
  `id` int NOT NULL AUTO_INCREMENT,
  `purchase_order_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_amount` decimal(18,0) NOT NULL,
  `payment_status` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `account_id` int NOT NULL,
  `ordered_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_purchase_order_account_id` (`account_id`),
  CONSTRAINT `fk_purchase_order_account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_order`
--

LOCK TABLES `purchase_order` WRITE;
/*!40000 ALTER TABLE `purchase_order` DISABLE KEYS */;
INSERT INTO `purchase_order` VALUES (1,'PO-000001',500000,'completed','2025-05-02 18:15:06',1,1,'2025-05-14 00:00:00'),(2,'PO-000006',300000,'pending','2025-05-02 22:53:58',1,1,'2025-05-03 00:00:00'),(3,'PO-000003',300000,'cancelled','2025-05-02 22:54:59',1,1,'2025-05-09 00:00:00'),(4,'PO-000004',150000,'completed','2025-05-02 23:04:28',1,1,'2025-05-09 00:00:00'),(5,'PO-000005',125000,'draft','2025-05-02 23:07:40',0,1,'2025-05-09 00:00:00'),(6,'PO-000006',1575000,'approved','2025-05-03 14:56:15',1,1,'2025-05-10 00:00:00'),(7,'PO-000007',25000,'draft','2025-05-03 15:01:11',1,5,'2025-05-10 00:00:00'),(8,'PO-000008',600000,'pending','2025-05-03 15:01:35',1,5,'2025-05-10 00:00:00'),(9,'PO-000009',550000,'completed','2025-05-08 11:25:24',1,1,'2025-05-15 00:00:00'),(10,'PO-000010',9500000,'completed','2025-05-08 13:53:45',1,1,'2025-05-08 00:00:00'),(11,'PO-000011',2500000,'completed','2025-05-08 14:08:09',1,1,'2025-05-15 00:00:00'),(12,'PO-000012',2400000,'completed','2025-05-08 16:22:20',1,1,'2025-05-15 00:00:00'),(13,'PO-000013',3400000,'completed','2025-05-08 16:26:10',1,1,'2025-05-15 00:00:00'),(14,'PO-000014',25000,'approved','2025-05-13 19:53:38',1,1,'2025-05-20 00:00:00'),(15,'PO-000015',600000,'completed','2025-05-28 00:13:48',1,1,'2025-06-04 00:00:00');
/*!40000 ALTER TABLE `purchase_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `purchase_order_detail`
--

DROP TABLE IF EXISTS `purchase_order_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_order_detail` (
  `id` int NOT NULL AUTO_INCREMENT,
  `quantity` int NOT NULL,
  `unit_price` decimal(18,0) NOT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `ingredient_id` int NOT NULL,
  `purchase_order_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_purchase_order_detail_ingredient_id` (`ingredient_id`),
  KEY `fk_purchase_order_detail_purchase_order_id` (`purchase_order_id`),
  CONSTRAINT `fk_purchase_order_detail_ingredient_id` FOREIGN KEY (`ingredient_id`) REFERENCES `ingredient` (`id`),
  CONSTRAINT `fk_purchase_order_detail_purchase_order_id` FOREIGN KEY (`purchase_order_id`) REFERENCES `purchase_order` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_order_detail`
--

LOCK TABLES `purchase_order_detail` WRITE;
/*!40000 ALTER TABLE `purchase_order_detail` DISABLE KEYS */;
INSERT INTO `purchase_order_detail` VALUES (1,10,50000,'2025-05-02 18:15:15',1,2,1),(2,50,5000,'2025-05-02 22:53:58',0,3,2),(3,5,60000,'2025-05-02 22:54:59',1,11,3),(4,3,50000,'2025-05-02 23:04:28',1,7,4),(5,5,25000,'2025-05-02 23:07:40',1,2,5),(6,50,5000,'2025-05-03 00:19:19',1,3,2),(7,10,5000,'2025-05-03 00:19:20',1,4,2),(8,10,55000,'2025-05-03 14:56:15',1,10,6),(9,5,45000,'2025-05-03 14:56:15',1,9,6),(10,10,80000,'2025-05-03 14:56:15',1,12,6),(11,1,25000,'2025-05-03 15:01:11',1,5,7),(12,3,200000,'2025-05-03 15:01:35',1,8,8),(13,10,55000,'2025-05-08 11:25:25',1,2,9),(14,50,150000,'2025-05-08 13:53:45',1,7,10),(15,10,200000,'2025-05-08 13:53:45',1,8,10),(16,30,50000,'2025-05-08 14:08:09',1,3,11),(17,10,100000,'2025-05-08 14:08:09',1,4,11),(18,18,50000,'2025-05-08 16:22:20',1,3,12),(19,30,50000,'2025-05-08 16:22:20',1,1,12),(20,50,68000,'2025-05-08 16:26:10',1,11,13),(21,1,25000,'2025-05-13 19:53:38',1,5,14),(22,10,60000,'2025-05-28 00:13:48',1,4,15);
/*!40000 ALTER TABLE `purchase_order_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `id` int NOT NULL AUTO_INCREMENT,
  `role_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'ADMIN','Admin','2025-03-21 09:18:33',1),(2,'QL-002','Quản lý','2025-04-13 22:31:16',0),(3,'QL-003','Quản lý','2025-04-13 22:31:37',0),(4,'QL-004','Quản lý','2025-04-13 22:31:43',1),(5,'PC-005','Nhân Viên Pha Chế','2025-04-13 22:32:08',0),(6,'PC-006','Nhân Viên Pha Chế','2025-04-13 22:32:12',1),(7,'PC-007','Nhân Viên Phục Vụ','2025-04-13 22:32:21',0),(8,'PC-008','Nhân Viên Phục Vụ','2025-04-13 22:32:24',1);
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schema_migration_history`
--

DROP TABLE IF EXISTS `schema_migration_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `schema_migration_history` (
  `migration_id` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_version` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`migration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schema_migration_history`
--

LOCK TABLES `schema_migration_history` WRITE;
/*!40000 ALTER TABLE `schema_migration_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `schema_migration_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock_level`
--

DROP TABLE IF EXISTS `stock_level`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_level` (
  `id` int NOT NULL AUTO_INCREMENT,
  `quantity` int NOT NULL,
  `expiration_at` datetime NOT NULL,
  `unit_price` decimal(18,0) NOT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `ingredient_id` int NOT NULL,
  `warehouse_id` int NOT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_stock_level_ingredient_id` (`ingredient_id`),
  KEY `fk_stock_level_warehouse_id` (`warehouse_id`),
  CONSTRAINT `fk_stock_level_ingredient_id` FOREIGN KEY (`ingredient_id`) REFERENCES `ingredient` (`id`),
  CONSTRAINT `fk_stock_level_warehouse_id` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_level`
--

LOCK TABLES `stock_level` WRITE;
/*!40000 ALTER TABLE `stock_level` DISABLE KEYS */;
INSERT INTO `stock_level` VALUES (1,6,'2025-06-27 08:39:23',9000,'2025-04-27 08:44:59',1,1,1,'2025-05-08 13:40:12'),(2,0,'2025-04-27 20:48:19',50000,'2025-04-27 20:48:52',0,2,1,'2025-05-08 11:28:22'),(3,0,'2015-04-27 20:49:52',0,'2025-04-27 20:50:06',0,3,1,'2025-05-08 11:23:29'),(4,0,'2025-08-01 14:26:33',50000,'2025-05-03 14:26:33',0,7,1,'2025-05-08 11:23:29'),(5,0,'2025-07-22 14:30:20',50000,'2025-05-03 14:30:20',0,2,1,'2025-05-08 11:28:33'),(6,10,'2026-05-04 00:00:00',30000,'2025-05-04 18:51:28',1,1,1,'2025-05-04 18:51:28'),(7,0,'2026-05-04 00:00:00',30000,'2025-05-04 19:04:22',0,3,1,'2025-05-08 11:23:29'),(8,5,'2026-05-07 00:00:00',34000,'2025-05-07 11:33:42',1,3,1,'2025-05-17 08:43:38'),(9,0,'2025-07-27 11:25:44',55000,'2025-05-08 11:25:44',0,2,1,'2025-05-08 11:31:09'),(10,40,'2025-08-06 14:06:29',150000,'2025-05-08 14:06:29',1,7,1,'2025-05-28 00:16:41'),(11,10,'2026-05-08 14:06:29',200000,'2025-05-08 14:06:29',1,8,1,'2025-05-08 14:06:29'),(12,0,'2025-05-23 14:08:25',50000,'2025-05-08 14:08:25',0,3,1,'2025-05-08 14:10:01'),(13,20,'2026-05-08 14:08:25',100000,'2025-05-08 14:08:25',1,4,1,'2025-05-17 08:44:02'),(14,18,'2025-05-23 16:24:32',50000,'2025-05-08 16:24:32',1,3,1,'2025-05-08 16:24:32'),(15,30,'2025-11-04 16:24:32',50000,'2025-05-08 16:24:32',1,1,1,'2025-05-08 16:24:32'),(16,50,'2025-07-27 16:26:22',68000,'2025-05-08 16:26:22',1,11,1,'2025-05-08 16:26:22'),(17,15,'2026-07-28 00:00:00',80000,'2025-05-17 08:44:02',1,12,1,'2025-05-24 14:25:45'),(18,20,'2026-05-28 00:14:23',60000,'2025-05-28 00:14:23',1,4,1,'2025-05-28 00:14:53');
/*!40000 ALTER TABLE `stock_level` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock_transaction`
--

DROP TABLE IF EXISTS `stock_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_transaction` (
  `id` int NOT NULL AUTO_INCREMENT,
  `stock_transaction_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `note` longtext COLLATE utf8mb4_unicode_ci,
  `transaction_type` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `total_amount` decimal(18,0) NOT NULL,
  `status` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `warehouse_id` int NOT NULL,
  `account_id` int NOT NULL,
  `transaction_at` datetime DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `approved_by_account_id` int DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL,
  `completed_by_account_id` int DEFAULT NULL,
  `canceled_at` datetime DEFAULT NULL,
  `canceled_by_account_id` int DEFAULT NULL,
  `status_history` longtext COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `fk_stock_transaction_account_id` (`account_id`),
  KEY `fk_stock_transaction_warehouse_id` (`warehouse_id`),
  CONSTRAINT `fk_stock_transaction_account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `fk_stock_transaction_warehouse_id` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_transaction`
--

LOCK TABLES `stock_transaction` WRITE;
/*!40000 ALTER TABLE `stock_transaction` DISABLE KEYS */;
INSERT INTO `stock_transaction` VALUES (2,'TX-IN-AUTO-PO-000004','Hệ thống nhập kho tự động cho đơn nhập hàng PO-000004','IMPORT',150000,'completed','2025-05-03 14:26:21',1,1,1,'2025-05-03 14:26:21',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(3,'TX-IN-AUTO-PO-000001','Hệ thống nhập kho tự động cho đơn nhập hàng PO-000001','IMPORT',500000,'completed','2025-05-03 14:30:20',1,1,1,'2025-05-03 14:30:20',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(4,'ADJ-20250504-291a2cfa','correction: ','ADJUSTMENT_IN',100000,'completed','2025-05-04 17:59:04',1,1,1,'2025-05-04 17:59:04',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(5,'ADJ-20250504-c65d4fb6','correction: ','ADJUSTMENT_IN',150000,'completed','2025-05-04 17:59:41',1,1,1,'2025-05-04 17:59:41',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(6,'ADJ-20250507-8844ccb3','damage: ','ADJUSTMENT_OUT',50000,'completed','2025-05-07 11:20:28',1,1,1,'2025-05-07 11:20:28',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(7,'ADJ-20250507-9b9129e3','correction: ','ADJUSTMENT_IN',250000,'completed','2025-05-07 11:20:41',1,1,1,'2025-05-07 11:20:41',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(8,'ADJ-20250507-70e5835b','correction: ','ADJUSTMENT_IN',250000,'completed','2025-05-07 11:21:23',1,1,1,'2025-05-07 11:21:23',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(9,'ADJ-20250507-40e4ac2c','expired: ','ADJUSTMENT_IN',150000,'completed','2025-05-07 17:42:26',1,1,1,'2025-05-07 17:42:26',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(10,'TX-IN-20250507203742','','IMPORT',150000,'pending','2025-05-07 20:37:42',1,1,1,'2025-05-07 20:37:42',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(11,'TX-IN-20250507203909','','IMPORT',150000,'pending','2025-05-07 20:39:09',1,1,1,'2025-05-07 20:39:09',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(12,'TX-IN-20250507204007','','IMPORT',100000,'pending','2025-05-07 20:40:07',1,1,1,'2025-05-07 20:40:07',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(13,'TX-IN-20250507205407','nhập hàng 07/07 cf đen','IMPORT',50000,'completed','2025-05-07 20:54:07',1,1,1,'2025-05-07 20:54:07',NULL,NULL,'2025-05-08 13:40:12',1,NULL,NULL,'[{\"Status\":\"pending\",\"Date\":\"2025-05-08T11:18:45.4853795+07:00\",\"UserId\":1,\"Reason\":\"\"},{\"Status\":\"completed\",\"Date\":\"2025-05-08T13:40:12.9997283+07:00\",\"UserId\":1,\"Reason\":\"\"}]'),(14,'TX-OUT-20250507233630','','EXPORT',500000,'completed','2025-05-07 23:36:30',1,1,1,'2025-05-07 23:36:30',NULL,NULL,'2025-05-08 11:16:43',1,NULL,NULL,'[{\"Status\":\"completed\",\"Date\":\"2025-05-08T11:16:43.9202991+07:00\",\"UserId\":1,\"Reason\":\"\"}]'),(15,'TX-OUT-20250508112325','xuất cho bếp 1','EXPORT',1030000,'completed','2025-05-08 11:23:25',1,1,1,'2025-05-08 11:23:25',NULL,NULL,'2025-05-08 11:23:29',1,NULL,NULL,'[{\"Status\":\"completed\",\"Date\":\"2025-05-08T11:23:29.9576817+07:00\",\"UserId\":1,\"Reason\":\"\"}]'),(16,'TX-IN-AUTO-PO-000009','Hệ thống nhập kho tự động cho đơn nhập hàng PO-000009','IMPORT',550000,'completed','2025-05-08 11:25:44',1,1,1,'2025-05-08 11:25:44',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(17,'TX-OUT-20250508112616','','EXPORT',1035000,'completed','2025-05-08 11:26:16',1,1,1,'2025-05-08 11:26:16',NULL,NULL,'2025-05-08 11:27:02',1,NULL,NULL,'[{\"Status\":\"completed\",\"Date\":\"2025-05-08T11:29:45.6050898+07:00\",\"UserId\":1,\"Reason\":\"\"}]'),(18,'TX-OUT-20250508113103','','EXPORT',165000,'completed','2025-05-08 11:31:03',1,1,1,'2025-05-08 11:31:03',NULL,NULL,'2025-05-08 11:31:09',1,NULL,NULL,'[{\"Status\":\"completed\",\"Date\":\"2025-05-08T11:31:09.0496065+07:00\",\"UserId\":1,\"Reason\":\"\"}]'),(19,'TX-IN-AUTO-PO-000010','Hệ thống nhập kho tự động cho đơn nhập hàng PO-000010','IMPORT',9500000,'completed','2025-05-08 14:06:29',1,1,1,'2025-05-08 14:06:29',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(20,'TX-IN-AUTO-PO-000011','Hệ thống nhập kho tự động cho đơn nhập hàng PO-000011','IMPORT',2500000,'completed','2025-05-08 14:08:25',1,1,1,'2025-05-08 14:08:25',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(21,'TX-OUT-20250508140914',' | không cần thiết','EXPORT',200000,'canceled','2025-05-08 14:09:14',1,1,1,'2025-05-08 14:09:14',NULL,NULL,NULL,NULL,'2025-05-08 14:09:38',1,'[{\"Status\":\"canceled\",\"Date\":\"2025-05-08T14:09:38.920982+07:00\",\"UserId\":1,\"Reason\":\"không cần thiết\"}]'),(22,'TX-OUT-20250508140954','','EXPORT',1500000,'completed','2025-05-08 14:09:54',1,1,1,'2025-05-08 14:09:54',NULL,NULL,'2025-05-08 14:10:01',1,NULL,NULL,'[{\"Status\":\"completed\",\"Date\":\"2025-05-08T14:10:01.2749833+07:00\",\"UserId\":1,\"Reason\":\"\"}]'),(23,'TX-IN-AUTO-PO-000012','Hệ thống nhập kho tự động cho đơn nhập hàng PO-000012','IMPORT',2400000,'completed','2025-05-08 16:24:32',1,1,1,'2025-05-08 16:24:32',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(24,'TX-IN-AUTO-PO-000013','Hệ thống nhập kho tự động cho đơn nhập hàng PO-000013','IMPORT',3400000,'completed','2025-05-08 16:26:22',1,1,1,'2025-05-08 16:26:22',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(25,'TX-IN-20250513195541','','IMPORT',50000,'completed','2025-05-13 19:55:41',1,1,1,'2025-05-13 19:55:41',NULL,NULL,'2025-05-17 08:43:38',1,NULL,NULL,'[{\"Status\":\"completed\",\"Date\":\"2025-05-17T08:43:38.4654638+07:00\",\"UserId\":1,\"Reason\":\"\"}]'),(26,'TX-IN-20250517084302','','IMPORT',1800000,'completed','2025-05-17 08:43:02',1,1,1,'2025-05-17 08:43:02',NULL,NULL,'2025-05-17 08:44:02',1,NULL,NULL,'[{\"Status\":\"pending\",\"Date\":\"2025-05-17T08:43:30.7414112+07:00\",\"UserId\":1,\"Reason\":\"\"},{\"Status\":\"completed\",\"Date\":\"2025-05-17T08:44:02.7179458+07:00\",\"UserId\":1,\"Reason\":\"\"}]'),(27,'ADJ-20250520-35992aeb','correction: ','ADJUSTMENT_IN',80000,'completed','2025-05-20 06:39:34',1,1,1,'2025-05-20 06:39:34',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(28,'ADJ-20250524-05203d2a','correction: ','ADJUSTMENT_IN',320000,'completed','2025-05-24 14:25:45',1,1,1,'2025-05-24 14:25:45',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(29,'TX-IN-AUTO-PO-000015','Hệ thống nhập kho tự động cho đơn nhập hàng PO-000015','IMPORT',600000,'completed','2025-05-28 00:14:23',1,1,1,'2025-05-28 00:14:23',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(30,'ADJ-20250528-09f3d595','damage: ','ADJUSTMENT_IN',300000,'completed','2025-05-28 00:14:46',1,1,1,'2025-05-28 00:14:46',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(31,'ADJ-20250528-2e15bd9f','correction: ','ADJUSTMENT_IN',300000,'completed','2025-05-28 00:14:53',1,1,1,'2025-05-28 00:14:53',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(32,'TX-IN-20250528001603','','IMPORT',80000,'pending','2025-05-28 00:16:03',1,1,1,'2025-05-28 00:16:03',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(33,'TX-OUT-20250528001630','','EXPORT',1500000,'completed','2025-05-28 00:16:30',1,1,1,'2025-05-28 00:16:30',NULL,NULL,'2025-05-28 00:16:41',1,NULL,NULL,'[{\"Status\":\"completed\",\"Date\":\"2025-05-28T00:16:41.2679505+07:00\",\"UserId\":1,\"Reason\":\"\"}]');
/*!40000 ALTER TABLE `stock_transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock_transaction_detail`
--

DROP TABLE IF EXISTS `stock_transaction_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_transaction_detail` (
  `id` int NOT NULL AUTO_INCREMENT,
  `stock_level_id` int NOT NULL,
  `stock_transaction_id` int NOT NULL,
  `quantity` int NOT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `fk_stock_transaction_detail_stock_level_id` (`stock_level_id`),
  KEY `fk_stock_transaction_detail_stock_transaction_id` (`stock_transaction_id`),
  CONSTRAINT `fk_stock_transaction_detail_stock_level_id` FOREIGN KEY (`stock_level_id`) REFERENCES `stock_level` (`id`),
  CONSTRAINT `fk_stock_transaction_detail_stock_transaction_id` FOREIGN KEY (`stock_transaction_id`) REFERENCES `stock_transaction` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_transaction_detail`
--

LOCK TABLES `stock_transaction_detail` WRITE;
/*!40000 ALTER TABLE `stock_transaction_detail` DISABLE KEYS */;
INSERT INTO `stock_transaction_detail` VALUES (1,4,2,3,'2025-05-03 14:26:35',1),(2,5,3,10,'2025-05-03 14:30:20',1),(3,5,4,2,'2025-05-04 17:59:04',1),(4,4,5,3,'2025-05-04 17:59:41',1),(5,4,6,1,'2025-05-07 11:20:29',1),(6,4,7,5,'2025-05-07 11:20:41',1),(7,4,8,5,'2025-05-07 11:21:23',1),(8,8,9,5,'2025-05-07 17:42:26',1),(9,3,14,1,'2025-05-08 11:16:43',1),(10,4,14,10,'2025-05-08 11:16:43',1),(11,3,15,24,'2025-05-08 11:23:29',1),(12,7,15,5,'2025-05-08 11:23:29',1),(13,8,15,21,'2025-05-08 11:23:29',1),(14,4,15,5,'2025-05-08 11:23:29',1),(15,9,16,10,'2025-05-08 11:25:44',1),(16,2,17,1,'2025-05-08 11:28:26',1),(17,5,17,12,'2025-05-08 11:28:36',1),(18,9,17,7,'2025-05-08 11:28:46',1),(19,9,18,3,'2025-05-08 11:31:09',1),(20,1,13,1,'2025-05-08 13:40:12',1),(21,10,19,50,'2025-05-08 14:06:29',1),(22,11,19,10,'2025-05-08 14:06:29',1),(23,12,20,30,'2025-05-08 14:08:25',1),(24,13,20,10,'2025-05-08 14:08:25',1),(25,12,22,30,'2025-05-08 14:10:01',1),(26,14,23,18,'2025-05-08 16:24:32',1),(27,15,23,30,'2025-05-08 16:24:32',1),(28,16,24,50,'2025-05-08 16:26:22',1),(29,8,25,1,'2025-05-17 08:43:38',1),(30,13,26,10,'2025-05-17 08:44:02',1),(31,17,26,10,'2025-05-17 08:44:02',1),(32,17,27,1,'2025-05-20 06:39:34',1),(33,17,28,4,'2025-05-24 14:25:45',1),(34,18,29,10,'2025-05-28 00:14:23',1),(35,18,30,5,'2025-05-28 00:14:46',1),(36,18,31,5,'2025-05-28 00:14:53',1),(37,10,33,10,'2025-05-28 00:16:41',1);
/*!40000 ALTER TABLE `stock_transaction_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock_transaction_draft_detail`
--

DROP TABLE IF EXISTS `stock_transaction_draft_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_transaction_draft_detail` (
  `id` int NOT NULL AUTO_INCREMENT,
  `stock_transaction_id` int NOT NULL,
  `ingredient_id` int NOT NULL,
  `quantity` int NOT NULL,
  `unit_price` decimal(18,2) NOT NULL,
  `expiration_at` datetime NOT NULL,
  `note` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_new_batch` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL,
  `stock_level_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_stock_transaction_draft_detail_ingredient_id` (`ingredient_id`),
  KEY `idx_stock_transaction_draft_detail_stock_level_id` (`stock_level_id`),
  KEY `idx_stock_transaction_draft_detail_stock_transaction_id` (`stock_transaction_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_transaction_draft_detail`
--

LOCK TABLES `stock_transaction_draft_detail` WRITE;
/*!40000 ALTER TABLE `stock_transaction_draft_detail` DISABLE KEYS */;
INSERT INTO `stock_transaction_draft_detail` VALUES (1,10,1,5,50000.00,'2026-05-07 00:00:00',NULL,0,'2025-05-07 20:37:42',NULL),(2,10,4,10,100000.00,'2025-05-30 00:00:00',NULL,0,'2025-05-07 20:37:42',NULL),(3,11,7,1,150000.00,'2026-05-07 00:00:00',NULL,0,'2025-05-07 20:39:14',NULL),(4,12,4,5,100000.00,'2026-05-07 00:00:00',NULL,0,'2025-05-07 20:40:21',NULL),(13,21,8,1,200000.00,'2025-05-08 14:09:14',NULL,0,'2025-05-08 14:09:14',NULL),(18,32,4,10,80000.00,'2026-05-28 00:00:00',NULL,0,'2025-05-28 00:16:03',NULL);
/*!40000 ALTER TABLE `stock_transaction_draft_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `supplier`
--

DROP TABLE IF EXISTS `supplier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier` (
  `id` int NOT NULL AUTO_INCREMENT,
  `supplier_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contact_info` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `supplier_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplier`
--

LOCK TABLES `supplier` WRITE;
/*!40000 ALTER TABLE `supplier` DISABLE KEYS */;
INSERT INTO `supplier` VALUES (1,'congtytnhhcafetrungnamhai','0988647249','Công ty TNHH Cafe Trung Nam Hải','234 An Lão, Hải Phòng','2025-04-25 11:04:51',1),(2,'tapoantrungnguyenlegend','1900 6011','Tập đoàn Trung Nguyên Legend','82-84 Bùi Thị Xuân, P. Bến Thành, Q.1, Tp Hồ Chí Minh','2025-04-26 10:40:31',1),(3,'congtytnhhnguyenlieuhungcuong','0315996465','Công ty TNHH Nguyên liệu Hùng Cường','Tầng 12, Tháp A2, Viettel Tower 285 Cách Mạng Tháng Tám, Phường 12, Quận 10, Thành phố HCM, Việt Nam.','2025-05-01 10:45:16',1),(4,'congtytnhhnestlevietnam','consumer.services@vn.nestle.com','Công ty TNHH Nestle Việt Nam','Lầu 5, Empress Tower, 138-142 Hai Bà Trưng, Phường Đa Kao, Quận 1, Tp.Hồ Chí Minh','2025-05-09 15:09:55',1),(5,'congtycophanmascopex','025 3831079','Công ty cổ phần Mascopex','38B Nguyễn Biểu, Phường Vĩnh Hải, Thành phố Nha Trang, Khánh Hòa','2025-05-09 17:03:39',1),(6,'congtycpcafekantata','keo.kantata@gmail.com','Công Ty CP Cafe Kantata','18 P. Võ Văn Dũng, Chợ Dừa, Đống Đa, Hà Nội','2025-05-09 17:47:45',1),(7,'congtytnhhkingcoffee','pcs@tnicorporation.com','Công ty TNHH King Coffee','208 Nguyễn Duy, Phường 9, Quận 8, TPHCM','2025-05-09 21:02:46',1),(8,'congtycophanvinacafebienhoa','info@vietnamreport.net','Công ty Cổ phần Vinacafe Biên Hòa','Khu công nghiệp Biên, Hoà 1, Đồng Nai','2025-05-09 21:03:55',1),(9,'SUP-12345','0912345678','Công ty TNHH Thương Mại Sài Gòn','123 Lê Lợi, Quận 1, TP.HCM','2024-12-22 09:15:23',1),(10,'SUP-67890','info@phucsinh.com','Công ty CP Phúc Sinh','36 Phạm Văn Đồng, Thủ Đức, TP.HCM','2024-12-23 14:30:45',1),(11,'SUP-45678','02438651234','Công ty TNHH Cafe Hòa Tan','45 Nguyễn Huệ, TP. Huế','2024-12-25 10:20:12',1),(12,'SUP-98765','support@anphatcoffee.vn','Công ty TNHH An Phát','78 Trần Phú, Nha Trang, Khánh Hòa','2024-12-26 16:45:33',1),(13,'SUP-23456','0935123456','Công ty CP Nguyên Liệu Việt','56 Nguyễn Trãi, Quận 5, TP.HCM','2024-12-28 11:10:56',1),(14,'SUP-78901','0312345678','Công ty TNHH Sản Xuất Minh Anh','12 Lý Thường Kiệt, Đà Nẵng','2024-12-30 08:25:17',1),(15,'SUP-34567','contact@thienphu.com','Công ty CP Thiên Phú','89 Hùng Vương, TP. Vinh, Nghệ An','2025-01-02 13:40:22',1),(16,'SUP-89012','0987654321','Công ty TNHH Cafe Sao Vàng','34 Nguyễn Đình Chiểu, Quận 3, TP.HCM','2025-01-05 15:55:44',1),(17,'SUP-56789','0243999888','Công ty CP Nông Sản Hà Nội','67 Kim Mã, Ba Đình, Hà Nội','2025-01-08 17:20:11',1),(18,'SUP-12389','info@vietcoffee.vn','Công ty TNHH Viet Coffee','45 Lê Văn Sỹ, Quận Tân Bình, TP.HCM','2025-01-10 09:35:33',1),(19,'SUP-45612','0918765432','Công ty CP Sản Xuất Hoàng Gia','23 Nguyễn Văn Cừ, TP. Hạ Long, Quảng Ninh','2025-01-12 12:15:55',1),(20,'SUP-78934','02838381234','Công ty TNHH Nguyên Liệu Á Châu','78 Nguyễn Thị Minh Khai, Quận 3, TP.HCM','2025-01-15 14:25:17',1),(21,'SUP-23478','contact@saigonespresso.vn','Công ty TNHH Saigon Espresso','56 Phạm Ngọc Thạch, Quận 3, TP.HCM','2025-01-18 16:40:22',1),(22,'SUP-67812','0933456789','Công ty CP Cafe Đắk Lắk','34 Nguyễn Văn Trỗi, TP. Buôn Ma Thuột','2025-01-20 10:50:44',1),(23,'SUP-34512','02436669999','Công ty TNHH Thương Mại Bắc Việt','89 Hoàn Kiếm, Hà Nội','2025-01-22 13:05:11',1),(24,'SUP-89023','0912349876','Công ty CP Nông Sản Sạch','45 Lê Đại Hành, TP. Đà Lạt, Lâm Đồng','2025-01-25 15:20:33',1),(25,'SUP-56712','info@dalatcoffee.vn','Công ty TNHH Cafe Đà Lạt','67 Trần Quốc Toản, TP. Đà Lạt','2025-01-28 17:35:55',1),(26,'SUP-12378','02839997777','Công ty CP Nguyên Liệu Tâm An','12 Nguyễn Thị Định, Quận 2, TP.HCM','2025-02-01 09:45:17',1),(27,'SUP-45623','0945671234','Công ty TNHH Cafe Hương Việt','34 Bùi Viện, Quận 1, TP.HCM','2025-02-05 11:55:22',1),(28,'SUP-78956','contact@vietnamcoffee.vn','Công ty CP Vietnam Coffee','78 Lý Tự Trọng, TP. Cần Thơ','2025-02-10 14:10:44',1);
/*!40000 ALTER TABLE `supplier` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `table_booking`
--

DROP TABLE IF EXISTS `table_booking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `table_booking` (
  `id` int NOT NULL AUTO_INCREMENT,
  `expected_arrive_time` datetime NOT NULL,
  `check_in_at` datetime DEFAULT NULL,
  `booking_status` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `deposit_amount` decimal(18,0) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `account_id` int NOT NULL,
  `dining_table_id` int NOT NULL,
  `customer_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone_number` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_table_booking_account_id` (`account_id`),
  KEY `fk_table_booking_dining_table_id` (`dining_table_id`),
  CONSTRAINT `fk_table_booking_account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
  CONSTRAINT `fk_table_booking_dining_table_id` FOREIGN KEY (`dining_table_id`) REFERENCES `dining_table` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `table_booking`
--

LOCK TABLES `table_booking` WRITE;
/*!40000 ALTER TABLE `table_booking` DISABLE KEYS */;
INSERT INTO `table_booking` VALUES (1,'2025-04-24 19:00:00',NULL,'cancelled',100000,'2025-04-23 16:23:18',1,1,3,'Trịnh Huy Khôi','0353022377','Note tạm thời'),(2,'2025-04-23 15:45:00',NULL,'expired',0,'2025-04-23 16:37:35',1,1,2,'Trịnh Huy Khôi','0353022377','Note tạm thời'),(3,'2025-04-23 18:00:00','2025-04-23 16:41:25','completed',0,'2025-04-23 16:40:25',1,1,8,'Trịnh Huy Khôi','0353022377','Note tạm thời'),(4,'2025-04-23 18:00:00',NULL,'expired',60000,'2025-04-23 20:48:17',1,1,2,'Trịnh Huy Khôi','0353022377','Note tạm thời'),(5,'2025-04-28 18:00:00','2025-04-23 21:16:42','completed',100000,'2025-04-23 21:16:31',1,1,2,'Trịnh Huy Khôi','0353022377','Note tạm thời'),(6,'2025-04-25 18:00:00','2025-04-23 21:20:38','completed',0,'2025-04-23 21:20:16',1,1,7,'Trịnh Huy Khôi','0353022377','Note tạm thời'),(7,'2025-04-23 21:40:00',NULL,'expired',0,'2025-04-23 21:26:38',1,1,10,'Nguyễn  Hải Ninh','0988254236','Note tạm thời'),(8,'2025-04-24 08:55:00','2025-04-24 08:48:22','completed',0,'2025-04-24 08:37:00',1,1,9,'Minh Tuấn','0294596633','Note tạm thời'),(9,'2025-04-24 09:15:00',NULL,'cancelled',0,'2025-04-24 08:54:55',1,1,2,'Trịnh Huy Khôi','0353022377','Note tạm thời'),(10,'2025-04-25 18:00:00',NULL,'expired',0,'2025-04-25 09:11:04',1,1,1,'Đặng Minh Đức','0962057739','Note tạm thời'),(11,'2025-04-25 19:00:00',NULL,'expired',0,'2025-04-25 19:20:25',1,1,1,'Khôi Trịnh','0962057739','Note tạm thời'),(12,'2025-04-30 18:00:00','2025-04-28 20:27:48','completed',0,'2025-04-28 20:27:19',1,1,7,'Khôi Trịnh','0962057739','Note tạm thời'),(13,'2025-05-03 20:00:00',NULL,'cancelled',0,'2025-04-28 20:30:33',1,1,3,'Khôi Trịnh','0962057739','Note tạm thời'),(14,'2025-05-31 18:00:00',NULL,'confirmed',0,'2025-05-09 11:05:24',1,1,1,'Khôi Trịnh','0962057739','Note tạm thời'),(15,'2025-05-13 18:00:00',NULL,'expired',0,'2025-05-13 19:51:55',1,1,6,'Khôi Trịnh','0962057739','Note tạm thời'),(16,'2025-05-31 21:00:00',NULL,'confirmed',0,'2025-05-13 21:55:23',1,1,1,'Khôi Trịnh','0962057739','Note tạm thời'),(17,'2025-05-28 08:15:00',NULL,'confirmed',0,'2025-05-28 00:11:42',1,1,4,'Trịnh Huy Khôi','0353022377','Note tạm thời');
/*!40000 ALTER TABLE `table_booking` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `unit`
--

DROP TABLE IF EXISTS `unit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `unit` (
  `id` int NOT NULL AUTO_INCREMENT,
  `unit_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `unit_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `unit`
--

LOCK TABLES `unit` WRITE;
/*!40000 ALTER TABLE `unit` DISABLE KEYS */;
INSERT INTO `unit` VALUES (1,'Kg','Kg','2025-04-25 08:55:51',1),(2,'Gram','Gram','2025-04-25 08:56:06',1),(3,'LIT','Lít','2025-04-25 08:56:20',1),(4,'ml','ml','2025-04-25 08:56:41',1),(5,'Hộp','Hộp','2025-04-25 08:56:52',1),(6,'Chai','Chai','2025-04-25 08:56:59',1),(7,'Gói','Gói','2025-04-25 08:57:10',1),(8,'cái','Cái','2025-04-25 08:57:17',1);
/*!40000 ALTER TABLE `unit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `warehouse`
--

DROP TABLE IF EXISTS `warehouse`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warehouse` (
  `id` int NOT NULL AUTO_INCREMENT,
  `warehouse_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `warehouse_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `location` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `note` longtext COLLATE utf8mb4_unicode_ci,
  `created_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `warehouse`
--

LOCK TABLES `warehouse` WRITE;
/*!40000 ALTER TABLE `warehouse` DISABLE KEYS */;
INSERT INTO `warehouse` VALUES (1,'WH-008','Kho tổng','Số 5, Đường Phan Đăng Lưu, TP. Biên Hòa, Tỉnh Đồng Nai','Tổng kho Long Bình.','2025-04-25 10:36:12',1),(2,'WH-003','Kho cafe tươi','Tầng 2, 123 Nguyễn Văn Linh, Quận 7, TP.HCM','Kho chứa nguyên liệu chính','2025-04-29 00:23:16',1),(3,'WH-002','Kho máy','Tầng hầm 2, 123 Nguyễn Văn Linh, Thành Phố Hà Nội','Kho chứa máy móc phụ tùng quán','2025-04-29 00:28:03',1),(4,'WH-001','Kho phụ','1A Hùng Vương, Quán Thánh, Ba Đình, Hà Nội','Kho chứa nguyên liệu chính','2025-04-29 00:28:38',1),(5,'WH-598','Kho phụ 3A','5B Phạm Đình Hổ, Quán Thánh, Ba Đình, Hà Nội','kho tạm thời','2025-04-29 01:11:55',1);
/*!40000 ALTER TABLE `warehouse` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-29 19:38:48
