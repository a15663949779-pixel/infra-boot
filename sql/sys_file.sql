-- ----------------------------
-- 文件元数据记录表
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
  `file_id`            BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '文件主键ID',
  `original_name`      VARCHAR(255) NOT NULL                 COMMENT '原始文件名 (如: 合同.pdf)',
  `file_name`          VARCHAR(255) NOT NULL                 COMMENT '磁盘实际存储文件名 (UUID生成)',
  `file_path`          VARCHAR(500) NOT NULL                 COMMENT '相对存储路径 (如: doc/2026/09/24/xxx.pdf)',
  `file_url`           VARCHAR(500) NOT NULL                 COMMENT '文件Web访问URL (如: /profile/doc/2026/09/24/xxx.pdf)',
  `file_extension`     VARCHAR(30)  DEFAULT ''               COMMENT '文件后缀名 (小写，如: pdf)',
  `file_size`          BIGINT(20)   DEFAULT 0                COMMENT '文件物理大小 (单位: 字节)',
  `file_size_format`   VARCHAR(30)  DEFAULT ''               COMMENT '格式化可读大小 (如: 2.50 MB)',
  `content_type`       VARCHAR(100) DEFAULT ''                COMMENT '文件 MIME 类型 (如: application/pdf)',
  `file_md5`           VARCHAR(64)  DEFAULT ''                COMMENT '文件 MD5 散列值 (用于去重校验)',
  `storage_type`       VARCHAR(20)  DEFAULT 'LOCAL'          COMMENT '主存储介质 (LOCAL / REMOTE)',
  `remote_sync_status` TINYINT(1)   DEFAULT 0                COMMENT '远程同步状态 (0:无需/未同步, 1:同步成功, 2:同步失败)',
  `upload_status`      TINYINT(1)   DEFAULT 0                COMMENT '上传状态 (0:上传中, 1:已完成, 2:上传失败)',
  `business_type`      VARCHAR(50)  DEFAULT 'default'        COMMENT '业务模块标识 (如: avatar, document)',
  `deleted`            INT(1)       DEFAULT 0                COMMENT '逻辑删除标志 (0正常 1已删除)',
  `create_by`          VARCHAR(64)  DEFAULT ''               COMMENT '上传人用户名',
  `create_time`        DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '上传创建时间',
  `update_by`          VARCHAR(64)  DEFAULT ''               COMMENT '更新人',
  `update_time`        DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark`             VARCHAR(500) DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`file_id`),
  KEY `idx_file_md5` (`file_md5`),
  KEY `idx_business_type` (`business_type`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_upload_status` (`upload_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件元数据记录表';

-- ----------------------------
-- 文件清理日志表
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_cleanup_log`;
CREATE TABLE `sys_file_cleanup_log` (
  `log_id`          BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '日志主键ID',
  `cleanup_type`    VARCHAR(20)  NOT NULL                 COMMENT '清理类型 (ORPHAN_SCAN:磁盘扫描孤儿, UPLOAD_TIMEOUT:上传超时)',
  `cleanup_mode`    VARCHAR(20)  NOT NULL                 COMMENT '执行方式 (AUTO:定时任务, MANUAL:手动触发)',
  `total_scanned`   INT(11)      DEFAULT 0                COMMENT '扫描到的待清理记录数',
  `total_cleaned`   INT(11)      DEFAULT 0                COMMENT '实际清理成功数',
  `total_failed`    INT(11)      DEFAULT 0                COMMENT '清理失败数',
  `cleaned_detail`  TEXT         DEFAULT NULL             COMMENT '清理详情 (JSON格式，记录被清理的文件ID列表)',
  `execution_time`  BIGINT(20)   DEFAULT 0                COMMENT '执行耗时 (毫秒)',
  `status`          TINYINT(1)   DEFAULT 1                COMMENT '执行状态 (0:失败, 1:成功)',
  `error_message`   VARCHAR(2000) DEFAULT NULL            COMMENT '错误信息',
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_cleanup_type` (`cleanup_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件清理日志表';
