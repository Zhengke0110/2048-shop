package fun.timu.shop.common.enums;

/**
 * 删除标记枚举
 * 
 * @author zhengke
 */
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

    DelFlagEnum(int flag, String description) {
        this.flag = flag;
        this.description = description;
    }

    public int getFlag() {
        return flag;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据标记值获取枚举
     * 
     * @param flag 标记值
     * @return 枚举实例
     */
    public static DelFlagEnum valueOf(int flag) {
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
}
