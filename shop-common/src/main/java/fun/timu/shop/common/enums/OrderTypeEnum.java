package fun.timu.shop.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单类型枚举
 *
 * @author zhengke
 * @since 2025-07-29
 */
@Getter
@AllArgsConstructor
public enum OrderTypeEnum {

    /**
     * 普通订单
     */
    DAILY("DAILY", "普通订单"),

    /**
     * 促销订单
     */
    PROMOTION("PROMOTION", "促销订单");

    private final String type;
    private final String description;

    /**
     * 根据订单类型获取枚举
     *
     * @param type 订单类型
     * @return 订单类型枚举
     */
    public static OrderTypeEnum getByType(String type) {
        for (OrderTypeEnum orderType : OrderTypeEnum.values()) {
            if (orderType.getType().equals(type)) {
                return orderType;
            }
        }
        throw new IllegalArgumentException("未找到对应的订单类型: " + type);
    }

    /**
     * 检查是否为有效订单类型
     *
     * @param type 订单类型
     * @return 是否有效
     */
    public static boolean isValidType(String type) {
        try {
            getByType(type);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
