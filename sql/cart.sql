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

-- 订单表
CREATE TABLE
    `product_order` (
        `id` bigint (11) NOT NULL AUTO_INCREMENT COMMENT '订单ID主键',
        `out_trade_no` varchar(64) NOT NULL COMMENT '订单唯一标识',
        `state` varchar(11) NOT NULL DEFAULT 'NEW' COMMENT 'NEW 未支付订单,PAY已经支付订单,CANCEL超时取消订单',
        `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '订单生成时间',
        `total_amount` decimal(16, 2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
        `pay_amount` decimal(16, 2) DEFAULT NULL COMMENT '订单实际支付价格',
        `pay_type` varchar(64) DEFAULT NULL COMMENT '支付类型，微信-银行-支付宝',
        `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
        `head_img` varchar(524) DEFAULT NULL COMMENT '头像',
        `user_id` bigint (11) NOT NULL COMMENT '用户id',
        `del` int (5) DEFAULT '0' COMMENT '0表示未删除，1表示已经删除',
        `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        `order_type` varchar(32) DEFAULT 'DAILY' COMMENT '订单类型 DAILY普通单，PROMOTION促销订单',
        `receiver_address` varchar(1024) DEFAULT NULL COMMENT '收货地址 json存储',
        -- 索引
        UNIQUE KEY `uk_out_trade_no` (`out_trade_no`) COMMENT '订单号唯一索引',
        INDEX `idx_user_id` (`user_id`) COMMENT '用户ID索引',
        INDEX `idx_state` (`state`) COMMENT '订单状态索引',
        INDEX `idx_create_time` (`create_time`) COMMENT '创建时间索引',
        PRIMARY KEY (`id`)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表';

-- 订单商品表
CREATE TABLE
    `product_order_item` (
        `id` bigint (11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
        `product_order_id` bigint (11) NOT NULL COMMENT '订单ID，关联product_order表',
        `out_trade_no` varchar(64) NOT NULL COMMENT '订单号，冗余字段便于查询',
        `product_id` bigint (11) NOT NULL COMMENT '产品id',
        `product_name` varchar(128) NOT NULL COMMENT '商品名称',
        `product_img` varchar(524) DEFAULT NULL COMMENT '商品图片',
        `category_id` bigint (20) DEFAULT NULL COMMENT '商品分类ID，下单时商品所属分类',
        `old_price` decimal(16, 2) DEFAULT NULL COMMENT '商品原价，下单时的原价',
        `buy_num` int (11) NOT NULL DEFAULT 1 COMMENT '购买数量',
        `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        `total_amount` decimal(16, 2) NOT NULL COMMENT '购物项商品总价格',
        `amount` decimal(16, 2) NOT NULL COMMENT '购物项商品单价（实际成交价）',
        -- 索引
        INDEX `idx_product_order_id` (`product_order_id`) COMMENT '订单ID索引',
        INDEX `idx_out_trade_no` (`out_trade_no`) COMMENT '订单号索引',
        INDEX `idx_product_id` (`product_id`) COMMENT '商品ID索引',
        INDEX `idx_category_id` (`category_id`) COMMENT '商品分类索引',
        -- 外键约束
        CONSTRAINT `fk_product_order_item_order_id` FOREIGN KEY (`product_order_id`) REFERENCES `product_order` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
        PRIMARY KEY (`id`)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单商品表';