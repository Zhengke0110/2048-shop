package fun.timu.shop.product.controller;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.common.request.BatchProductRequest;
import fun.timu.shop.common.request.LockProductRequest;
import fun.timu.shop.common.request.ValidateStockRequest;
import fun.timu.shop.product.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * @param batchRequest 批量获取商品详情请求
     * @param request      HTTP请求对象，用于获取调用方信息
     * @return 商品详情列表
     */
    @PostMapping("/batch")
    public JsonData getBatchProductDetails(@RequestBody BatchProductRequest batchRequest, HttpServletRequest request) {
        // RPC安全校验由拦截器处理，这里直接处理业务逻辑
        String rpcSource = request.getHeader("RPC-Source");
        log.info("RPC接口被调用 - 批量获取商品详情: rpcSource={}", rpcSource);

        if (batchRequest == null) {
            return JsonData.buildError("请求参数不能为空");
        }

        List<Long> productIds = batchRequest.getProductIds();
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
     * @param validateRequest 验证库存请求
     * @param request         HTTP请求对象，用于获取调用方信息
     * @return 验证结果
     */
    @PostMapping("/stock/validate")
    public JsonData validateStockForRpc(@RequestBody ValidateStockRequest validateRequest, HttpServletRequest request) {
        // RPC安全校验由拦截器处理，这里直接处理业务逻辑
        String rpcSource = request.getHeader("RPC-Source");
        log.info("RPC接口被调用 - 验证商品库存: rpcSource={}", rpcSource);

        if (validateRequest == null) {
            return JsonData.buildError("请求参数不能为空");
        }

        Long productId = validateRequest.getProductId();
        Integer quantity = validateRequest.getQuantity();

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

    /**
     * RPC - 锁定商品库存
     * 该接口用于其他微服务锁定商品库存
     *
     * @param lockProductRequest 锁定库存请求
     * @param request            HTTP请求对象，用于获取调用方信息
     * @return 锁定结果
     */
    @PostMapping("/stock/lock")
    public JsonData lockProductStock(@RequestBody LockProductRequest lockProductRequest, HttpServletRequest request) {
        // RPC安全校验由拦截器处理，这里直接处理业务逻辑
        String rpcSource = request.getHeader("RPC-Source");
        log.info("RPC接口被调用 - 锁定商品库存: rpcSource={}, request={}", rpcSource, lockProductRequest);

        if (lockProductRequest == null) {
            return JsonData.buildError("请求参数不能为空");
        }

        if (lockProductRequest.getOrderOutTradeNo() == null || lockProductRequest.getOrderOutTradeNo().trim().isEmpty()) {
            return JsonData.buildError("订单号不能为空");
        }

        if (lockProductRequest.getOrderItemList() == null || lockProductRequest.getOrderItemList().isEmpty()) {
            return JsonData.buildError("商品列表不能为空");
        }

        try {
            // 调用服务层方法锁定库存
            return productService.lockProductStock(lockProductRequest);
        } catch (Exception e) {
            log.error("锁定商品库存失败: request={}", lockProductRequest, e);
            return JsonData.buildError("锁定库存失败: " + e.getMessage());
        }
    }
}
