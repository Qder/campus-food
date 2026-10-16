-- 已有库升级：为 sys_user 增加 role_id（与 建表语句.sql / UserMapper.xml 一致）
-- 在 MySQL 客户端执行：source 本文件 或复制粘贴执行

USE campus_food;

-- 若已加过列会报错 Duplicate column，可忽略
ALTER TABLE sys_user
  ADD COLUMN role_id TINYINT UNSIGNED NOT NULL DEFAULT 1
    COMMENT '角色ID，对应sys_role.id，默认普通用户(USER)'
  AFTER status;

ALTER TABLE sys_user
  ADD INDEX idx_user_role (role_id);

-- 角色种子（表为空或缺少数据时执行）
INSERT INTO sys_role (id, code, name, remark) VALUES
  (1, 'USER', '普通用户', '默认注册角色'),
  (2, 'MERCHANT', '商家用户', '入驻审核通过后授予'),
  (3, 'ADMIN', '平台管理员', '后台初始化账号')
ON DUPLICATE KEY UPDATE name = VALUES(name);
