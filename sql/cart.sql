-- 创建订单数据库
CREATE DATABASE IF NOT EXISTS shop_order DEFAULT CHARACTER
SET
    utf8mb4 COLLATE utf8mb4_unicode_ci;

USE shop_order;

-- 购物车表
CREATE TABLE
    IF NOT EXISTS cart (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
        user_id BIGINT NOT NULL COMMENT '用户ID',
        product_id BIGINT NOT NULL COMMENT '商品ID',
        quantity INT NOT NULL DEFAULT 1 COMMENT '商品数量',
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        -- 索引
        UNIQUE KEY uk_user_product (user_id, product_id) COMMENT '用户商品唯一索引',
        INDEX idx_user_id (user_id) COMMENT '用户ID索引',
        INDEX idx_update_time (update_time) COMMENT '更新时间索引'
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '购物车表';

-- 订单表（预留）
CREATE TABLE
    IF NOT EXISTS `order` (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
        user_id BIGINT NOT NULL COMMENT '用户ID',
        order_no VARCHAR(64) NOT NULL COMMENT '订单号',
        total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
        status INT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消',
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        -- 索引
        UNIQUE KEY uk_order_no (order_no) COMMENT '订单号唯一索引',
        INDEX idx_user_id (user_id) COMMENT '用户ID索引',
        INDEX idx_status (status) COMMENT '订单状态索引',
        INDEX idx_create_time (create_time) COMMENT '创建时间索引'
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表';

-- 订单商品表（预留）
CREATE TABLE
    IF NOT EXISTS order_item (
        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
        order_id BIGINT NOT NULL COMMENT '订单ID',
        product_id BIGINT NOT NULL COMMENT '商品ID',
        product_title VARCHAR(128) NOT NULL COMMENT '商品标题',
        product_img VARCHAR(256) COMMENT '商品图片',
        price DECIMAL(10, 2) NOT NULL COMMENT '商品单价',
        quantity INT NOT NULL DEFAULT 1 COMMENT '商品数量',
        total_amount DECIMAL(10, 2) NOT NULL COMMENT '商品总金额',
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        -- 索引
        INDEX idx_order_id (order_id) COMMENT '订单ID索引',
        INDEX idx_product_id (product_id) COMMENT '商品ID索引'
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单商品表';

-- 插入一些测试数据（可选）
INSERT INTO
    cart (user_id, product_id, quantity)
VALUES
    (1, 1001, 2),
    (1, 1002, 1),
    (2, 1001, 3),
    (2, 1003, 1),
    (3, 1002, 2) ON DUPLICATE KEY
UPDATE quantity =
VALUES
    (quantity);