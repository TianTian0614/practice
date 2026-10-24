-- agri-assist 数据库结构（MySQL 8）
CREATE DATABASE IF NOT EXISTS agri_assist DEFAULT CHARACTER SET utf8mb4;
USE agri_assist;

CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20),
    nickname VARCHAR(50),
    contact VARCHAR(100),
    region VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE supply_demand (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(10) NOT NULL,
    category VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    region VARCHAR(50),
    price DECIMAL(12,2),
    brand_story VARCHAR(2000),
    status INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_type_status (type, status),
    INDEX idx_category (category)
);

CREATE TABLE negotiation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    supply_demand_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    message VARCHAR(1000),
    status VARCHAR(20) DEFAULT 'PENDING',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pre_sale (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    expect_price DECIMAL(12,2),
    total_quota INT,
    reserved_count INT DEFAULT 0,
    end_date DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pre_sale_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    quantity INT DEFAULT 1,
    status VARCHAR(20) DEFAULT 'RESERVED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE article (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    category VARCHAR(50),
    create_time BIGINT
);

CREATE TABLE community_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    status VARCHAR(20) DEFAULT 'OPEN',
    best_comment_id BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE consultation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    question TEXT,
    answer TEXT,
    source_refs TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    auditor_id BIGINT,
    note VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE knowledge_chunk (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source VARCHAR(500) NOT NULL,
    content TEXT,
    embedding TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 可信溯源记录（2026-10-20 新增：C组亮点模块）
CREATE TABLE trace_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    batch_no VARCHAR(50) NOT NULL,
    product_name VARCHAR(50) NOT NULL,
    step VARCHAR(30) NOT NULL,
    detail VARCHAR(500),
    location VARCHAR(50),
    operator VARCHAR(50),
    occurred_at DATETIME NOT NULL,
    is_demo TINYINT NOT NULL DEFAULT 1 COMMENT '1=系统演示数据 0=真实业务录入',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_batch_no (batch_no)
);

-- 农产品价格行情（2026-10-20 新增：C组亮点模块）
CREATE TABLE price_quote (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(50) NOT NULL,
    market VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    quote_date DATE NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_date (product_name, quote_date)
);
