package fun.timu.shop.common.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrderItemRequest {
    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("buy_num")
    private Integer buyNum;
}
