package fun.timu.shop.product.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 删除标记枚举
 *
 * @author zhengke
 */
@Getter
@AllArgsConstructor
public enum DelFlagEnum {

    /**
     * 未删除
     */
    NOT_DELETED(0, "未删除"),

    /**
     * 已删除
     */
    DELETED(1, "已删除");

    private final int code;
    private final String desc;

    /**
     * 根据代码获取枚举
     */
    public static DelFlagEnum getByCode(int code) {
        for (DelFlagEnum delFlagEnum : values()) {
            if (delFlagEnum.code == code) {
                return delFlagEnum;
            }
        }
        return null;
    }

    /**
     * 判断代码是否有效
     */
    public static boolean isValid(int code) {
        return getByCode(code) != null;
    }
}
