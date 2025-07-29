package fun.timu.shop.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 *
 * @author zhengke
 * @since 2025-07-29
 */
@Getter
@AllArgsConstructor
public enum OrderStateEnum {

    /**
     * 新订单，未支付
     */
    NEW("NEW", "未支付订单"),

    /**
     * 已支付订单
     */
    PAY("PAY", "已支付订单"),

    /**
     * 超时取消订单
     */
    CANCEL("CANCEL", "超时取消订单");

    private final String state;
    private final String description;

    /**
     * 根据状态值获取枚举
     *
     * @param state 状态值
     * @return 订单状态枚举
     */
    public static OrderStateEnum getByState(String state) {
        for (OrderStateEnum orderState : OrderStateEnum.values()) {
            if (orderState.getState().equals(state)) {
                return orderState;
            }
        }
        throw new IllegalArgumentException("未找到对应的订单状态: " + state);
    }

    /**
     * 检查是否为有效状态
     *
     * @param state 状态值
     * @return 是否有效
     */
    public static boolean isValidState(String state) {
        try {
            getByState(state);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
