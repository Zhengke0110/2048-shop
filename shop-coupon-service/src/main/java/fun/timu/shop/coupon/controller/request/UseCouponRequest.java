package fun.timu.shop.coupon.controller.request;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * 使用优惠券请求对象
 */
@Data
public class UseCouponRequest {
    
    @NotNull(message = "优惠券记录ID不能为空")
    @Positive(message = "优惠券记录ID必须大于0")
    private Long recordId;
    
    @NotNull(message = "订单ID不能为空")
    @Positive(message = "订单ID必须大于0")
    private Long orderId;
    
    private BigDecimal actualDiscountAmount;
}
