-- 后台管理员账号（供需审核用）
-- 登录：用户名 admin / 密码 123456（BCrypt，与本地开发库一致）
INSERT INTO user (username, password, role, nickname, create_time)
SELECT 'admin', '$2a$10$CvvrOf4mAZG80e2MHTgV9O71Pk9ZjvfxAWTFTeb97boxiAVq2q/Tq', 'ADMIN', '系统管理员', NOW()
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'admin');
