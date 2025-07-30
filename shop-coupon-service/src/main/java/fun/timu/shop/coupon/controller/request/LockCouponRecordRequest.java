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
    
    /**
     * 用户ID - 用于RPC调用时传递用户信息
     * 正常web请求时此字段可为空，从登录上下文获取
     */
    private Long userId;
}
