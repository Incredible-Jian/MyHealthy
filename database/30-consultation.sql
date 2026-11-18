SET NAMES utf8mb4;
CREATE TABLE t_consultation (id BIGINT PRIMARY KEY AUTO_INCREMENT,user_id BIGINT NOT NULL,practitioner_id BIGINT NOT NULL,status VARCHAR(20) NOT NULL,created_at DATETIME NOT NULL,started_at DATETIME,closed_at DATETIME,INDEX idx_consultation_queue(practitioner_id,status,created_at),FOREIGN KEY(user_id) REFERENCES sys_user(user_id),FOREIGN KEY(practitioner_id) REFERENCES sys_user(user_id));
CREATE TABLE t_chat_message (id BIGINT PRIMARY KEY AUTO_INCREMENT,consultation_id BIGINT NOT NULL,sender_id BIGINT NOT NULL,content VARCHAR(2000) NOT NULL,created_at DATETIME NOT NULL,FOREIGN KEY(consultation_id) REFERENCES t_consultation(id),FOREIGN KEY(sender_id) REFERENCES sys_user(user_id));
CREATE TABLE t_health_article (id BIGINT PRIMARY KEY AUTO_INCREMENT,title VARCHAR(150) NOT NULL,content TEXT NOT NULL,media_type VARCHAR(20) NOT NULL,media_url VARCHAR(300));
INSERT INTO t_health_article(title,content,media_type,media_url) VALUES
('如何记录健康指标','使用本系统时，先核对个人资料，再选择对应模拟设备。模拟数据带有教学标记，不能替代真实检测。测量完成后可在记录页与趋势页查看。','article',''),
('如何理解模拟与真实接入','本地模拟器按照演示规则生成数值；真实智能体返回须标识来源。健康报告在健康师审核前保持草稿状态。任何演示内容不应作为诊断或用药依据。','article','');
