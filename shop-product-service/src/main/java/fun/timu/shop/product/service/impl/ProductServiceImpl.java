package fun.timu.shop.product.service.impl;

import fun.timu.shop.common.enums.*;
import fun.timu.shop.common.exception.BizException;
import fun.timu.shop.common.interceptor.LoginInterceptor;
import fun.timu.shop.common.model.LoginUser;
import fun.timu.shop.common.model.ProductMessage;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.common.util.RabbitMQUtil;
import fun.timu.shop.common.request.LockProductRequest;
import fun.timu.shop.common.request.OrderItemRequest;
import fun.timu.shop.common.request.QueryOrderStateRequest;
import fun.timu.shop.product.config.RabbitMQConfig;
import fun.timu.shop.product.controller.request.ProductCreateRequest;
import fun.timu.shop.product.controller.request.ProductQueryRequest;
import fun.timu.shop.product.controller.request.ProductUpdateRequest;
import fun.timu.shop.product.converter.ProductConverter;
import fun.timu.shop.product.feign.OrderFeignService;
import fun.timu.shop.product.manager.ProductManager;
import fun.timu.shop.product.manager.ProductTaskManager;
import fun.timu.shop.product.mapper.ProductMapper;
import fun.timu.shop.product.model.DO.ProductDO;
import fun.timu.shop.product.model.DO.ProductTaskDO;
import fun.timu.shop.product.model.VO.ProductVO;
import fun.timu.shop.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhengke
 * @description 针对表【product(商品表)】的数据库操作Service实现
 * @createDate 2025-07-27 16:38:53
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductManager productManager;
    private final ProductConverter productConverter;
    private final ProductTaskManager productTaskManager;
    private final RabbitMQUtil rabbitMQUtil;
    private final RabbitMQConfig rabbitMQConfig;
    private final OrderFeignService orderFeignService;

    @Override
    public JsonData list(ProductQueryRequest queryRequest) {
        try {
            List<ProductDO> productDOList = productManager.listByQuery(queryRequest);
            List<ProductVO> productVOList = productConverter.convertToVOList(productDOList);
            return JsonData.buildSuccess(productVOList);
        } catch (BizException e) {
            log.error("查询商品列表失败: {}", e.getMessage());
            return JsonData.buildError(e.getMessage());
        } catch (Exception e) {
            log.error("查询商品列表异常", e);
            return JsonData.buildError("查询商品列表失败");
        }
    }

    @Override
    public JsonData getById(Long id) {
        try {
            if (id == null || id <= 0) {
                return JsonData.buildError("商品ID不能为空或无效");
            }

            ProductDO productDO = productManager.getByIdNotDeleted(id);
            if (productDO == null) {
                return JsonData.buildError("商品不存在");
            }

            ProductVO productVO = productConverter.convertToVO(productDO);
            return JsonData.buildSuccess(productVO);
        } catch (BizException e) {
            log.error("获取商品详情失败: {}", e.getMessage());
            return JsonData.buildError(e.getMessage());
        } catch (Exception e) {
            log.error("获取商品详情异常", e);
            return JsonData.buildError("获取商品详情失败");
        }
    }

    @Override
    public JsonData create(ProductCreateRequest createRequest) {
        try {
            // 权限校验：创建商品需要管理员权限
            LoginUser currentUser = validateAdminPermission();

            if (createRequest == null) {
                return JsonData.buildError("商品数据不能为空");
            }

            // 将Request转换为DO对象
            ProductDO productDO = productConverter.convertCreateRequestToDO(createRequest);

            // 设置默认值
            if (productDO.getSort() == null) {
                productDO.setSort(0);
            }
            if (productDO.getStatus() == null) {
                productDO.setStatus(ProductStatusEnum.ONLINE.getCode());
            }
            if (productDO.getSalesCount() == null) {
                productDO.setSalesCount(0);
            }
            if (productDO.getLockStock() == null) {
                productDO.setLockStock(0);
            }
            if (productDO.getDelFlag() == null) {
                productDO.setDelFlag(DelFlagEnum.NOT_DELETED.getFlag());
            }

            log.info("管理员创建商品, 操作人: {}, 商品标题: {}", currentUser.getName(), productDO.getTitle());
            boolean success = productManager.save(productDO);
            if (success) {
                return JsonData.buildSuccess("创建商品成功");
            } else {
                return JsonData.buildError("创建商品失败");
            }
        } catch (BizException e) {
            log.error("创建商品失败: {}", e.getMessage());
            return JsonData.buildError(e.getMessage());
        } catch (Exception e) {
            log.error("创建商品异常", e);
            return JsonData.buildError("创建商品失败");
        }
    }

    @Override
    public JsonData update(Long id, ProductUpdateRequest updateRequest) {
        try {
            // 权限校验：更新商品需要管理员权限
            LoginUser currentUser = validateAdminPermission();

            if (id == null || id <= 0) {
                return JsonData.buildError("商品ID不能为空或无效");
            }

            if (updateRequest == null) {
                return JsonData.buildError("商品数据不能为空");
            }

            // 验证商品是否存在
            ProductDO existingProduct = productManager.getByIdNotDeleted(id);
            if (existingProduct == null) {
                return JsonData.buildError("商品不存在或已被删除");
            }

            // 将Request转换为DO对象
            ProductDO productDO = productConverter.convertUpdateRequestToDO(id, updateRequest);

            log.info("管理员更新商品, 操作人: {}, 商品ID: {}", currentUser.getName(), id);
            boolean success = productManager.updateById(productDO);
            if (success) {
                return JsonData.buildSuccess("更新商品成功");
            } else {
                return JsonData.buildError("更新商品失败");
            }
        } catch (BizException e) {
            log.error("更新商品失败: {}", e.getMessage());
            return JsonData.buildError(e.getMessage());
        } catch (Exception e) {
            log.error("更新商品异常", e);
            return JsonData.buildError("更新商品失败");
        }
    }

    @Override
    public JsonData delete(Long id) {
        try {
            // 权限校验：删除商品需要管理员权限
            LoginUser currentUser = validateAdminPermission();

            if (id == null || id <= 0) {
                return JsonData.buildError("商品ID不能为空或无效");
            }

            // 验证商品是否存在
            ProductDO existingProduct = productManager.getByIdNotDeleted(id);
            if (existingProduct == null) {
                return JsonData.buildError("商品不存在或已被删除");
            }

            log.info("管理员删除商品, 操作人: {}, 商品ID: {}", currentUser.getName(), id);
            boolean success = productManager.logicDeleteById(id);
            if (success) {
                return JsonData.buildSuccess("删除商品成功");
            } else {
                return JsonData.buildError("删除商品失败");
            }
        } catch (BizException e) {
            log.error("删除商品失败: {}", e.getMessage());
            return JsonData.buildError(e.getMessage());
        } catch (Exception e) {
            log.error("删除商品异常", e);
            return JsonData.buildError("删除商品失败");
        }
    }

    @Override
    public JsonData batchDelete(List<Long> ids) {
        try {
            // 权限校验：批量删除商品需要管理员权限
            LoginUser currentUser = validateAdminPermission();

            if (ids == null || ids.isEmpty()) {
                return JsonData.buildError("商品ID列表不能为空");
            }

            log.info("管理员批量删除商品, 操作人: {}, 删除数量: {}", currentUser.getName(), ids.size());
            boolean success = productManager.logicDeleteBatchByIds(ids);
            if (success) {
                return JsonData.buildSuccess("批量删除商品成功");
            } else {
                return JsonData.buildError("批量删除商品失败");
            }
        } catch (BizException e) {
            log.error("批量删除商品失败: {}", e.getMessage());
            return JsonData.buildError(e.getMessage());
        } catch (Exception e) {
            log.error("批量删除商品异常", e);
            return JsonData.buildError("批量删除商品失败");
        }
    }

    @Override
    public JsonData updateStatus(Long id, Integer status) {
        try {
            // 权限校验：更新商品状态需要管理员权限
            LoginUser currentUser = validateAdminPermission();

            if (id == null || id <= 0) {
                return JsonData.buildError("商品ID不能为空或无效");
            }

            if (!ProductStatusEnum.isValid(status)) {
                return JsonData.buildError("商品状态无效");
            }

            // 验证商品是否存在
            ProductDO existingProduct = productManager.getByIdNotDeleted(id);
            if (existingProduct == null) {
                return JsonData.buildError("商品不存在或已被删除");
            }

            ProductStatusEnum productStatus = ProductStatusEnum.getByCode(status);
            String statusDesc = productStatus != null ? productStatus.getDesc() : "未知状态";
            log.info("管理员更新商品状态, 操作人: {}, 商品ID: {}, 状态: {}", currentUser.getName(), id, statusDesc);

            boolean success = productManager.updateStatusById(id, status);
            if (success) {
                return JsonData.buildSuccess(statusDesc + "商品成功");
            } else {
                return JsonData.buildError(statusDesc + "商品失败");
            }
        } catch (BizException e) {
            log.error("更新商品状态失败: {}", e.getMessage());
            return JsonData.buildError(e.getMessage());
        } catch (Exception e) {
            log.error("更新商品状态异常", e);
            return JsonData.buildError("更新商品状态失败");
        }
    }

    @Override
    public JsonData batchUpdateStatus(List<Long> ids, Integer status) {
        try {
            // 权限校验：批量更新商品状态需要管理员权限
            LoginUser currentUser = validateAdminPermission();

            if (ids == null || ids.isEmpty()) {
                return JsonData.buildError("商品ID列表不能为空");
            }

            if (!ProductStatusEnum.isValid(status)) {
                return JsonData.buildError("商品状态无效");
            }

            ProductStatusEnum productStatus = ProductStatusEnum.getByCode(status);
            String statusDesc = productStatus != null ? productStatus.getDesc() : "未知状态";
            log.info("管理员批量更新商品状态, 操作人: {}, 商品数量: {}, 状态: {}", currentUser.getName(), ids.size(), statusDesc);

            boolean success = productManager.batchUpdateStatus(ids, status);
            if (success) {
                return JsonData.buildSuccess("批量" + statusDesc + "商品成功");
            } else {
                return JsonData.buildError("批量" + statusDesc + "商品失败");
            }
        } catch (BizException e) {
            log.error("批量更新商品状态失败: {}", e.getMessage());
            return JsonData.buildError(e.getMessage());
        } catch (Exception e) {
            log.error("批量更新商品状态异常", e);
            return JsonData.buildError("批量更新商品状态失败");
        }
    }

    @Override
    public JsonData decreaseStock(Long id, Integer quantity) {
        try {
            if (id == null || id <= 0) {
                return JsonData.buildError("商品ID不能为空或无效");
            }

            if (quantity == null || quantity <= 0) {
                return JsonData.buildError("扣减数量必须大于0");
            }

            boolean success = productManager.decreaseStock(id, quantity);
            if (success) {
                return JsonData.buildSuccess("扣减库存成功");
            } else {
                return JsonData.buildError("扣减库存失败，可能库存不足");
            }
        } catch (Exception e) {
            log.error("扣减库存异常", e);
            return JsonData.buildError("扣减库存失败");
        }
    }

    @Override
    public JsonData increaseStock(Long id, Integer quantity) {
        try {
            if (id == null || id <= 0) {
                return JsonData.buildError("商品ID不能为空或无效");
            }

            if (quantity == null || quantity <= 0) {
                return JsonData.buildError("增加数量必须大于0");
            }

            boolean success = productManager.increaseStock(id, quantity);
            if (success) {
                return JsonData.buildSuccess("增加库存成功");
            } else {
                return JsonData.buildError("增加库存失败");
            }
        } catch (Exception e) {
            log.error("增加库存异常", e);
            return JsonData.buildError("增加库存失败");
        }
    }

    @Override
    public JsonData lockStock(Long id, Integer quantity) {
        try {
            if (id == null || id <= 0) {
                return JsonData.buildError("商品ID不能为空或无效");
            }

            if (quantity == null || quantity <= 0) {
                return JsonData.buildError("锁定数量必须大于0");
            }

            boolean success = productManager.lockStock(id, quantity);
            if (success) {
                return JsonData.buildSuccess("锁定库存成功");
            } else {
                return JsonData.buildError("锁定库存失败，可能库存不足");
            }
        } catch (Exception e) {
            log.error("锁定库存异常", e);
            return JsonData.buildError("锁定库存失败");
        }
    }

    /**
     * 锁定商品库存
     * <p>
     * 1)遍历商品，锁定每个商品购买数量
     * 2)每一次锁定的时候，都要发送延迟消息
     *
     * @param lockProductRequest
     * @return
     */
    @Override
    public JsonData lockProductStock(LockProductRequest lockProductRequest) {
        String outTradeNo = lockProductRequest.getOrderOutTradeNo();
        List<OrderItemRequest> itemList = lockProductRequest.getOrderItemList();

        // 一行代码，提取对象里面的id并加入到集合里面
        List<Long> productIdList = itemList.stream().map(OrderItemRequest::getProductId).collect(Collectors.toList());

        // 批量查询
        List<ProductVO> productVOList = this.findProductsByIdBatch(productIdList);

        // 分组
        Map<Long, ProductVO> productMap = productVOList.stream().collect(Collectors.toMap(ProductVO::getId, Function.identity()));

        for (OrderItemRequest item : itemList) {
            // 锁定商品记录
            boolean result = productManager.lockStock(item.getProductId(), item.getBuyNum());
            if (result) {
                // 锁定成功，插入商品product_task记录
                ProductVO productVO = productMap.get(item.getProductId());
                ProductTaskDO productTaskDO = new ProductTaskDO();
                productTaskDO.setBuyNum(item.getBuyNum());
                productTaskDO.setLockState(StockTaskStateEnum.LOCK.name());
                productTaskDO.setProductId(item.getProductId());
                productTaskDO.setProductName(productVO.getTitle());
                productTaskDO.setOutTradeNo(outTradeNo);
                productTaskManager.insert(productTaskDO);
                log.info("商品库存锁定成功-插入商品product_task成功:{}", productTaskDO);

                // 发送MQ延迟消息，用于超时释放商品库存（30分钟后）
                ProductMessage productMessage = new ProductMessage();
                productMessage.setOutTradeNo(outTradeNo);
                productMessage.setTaskId(productTaskDO.getId());

                // 使用 RabbitMQUtil 发送延迟消息，30分钟后自动释放商品库存
                Long delayTime = 30 * 60 * 1000L; // 30分钟延迟时间（毫秒）
                rabbitMQUtil.sendDelayMessage(
                        rabbitMQConfig.getEventExchange(), 
                        rabbitMQConfig.getStockReleaseDelayRoutingKey(), 
                        productMessage, 
                        delayTime
                );
                
                log.info("商品库存锁定延迟消息发送成功: outTradeNo={}, taskId={}, delayTime={}ms", 
                        outTradeNo, productTaskDO.getId(), delayTime);
            } else {
                // 锁定失败，抛出异常
                log.error("商品库存锁定失败: productId={}, buyNum={}", item.getProductId(), item.getBuyNum());
                throw new BizException(BizCodeEnum.ORDER_CONFIRM_LOCK_PRODUCT_FAIL);
            }
        }
        return JsonData.buildSuccess();
    }

    @Override
    public JsonData releaseLockStock(Long id, Integer quantity) {
        try {
            if (id == null || id <= 0) {
                return JsonData.buildError("商品ID不能为空或无效");
            }

            if (quantity == null || quantity <= 0) {
                return JsonData.buildError("释放数量必须大于0");
            }

            boolean success = productManager.releaseLockStock(id, quantity);
            if (success) {
                return JsonData.buildSuccess("释放锁定库存成功");
            } else {
                return JsonData.buildError("释放锁定库存失败");
            }
        } catch (Exception e) {
            log.error("释放锁定库存异常", e);
            return JsonData.buildError("释放锁定库存失败");
        }
    }

    private LoginUser validateLogin() {
        LoginUser currentUser = LoginInterceptor.threadLocal.get();
        if (currentUser == null) {
            throw new BizException(BizCodeEnum.ACCOUNT_UNLOGIN);
        }
        return currentUser;
    }

    private LoginUser validateAdminPermission() {
        LoginUser currentUser = validateLogin();

        if (!isAdmin(currentUser)) {
            log.warn("用户权限不足, userId: {}, userName: {}", currentUser.getId(), currentUser.getName());
            throw new BizException(BizCodeEnum.ACCOUNT_FORBIDDEN);
        }

        return currentUser;
    }

    private boolean isAdmin(LoginUser user) {
        if (user == null) {
            return false;
        }

        // 方案1: 根据用户ID判断（超级管理员）
        if (user.getId().equals(1L)) {
            return true;
        }

        // 方案2: 根据用户名判断
        if ("admin".equals(user.getName()) || user.getName().startsWith("admin_")) {
            return true;
        }

        // 方案3: 根据邮箱域名判断
        if (user.getMail() != null && user.getMail().endsWith("@admin.com")) {
            return true;
        }

        return false;
    }

    // ==================== RPC 相关方法实现 ====================

    @Override
    public JsonData getBatchProductDetails(List<Long> productIds) {
        try {
            if (productIds == null || productIds.isEmpty()) {
                return JsonData.buildError("商品ID列表不能为空");
            }

            log.info("批量获取商品详情: productIds={}", productIds);

            List<ProductDO> productDOList = productManager.listByIds(productIds);

            if (productDOList.isEmpty()) {
                log.warn("未找到任何商品: productIds={}", productIds);
                return JsonData.buildSuccess(List.of());
            }

            // 转换为VO对象，这里返回Map格式方便购物车服务使用
            Map<String, Object> productMap = new HashMap<>();
            for (ProductDO productDO : productDOList) {
                Map<String, Object> productInfo = new HashMap<>();
                productInfo.put("id", productDO.getId());
                productInfo.put("title", productDO.getTitle());
                productInfo.put("coverImg", productDO.getCoverImg());
                productInfo.put("price", productDO.getPrice());
                productInfo.put("stock", productDO.getStock());
                productInfo.put("status", productDO.getStatus());

                productMap.put(productDO.getId().toString(), productInfo);
            }

            log.info("批量获取商品详情成功: count={}", productDOList.size());
            return JsonData.buildSuccess(productMap);

        } catch (Exception e) {
            log.error("批量获取商品详情失败: productIds={}", productIds, e);
            return JsonData.buildError("批量获取商品详情失败: " + e.getMessage());
        }
    }

    @Override
    public JsonData validateStock(Long productId, Integer quantity) {
        try {
            if (productId == null) {
                return JsonData.buildError("商品ID不能为空");
            }

            if (quantity == null || quantity <= 0) {
                return JsonData.buildError("验证数量必须大于0");
            }

            log.info("验证商品库存: productId={}, quantity={}", productId, quantity);

            ProductDO productDO = productManager.selectById(productId);
            if (productDO == null) {
                log.warn("商品不存在: productId={}", productId);
                return JsonData.buildError("商品不存在");
            }

            // 检查商品状态
            if (!ProductStatusEnum.ONLINE.getCode().equals(productDO.getStatus())) {
                log.warn("商品已下架: productId={}, status={}", productId, productDO.getStatus());
                return JsonData.buildError("商品已下架");
            }

            // 检查是否已删除
            if (DelFlagEnum.DELETED.getFlag() == productDO.getDelFlag()) {
                log.warn("商品已删除: productId={}", productId);
                return JsonData.buildError("商品已删除");
            }

            // 检查库存是否充足
            Integer availableStock = productDO.getStock() - (productDO.getLockStock() != null ? productDO.getLockStock() : 0);
            if (availableStock < quantity) {
                log.warn("库存不足: productId={}, 需要数量={}, 可用库存={}", productId, quantity, availableStock);
                return JsonData.buildError("库存不足，当前可用库存：" + availableStock);
            }

            log.info("库存验证通过: productId={}, quantity={}, availableStock={}", productId, quantity, availableStock);

            Map<String, Object> result = new HashMap<>();
            result.put("productId", productId);
            result.put("requestQuantity", quantity);
            result.put("availableStock", availableStock);
            result.put("valid", true);

            return JsonData.buildSuccess(result);

        } catch (Exception e) {
            log.error("验证商品库存失败: productId={}, quantity={}", productId, quantity, e);
            return JsonData.buildError("验证库存失败: " + e.getMessage());
        }
    }

    @Override
    public List<ProductVO> findProductsByIdBatch(List<Long> productIds) {
        try {
            if (productIds == null || productIds.isEmpty()) {
                return List.of();
            }

            List<ProductDO> productDOList = productManager.listByIds(productIds);
            return productConverter.convertToVOList(productDOList);
        } catch (Exception e) {
            log.error("批量查询商品失败: productIds={}", productIds, e);
            return List.of();
        }
    }

    /**
     * 释放商品库存
     *
     * @param productMessage
     * @return
     */
    @Override
    public boolean releaseProductStock(ProductMessage productMessage) {
        if (productMessage == null) {
            log.warn("商品消息为空");
            return true;
        }

        if (productMessage.getTaskId() == null) {
            log.warn("任务ID为空，消息体为:{}", productMessage);
            return true;
        }

        // 查询工作单状态
        ProductTaskDO taskDO = productTaskManager.selectById(productMessage.getTaskId());
        if (taskDO == null) {
            log.warn("工作单不存在，消息体为:{}", productMessage);
            return true;
        }

        // lock状态才处理
        if (taskDO.getLockState().equalsIgnoreCase(StockTaskStateEnum.LOCK.name())) {

            // 查询订单状态
            QueryOrderStateRequest queryRequest = new QueryOrderStateRequest();
            queryRequest.setOutTradeNo(productMessage.getOutTradeNo());

            JsonData jsonData = orderFeignService.queryProductOrderState(queryRequest);

            if (jsonData.getCode() == 0) {
                String state = jsonData.getData().toString();

                if (OrderStateEnum.NEW.name().equalsIgnoreCase(state)) {
                    // 状态是NEW新建状态，则返回给消息队列重新投递
                    log.warn("订单状态是NEW,返回给消息队列，重新投递:{}", productMessage);
                    return false;
                }

                // 如果是已经支付
                if (OrderStateEnum.PAY.name().equalsIgnoreCase(state)) {
                    // 如果已经支付，修改task状态为finish
                    taskDO.setLockState(StockTaskStateEnum.FINISH.name());
                    productTaskManager.updateEntity(taskDO, productMessage.getTaskId());
                    log.info("订单已经支付，修改库存锁定工作单FINISH状态:{}", productMessage);
                    return true;
                }
            }

            // 订单不存在，或者订单被取消，确认消息,修改task状态为CANCEL,恢复商品库存
            log.warn("订单不存在，或者订单被取消，确认消息,修改task状态为CANCEL,恢复商品库存,message:{}", productMessage);
            taskDO.setLockState(StockTaskStateEnum.CANCEL.name());
            productTaskManager.updateEntity(taskDO, productMessage.getTaskId());

            // 恢复商品库存，将锁定库存的值减去当前购买的值
            boolean result = productManager.releaseLockStock(taskDO.getProductId(), taskDO.getBuyNum());
            if (result) {
                log.info("成功恢复商品库存，商品ID:{}, 恢复数量:{}", taskDO.getProductId(), taskDO.getBuyNum());
            } else {
                log.warn("恢复商品库存失败，商品ID:{}, 恢复数量:{}", taskDO.getProductId(), taskDO.getBuyNum());
            }

            return true;

        } else {
            log.warn("工作单状态不是LOCK,state={},消息体={}", taskDO.getLockState(), productMessage);
            return true;
        }
    }
}