package fun.timu.shop.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品状态枚举
 * @author zhengke
 */
@Getter
@AllArgsConstructor
public enum ProductStatusEnum {
    
    /** 下架 */
    OFFLINE(0, "下架"),
    
    /** 上架 */
    ONLINE(1, "上架");
    
    private final Integer code;
    private final String desc;
    
    /**
     * 根据代码获取枚举
     */
    public static ProductStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 判断代码是否有效
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }
}
