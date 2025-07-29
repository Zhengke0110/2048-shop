package fun.timu.shop.common.enums;

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

    private final int flag;
    private final String description;

    /**
     * 根据标记值获取枚举
     *
     * @param flag 标记值
     * @return 枚举实例
     */
    public static DelFlagEnum getByFlag(int flag) {
        for (DelFlagEnum delFlagEnum : DelFlagEnum.values()) {
            if (delFlagEnum.getFlag() == flag) {
                return delFlagEnum;
            }
        }
        throw new IllegalArgumentException("无效的删除标记: " + flag);
    }

    /**
     * 判断是否为删除状态
     *
     * @param flag 标记值
     * @return true-已删除，false-未删除
     */
    public static boolean isDeleted(Integer flag) {
        return flag != null && flag.equals(DELETED.getFlag());
    }

    /**
     * 判断是否为未删除状态
     *
     * @param flag 标记值
     * @return true-未删除，false-已删除
     */
    public static boolean isNotDeleted(Integer flag) {
        return flag != null && flag.equals(NOT_DELETED.getFlag());
    }

    /**
     * 检查是否为有效标记
     *
     * @param flag 标记值
     * @return 是否有效
     */
    public static boolean isValidFlag(Integer flag) {
        try {
            getByFlag(flag);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
