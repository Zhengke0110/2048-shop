CREATE TABLE `user`
(
    `id`          bigint(20) unsigned NOT NULL COMMENT '用户ID，使用分布式ID生成器生成',
    `name`        varchar(128) DEFAULT NULL COMMENT '昵称',
    `pwd`         varchar(124) DEFAULT NULL COMMENT '密码',
    `head_img`    varchar(524) DEFAULT NULL COMMENT '头像',
    `slogan`      varchar(524) DEFAULT NULL COMMENT '用户签名',
    `sex`         tinyint(2) DEFAULT '1' COMMENT '0表示女，1表示男',
    `points`      int(10) DEFAULT '0' COMMENT '积分',
    `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `mail`        varchar(64)  DEFAULT NULL COMMENT '邮箱',
    `secret`      varchar(16)  DEFAULT NULL COMMENT '盐，用于个人敏感信息处理',
    `del_flag`    tinyint(1) DEFAULT '0' COMMENT '删除标记：0->未删除；1->已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `mail_idx` (`mail`),
    KEY `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `address`
(
    `id`             bigint(20) unsigned NOT NULL COMMENT '地址ID，使用分布式ID生成器生成',
    `user_id`        bigint(20) DEFAULT NULL COMMENT '用户id',
    `default_status` int(1) DEFAULT NULL COMMENT '是否默认收货地址：0->否；1->是',
    `receive_name`   varchar(64)  DEFAULT NULL COMMENT '收发货人姓名',
    `phone`          varchar(64)  DEFAULT NULL COMMENT '收货人电话',
    `province`       varchar(64)  DEFAULT NULL COMMENT '省/直辖市',
    `city`           varchar(64)  DEFAULT NULL COMMENT '市',
    `region`         varchar(64)  DEFAULT NULL COMMENT '区',
    `detail_address` varchar(200) DEFAULT NULL COMMENT '详细地址',
    `create_time`    datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time`    datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag`       tinyint(1) DEFAULT '0' COMMENT '删除标记：0->未删除；1->已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_del_flag` (`del_flag`),
    KEY `idx_user_default` (`user_id`, `default_status`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电商-公司收发货地址表';

-- =====================================
-- 为现有表添加软删除和更新时间字段的ALTER语句
-- 如果表已存在，可以使用以下语句添加字段
-- =====================================

-- 为user表添加软删除和更新时间字段
-- ALTER TABLE `user` ADD COLUMN `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`;
-- ALTER TABLE `user` ADD COLUMN `del_flag` tinyint(1) DEFAULT '0' COMMENT '删除标记：0->未删除；1->已删除' AFTER `secret`;
-- ALTER TABLE `user` ADD KEY `idx_del_flag` (`del_flag`);

-- 为address表添加软删除和更新时间字段
-- ALTER TABLE `address` ADD COLUMN `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`;
-- ALTER TABLE `address` ADD COLUMN `del_flag` tinyint(1) DEFAULT '0' COMMENT '删除标记：0->未删除；1->已删除' AFTER `detail_address`;
-- ALTER TABLE `address` ADD KEY `idx_user_id` (`user_id`);
-- ALTER TABLE `address` ADD KEY `idx_del_flag` (`del_flag`);
-- ALTER TABLE `address` ADD KEY `idx_user_default` (`user_id`, `default_status`, `del_flag`);