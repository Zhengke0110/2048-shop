package fun.timu.shop.common.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 查询订单状态请求类
 *
 * @author zhengke
 */
@Data
public class QueryOrderStateRequest {
    
    /**
     * 订单流水号
     */
    @JsonProperty("out_trade_no")
    private String outTradeNo;
}
