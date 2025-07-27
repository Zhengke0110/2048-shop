package fun.timu.shop.order.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量操作购物车请求
 * 
 * @author zhengke
 */
@Data
public class BatchCartRequest {

    /**
     * 商品ID列表
     */
    @NotEmpty(message = "商品ID列表不能为空")
    @JsonProperty("product_ids")
    private List<Long> productIds;
}
