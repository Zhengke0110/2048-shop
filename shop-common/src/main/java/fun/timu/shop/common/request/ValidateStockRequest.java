package fun.timu.shop.common.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 验证商品库存请求类
 *
 * @author zhengke
 */
@Data
public class ValidateStockRequest {
    
    /**
     * 商品ID
     */
    @JsonProperty("product_id")
    private Long productId;
    
    /**
     * 需要验证的数量
     */
    @JsonProperty("quantity")
    private Integer quantity;
}
