-- 后台管理员账号（供需审核用）
-- 登录：用户名 admin / 密码 123456（BCrypt，与本地开发库一致）
INSERT INTO user (username, password, role, nickname, create_time)
SELECT 'admin', '$2b$10$QrUhSg/f3EzW0gGFFI0GIe7LDN1TqR9.OSqWvQYFPcvyTsn7DDQL.', 'ADMIN', '系统管理员', NOW()
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'admin');
