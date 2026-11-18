-- Course business schema. Run only in the dedicated myhealthy_course database.

SET NAMES utf8mb4;

CREATE TABLE t_blood_pressure (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  device_id BIGINT,
  measure_time DATETIME,
  systolic INT,
  diastolic INT,
  heart_rate INT,
  INDEX idx_measurement_user (user_id),
  CONSTRAINT fk_measurement_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='血压测量';

CREATE TABLE t_blood_glucose (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  device_id BIGINT,
  measure_time DATETIME,
  glucose DECIMAL(12,3),
  meal_state VARCHAR(500),
  INDEX idx_myblood_user (user_id),
  CONSTRAINT fk_myblood_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='血糖测量';

CREATE TABLE t_blood_lipid (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  device_id BIGINT,
  measure_time DATETIME,
  total_cholesterol DECIMAL(12,3),
  triglycerides DECIMAL(12,3),
  hdl DECIMAL(12,3),
  ldl DECIMAL(12,3),
  INDEX idx_lipid_user (user_id),
  CONSTRAINT fk_lipid_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='血脂测量';

CREATE TABLE t_ecg_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  device_id BIGINT,
  measure_time DATETIME,
  heart_rate INT,
  duration_seconds INT,
  waveform LONGTEXT,
  INDEX idx_heart_user (user_id),
  CONSTRAINT fk_heart_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心电测量';

CREATE TABLE t_health_device (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  device_name VARCHAR(500),
  device_type VARCHAR(500),
  serial_number VARCHAR(500),
  status VARCHAR(500),
  INDEX idx_device_user (user_id),
  CONSTRAINT fk_device_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康设备';

CREATE TABLE t_collection_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  device_id BIGINT,
  collected_at DATETIME,
  status VARCHAR(500),
  message VARCHAR(500),
  INDEX idx_mylog_user (user_id),
  CONSTRAINT fk_mylog_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采集日志';

CREATE TABLE t_medication (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  medicine_name VARCHAR(500),
  dosage VARCHAR(500),
  frequency VARCHAR(500),
  start_date DATETIME,
  end_date DATETIME,
  notes VARCHAR(500),
  INDEX idx_medichine_user (user_id),
  CONSTRAINT fk_medichine_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用药记录';

CREATE TABLE t_health_plan (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(500),
  content LONGTEXT,
  start_date DATETIME,
  end_date DATETIME,
  status VARCHAR(500),
  INDEX idx_plan_user (user_id),
  CONSTRAINT fk_plan_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康计划';

CREATE TABLE t_follow_up (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  practitioner_id BIGINT,
  visit_time DATETIME,
  content LONGTEXT,
  next_visit_time DATETIME,
  INDEX idx_record_user (user_id),
  CONSTRAINT fk_record_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='随访记录';

CREATE TABLE t_health_report (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(500),
  content LONGTEXT,
  source VARCHAR(500),
  status VARCHAR(500),
  reviewer_id BIGINT,
  reviewed_at DATETIME,
  INDEX idx_report_user (user_id),
  CONSTRAINT fk_report_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康报告';

CREATE TABLE t_health_analysis (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  period_start DATETIME,
  period_end DATETIME,
  summary LONGTEXT,
  INDEX idx_analysis_user (user_id),
  CONSTRAINT fk_analysis_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康分析';

CREATE TABLE t_health_alert (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  measurement_type VARCHAR(500),
  measurement_id BIGINT,
  severity VARCHAR(500),
  message VARCHAR(500),
  status VARCHAR(500),
  INDEX idx_up_user (user_id),
  CONSTRAINT fk_up_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常预警';

CREATE TABLE t_patient_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  real_name VARCHAR(500),
  age INT,
  height_cm DECIMAL(12,3),
  weight_kg DECIMAL(12,3),
  phone VARCHAR(500),
  practitioner_id BIGINT,
  INDEX idx_tuser_user (user_id),
  CONSTRAINT fk_tuser_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者档案';

CREATE TABLE t_sports_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  measure_time DATETIME,
  steps INT,
  duration_seconds INT,
  INDEX idx_sports_user (user_id),
  CONSTRAINT fk_sports_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动记录';