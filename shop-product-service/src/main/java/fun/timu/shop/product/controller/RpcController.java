package fun.timu.shop.product.controller;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.product.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品服务 RPC 接口控制器
 * 专门处理服务间调用的接口
 */
@Slf4j
@RestController
@RequestMapping("/api/product/v1/rpc")
@RequiredArgsConstructor
public class RpcController {

    private final ProductService productService;

    /**
     * RPC - 批量获取商品详情
     * 该接口用于其他微服务批量获取商品信息
     *
     * @param requestBody 请求体，包含商品ID列表
     * @param request     HTTP请求对象，用于获取调用方信息
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
     * @param request     HTTP请求对象，用于获取调用方信息
     * @return 验证结果
     */
    @PostMapping("/stock/validate")
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
