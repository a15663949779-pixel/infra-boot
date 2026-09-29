-- ----------------------------
-- 操作日志记录表
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
  `id`              BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title`           VARCHAR(50)  DEFAULT ''               COMMENT '模块标题',
  `business_type`   VARCHAR(20)  DEFAULT ''               COMMENT '业务类型（INSERT, UPDATE, DELETE, EXPORT, OTHER）',
  `method`          VARCHAR(100) DEFAULT ''               COMMENT '方法名称（类路径.方法名）',
  `request_method`  VARCHAR(10)  DEFAULT ''               COMMENT '请求方式（GET, POST, PUT, DELETE）',
  `oper_user_id`    BIGINT(20)   DEFAULT NULL             COMMENT '操作人员ID',
  `oper_name`       VARCHAR(50)  DEFAULT ''               COMMENT '操作人员账号',
  `oper_url`        VARCHAR(255) DEFAULT ''               COMMENT '请求URL',
  `oper_ip`         VARCHAR(128) DEFAULT ''               COMMENT '主机地址',
  `oper_location`   VARCHAR(255) DEFAULT ''               COMMENT '操作地点',
  `oper_param`      TEXT         DEFAULT NULL             COMMENT '请求参数',
  `json_result`     TEXT         DEFAULT NULL             COMMENT '返回参数',
  `status`          TINYINT(1)   DEFAULT '1'              COMMENT '操作状态（1正常 0异常）',
  `error_msg`       TEXT         DEFAULT NULL             COMMENT '错误消息',
  `cost_time`       BIGINT(20)   DEFAULT '0'              COMMENT '消耗时间（毫秒）',
  `oper_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_oper_time` (`oper_time`),
  KEY `idx_oper_name` (`oper_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志记录';

-- ----------------------------
-- 操作日志菜单和权限
-- ----------------------------
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `path`, `component`, `perms`, `icon`, `menu_type`, `visible`, `sort`, `status`, `deleted`)
VALUES
    (500, 5, '操作日志', 'operlog', 'system/operlog/index', 'system:operlog:list', 'log', 1, 1, 4, 1, 0),
    (501, 500, '日志查询', NULL, NULL, 'system:operlog:query', NULL, 2, 0, 1, 1, 0),
    (502, 500, '日志删除', NULL, NULL, 'system:operlog:remove', NULL, 2, 0, 2, 1, 0);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 500), (1, 501), (1, 502);
