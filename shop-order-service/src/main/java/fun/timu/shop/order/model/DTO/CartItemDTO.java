package fun.timu.shop.order.model.DTO;

import lombok.Data;

/**
 * Redis购物车项数据传输对象
 * 
 * @author zhengke
 */
@Data
public class CartItemDTO {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 添加时间戳
     */
    private Long addTime;

    /**
     * 更新时间戳
     */
    private Long updateTime;

    public CartItemDTO() {
        long currentTime = System.currentTimeMillis();
        this.addTime = currentTime;
        this.updateTime = currentTime;
    }

    public CartItemDTO(Long productId, Integer quantity) {
        this();
        this.productId = productId;
        this.quantity = quantity;
    }
}
