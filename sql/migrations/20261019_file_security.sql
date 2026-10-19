-- 在目标数据库中执行，可重复执行；不推断历史文件归属，不删除任何业务数据。
SET @file_security_ddl = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_file' AND COLUMN_NAME = 'owner_id'),
  'SELECT 1', 'ALTER TABLE sys_file ADD COLUMN owner_id BIGINT NULL COMMENT ''上传用户ID''');
PREPARE file_security_statement FROM @file_security_ddl;
EXECUTE file_security_statement;
DEALLOCATE PREPARE file_security_statement;

SET @file_security_ddl = IF(
  EXISTS (SELECT 1 FROM information_schema.STATISTICS
          WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_file' AND INDEX_NAME = 'idx_file_owner_id'),
  'SELECT 1', 'CREATE INDEX idx_file_owner_id ON sys_file(owner_id)');
PREPARE file_security_statement FROM @file_security_ddl;
EXECUTE file_security_statement;
DEALLOCATE PREPARE file_security_statement;

-- 权限作为隐藏按钮挂在系统管理下，不新增无实际页面的路由。
INSERT INTO sys_menu (parent_id, menu_name, perms, menu_type, visible, sort, status, deleted)
SELECT 5, p.label, p.perm, 2, 0, 90, 1, 0
FROM (
  SELECT '文件上传' AS label, 'system:file:upload' AS perm
  UNION ALL SELECT '文件列表', 'system:file:list'
  UNION ALL SELECT '文件详情', 'system:file:query'
  UNION ALL SELECT '文件下载', 'system:file:download'
  UNION ALL SELECT '文件删除', 'system:file:delete'
  UNION ALL SELECT '文件清理', 'system:file:cleanup'
) p
WHERE NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.perms = p.perm AND m.deleted = 0);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.id FROM sys_menu m
WHERE m.perms LIKE 'system:file:%' AND m.deleted = 0
AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.id);
