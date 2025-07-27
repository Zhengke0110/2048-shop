package fun.timu.shop.order.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;



/**
 * 更新购物车请求
 * 
 * @author zhengke
 */
@Data
public class UpdateCartRequest {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    @JsonProperty("product_id")
    private Long productId;

    /**
     * 商品数量
     */
    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量必须大于0")
    @JsonProperty("quantity")
    private Integer quantity;
}
