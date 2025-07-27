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

    // ==================== RPC 接口 ====================

    /**
     * RPC - 批量获取商品详情
     * 该接口用于其他微服务批量获取商品信息
     *
     * @param requestBody 请求体，包含商品ID列表
     * @param request HTTP请求对象，用于获取调用方信息
     * @return 商品详情列表
     */
    @PostMapping("/batch")
    public JsonData getBatchProductDetails(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        // RPC安全校验由拦截器处理，这里直接处理业务逻辑
        String rpcSource = request.getHeader("RPC-Source");
        log.info("RPC接口被调用 - 批量获取商品详情: rpcSource={}", rpcSource);

        // 从请求体中获取商品ID列表
        List<Long> productIds = null;
        try {
            @SuppressWarnings("unchecked")
            List<Object> productIdObjs = (List<Object>) requestBody.get("productIds");
            if (productIdObjs != null) {
                productIds = productIdObjs.stream()
                    .map(obj -> Long.valueOf(obj.toString()))
                    .toList();
            }
        } catch (Exception e) {
            log.error("解析商品ID列表失败", e);
            return JsonData.buildError("商品ID列表格式错误");
        }

        if (productIds == null || productIds.isEmpty()) {
            return JsonData.buildError("商品ID列表不能为空");
        }

        log.info("批量获取商品详情: productIds={}", productIds);

        try {
            // 调用服务层方法批量获取商品详情
            return productService.getBatchProductDetails(productIds);
        } catch (Exception e) {
            log.error("批量获取商品详情失败: productIds={}", productIds, e);
            return JsonData.buildError("获取商品详情失败: " + e.getMessage());
        }
    }

    /**
     * RPC - 验证商品库存
     * 该接口用于其他微服务验证商品库存是否充足
     *
     * @param requestBody 请求体，包含商品ID和需要验证的数量
     * @param request HTTP请求对象，用于获取调用方信息
     * @return 验证结果
     */
    @PostMapping("/rpc/stock/validate")
    public JsonData validateStockForRpc(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        // RPC安全校验由拦截器处理，这里直接处理业务逻辑
        String rpcSource = request.getHeader("RPC-Source");
        log.info("RPC接口被调用 - 验证商品库存: rpcSource={}", rpcSource);

        // 从请求体中获取商品ID和数量
        Long productId = null;
        Integer quantity = null;
        try {
            Object productIdObj = requestBody.get("productId");
            if (productIdObj != null) {
                productId = Long.valueOf(productIdObj.toString());
            }
            
            Object quantityObj = requestBody.get("quantity");
            if (quantityObj != null) {
                quantity = Integer.valueOf(quantityObj.toString());
            }
        } catch (Exception e) {
            log.error("解析请求参数失败", e);
            return JsonData.buildError("请求参数格式错误");
        }

        if (productId == null) {
            return JsonData.buildError("商品ID不能为空");
        }

        if (quantity == null || quantity <= 0) {
            return JsonData.buildError("验证数量必须大于0");
        }

        log.info("验证商品库存: productId={}, quantity={}", productId, quantity);

        try {
            // 调用服务层方法验证库存
            return productService.validateStock(productId, quantity);
        } catch (Exception e) {
            log.error("验证商品库存失败: productId={}, quantity={}", productId, quantity, e);
            return JsonData.buildError("验证库存失败: " + e.getMessage());
        }
    }
}
