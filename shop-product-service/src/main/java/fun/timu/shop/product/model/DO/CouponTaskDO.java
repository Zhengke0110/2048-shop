package fun.timu.shop.product.model.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 优惠券锁定任务表
 * @TableName coupon_task
 */
@TableName(value ="coupon_task")
@Data
public class CouponTaskDO implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券记录id
     */
    private Long couponRecordId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 订单号
     */
    private String outTradeNo;

    /**
     * 锁定状态 LOCK锁定 FINISH完成 CANCEL取消
     */
    private String lockState;

    /**
     * 锁定过期时间
     */
    private Date expireTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}