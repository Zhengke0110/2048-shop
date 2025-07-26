package fun.timu.shop.coupon.model.VO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 优惠券视图对象
 */
@Data
public class CouponVO {
    /**
     * id
     */
    private Long id;

    /**
     * 优惠卷类型[NEW_USER注册赠券，TASK任务卷，PROMOTION促销劵]
     */
    private String category;

    /**
     * 发布状态, PUBLISH发布，DRAFT草稿，OFFLINE下线
     */
    private String publish;

    /**
     * 优惠券图片
     */
    private String couponImg;

    /**
     * 优惠券标题
     */
    private String couponTitle;

    /**
     * 抵扣价格
     */
    private BigDecimal price;

    /**
     * 每人限制张数
     */
    private Integer userLimit;

    /**
     * 优惠券开始有效时间
     */
    private Date startTime;

    /**
     * 优惠券失效时间
     */
    private Date endTime;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 满多少才可以使用
     */
    private BigDecimal conditionPrice;

    /**
     * 优惠类型[AMOUNT固定金额，RATE百分比折扣，FULL_REDUCE满减]
     */
    private String discountType;

    /**
     * 折扣率(0-100)，用于百分比折扣
     */
    private BigDecimal discountRate;

    /**
     * 最大优惠金额，用于百分比折扣封顶
     */
    private BigDecimal maxDiscountAmount;

    /**
     * 每个订单可使用的张数限制
     */
    private Integer useLimitPerOrder;

    /**
     * 是否可与其他优惠券叠加使用 0否 1是
     */
    private Integer stackable;

    /**
     * 是否仅限首单使用 0否 1是
     */
    private Integer firstOrderOnly;

    /**
     * 优惠券领取开始时间
     */
    private Date receiveStartTime;

    /**
     * 优惠券领取结束时间
     */
    private Date receiveEndTime;

    /**
     * 每日限量发放数量
     */
    private Integer dailyLimit;

    /**
     * 优惠券详细描述
     */
    private String description;

    /**
     * 使用规则说明
     */
    private String usageRules;
}
