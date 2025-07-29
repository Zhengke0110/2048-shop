package fun.timu.shop.product.controller;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.product.controller.request.ProductCreateRequest;
import fun.timu.shop.product.controller.request.ProductQueryRequest;
import fun.timu.shop.product.controller.request.ProductUpdateRequest;
import fun.timu.shop.common.enums.ProductStatusEnum;
import fun.timu.shop.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品管理控制器
 * 管理员专用，包含商品的增删改查操作
 * 需要管理员权限才能访问
 *
 * @author zhengke
 */
@Slf4j
@RestController
@RequestMapping("/api/product/v1/admin/product")
@RequiredArgsConstructor
public class ProductAdminController {

    private final ProductService productService;

    /**
     * 管理员查询商品列表（包含所有状态的商品）
     */
    @PostMapping("/list")
    public JsonData adminList(@RequestBody(required = false) ProductQueryRequest queryRequest) {
        // 如果没有传查询条件，使用默认条件
        if (queryRequest == null) {
            queryRequest = new ProductQueryRequest();
        }
        // 管理员可以查看所有状态的商品，包括下架的
        // 不设置状态过滤条件，在Manager层会有默认处理

        return productService.list(queryRequest);
    }

    /**
     * 创建商品
     */
    @PostMapping("")
    public JsonData create(@Valid @RequestBody ProductCreateRequest createRequest) {
        return productService.create(createRequest);
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public JsonData update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest updateRequest) {
        return productService.update(id, updateRequest);
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public JsonData delete(@PathVariable Long id) {
        return productService.delete(id);
    }

    /**
     * 批量删除商品
     */
    @DeleteMapping("/batch")
    public JsonData batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return JsonData.buildError("商品ID列表不能为空");
        }
        return productService.batchDelete(ids);
    }

    /**
     * 上架商品
     */
    @PutMapping("/{id}/online")
    public JsonData online(@PathVariable Long id) {
        return productService.updateStatus(id, ProductStatusEnum.ONLINE.getCode());
    }

    /**
     * 下架商品
     */
    @PutMapping("/{id}/offline")
    public JsonData offline(@PathVariable Long id) {
        return productService.updateStatus(id, ProductStatusEnum.OFFLINE.getCode());
    }

    /**
     * 批量上架商品
     */
    @PutMapping("/batch/online")
    public JsonData batchOnline(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return JsonData.buildError("商品ID列表不能为空");
        }
        return productService.batchUpdateStatus(ids, ProductStatusEnum.ONLINE.getCode());
    }

    /**
     * 批量下架商品
     */
    @PutMapping("/batch/offline")
    public JsonData batchOffline(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return JsonData.buildError("商品ID列表不能为空");
        }
        return productService.batchUpdateStatus(ids, ProductStatusEnum.OFFLINE.getCode());
    }

    /**
     * 获取商品详情（管理员视图，包含更多信息）
     */
    @GetMapping("/{id}")
    public JsonData getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    /**
     * 库存管理 - 增加库存
     */
    @PutMapping("/{id}/stock/increase")
    public JsonData increaseStock(@PathVariable Long id, @RequestParam Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return JsonData.buildError("增加数量必须大于0");
        }
        return productService.increaseStock(id, quantity);
    }

    /**
     * 库存管理 - 扣减库存
     */
    @PutMapping("/{id}/stock/decrease")
    public JsonData decreaseStock(@PathVariable Long id, @RequestParam Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return JsonData.buildError("扣减数量必须大于0");
        }
        return productService.decreaseStock(id, quantity);
    }

    /**
     * 库存管理 - 锁定库存
     */
    @PutMapping("/{id}/stock/lock")
    public JsonData lockStock(@PathVariable Long id, @RequestParam Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return JsonData.buildError("锁定数量必须大于0");
        }
        return productService.lockStock(id, quantity);
    }

    /**
     * 库存管理 - 释放锁定库存
     */
    @PutMapping("/{id}/stock/release")
    public JsonData releaseLockStock(@PathVariable Long id, @RequestParam Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return JsonData.buildError("释放数量必须大于0");
        }
        return productService.releaseLockStock(id, quantity);
    }
}
