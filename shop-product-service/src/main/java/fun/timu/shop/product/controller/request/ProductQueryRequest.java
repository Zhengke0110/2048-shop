package fun.timu.shop.product.controller.request;

import fun.timu.shop.common.enums.ProductStatusEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品查询请求类
 * @author zhengke
 */
@Data
public class ProductQueryRequest {

    /**
     * 商品标题关键词
     */
    private String title;

    /**
     * 商品分类ID
     */
    private Long categoryId;

    /**
     * 商品状态
     */
    private ProductStatusEnum status;

    /**
     * 最小价格
     */
    private BigDecimal minPrice;

    /**
     * 最大价格
     */
    private BigDecimal maxPrice;

    /**
     * 是否只查询有库存商品
     */
    private Boolean onlyInStock = false;

    /**
     * 排序字段：price-价格，sales_count-销量，sort-权重，create_time-创建时间
     */
    private String orderBy = "sort";

    /**
     * 排序方向：ASC-升序，DESC-降序
     */
    private String orderDirection = "DESC";

    /**
     * 页码（从1开始）
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;
}
