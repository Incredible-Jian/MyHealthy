SET NAMES utf8mb4;
CREATE TABLE t_seckill_goods (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,title VARCHAR(150) NOT NULL,image_url VARCHAR(300),price DECIMAL(10,2) NOT NULL,
 stock INT NOT NULL,start_time DATETIME NOT NULL,end_time DATETIME NOT NULL,CHECK(stock>=0),CHECK(price>=0));
CREATE TABLE t_seckill_order (
 id VARCHAR(36) PRIMARY KEY,user_id BIGINT NOT NULL,goods_id BIGINT NOT NULL,amount DECIMAL(10,2) NOT NULL,
 status VARCHAR(20) NOT NULL,created_at DATETIME NOT NULL,expires_at DATETIME NOT NULL,paid_at DATETIME,
 user_name VARCHAR(100),user_phone VARCHAR(30),INDEX idx_order_user_goods(user_id,goods_id,status),INDEX idx_order_expire(status,expires_at),
 FOREIGN KEY(user_id) REFERENCES sys_user(user_id), FOREIGN KEY(goods_id) REFERENCES t_seckill_goods(id));
CREATE TABLE t_seckill_payment (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,order_id VARCHAR(36) NOT NULL UNIQUE,trade_no VARCHAR(100) NOT NULL UNIQUE,
 amount DECIMAL(10,2) NOT NULL,provider VARCHAR(30) NOT NULL,paid_at DATETIME NOT NULL,FOREIGN KEY(order_id) REFERENCES t_seckill_order(id));
CREATE TABLE t_event_outbox (id VARCHAR(36) PRIMARY KEY,event_type VARCHAR(50) NOT NULL,payload VARCHAR(500) NOT NULL,sent TINYINT NOT NULL DEFAULT 0);
CREATE TABLE t_event_receipt (id VARCHAR(36) PRIMARY KEY,received_at DATETIME NOT NULL);
INSERT INTO t_seckill_goods(title,image_url,price,stock,start_time,end_time) VALUES
('教学体检优惠券','',19.90,100,DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(NOW(),INTERVAL 30 DAY)),
('教学健康课程券','',9.90,50,DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(NOW(),INTERVAL 30 DAY));
