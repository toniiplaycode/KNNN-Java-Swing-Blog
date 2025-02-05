CREATE DATABASE  IF NOT EXISTS `blogdb` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `blogdb`;
-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: blogdb
-- ------------------------------------------------------
-- Server version	8.0.36

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
-- Table structure for table `tbl_admin`
--

DROP TABLE IF EXISTS `tbl_admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_admin` (
  `id` int NOT NULL AUTO_INCREMENT,
  `password` varchar(255) NOT NULL,
  `username` varchar(100) NOT NULL,
  `display_name` varchar(100) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `gender` enum('Male','Female','Other') DEFAULT NULL,
  `address` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_admin`
--

LOCK TABLES `tbl_admin` WRITE;
/*!40000 ALTER TABLE `tbl_admin` DISABLE KEYS */;
INSERT INTO `tbl_admin` VALUES (1,'$2a$10$CRNT8MHFZAjAszua6.i8x.Od.4.oxpd6JID1xv.vfrYzr7ERpWRXO','Ty Admin','Ty Admin','tyadmin@example.com','Male','Ca Mau','2024-10-09 11:00:34');
/*!40000 ALTER TABLE `tbl_admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tbl_category`
--

DROP TABLE IF EXISTS `tbl_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_category` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `description` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_category`
--

LOCK TABLES `tbl_category` WRITE;
/*!40000 ALTER TABLE `tbl_category` DISABLE KEYS */;
INSERT INTO `tbl_category` VALUES (1,'Lập trình ','Các bài viết về ngôn ngữ lập trình'),(2,'Công nghệ','Cập nhật mới nhất về công nghệ và đổi mới'),(3,'Học tập','Tài liệu và hướng dẫn liên quan đến học tập và giáo dục'),(4,'Trực tuyến','Các nền tảng học trực tuyến, khóa học và công cụ'),(5,'Khoa học','Những khám phá khoa học và nghiên cứu nổi bật'),(6,'Giáo dục','Công cụ và xu hướng công nghệ trong giáo dục'),(7,'AI','Thông tin về AI, học máy và học sâu'),(8,'Web','Hướng dẫn, xu hướng và bài viết về phát triển web'),(9,'Bảo mật','Hướng dẫn và mẹo bảo mật dữ liệu, hệ thống'),(10,'Marketing','Chiến lược marketing, SEO và xây dựng thương hiệu trực tuyến'),(11,'Nghề IT','Lời khuyên và cơ hội nghề nghiệp trong ngành IT'),(12,'Giải trí','Sự kiện, giải trí và cập nhật liên quan đến công nghệ');
/*!40000 ALTER TABLE `tbl_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tbl_comment`
--

DROP TABLE IF EXISTS `tbl_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_comment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `post_id` int NOT NULL,
  `comment_id` int DEFAULT NULL,
  `content` text NOT NULL,
  `create_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `post_id` (`post_id`),
  KEY `comment_id` (`comment_id`),
  CONSTRAINT `tbl_comment_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `tbl_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `tbl_comment_ibfk_2` FOREIGN KEY (`post_id`) REFERENCES `tbl_post` (`id`) ON DELETE CASCADE,
  CONSTRAINT `tbl_comment_ibfk_3` FOREIGN KEY (`comment_id`) REFERENCES `tbl_comment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=400 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_comment`
--

LOCK TABLES `tbl_comment` WRITE;
/*!40000 ALTER TABLE `tbl_comment` DISABLE KEYS */;
INSERT INTO `tbl_comment` VALUES (1,1,20,NULL,'Mọi người thấy bài viết này thế nào ?','2024-11-26 01:20:40'),(2,4,20,1,'hay lắm ạ','2024-11-26 01:22:16'),(3,1,1,NULL,'Bài viết này thực sự rất thú vị, mọi người nghĩ sao?','2024-11-26 01:30:00'),(4,2,1,NULL,'Cảm ơn bạn đã chia sẻ thông tin hữu ích.','2024-11-26 01:35:00'),(5,3,2,NULL,'Chủ đề này rất hay, tôi đã học được nhiều điều.','2024-11-26 01:40:00'),(6,4,3,NULL,'Đây là bài viết tôi đang tìm kiếm. Rất bổ ích!','2024-11-26 01:45:00'),(7,5,4,NULL,'Rất thích bài viết này, nội dung rất chi tiết.','2024-11-26 01:50:00'),(8,4,20,NULL,'rất hữu ích cho người mới','2024-11-26 01:22:01'),(337,1,20,2,'cảm ơn ạ','2024-11-26 01:43:37'),(347,1,19,NULL,'rất bổ ích','2024-11-26 23:36:16'),(348,1,4,7,'cảm ơn bạn !','2024-11-26 23:37:28'),(349,1,11,NULL,'rất hay và ý nghĩa','2024-11-27 06:34:48'),(354,1,10,NULL,'áđá','2025-02-03 01:49:52'),(356,1,10,NULL,'hello','2025-02-03 02:24:02'),(386,2,12,NULL,'1','2025-02-03 09:03:06'),(387,2,12,386,'2','2025-02-03 09:03:11'),(388,2,12,386,'3','2025-02-03 09:03:18'),(389,2,12,387,'3','2025-02-03 09:03:27'),(391,1,1,3,'đúng vậy !','2025-02-03 09:52:42'),(392,2,20,NULL,'hello','2025-02-03 14:36:43'),(393,1,13,NULL,'rất hay ạ','2025-02-03 23:28:41'),(395,1,1,391,'hay lắm nha','2025-02-03 23:30:06'),(399,1,13,393,'cảm ơn ạ','2025-02-04 03:35:13');
/*!40000 ALTER TABLE `tbl_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tbl_like`
--

DROP TABLE IF EXISTS `tbl_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_like` (
  `user_id` int NOT NULL,
  `post_id` int NOT NULL,
  `create_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`,`post_id`),
  UNIQUE KEY `user_id` (`user_id`,`post_id`),
  KEY `post_id` (`post_id`),
  CONSTRAINT `tbl_like_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `tbl_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `tbl_like_ibfk_2` FOREIGN KEY (`post_id`) REFERENCES `tbl_post` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_like`
--

LOCK TABLES `tbl_like` WRITE;
/*!40000 ALTER TABLE `tbl_like` DISABLE KEYS */;
INSERT INTO `tbl_like` VALUES (1,1,'2024-11-27 06:48:44'),(1,3,'2025-02-03 09:52:33'),(1,4,'2024-11-26 23:37:22'),(1,7,'2025-02-03 00:36:47'),(1,9,'2025-02-03 13:41:48'),(1,12,'2025-02-03 03:24:03'),(1,13,'2025-02-03 03:52:19'),(1,14,'2025-02-04 03:34:59'),(1,15,'2024-11-26 01:37:24'),(1,16,'2024-11-26 01:37:23'),(1,17,'2024-11-27 01:15:23'),(1,18,'2024-11-26 01:37:20'),(1,19,'2024-11-26 23:36:10'),(1,231,'2025-02-04 01:14:12'),(2,18,'2024-11-27 06:14:58'),(2,20,'2024-11-27 06:30:15'),(4,20,'2024-11-26 01:21:48');
/*!40000 ALTER TABLE `tbl_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tbl_post`
--

DROP TABLE IF EXISTS `tbl_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_post` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `title` varchar(255) NOT NULL,
  `content` text,
  `create_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `hash_img` longtext,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `tbl_post_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `tbl_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=232 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_post`
--

LOCK TABLES `tbl_post` WRITE;
/*!40000 ALTER TABLE `tbl_post` DISABLE KEYS */;
INSERT INTO `tbl_post` VALUES (1,1,'Vai trò của Content Marketing trong MMO','<p>Content Marketing là chìa khóa thành công trong MMO (Make Money Online):</p><p><strong>1. Tạo nội dung giá trị:</strong> Cung cấp thông tin hữu ích và giải pháp cho khách hàng tiềm năng.</p><p><strong>2. Đa dạng hóa hình thức:</strong> Kết hợp giữa bài viết blog, video và infographic để tăng tương tác.</p><p><strong>3. Phân tích hiệu quả:</strong> Theo dõi số liệu thống kê để tối ưu chiến lược nội dung.</p>','2024-11-25 23:31:41','2024-11-26 01:17:53',''),(2,3,'Các loại Proxy và cách sử dụng','<p>Các loại Proxy phổ biến hiện nay:</p><ul><li><strong>HTTP Proxy:</strong> Phù hợp cho việc duyệt web thông thường.</li><li><strong>SOCKS Proxy:</strong> Hỗ trợ nhiều giao thức và ứng dụng hơn HTTP Proxy.</li></ul>','2024-11-25 23:31:41','2024-11-26 01:09:21',''),(3,3,'Proxy MMO và Vai Trò Của Nó Trong Các Trò Chơi Trực Tuyến',' <p><strong>Proxy MMO là gì?</strong></p>\n    <p>Proxy MMO giúp thay đổi địa chỉ IP khi chơi game online, bảo vệ danh tính và vượt qua hạn chế khu vực.</p>\n\n    <p><strong>Lý Do Sử Dụng Proxy MMO</strong></p>\n    <ul>\n        <li><strong>Bảo vệ danh tính:</strong> Ẩn địa chỉ IP thật, tránh bị tấn công.</li>\n        <li><strong>Vượt qua hạn chế khu vực:</strong> Tránh bị chặn tài khoản theo khu vực.</li>\n        <li><strong>Tối ưu kết nối:</strong> Giảm độ trễ, cải thiện trải nghiệm game.</li>\n    </ul>\n\n    <p><strong>Cách Proxy Hoạt Động</strong></p>\n    <p>Proxy thay đổi địa chỉ IP và chuyển yêu cầu của bạn qua máy chủ proxy, giúp bảo vệ sự riêng tư.</p>\n\n    <p><strong>Lưu Ý Khi Dùng Proxy MMO</strong></p>\n    <ul>\n        <li>Chọn proxy uy tín để đảm bảo bảo mật và tốc độ.</li>\n        <li>Tránh dùng proxy miễn phí vì có thể chứa phần mềm độc hại.</li>\n    </ul>\n\n    <p><strong>Kết Luận</strong></p>\n    <p>Proxy MMO là công cụ hữu ích giúp bảo vệ danh tính và tối ưu kết nối khi chơi game trực tuyến.</p>\n','2024-11-25 23:31:41','2024-12-10 06:33:19',''),(4,2,'DevOps và CI/CD trong phát triển phần mềm','<p>DevOps và CI/CD là những yếu tố quan trọng trong lập trình hiện đại:</p><ul><li><strong>DevOps:</strong> Cải thiện sự hợp tác giữa đội phát triển và đội vận hành thông qua công cụ như Jenkins.</li><li><strong>CI/CD:</strong> Tự động hóa kiểm thử và triển khai phần mềm, giảm thời gian ra mắt sản phẩm.</li></ul>','2024-11-25 23:31:41','2024-11-26 01:13:12','http://res.cloudinary.com/dj8ae1gpq/image/upload/v1732583592/v7daqp49dfa18ngb8ddf.png'),(5,2,'Làm quen với Docker và Kubernetes','<p>Docker và Kubernetes giúp tối ưu hóa việc quản lý ứng dụng:</p><p><strong>1. Docker:</strong> Đóng gói ứng dụng thành container, dễ dàng triển khai ở nhiều môi trường.</p><p><strong>2. Kubernetes:</strong> Quản lý container một cách tự động, hỗ trợ mở rộng linh hoạt.</p>','2024-11-25 23:31:41','2024-11-26 01:12:27','http://res.cloudinary.com/dj8ae1gpq/image/upload/v1732583548/ndb6ljkedjxk5pnciqiz.png'),(6,2,'Top công nghệ đáng học năm 2024','<p>Dưới đây là những công nghệ nổi bật mà bạn nên học trong năm 2024:</p><ol><li><strong>Trí tuệ nhân tạo (AI):</strong> Ứng dụng AI đang ngày càng phổ biến trong phân tích dữ liệu và tự động hóa.</li><li><strong>Blockchain:</strong> Công nghệ bảo mật dữ liệu và xây dựng các ứng dụng phi tập trung.</li><li><strong>Điện toán đám mây:</strong> Tăng cường khả năng lưu trữ và xử lý dữ liệu từ xa.</li></ol><p>Những kỹ năng này không chỉ mở ra cơ hội nghề nghiệp mà còn giúp bạn bắt kịp xu hướng toàn cầu.</p>','2024-11-25 23:31:41','2024-11-26 01:11:54',''),(7,1,'Các nền tảng kiếm tiền online phổ biến','<p>Dưới đây là những nền tảng phổ biến giúp bạn bắt đầu hành trình kiếm tiền online hiệu quả:</p><ul><li><strong>Fiverr:</strong> Nền tảng freelancer cho phép bạn cung cấp các dịch vụ như viết lách, thiết kế đồ họa, lập trình.</li><li><strong>Upwork:</strong> Kết nối bạn với các khách hàng cần tìm kiếm dịch vụ chuyên nghiệp.</li><li><strong>Amazon Affiliate:</strong> Tham gia tiếp thị liên kết và kiếm hoa hồng từ sản phẩm bán được.</li></ul><p>Ngoài ra, đừng quên đầu tư vào nội dung chất lượng để nổi bật giữa hàng triệu người khác.</p>','2024-11-25 23:31:41','2024-11-26 01:17:49','http://res.cloudinary.com/dj8ae1gpq/image/upload/v1732583870/eirl1nqcp5wx6uv9h0a1.jpg'),(8,1,'Hướng dẫn kiếm tiền online cho người mới bắt đầu','<p>Kiếm tiền online đang trở thành xu hướng toàn cầu, đặc biệt trong thời đại công nghệ phát triển như hiện nay.</p><p><strong>1. Xác định lĩnh vực phù hợp:</strong> Bạn cần chọn lĩnh vực mà bạn am hiểu hoặc có đam mê như Affiliate Marketing, Dropshipping, hoặc viết blog cá nhân.</p><p><strong>2. Sử dụng các công cụ hỗ trợ:</strong></p><ul><li>Canva: Tạo nội dung trực quan hấp dẫn.</li><li>Google Analytics: Theo dõi hiệu quả chiến dịch quảng bá.</li></ul><p><strong>3. Xây dựng thương hiệu cá nhân:</strong> Tận dụng mạng xã hội để tăng uy tín và tiếp cận khách hàng mục tiêu.</p>','2024-11-25 23:31:41','2024-11-26 01:17:40',''),(9,3,'Lợi ích của Proxy trong bảo mật mạng','<p>Proxy là giải pháp tuyệt vời để bảo vệ thông tin cá nhân:</p><p><strong>1. Ẩn IP:</strong> Địa chỉ IP của bạn sẽ được thay thế bằng địa chỉ của Proxy.</p><p><strong>2. Tăng tốc duyệt web:</strong> Proxy lưu cache dữ liệu để giảm thời gian tải trang.</p>','2024-11-25 23:31:41','2024-11-26 01:08:36',''),(10,2,'Học lập trình web với React và Node.js','<p>React và Node.js là lựa chọn hoàn hảo cho lập trình viên web:</p><p><strong>1. ReactJS:</strong> Thư viện giúp xây dựng giao diện người dùng mượt mà và tương tác cao.</p><p><strong>2. Node.js:</strong> Một môi trường runtime hiệu quả cho backend, hỗ trợ nhiều request đồng thời.</p>','2024-11-25 23:31:41','2024-11-26 01:11:25','http://res.cloudinary.com/dj8ae1gpq/image/upload/v1732583477/m2gyfzwtwr8do5csllnv.jpg'),(11,1,'Hướng dẫn phát triển kênh YouTube từ con số 0','<p>YouTube đang là nền tảng lý tưởng để kiếm tiền:</p><p><strong>1. Lựa chọn chủ đề:</strong> Chọn một chủ đề cụ thể, ví dụ như hướng dẫn nấu ăn, công nghệ, hoặc du lịch. Điều này giúp bạn thu hút đúng đối tượng khán giả.</p><p><strong>2. Đầu tư vào chất lượng:</strong> Mua thiết bị quay phim và chỉnh sửa video chuyên nghiệp để tăng giá trị cho nội dung.</p><p><strong>3. Sử dụng SEO:</strong> Tối ưu tiêu đề, mô tả và từ khóa để tăng khả năng hiển thị trên YouTube.</p>','2024-11-25 23:31:41','2024-12-13 00:43:10','http://res.cloudinary.com/dj8ae1gpq/image/upload/v1732583855/lbe7f2aecohthj7gsuta.jpg'),(12,3,'Cách cài đặt Proxy Server','<p>Để cài đặt Proxy Server, bạn cần thực hiện:</p><p><strong>1. Chọn phần mềm:</strong> Squid Proxy hoặc Nginx là lựa chọn phổ biến.</p><p><strong>2. Cấu hình:</strong> Tùy chỉnh tệp cấu hình để phù hợp với yêu cầu.</p>','2024-11-25 23:31:41','2024-11-26 01:08:30','http://res.cloudinary.com/dj8ae1gpq/image/upload/v1732583310/rzealbmvseo9khhogrbh.png'),(13,4,'Lập trình Python cơ bản đến nâng cao','<p>Python là một ngôn ngữ lập trình mạnh mẽ và dễ học, phù hợp cho người mới bắt đầu lẫn chuyên gia:</p><p><strong>1. Tổng quan:</strong> Python là ngôn ngữ linh hoạt, được sử dụng trong phát triển web, khoa học dữ liệu và trí tuệ nhân tạo.</p><p><strong>2. Công cụ:</strong> Hãy bắt đầu với các công cụ phổ biến như Jupyter Notebook hoặc PyCharm.</p><p><strong>3. Các thư viện phổ biến:</strong></p><ul><li>NumPy: Hỗ trợ tính toán số học nhanh chóng.</li><li>Pandas: Xử lý dữ liệu hiệu quả.</li><li>TensorFlow: Dành cho học máy (Machine Learning).</li></ul>','2024-11-25 23:34:44','2025-02-03 03:34:00','http://res.cloudinary.com/dj8ae1gpq/image/upload/v1732583070/r1bffb9my394stmpjytd.jpg'),(14,4,'Những xu hướng công nghệ nổi bật năm 2024','<p>Năm 2024 là năm bùng nổ của nhiều xu hướng công nghệ mới:</p><p><strong>1. Trí tuệ nhân tạo (AI):</strong> AI tiếp tục thay đổi cách con người làm việc và giải trí.</p><p><strong>2. Web 3.0:</strong> Một kỷ nguyên mới của internet với Blockchain và tính phi tập trung.</p><p><strong>3. Phát triển phần mềm không cần mã (No-code):</strong> Công cụ như Bubble hoặc Adalo giúp tạo ứng dụng mà không cần biết lập trình.</p>','2024-11-25 23:34:44','2024-11-26 01:03:00',''),(15,4,'Học lập trình Full-stack trong 6 tháng','<p>Lập trình Full-stack là lựa chọn lý tưởng cho những ai muốn làm việc với cả frontend và backend:</p><p><strong>1. Backend:</strong> Bắt đầu với Node.js hoặc Django để xây dựng API mạnh mẽ.</p><p><strong>2. Frontend:</strong> Tập trung học React hoặc Angular để tạo giao diện hiện đại.</p><p><strong>3. Tích hợp:</strong> Kết hợp cả hai kỹ năng để phát triển ứng dụng hoàn chỉnh.</p>','2024-11-25 23:34:44','2024-11-26 01:01:49','http://res.cloudinary.com/dj8ae1gpq/image/upload/v1732582909/rxk4xadbdplpvwhj22ak.png'),(16,4,'Tối ưu hóa mã nguồn với Git và CI/CD','<p>Sử dụng Git và tích hợp CI/CD giúp tăng hiệu quả trong lập trình:</p><p><strong>1. Git:</strong> Quản lý mã nguồn hiệu quả với các nhánh (branch) và commit.</p><p><strong>2. CI/CD:</strong> Tự động hóa kiểm thử và triển khai để giảm thời gian ra mắt sản phẩm.</p>','2024-11-25 23:34:44','2024-11-26 01:01:33','http://res.cloudinary.com/dj8ae1gpq/image/upload/v1732582894/jxehbsvjgcnvz27qkm7y.png'),(17,5,'Thiết kế giao diện người dùng (UI) đẹp và trực quan','<p>UI là yếu tố quan trọng trong việc tạo ra trải nghiệm người dùng tốt:</p><p><strong>1. Nguyên tắc cơ bản:</strong></p><ul><li>Sử dụng màu sắc hài hòa.</li><li>Tuân thủ nguyên lý thiết kế đơn giản (minimalism).</li></ul><p><strong>2. Công cụ hỗ trợ:</strong> Sử dụng Figma hoặc Adobe XD để tạo mockup nhanh chóng.</p>','2024-11-25 23:34:44','2025-02-03 03:51:12','http://res.cloudinary.com/dj8ae1gpq/image/upload/v1732582850/sxfsse0mgfrohuxs9law.jpg'),(18,5,'Xu hướng thiết kế đồ họa 2024','<p>Thiết kế đồ họa không ngừng thay đổi để phù hợp với xu hướng:</p><p><strong>1. Gradient:</strong> Kết hợp màu gradient trong logo và giao diện người dùng.</p><p><strong>2. 3D Elements:</strong> Sử dụng các yếu tố 3D để tạo chiều sâu cho thiết kế.</p><p><strong>3. Typographic Art:</strong> Sử dụng typography để tạo điểm nhấn sáng tạo.</p>','2024-11-25 23:34:44','2024-11-26 01:00:40','http://res.cloudinary.com/dj8ae1gpq/image/upload/v1732582840/ruhusgwwlhjh6wikjtw6.png'),(19,5,'Làm quen với thiết kế thương hiệu chuyên nghiệp','<p>Thiết kế thương hiệu giúp tạo dấu ấn riêng cho doanh nghiệp:</p><p><strong>1. Logo:</strong> Thiết kế logo đơn giản nhưng truyền tải thông điệp mạnh mẽ.</p><p><strong>2. Bộ nhận diện thương hiệu:</strong> Kết hợp màu sắc và font chữ đồng nhất.</p><p><strong>3. Truyền thông:</strong> Đảm bảo hình ảnh nhất quán trên các nền tảng mạng xã hội.</p>','2024-11-25 23:34:44','2024-11-26 01:00:30','http://res.cloudinary.com/dj8ae1gpq/image/upload/v1732582829/oezhjbsk4ez2glpegm6u.png'),(20,5,'Kỹ thuật dựng hình 3D với Blender','<p>Blender là công cụ mạnh mẽ dành cho thiết kế 3D:</p><p><strong>1. Cơ bản:</strong> Tìm hiểu cách tạo hình khối và ánh sáng trong Blender.</p><p><strong>2. Nâng cao:</strong> Áp dụng hiệu ứng vật liệu và hoạt hình (animation).</p>','2024-11-25 23:34:44','2024-11-26 00:59:47','http://res.cloudinary.com/dj8ae1gpq/image/upload/v1732582787/bnhftvl57f3vjpy1cuid.jpg'),(231,1,'a1','<p style=\"margin-top: 0\"> a1 </p>','2025-02-04 01:13:26','2025-02-04 09:20:19','https://user-images.githubusercontent.com/58245926/135955869-a4be26f1-e28d-4362-b4f4-67ba1f8e2328.png');
/*!40000 ALTER TABLE `tbl_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tbl_post_category`
--

DROP TABLE IF EXISTS `tbl_post_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_post_category` (
  `post_id` int NOT NULL,
  `category_id` int NOT NULL,
  PRIMARY KEY (`post_id`,`category_id`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `tbl_post_category_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `tbl_post` (`id`) ON DELETE CASCADE,
  CONSTRAINT `tbl_post_category_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `tbl_category` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_post_category`
--

LOCK TABLES `tbl_post_category` WRITE;
/*!40000 ALTER TABLE `tbl_post_category` DISABLE KEYS */;
INSERT INTO `tbl_post_category` VALUES (1,1),(5,1),(7,1),(9,1),(10,1),(13,1),(15,1),(2,2),(8,2),(14,2),(231,2),(3,3),(6,3),(11,3),(17,3),(18,3),(19,3),(20,3),(12,4),(19,4),(1,5),(3,5),(15,5),(2,6),(7,6),(10,6),(12,6),(13,6),(5,7),(6,7),(9,7),(10,7),(16,7),(231,7),(4,8),(16,8),(4,9),(8,9),(14,9),(231,9),(17,10),(20,11),(11,12),(18,12);
/*!40000 ALTER TABLE `tbl_post_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tbl_user`
--

DROP TABLE IF EXISTS `tbl_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `password` varchar(255) NOT NULL,
  `username` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `gender` enum('Male','Female','Other') DEFAULT NULL,
  `address` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `avatar` longtext,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `email_2` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=88 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_user`
--

LOCK TABLES `tbl_user` WRITE;
/*!40000 ALTER TABLE `tbl_user` DISABLE KEYS */;
INSERT INTO `tbl_user` VALUES (1,'$2a$10$CRNT8MHFZAjAszua6.i8x.Od.4.oxpd6JID1xv.vfrYzr7ERpWRXO','Ngọc Tỷ MMO','ty@example.com','Male','Cà Mau','2024-10-26 17:11:13','http://res.cloudinary.com/dj8ae1gpq/image/upload/v1731668757/khtm993tjptpj5jiauou.png'),(2,'$2a$10$CRNT8MHFZAjAszua6.i8x.Od.4.oxpd6JID1xv.vfrYzr7ERpWRXO','Duy Luân Tech','luan@example.com','Female','Sài Gòn','2024-10-26 17:11:13','http://res.cloudinary.com/dj8ae1gpq/image/upload/v1731668597/woqklnnd7s2d4xf3u71c.png'),(3,'','David Ngô Proxy','ngo@example.com','Male','Cần Thơ','2024-10-26 17:11:13','http://res.cloudinary.com/dj8ae1gpq/image/upload/v1730936768/jvhngokghumq3gkuvj61.png'),(4,'$2a$10$CRNT8MHFZAjAszua6.i8x.Od.4.oxpd6JID1xv.vfrYzr7ERpWRXO','Minh Tuấn IT','minhtuan@example.com','Male','Hà Nội','2024-11-26 03:00:00',NULL),(5,'','Anh Khoa Design','anhkhoa@example.com','Male','Đà Nẵng','2024-11-26 03:10:00',NULL),(62,'','Toàn Dev','toandev@gmail.com','Male','Cần Thơ','2024-12-14 13:08:30','http://res.cloudinary.com/dj8ae1gpq/image/upload/v1731668757/khtm993tjptpj5jiauou.png'),(87,'$2a$10$3I2TLwmg1jWpp1.Q0WYcDOuWLwCb1HmaL8PXzCci0MwOcRGwRyoaO','toàn nè','toan@gmail.com','Male','Cần Thơ','2025-02-03 08:39:37',NULL);
/*!40000 ALTER TABLE `tbl_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-02-05 11:29:17
