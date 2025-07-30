CREATE TABLE
    `banner` (
        `id` int (11) unsigned NOT NULL AUTO_INCREMENT,
        `title` varchar(128) DEFAULT NULL COMMENT '轮播图标题',
        `description` varchar(500) DEFAULT NULL COMMENT '描述信息',
        `img` varchar(524) DEFAULT NULL COMMENT '图片',
        `url` varchar(524) DEFAULT NULL COMMENT '跳转地址',
        `target_type` varchar(32) DEFAULT 'URL' COMMENT '跳转类型：URL-外部链接，PRODUCT-商品详情，CATEGORY-分类页，PAGE-页面',
        `target_id` bigint (20) DEFAULT NULL COMMENT '目标ID，根据target_type关联对应表的ID',
        `position` varchar(32) DEFAULT 'HOME' COMMENT '显示位置：HOME-首页轮播，CATEGORY-分类页轮播，PRODUCT-商品页轮播',
        `weight` int (11) DEFAULT '0' COMMENT '权重，数值越大越靠前',
        `status` tinyint (1) DEFAULT '1' COMMENT '启用状态：0-禁用，1-启用',
        `start_time` datetime DEFAULT NULL COMMENT '轮播开始时间',
        `end_time` datetime DEFAULT NULL COMMENT '轮播结束时间',
        `click_count` int (11) DEFAULT '0' COMMENT '点击统计',
        `creator_id` bigint (20) DEFAULT NULL COMMENT '创建人ID',
        `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        `del_flag` tinyint (1) DEFAULT '0' COMMENT '删除标记：0-未删除，1-已删除',
        PRIMARY KEY (`id`),
        KEY `idx_status` (`status`),
        KEY `idx_position` (`position`),
        KEY `idx_weight` (`weight`),
        KEY `idx_time_range` (`start_time`, `end_time`),
        KEY `idx_del_flag` (`del_flag`),
        KEY `idx_creator` (`creator_id`)
    ) ENGINE = InnoDB AUTO_INCREMENT = 4 DEFAULT CHARSET = utf8mb4 COMMENT = '轮播图表';

CREATE TABLE
    `product` (
        `id` bigint (11) unsigned NOT NULL AUTO_INCREMENT,
        `title` varchar(128) DEFAULT NULL COMMENT '商品标题',
        `cover_img` varchar(128) DEFAULT NULL COMMENT '封面图',
        `detail` varchar(256) DEFAULT '' COMMENT '商品详情',
        `category_id` bigint (20) DEFAULT NULL COMMENT '商品分类ID',
        `old_price` decimal(16, 2) DEFAULT NULL COMMENT '原价',
        `price` decimal(16, 2) DEFAULT NULL COMMENT '现价',
        `stock` int (11) DEFAULT NULL COMMENT '库存',
        `lock_stock` int (11) DEFAULT '0' COMMENT '锁定库存',
        `sales_count` int (11) DEFAULT '0' COMMENT '销售数量',
        `sort` int (11) DEFAULT '0' COMMENT '排序权重，数值越大越靠前',
        `status` tinyint (1) DEFAULT '1' COMMENT '商品状态：0-下架，1-上架',
        `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        `del_flag` tinyint (1) DEFAULT '0' COMMENT '删除标记：0-未删除，1-已删除',
        PRIMARY KEY (`id`),
        KEY `idx_category` (`category_id`),
        KEY `idx_status` (`status`),
        KEY `idx_sort` (`sort`),
        KEY `idx_sales` (`sales_count`),
        KEY `idx_del_flag` (`del_flag`)
    ) ENGINE = InnoDB AUTO_INCREMENT = 5 DEFAULT CHARSET = utf8mb4 COMMENT = '商品表';


CREATE TABLE `coupon_task` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT,
  `coupon_record_id` bigint(11) unsigned NOT NULL COMMENT '优惠券记录id',
  `user_id` bigint(11) NOT NULL COMMENT '用户id',
  `out_trade_no` varchar(64) NOT NULL COMMENT '订单号',
  `lock_state` varchar(32) DEFAULT 'LOCK' COMMENT '锁定状态 LOCK锁定 FINISH完成 CANCEL取消',
  `expire_time` datetime NOT NULL COMMENT '锁定过期时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  -- 索引
  UNIQUE KEY `uk_out_trade_no_coupon` (`out_trade_no`, `coupon_record_id`) COMMENT '订单优惠券唯一索引',
  INDEX `idx_coupon_record_id` (`coupon_record_id`) COMMENT '优惠券记录ID索引',
  INDEX `idx_user_id` (`user_id`) COMMENT '用户ID索引',
  INDEX `idx_lock_state` (`lock_state`) COMMENT '锁定状态索引',
  INDEX `idx_expire_time` (`expire_time`) COMMENT '过期时间索引',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='优惠券锁定任务表';