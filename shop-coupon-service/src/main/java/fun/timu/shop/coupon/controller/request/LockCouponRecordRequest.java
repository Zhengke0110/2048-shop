package fun.timu.shop.coupon.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;


import java.util.List;

@Data
public class LockCouponRecordRequest {
    
    @NotEmpty(message = "锁定优惠券记录ID列表不能为空")
    @Size(min = 1, max = 50, message = "一次最多只能锁定50张优惠券")
    private List<Long> lockCouponRecordIds;
    
    @NotBlank(message = "订单号不能为空")
    private String orderOutTradeNo;
}
