-- =========================================================
-- 字典模块
-- =========================================================

-- ----------------------------
-- 字典类型表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) NOT NULL COMMENT '字典名称',
  `dict_type` varchar(100) NOT NULL COMMENT '字典类型编码',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1正常 0停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0存在 2删除）',
  PRIMARY KEY (`dict_id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- ----------------------------
-- 字典数据表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码主键',
  `dict_sort` int NOT NULL DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) NOT NULL COMMENT '字典标签',
  `dict_value` varchar(100) NOT NULL COMMENT '字典键值',
  `dict_type` varchar(100) NOT NULL COMMENT '字典类型',
  `css_class` varchar(100) DEFAULT NULL COMMENT '样式属性',
  `list_class` varchar(100) DEFAULT 'default' COMMENT '表格回显样式',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认（1是 0否）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1正常 0停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0存在 2删除）',
  PRIMARY KEY (`dict_code`),
  KEY `idx_dict_type` (`dict_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- ----------------------------
-- 种子数据：字典类型
-- ----------------------------
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`) VALUES
('用户性别', 'sys_user_sex', 1, '用户性别列表'),
('系统状态', 'sys_normal_disable', 1, '系统正常禁用状态'),
('系统是否', 'sys_yes_no', 1, '系统是否选项');

-- ----------------------------
-- 种子数据：字典数据
-- ----------------------------
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`) VALUES
(1, '男', '0', 'sys_user_sex', 'primary', 0),
(2, '女', '1', 'sys_user_sex', 'danger', 0),
(3, '未知', '2', 'sys_user_sex', 'info', 1),
(1, '正常', '1', 'sys_normal_disable', 'success', 1),
(2, '停用', '0', 'sys_normal_disable', 'danger', 0),
(1, '是', '1', 'sys_yes_no', 'success', 1),
(2, '否', '0', 'sys_yes_no', 'danger', 0);

-- ----------------------------
-- 菜单：字典管理
-- ----------------------------
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `path`, `component`, `perms`, `menu_type`, `icon`, `sort`, `visible`, `status`)
VALUES (
  (SELECT id FROM (SELECT m.id FROM sys_menu m WHERE m.path = '/system' AND m.menu_type = 0 LIMIT 1) tmp),
  '字典管理', 'dict', 'system/dict/index', '', 1, 'dict', 5, 1, 1
);

SET @dictMenuId = (SELECT id FROM (SELECT m.id FROM sys_menu m WHERE m.perms = '' AND m.menu_name = '字典管理' LIMIT 1) tmp);

INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `path`, `component`, `perms`, `menu_type`, `icon`, `sort`, `visible`, `status`) VALUES
(@dictMenuId, '字典查询', '', '', 'system:dict:list',    2, '', 1, 1, 1),
(@dictMenuId, '字典新增', '', '', 'system:dict:add',     2, '', 2, 1, 1),
(@dictMenuId, '字典修改', '', '', 'system:dict:edit',    2, '', 3, 1, 1),
(@dictMenuId, '字典删除', '', '', 'system:dict:remove',  2, '', 4, 1, 1);

-- ----------------------------
-- 角色菜单关联（admin 角色）
-- ----------------------------
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.id FROM sys_menu m WHERE m.menu_name IN ('字典管理', '字典查询', '字典新增', '字典修改', '字典删除')
AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.id);
