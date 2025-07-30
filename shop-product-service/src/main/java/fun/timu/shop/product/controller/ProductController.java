package fun.timu.shop.product.controller;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.product.controller.request.ProductQueryRequest;
import fun.timu.shop.product.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品查询控制器
 * 普通用户可以访问的商品相关接口
 *
 * @author zhengke
 */
@Slf4j
@RestController
@RequestMapping("/api/product/v1/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 查询商品列表
     */
    @PostMapping("/list")
    public JsonData list(@RequestBody(required = false) ProductQueryRequest queryRequest) {
        // 如果没有传查询条件，使用默认条件
        if (queryRequest == null) {
            queryRequest = new ProductQueryRequest();
        }

        return productService.list(queryRequest);
    }

    /**
     * 根据分类ID查询商品列表
     */
    @GetMapping("/list/category/{categoryId}")
    public JsonData listByCategory(@PathVariable Long categoryId) {
        ProductQueryRequest queryRequest = new ProductQueryRequest();
        queryRequest.setCategoryId(categoryId);
        queryRequest.setOnlyInStock(true); // 只查询有库存的商品

        return productService.list(queryRequest);
    }

    /**
     * 获取热销商品列表
     */
    @GetMapping("/hot")
    public JsonData getHotProducts() {
        ProductQueryRequest queryRequest = new ProductQueryRequest();
        queryRequest.setOrderBy("sales_count");
        queryRequest.setOrderDirection("DESC");
        queryRequest.setOnlyInStock(true); // 只获取有库存的商品
        queryRequest.setPageSize(10); // 限制返回10个热销商品

        return productService.list(queryRequest);
    }

    /**
     * 获取推荐商品列表（按权重排序）
     */
    @GetMapping("/recommend")
    public JsonData getRecommendProducts() {
        ProductQueryRequest queryRequest = new ProductQueryRequest();
        queryRequest.setOrderBy("sort");
        queryRequest.setOrderDirection("DESC");
        queryRequest.setOnlyInStock(true); // 只获取有库存的商品
        queryRequest.setPageSize(10); // 限制返回10个推荐商品

        return productService.list(queryRequest);
    }

    /**
     * 根据ID获取商品详情
     */
    @GetMapping("/{id}")
    public JsonData getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    /**
     * 搜索商品（根据标题关键词）
     */
    @GetMapping("/search")
    public JsonData search(@RequestParam String keyword) {
        ProductQueryRequest queryRequest = new ProductQueryRequest();
        queryRequest.setTitle(keyword);
        queryRequest.setOnlyInStock(true); // 只搜索有库存的商品

        return productService.list(queryRequest);
    }

    /**
     * 根据价格区间查询商品
     */
    @GetMapping("/price-range")
    public JsonData listByPriceRange(@RequestParam(required = false) String minPrice,
                                     @RequestParam(required = false) String maxPrice) {
        ProductQueryRequest queryRequest = new ProductQueryRequest();

        if (minPrice != null && !minPrice.isEmpty()) {
            try {
                queryRequest.setMinPrice(new java.math.BigDecimal(minPrice));
            } catch (NumberFormatException e) {
                return JsonData.buildError("最小价格格式不正确");
            }
        }

        if (maxPrice != null && !maxPrice.isEmpty()) {
            try {
                queryRequest.setMaxPrice(new java.math.BigDecimal(maxPrice));
            } catch (NumberFormatException e) {
                return JsonData.buildError("最大价格格式不正确");
            }
        }

        queryRequest.setOnlyInStock(true); // 只查询有库存的商品

        return productService.list(queryRequest);
    }
}
