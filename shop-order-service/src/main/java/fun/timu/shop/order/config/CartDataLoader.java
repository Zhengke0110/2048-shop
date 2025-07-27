package fun.timu.shop.order.config;

import com.alibaba.fastjson2.JSON;
import fun.timu.shop.order.manager.CartManager;
import fun.timu.shop.order.model.DO.CartDO;
import fun.timu.shop.order.model.DTO.CartItemDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 购物车数据加载器
 * 在应用启动时将MySQL中的购物车数据加载到Redis中
 * 
 * @author zhengke
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CartDataLoader implements ApplicationRunner {

    private final CartManager cartManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CartProperties cartProperties;

    private final AtomicInteger loadedCount = new AtomicInteger(0);
    private final AtomicInteger totalCount = new AtomicInteger(0);

    @Override
    public void run(ApplicationArguments args) {
        if (!cartProperties.getStartup().getLoadFromDb()) {
            log.info("购物车数据加载功能已禁用");
            return;
        }

        log.info("开始加载购物车数据到Redis...");
        long startTime = System.currentTimeMillis();

        try {
            String loadStrategy = cartProperties.getStartup().getLoadStrategy();
            
            switch (loadStrategy.toLowerCase()) {
                case "all":
                    loadAllCartData();
                    break;
                case "hot":
                    loadHotCartData();
                    break;
                default:
                    log.warn("未知的加载策略: {}, 使用默认策略: hot", loadStrategy);
                    loadHotCartData();
                    break;
            }

            long endTime = System.currentTimeMillis();
            log.info("购物车数据加载完成! 总数: {}, 耗时: {}ms", 
                    loadedCount.get(), (endTime - startTime));

        } catch (Exception e) {
            log.error("购物车数据加载失败", e);
            // 加载失败不影响应用启动
        }
    }

    /**
     * 加载所有购物车数据
     */
    private void loadAllCartData() {
        log.info("使用全量加载策略");
        
        try {
            // 获取总数
            Long total = cartManager.countAllCartData();
            totalCount.set(total.intValue());
            
            if (total == 0) {
                log.info("没有购物车数据需要加载");
                return;
            }

            log.info("需要加载的购物车数据总数: {}", total);

            int batchSize = cartProperties.getStartup().getBatchSize();
            int totalBatches = (int) Math.ceil((double) total / batchSize);

            // 分批加载
            for (int i = 0; i < totalBatches; i++) {
                int offset = i * batchSize;
                List<CartDO> cartList = cartManager.selectAllCartData(offset, batchSize);
                
                if (!cartList.isEmpty()) {
                    batchLoadToRedis(cartList);
                    loadedCount.addAndGet(cartList.size());
                    
                    // 记录进度
                    if ((i + 1) % 10 == 0 || i == totalBatches - 1) {
                        log.info("购物车数据加载进度: {}/{} ({:.1f}%)", 
                                loadedCount.get(), total, 
                                (double) loadedCount.get() / total * 100);
                    }
                }
            }

        } catch (Exception e) {
            log.error("全量加载购物车数据失败", e);
            throw e;
        }
    }

    /**
     * 加载热点购物车数据（最近N天有更新的）
     */
    private void loadHotCartData() {
        Integer hotUserDays = cartProperties.getStartup().getHotUserDays();
        log.info("使用热点数据加载策略，加载最近 {} 天的购物车数据", hotUserDays);
        
        try {
            List<CartDO> hotCartList = cartManager.selectHotUserCartData(hotUserDays);
            totalCount.set(hotCartList.size());
            
            if (hotCartList.isEmpty()) {
                log.info("没有热点购物车数据需要加载");
                return;
            }

            log.info("需要加载的热点购物车数据总数: {}", hotCartList.size());

            // 分批加载
            int batchSize = cartProperties.getStartup().getBatchSize();
            for (int i = 0; i < hotCartList.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, hotCartList.size());
                List<CartDO> batch = hotCartList.subList(i, endIndex);
                
                batchLoadToRedis(batch);
                loadedCount.addAndGet(batch.size());
                
                // 记录进度
                if ((i / batchSize + 1) % 10 == 0 || endIndex == hotCartList.size()) {
                    log.info("热点购物车数据加载进度: {}/{} ({:.1f}%)", 
                            loadedCount.get(), hotCartList.size(), 
                            (double) loadedCount.get() / hotCartList.size() * 100);
                }
            }

        } catch (Exception e) {
            log.error("热点数据加载购物车数据失败", e);
            throw e;
        }
    }

    /**
     * 批量加载购物车数据到Redis
     */
    private void batchLoadToRedis(List<CartDO> cartList) {
        if (cartList.isEmpty()) {
            return;
        }

        try {
            // 按用户分组
            Map<Long, List<CartDO>> userCartMap = cartList.stream()
                    .collect(Collectors.groupingBy(CartDO::getUserId));

            // 使用Pipeline批量操作
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                userCartMap.forEach(this::loadUserCartToRedis);
                return null;
            });

        } catch (Exception e) {
            log.error("批量加载购物车数据到Redis失败", e);
            throw e;
        }
    }

    /**
     * 加载单个用户的购物车数据到Redis
     */
    private void loadUserCartToRedis(Long userId, List<CartDO> userCartList) {
        try {
            String redisKey = cartProperties.getRedis().getKeyPrefix() + userId;
            Map<String, String> cartMap = new HashMap<>();

            for (CartDO cart : userCartList) {
                CartItemDTO cartItem = new CartItemDTO();
                cartItem.setProductId(cart.getProductId());
                cartItem.setQuantity(cart.getQuantity());
                cartItem.setAddTime(cart.getCreateTime().getTime());
                cartItem.setUpdateTime(cart.getUpdateTime().getTime());

                String productField = "product:" + cart.getProductId();
                cartMap.put(productField, JSON.toJSONString(cartItem));
            }

            if (!cartMap.isEmpty()) {
                redisTemplate.opsForHash().putAll(redisKey, cartMap);
                redisTemplate.expire(redisKey, cartProperties.getRedis().getExpireDays(), TimeUnit.DAYS);
            }

        } catch (Exception e) {
            log.error("加载用户购物车数据到Redis失败: userId={}", userId, e);
        }
    }
}
