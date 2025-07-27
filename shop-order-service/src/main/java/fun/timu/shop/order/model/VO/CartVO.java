package fun.timu.shop.order.model.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车视图对象
 * 
 * @author zhengke
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartVO {

    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    private Long userId;

    /**
     * 购物车商品列表
     */
    @JsonProperty("items")
    private List<CartItemVO> items;

    /**
     * 商品总数量
     */
    @JsonProperty("total_quantity")
    private Integer totalQuantity;

    /**
     * 总金额
     */
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    /**
     * 有效商品数量
     */
    @JsonProperty("valid_count")
    private Integer validCount;

    /**
     * 无效商品数量
     */
    @JsonProperty("invalid_count")
    private Integer invalidCount;
}
