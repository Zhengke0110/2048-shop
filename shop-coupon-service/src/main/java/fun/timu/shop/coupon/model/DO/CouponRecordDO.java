package fun.timu.shop.coupon.model.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName coupon_record
 */
@TableName(value ="coupon_record")
@Data
public class CouponRecordDO implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
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
     * 用户id
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String userName;

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
     * 订单id
     */
    private Long orderId;

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

    /**
     * 分享人ID
     */
    private Long shareUserId;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除标记 0正常 1删除
     */
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}