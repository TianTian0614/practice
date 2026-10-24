-- 修复供需匹配：将粗粒度品类(水果/粮食/干果/蔬菜/畜禽)细化为具体品名
-- 目的：让"求购核桃"只匹配"核桃"供应，不再误配"红枣"

-- 1) 把 id1-8 的品类从粗分类改为具体品名（与标题一致）
UPDATE supply_demand SET category='苹果'        WHERE id=1;   -- 红富士苹果
UPDATE supply_demand SET category='小米'        WHERE id=2;   -- 沁州黄小米
UPDATE supply_demand SET category='红枣'        WHERE id=3;   -- 吕梁特级红枣
UPDATE supply_demand SET category='大同黄花菜'  WHERE id=4;   -- 大同黄花菜
UPDATE supply_demand SET category='阳光玫瑰葡萄' WHERE id=5;   -- 求购阳光玫瑰葡萄
UPDATE supply_demand SET category='玉米'        WHERE id=6;   -- 求购玉米
UPDATE supply_demand SET category='土鸡蛋'      WHERE id=7;   -- 散养土鸡蛋
UPDATE supply_demand SET category='核桃'        WHERE id=8;   -- 求购核桃

-- 2) 补对侧数据，让每个具体品类都有可匹配的对方（status=1 已通过）
INSERT INTO supply_demand (type, category, title, region, price, status, user_id, create_time) VALUES
  ('SUPPLY','核桃','吕梁太行山核桃 带壳 5斤','山西汾阳',16.00,1,1,NOW()),       -- 匹配 id8 核桃需求
  ('DEMAND','红枣','求购 红枣 礼盒装 3吨','山西吕梁',55.00,1,2,NOW()),           -- 匹配 id3 红枣供应
  ('DEMAND','小米','求购 沁州黄小米 2吨','山西长治',40.00,1,3,NOW()),            -- 匹配 id2 小米供应
  ('SUPPLY','玉米','山西晋中饲料玉米 散装','山西晋中',1.20,1,1,NOW()),           -- 匹配 id6 玉米需求
  ('DEMAND','土鸡蛋','批量求购 散养土鸡蛋','山西临汾',33.00,1,4,NOW());          -- 匹配 id7 土鸡蛋供应
