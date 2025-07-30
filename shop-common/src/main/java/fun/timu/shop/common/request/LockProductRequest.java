package fun.timu.shop.common.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class LockProductRequest {
    @JsonProperty("order_out_trade_no")
    private String orderOutTradeNo;

    @JsonProperty("order_item_list")
    private List<OrderItemRequest> orderItemList;
}
