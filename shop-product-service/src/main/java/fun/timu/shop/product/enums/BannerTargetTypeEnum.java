package fun.timu.shop.product.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 轮播图跳转类型枚举
 *
 * @author zhengke
 */
@Getter
@AllArgsConstructor
public enum BannerTargetTypeEnum {

    /**
     * 外部链接
     */
    URL("URL", "外部链接"),

    /**
     * 商品详情
     */
    PRODUCT("PRODUCT", "商品详情"),

    /**
     * 分类页
     */
    CATEGORY("CATEGORY", "分类页"),

    /**
     * 页面
     */
    PAGE("PAGE", "页面");

    private final String code;
    private final String desc;

    /**
     * 根据代码获取枚举
     */
    public static BannerTargetTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (BannerTargetTypeEnum targetType : values()) {
            if (targetType.code.equals(code)) {
                return targetType;
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
