package fun.timu.shop.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付类型枚举
 *
 * @author zhengke
 * @since 2025-07-29
 */
@Getter
@AllArgsConstructor
public enum OrderPayTypeEnum {

    /**
     * 微信支付
     */
    WECHAT("微信", "微信支付"),

    /**
     * 银行卡支付
     */
    BANK("银行", "银行卡支付"),

    /**
     * 支付宝支付
     */
    ALIPAY("支付宝", "支付宝支付");

    private final String type;
    private final String description;

    /**
     * 根据支付类型获取枚举
     *
     * @param type 支付类型
     * @return 支付类型枚举
     */
    public static OrderPayTypeEnum getByType(String type) {
        for (OrderPayTypeEnum payType : OrderPayTypeEnum.values()) {
            if (payType.getType().equals(type)) {
                return payType;
            }
        }
        throw new IllegalArgumentException("未找到对应的支付类型: " + type);
    }

    /**
     * 检查是否为有效支付类型
     *
     * @param type 支付类型
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
