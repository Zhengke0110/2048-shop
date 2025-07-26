package fun.timu.shop.coupon.model.VO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 优惠券记录视图对象
 */
@Data
public class CouponRecordVO {
    /**
     * id
     */
    private Long id;

    /**
     * 优惠券id
     */
    private Long couponId;

    /**
     * 创建时间获得时间
     */
    private Date createTime;

    /**
     * 使用状态  可用 NEW,已使用USED,过期 EXPIRED;
     */
    private String useState;

    /**
     * 优惠券标题
     */
    private String couponTitle;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 抵扣价格
     */
    private BigDecimal price;

    /**
     * 满多少才可以使用
     */
    private BigDecimal conditionPrice;

    /**
     * 实际使用时间
     */
    private Date useTime;

    /**
     * 实际优惠金额
     */
    private BigDecimal actualDiscountAmount;

    /**
     * 领取渠道[ACTIVITY活动页面，SHARE分享链接，AUTO自动发放]
     */
    private String receiveChannel;
}
