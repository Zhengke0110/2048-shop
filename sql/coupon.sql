#优惠券表
CREATE TABLE `coupon`
(
    `id`                    bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `category`              varchar(11)    DEFAULT NULL COMMENT '优惠卷类型[NEW_USER注册赠券，TASK任务卷，PROMOTION促销劵]',
    `publish`               varchar(11)    DEFAULT NULL COMMENT '发布状态, PUBLISH发布，DRAFT草稿，OFFLINE下线',
    `coupon_img`            varchar(524)   DEFAULT NULL COMMENT '优惠券图片',
    `coupon_title`          varchar(128)   DEFAULT NULL COMMENT '优惠券标题',
    `price`                 decimal(16, 2) DEFAULT NULL COMMENT '抵扣价格',
    `user_limit`            int(11) DEFAULT NULL COMMENT '每人限制张数',
    `start_time`            datetime       DEFAULT NULL COMMENT '优惠券开始有效时间',
    `end_time`              datetime       DEFAULT NULL COMMENT '优惠券失效时间',
    `publish_count`         int(11) DEFAULT NULL COMMENT '优惠券总量',
    `stock`                 int(11) DEFAULT '0' COMMENT '库存',
    `create_time`           datetime       DEFAULT NULL,
    `condition_price`       decimal(16, 2) DEFAULT NULL COMMENT '满多少才可以使用',

    #                       使用范围限制 `applicable_products`   text           DEFAULT NULL COMMENT '可使用的商品ID列表，逗号分隔',
    `exclude_products`      text           DEFAULT NULL COMMENT '排除的商品ID列表，逗号分隔',
    `min_product_count`     int(11) DEFAULT 1 COMMENT '最少购买商品数量',
    `applicable_categories` text           DEFAULT NULL COMMENT '适用的商品分类ID列表，逗号分隔',

    #                       优惠券类型扩展 `discount_type`         varchar(32) DEFAULT 'AMOUNT' COMMENT '优惠类型[AMOUNT固定金额，RATE百分比折扣，FULL_REDUCE满减]',
    `discount_rate`         decimal(5, 2)  DEFAULT NULL COMMENT '折扣率(0-100)，用于百分比折扣',
    `max_discount_amount`   decimal(16, 2) DEFAULT NULL COMMENT '最大优惠金额，用于百分比折扣封顶',

    #                       使用限制 `use_limit_per_order`   int(11) DEFAULT 1 COMMENT '每个订单可使用的张数限制',
    `stackable`             tinyint(1) DEFAULT 0 COMMENT '是否可与其他优惠券叠加使用 0否 1是',
    `first_order_only`      tinyint(1) DEFAULT 0 COMMENT '是否仅限首单使用 0否 1是',

    #                       领取限制 `receive_start_time`    datetime       DEFAULT NULL COMMENT '优惠券领取开始时间',
    `receive_end_time`      datetime       DEFAULT NULL COMMENT '优惠券领取结束时间',
    `daily_limit`           int(11) DEFAULT NULL COMMENT '每日限量发放数量',

    #                       基础信息完善 `description`           text           DEFAULT NULL COMMENT '优惠券详细描述',
    `usage_rules`           text           DEFAULT NULL COMMENT '使用规则说明',
    `creator_id`            bigint(20) DEFAULT NULL COMMENT '创建人ID',
    `update_time`           datetime       DEFAULT NULL COMMENT '更新时间',
    `del_flag`              tinyint(1) DEFAULT 0 COMMENT '删除标记 0正常 1删除',

    PRIMARY KEY (`id`),
    KEY                     `idx_category` (`category`),
    KEY                     `idx_publish` (`publish`),
    KEY                     `idx_start_end_time` (`start_time`, `end_time`),
    KEY                     `idx_receive_time` (`receive_start_time`, `receive_end_time`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4;


#优惠券领劵记录
CREATE TABLE `coupon_record`
(
    `id`                     bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `coupon_id`              bigint(11) DEFAULT NULL COMMENT '优惠券id',
    `create_time`            datetime       DEFAULT NULL COMMENT '创建时间获得时间',
    `use_state`              varchar(32)    DEFAULT NULL COMMENT '使用状态  可用 NEW,已使用USED,过期 EXPIRED;',
    `user_id`                bigint(11) DEFAULT NULL COMMENT '用户id',
    `user_name`              varchar(128)   DEFAULT NULL COMMENT '用户昵称',
    `coupon_title`           varchar(128)   DEFAULT NULL COMMENT '优惠券标题',
    `start_time`             datetime       DEFAULT NULL COMMENT '开始时间',
    `end_time`               datetime       DEFAULT NULL COMMENT '结束时间',
    `order_id`               bigint(11) DEFAULT NULL COMMENT '订单id',
    `price`                  decimal(16, 2) DEFAULT NULL COMMENT '抵扣价格',
    `condition_price`        decimal(16, 2) DEFAULT NULL COMMENT '满多少才可以使用',

    #                        使用详情 `use_time`               datetime       DEFAULT NULL COMMENT '实际使用时间',
    `actual_discount_amount` decimal(16, 2) DEFAULT NULL COMMENT '实际优惠金额',

    #                        来源追踪 `receive_channel`        varchar(64)    DEFAULT NULL COMMENT '领取渠道[ACTIVITY活动页面，SHARE分享链接，AUTO自动发放]',
    `share_user_id`          bigint(11) DEFAULT NULL COMMENT '分享人ID',

    #                        基础信息 `update_time`            datetime       DEFAULT NULL COMMENT '更新时间',
    `del_flag`               tinyint(1) DEFAULT 0 COMMENT '删除标记 0正常 1删除',

    PRIMARY KEY (`id`),
    KEY                      `idx_coupon_id` (`coupon_id`),
    KEY                      `idx_user_id` (`user_id`),
    KEY                      `idx_use_state` (`use_state`),
    KEY                      `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=141 DEFAULT CHARSET=utf8mb4;