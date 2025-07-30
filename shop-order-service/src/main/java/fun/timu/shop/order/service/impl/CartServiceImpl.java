package fun.timu.shop.order.service.impl;

import com.alibaba.fastjson2.JSON;
import fun.timu.shop.common.enums.BizCodeEnum;
import fun.timu.shop.common.interceptor.LoginInterceptor;
import fun.timu.shop.common.model.LoginUser;
import fun.timu.shop.common.request.BatchProductRequest;
import fun.timu.shop.common.request.ValidateStockRequest;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.order.feign.ProductFeignService;
import fun.timu.shop.order.config.CartProperties;
import fun.timu.shop.order.controller.request.AddToCartRequest;
import fun.timu.shop.order.controller.request.BatchCartRequest;
import fun.timu.shop.order.controller.request.UpdateCartRequest;
import fun.timu.shop.order.converter.CartConverter;
import fun.timu.shop.order.manager.CartManager;
import fun.timu.shop.order.model.DO.CartDO;
import fun.timu.shop.order.model.DTO.CartItemDTO;
import fun.timu.shop.order.model.VO.CartItemVO;
import fun.timu.shop.order.model.VO.CartVO;
import fun.timu.shop.order.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类
 *
 * @author zhengke
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartManager cartManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CartProperties cartProperties;
    private final ProductFeignService productFeignService;
    private final CartConverter cartConverter;

    @Override
    @Transactional
    public JsonData addToCart(AddToCartRequest request) {
        LoginUser loginUser = LoginInterceptor.getCurrentUser();
        if (loginUser == null) {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
        }

        Long userId = loginUser.getId();
        Long productId = request.getProductId();
        Integer quantity = request.getQuantity();

        log.info("用户添加商品到购物车: userId={}, productId={}, quantity={}", userId, productId, quantity);

        try {
            // 1. 验证商品是否存在和有效
            JsonData productResult = productFeignService.getProductById(productId);
            if (productResult.getCode() != 0) {
                return JsonData.buildError("商品不存在或已下架");
            }

                        // 2. 验证库存
            ValidateStockRequest stockRequest = new ValidateStockRequest();
            stockRequest.setProductId(productId);
            stockRequest.setQuantity(quantity);
            JsonData stockResult = productFeignService.validateStock(stockRequest);
            if (stockResult.getCode() != 0) {
                return JsonData.buildError("商品库存不足");
            }

            // 3. 更新Redis购物车
            String redisKey = getCartRedisKey(userId);
            String productField = getProductField(productId);

            // 检查是否已存在该商品
            Object existingItem = redisTemplate.opsForHash().get(redisKey, productField);
            CartItemDTO cartItem;

            if (existingItem != null) {
                // 已存在，更新数量
                cartItem = JSON.parseObject(existingItem.toString(), CartItemDTO.class);
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
                cartItem.setUpdateTime(System.currentTimeMillis());
            } else {
                // 不存在，新增
                cartItem = new CartItemDTO(productId, quantity);
            }

            // 写入Redis
            redisTemplate.opsForHash().put(redisKey, productField, JSON.toJSONString(cartItem));
            redisTemplate.expire(redisKey, cartProperties.getRedis().getExpireDays(), TimeUnit.DAYS);

            // 4. 同步到MySQL
            cartManager.insertOrUpdate(userId, productId, cartItem.getQuantity());

            log.info("添加商品到购物车成功: userId={}, productId={}, quantity={}", userId, productId, quantity);
            return JsonData.buildSuccess("添加成功");

        } catch (Exception e) {
            log.error("添加商品到购物车失败", e);
            return JsonData.buildError("添加失败，请重试");
        }
    }

    @Override
    @Transactional
    public JsonData updateCart(UpdateCartRequest request) {
        LoginUser loginUser = LoginInterceptor.getCurrentUser();
        if (loginUser == null) {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
        }

        Long userId = loginUser.getId();
        Long productId = request.getProductId();
        Integer quantity = request.getQuantity();

        log.info("用户更新购物车商品数量: userId={}, productId={}, quantity={}", userId, productId, quantity);

        try {
            // 1. 验证库存
            ValidateStockRequest stockRequest = new ValidateStockRequest();
            stockRequest.setProductId(productId);
            stockRequest.setQuantity(quantity);
            JsonData stockResult = productFeignService.validateStock(stockRequest);
            if (stockResult.getCode() != 0) {
                return JsonData.buildError("商品库存不足");
            }

            // 2. 更新Redis
            String redisKey = getCartRedisKey(userId);
            String productField = getProductField(productId);

            Object existingItem = redisTemplate.opsForHash().get(redisKey, productField);
            if (existingItem == null) {
                return JsonData.buildError("购物车中不存在该商品");
            }

            CartItemDTO cartItem = JSON.parseObject(existingItem.toString(), CartItemDTO.class);
            cartItem.setQuantity(quantity);
            cartItem.setUpdateTime(System.currentTimeMillis());

            redisTemplate.opsForHash().put(redisKey, productField, JSON.toJSONString(cartItem));

            // 3. 同步到MySQL
            cartManager.insertOrUpdate(userId, productId, quantity);

            log.info("更新购物车商品数量成功: userId={}, productId={}, quantity={}", userId, productId, quantity);
            return JsonData.buildSuccess("更新成功");

        } catch (Exception e) {
            log.error("更新购物车商品数量失败", e);
            return JsonData.buildError("更新失败，请重试");
        }
    }

    @Override
    @Transactional
    public JsonData removeFromCart(Long productId) {
        LoginUser loginUser = LoginInterceptor.getCurrentUser();
        if (loginUser == null) {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
        }

        Long userId = loginUser.getId();

        log.info("用户从购物车删除商品: userId={}, productId={}", userId, productId);

        try {
            // 1. 从Redis删除
            String redisKey = getCartRedisKey(userId);
            String productField = getProductField(productId);
            redisTemplate.opsForHash().delete(redisKey, productField);

            // 2. 从MySQL删除
            cartManager.deleteBatchByUserIdAndProductIds(userId, Arrays.asList(productId));

            log.info("从购物车删除商品成功: userId={}, productId={}", userId, productId);
            return JsonData.buildSuccess("删除成功");

        } catch (Exception e) {
            log.error("从购物车删除商品失败", e);
            return JsonData.buildError("删除失败，请重试");
        }
    }

    @Override
    @Transactional
    public JsonData batchRemove(BatchCartRequest request) {
        LoginUser loginUser = LoginInterceptor.getCurrentUser();
        if (loginUser == null) {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
        }

        Long userId = loginUser.getId();
        List<Long> productIds = request.getProductIds();

        log.info("用户批量删除购物车商品: userId={}, productIds={}", userId, productIds);

        try {
            // 1. 从Redis批量删除
            String redisKey = getCartRedisKey(userId);
            String[] productFields = productIds.stream()
                    .map(this::getProductField)
                    .toArray(String[]::new);
            redisTemplate.opsForHash().delete(redisKey, (Object[]) productFields);

            // 2. 从MySQL批量删除
            cartManager.deleteBatchByUserIdAndProductIds(userId, productIds);

            log.info("批量删除购物车商品成功: userId={}, count={}", userId, productIds.size());
            return JsonData.buildSuccess("删除成功");

        } catch (Exception e) {
            log.error("批量删除购物车商品失败", e);
            return JsonData.buildError("删除失败，请重试");
        }
    }

    @Override
    @Transactional
    public JsonData clearCart() {
        LoginUser loginUser = LoginInterceptor.getCurrentUser();
        if (loginUser == null) {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
        }

        Long userId = loginUser.getId();

        log.info("用户清空购物车: userId={}", userId);

        try {
            // 1. 清空Redis
            String redisKey = getCartRedisKey(userId);
            redisTemplate.delete(redisKey);

            // 2. 清空MySQL
            cartManager.deleteByUserId(userId);

            log.info("清空购物车成功: userId={}", userId);
            return JsonData.buildSuccess("清空成功");

        } catch (Exception e) {
            log.error("清空购物车失败", e);
            return JsonData.buildError("清空失败，请重试");
        }
    }

    @Override
    public JsonData getCartList() {
        LoginUser loginUser = LoginInterceptor.getCurrentUser();
        if (loginUser == null) {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
        }

        Long userId = loginUser.getId();

        try {
            // 从Redis获取购物车数据
            String redisKey = getCartRedisKey(userId);
            Map<Object, Object> cartMap = redisTemplate.opsForHash().entries(redisKey);

            if (cartMap.isEmpty()) {
                // Redis没有数据，尝试从MySQL加载
                loadUserCartFromDB(userId);
                cartMap = redisTemplate.opsForHash().entries(redisKey);
            }

            List<CartItemDTO> cartItems = cartMap.values().stream()
                    .map(item -> JSON.parseObject(item.toString(), CartItemDTO.class))
                    .sorted(Comparator.comparing(CartItemDTO::getUpdateTime).reversed())
                    .collect(Collectors.toList());

            return JsonData.buildSuccess(cartItems);

        } catch (Exception e) {
            log.error("获取购物车列表失败", e);
            return JsonData.buildError("获取失败，请重试");
        }
    }

    @Override
    public JsonData getCartCount() {
        LoginUser loginUser = LoginInterceptor.getCurrentUser();
        if (loginUser == null) {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
        }

        Long userId = loginUser.getId();

        try {
            String redisKey = getCartRedisKey(userId);
            Long count = redisTemplate.opsForHash().size(redisKey);
            return JsonData.buildSuccess(count);

        } catch (Exception e) {
            log.error("获取购物车商品数量失败", e);
            return JsonData.buildError("获取失败，请重试");
        }
    }

    @Override
    public JsonData getCartDetails() {
        LoginUser loginUser = LoginInterceptor.getCurrentUser();
        if (loginUser == null) {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
        }

        Long userId = loginUser.getId();

        try {
            // 1. 获取购物车商品列表
            JsonData cartListResult = getCartList();
            if (cartListResult.getCode() != 0) {
                return cartListResult;
            }

            @SuppressWarnings("unchecked")
            List<CartItemDTO> cartItems = (List<CartItemDTO>) cartListResult.getData();

            if (cartItems.isEmpty()) {
                return JsonData.buildSuccess(cartConverter.toEmptyCart(userId));
            }

            // 2. 获取商品详情
            List<Long> productIds = cartItems.stream()
                    .map(CartItemDTO::getProductId)
                    .collect(Collectors.toList());

            BatchProductRequest batchRequest = new BatchProductRequest();
            batchRequest.setProductIds(productIds);
            JsonData productResult = productFeignService.getBatchProductDetails(batchRequest);
            if (productResult.getCode() != 0) {
                return JsonData.buildError("获取商品信息失败");
            }

            // 3. 组装购物车详情
            CartVO cartVO = cartConverter.toCartWithDetails(userId, cartItems, productResult.getData());

            return JsonData.buildSuccess(cartVO);

        } catch (Exception e) {
            log.error("获取购物车详情失败", e);
            return JsonData.buildError("获取失败，请重试");
        }
    }

    @Override
    public JsonData validateCartItems() {
        LoginUser loginUser = LoginInterceptor.getCurrentUser();
        if (loginUser == null) {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
        }

        // 获取购物车详情，内部会验证商品有效性
        return getCartDetails();
    }

    @Override
    @Transactional
    public JsonData confirmCartItems(List<Long> productIds) {
        LoginUser loginUser = LoginInterceptor.getCurrentUser();
        if (loginUser == null) {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
        }

        if (productIds == null || productIds.isEmpty()) {
            return JsonData.buildError("商品ID列表不能为空");
        }

        Long userId = loginUser.getId();

        log.info("用户确认购物车商品信息: userId={}, productIds={}", userId, productIds);

        try {
            // 1. 获取购物车详情
            JsonData cartDetailsResult = getCartDetails();
            if (cartDetailsResult.getCode() != 0) {
                return cartDetailsResult;
            }

            // 解析购物车数据
            CartVO cartVO = JSON.parseObject(
                    JSON.toJSONString(cartDetailsResult.getData()),
                    CartVO.class
            );

            // 2. 根据指定的商品ID进行过滤，并准备清空对应的购物项
            List<CartItemVO> confirmedItems = cartVO.getItems().stream()
                    .filter(item -> productIds.contains(item.getProductId()))
                    .collect(Collectors.toList());

            if (confirmedItems.isEmpty()) {
                return JsonData.buildError("购物车中没有找到指定的商品");
            }

            // 3. 验证库存（确认前最后一次检查）
            for (CartItemVO item : confirmedItems) {
                ValidateStockRequest stockRequest = new ValidateStockRequest();
                stockRequest.setProductId(item.getProductId());
                stockRequest.setQuantity(item.getQuantity());
                JsonData stockResult = productFeignService.validateStock(stockRequest);
                if (stockResult.getCode() != 0) {
                    return JsonData.buildError("商品 " + item.getTitle() + " 库存不足");
                }
            }

            // 4. 清空对应的购物项
            List<Long> confirmedProductIds = confirmedItems.stream()
                    .map(CartItemVO::getProductId)
                    .collect(Collectors.toList());

            // 从Redis批量删除
            String redisKey = getCartRedisKey(userId);
            String[] productFields = confirmedProductIds.stream()
                    .map(this::getProductField)
                    .toArray(String[]::new);
            redisTemplate.opsForHash().delete(redisKey, (Object[]) productFields);

            // 从MySQL批量删除
            cartManager.deleteBatchByUserIdAndProductIds(userId, confirmedProductIds);

            log.info("确认购物车商品信息成功: userId={}, confirmedCount={}", userId, confirmedItems.size());

            return JsonData.buildSuccess(confirmedItems);

        } catch (Exception e) {
            log.error("确认购物车商品信息失败: userId={}, productIds={}", userId, productIds, e);
            return JsonData.buildError("确认失败，请重试");
        }
    }

    /**
     * 从数据库加载用户购物车数据到Redis
     */
    private void loadUserCartFromDB(Long userId) {
        try {
            List<CartDO> cartList = cartManager.selectByUserId(userId);
            if (cartList.isEmpty()) {
                return;
            }

            String redisKey = getCartRedisKey(userId);
            Map<String, String> cartMap = new HashMap<>();

            for (CartDO cart : cartList) {
                CartItemDTO cartItem = new CartItemDTO();
                cartItem.setProductId(cart.getProductId());
                cartItem.setQuantity(cart.getQuantity());
                cartItem.setAddTime(cart.getCreateTime().getTime());
                cartItem.setUpdateTime(cart.getUpdateTime().getTime());

                String productField = getProductField(cart.getProductId());
                cartMap.put(productField, JSON.toJSONString(cartItem));
            }

            redisTemplate.opsForHash().putAll(redisKey, cartMap);
            redisTemplate.expire(redisKey, cartProperties.getRedis().getExpireDays(), TimeUnit.DAYS);

            log.info("从数据库加载用户购物车数据到Redis: userId={}, count={}", userId, cartList.size());

        } catch (Exception e) {
            log.error("从数据库加载用户购物车数据失败: userId={}", userId, e);
        }
    }

    /**
     * 获取购物车Redis Key
     */
    private String getCartRedisKey(Long userId) {
        return cartProperties.getRedis().getKeyPrefix() + userId;
    }

    /**
     * 获取商品字段名
     */
    private String getProductField(Long productId) {
        return "product:" + productId;
    }
}
