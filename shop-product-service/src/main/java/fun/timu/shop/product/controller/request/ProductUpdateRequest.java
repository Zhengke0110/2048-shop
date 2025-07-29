package fun.timu.shop.product.controller.request;

import fun.timu.shop.common.enums.ProductStatusEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品更新请求类
 * @author zhengke
 */
@Data
public class ProductUpdateRequest {

    /**
     * 商品标题
     */
    @NotBlank(message = "商品标题不能为空")
    private String title;

    /**
     * 封面图
     */
    @NotBlank(message = "封面图不能为空")
    private String coverImg;

    /**
     * 商品详情
     */
    private String detail;

    /**
     * 商品分类ID
     */
    @NotNull(message = "商品分类不能为空")
    private Long categoryId;

    /**
     * 原价
     */
    @DecimalMin(value = "0.01", message = "原价必须大于0")
    private BigDecimal oldPrice;

    /**
     * 现价
     */
    @NotNull(message = "现价不能为空")
    @DecimalMin(value = "0.01", message = "现价必须大于0")
    private BigDecimal price;

    /**
     * 库存
     */
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能小于0")
    private Integer stock;

    /**
     * 排序权重
     */
    private Integer sort;

    /**
     * 商品状态
     */
    private ProductStatusEnum status;
}
