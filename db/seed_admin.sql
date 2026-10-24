-- 后台管理员账号（供需审核用）
-- 登录：用户名 admin / 密码 admin123（BCrypt，与 Spring Security 一致）
INSERT INTO user (username, password, role, nickname, create_time)
SELECT 'admin', '$2b$10$V79KouLz4l16jdzC.6QvF.BdcsCq9sPPNcfljlse8hAjgucxBxYIO', 'ADMIN', '系统管理员', NOW()
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'admin');
