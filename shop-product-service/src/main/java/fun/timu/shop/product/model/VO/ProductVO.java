package fun.timu.shop.product.model.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品视图对象
 */
@Data
public class ProductVO {

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 封面图
     */
    @JsonProperty("cover_img")
    private String coverImg;

    /**
     * 商品详情
     */
    private String detail;

    /**
     * 商品分类ID
     */
    @JsonProperty("category_id")
    private Long categoryId;

    /**
     * 原价
     */
    @JsonProperty("old_price")
    private BigDecimal oldPrice;

    /**
     * 现价
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 销售数量
     */
    @JsonProperty("sales_count")
    private Integer salesCount;

    /**
     * 排序权重，数值越大越靠前
     */
    private Integer sort;

    /**
     * 商品状态：0-下架，1-上架
     */
    private Integer status;
}
