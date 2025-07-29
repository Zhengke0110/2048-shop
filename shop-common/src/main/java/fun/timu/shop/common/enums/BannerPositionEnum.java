package fun.timu.shop.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 轮播图位置枚举
 *
 * @author zhengke
 */
@Getter
@AllArgsConstructor
public enum BannerPositionEnum {

    /**
     * 首页轮播
     */
    HOME("HOME", "首页轮播"),

    /**
     * 分类页轮播
     */
    CATEGORY("CATEGORY", "分类页轮播"),

    /**
     * 商品页轮播
     */
    PRODUCT("PRODUCT", "商品页轮播");

    private final String code;
    private final String desc;

    /**
     * 根据代码获取枚举
     */
    public static BannerPositionEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (BannerPositionEnum position : values()) {
            if (position.code.equals(code)) {
                return position;
            }
        }
        return null;
    }

    /**
     * 判断代码是否有效
     */
    public static boolean isValid(String code) {
        return getByCode(code) != null;
    }
}
