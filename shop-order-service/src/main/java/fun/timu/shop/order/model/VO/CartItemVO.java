package fun.timu.shop.order.model.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车商品视图对象
 *
 * @author zhengke
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemVO {

    /**
     * 商品ID
     */
    @JsonProperty("product_id")
    private Long productId;

    /**
     * 商品标题
     */
    @JsonProperty("title")
    private String title;

    /**
     * 商品图片
     */
    @JsonProperty("cover_img")
    private String coverImg;

    /**
     * 商品价格
     */
    @JsonProperty("price")
    private BigDecimal price;

    /**
     * 购物车中的数量
     */
    @JsonProperty("quantity")
    private Integer quantity;

    /**
     * 商品库存
     */
    @JsonProperty("stock")
    private Integer stock;

    /**
     * 商品状态（是否有效）
     */
    @JsonProperty("available")
    private Boolean available;

    /**
     * 小计金额
     */
    @JsonProperty("subtotal")
    private BigDecimal subtotal;

    /**
     * 添加到购物车的时间
     */
    @JsonProperty("add_time")
    private LocalDateTime addTime;

    /**
     * 更新时间
     */
    @JsonProperty("update_time")
    private LocalDateTime updateTime;
}
