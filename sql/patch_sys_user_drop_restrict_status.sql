-- 去掉 sys_user.status 的「限制」(2)：已有限制账号改记为禁用，并更新列注释。
-- 在 MySQL 客户端执行：source 本文件 或复制粘贴执行

USE campus_food;

UPDATE sys_user SET status = 0 WHERE status = 2;

ALTER TABLE sys_user
  MODIFY COLUMN status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0禁用1正常';
