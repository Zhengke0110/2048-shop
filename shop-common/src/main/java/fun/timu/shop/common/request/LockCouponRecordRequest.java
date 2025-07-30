package fun.timu.shop.common.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 锁定优惠券记录请求类
 *
 * @author zhengke
 */
@Data
public class LockCouponRecordRequest {
    
    /**
     * 锁定优惠券记录ID列表
     */
    @NotEmpty(message = "锁定优惠券记录ID列表不能为空")
    @Size(min = 1, max = 50, message = "一次最多只能锁定50张优惠券")
    @JsonProperty("lock_coupon_record_ids")
    private List<Long> lockCouponRecordIds;
    
    /**
     * 订单流水号
     */
    @NotBlank(message = "订单号不能为空")
    @JsonProperty("order_out_trade_no")
    private String orderOutTradeNo;
    
    /**
     * 用户ID - 用于RPC调用时传递用户信息
     * 正常web请求时此字段可为空，从登录上下文获取
     */
    @JsonProperty("user_id")
    private Long userId;
}
