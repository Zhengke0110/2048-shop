package fun.timu.shop.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 轮播图状态枚举
 *
 * @author zhengke
 */
@Getter
@AllArgsConstructor
public enum BannerStatusEnum {

    /**
     * 禁用
     */
    DISABLED(0, "禁用"),

    /**
     * 启用
     */
    ENABLED(1, "启用");

    private final int code;
    private final String desc;

    /**
     * 根据代码获取枚举
     */
    public static BannerStatusEnum getByCode(int code) {
        for (BannerStatusEnum status : values()) {
            if (status.code == code) {
                return status;
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
