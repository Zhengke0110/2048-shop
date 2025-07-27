package fun.timu.shop.order.converter;

import fun.timu.shop.order.model.DTO.CartItemDTO;
import fun.timu.shop.order.model.VO.CartItemVO;
import fun.timu.shop.order.model.VO.CartVO;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 购物车转换器
 * 负责购物车相关对象之间的转换
 *
 * @author zhengke
 */
@Slf4j
@Component
public class CartConverter {

    /**
     * 构建空购物车
     */
    public CartVO toEmptyCart(Long userId) {
        return CartVO.builder()
                .userId(userId)
                .items(Collections.emptyList())
                .totalQuantity(0)
                .totalAmount(BigDecimal.ZERO)
                .validCount(0)
                .invalidCount(0)
                .build();
    }

    /**
     * 构建包含商品详情的购物车
     */
    public CartVO toCartWithDetails(Long userId, List<CartItemDTO> cartItems, Object productData) {
        if (cartItems == null || cartItems.isEmpty()) {
            return toEmptyCart(userId);
        }

        // 解析商品数据
        @SuppressWarnings("unchecked")
        Map<String, Object> productMap = (Map<String, Object>) productData;

        // 转换商品项
        List<CartItemVO> itemVOs = convertToItemVOs(cartItems, productMap);

        // 计算统计信息
        CartStatistics statistics = calculateStatistics(itemVOs);

        return CartVO.builder()
                .userId(userId)
                .items(itemVOs)
                .totalQuantity(statistics.getTotalQuantity())
                .totalAmount(statistics.getTotalAmount())
                .validCount(statistics.getValidCount())
                .invalidCount(statistics.getInvalidCount())
                .build();
    }

    /**
     * 转换购物车商品项列表
     */
    private List<CartItemVO> convertToItemVOs(List<CartItemDTO> cartItems, Map<String, Object> productMap) {
        List<CartItemVO> itemVOs = new ArrayList<>();

        for (CartItemDTO cartItem : cartItems) {
            CartItemVO itemVO = convertToItemVO(cartItem, productMap);
            itemVOs.add(itemVO);
        }

        return itemVOs;
    }

    /**
     * 转换单个购物车商品项
     */
    private CartItemVO convertToItemVO(CartItemDTO cartItem, Map<String, Object> productMap) {
        CartItemVO.CartItemVOBuilder builder = CartItemVO.builder()
                .productId(cartItem.getProductId())
                .quantity(cartItem.getQuantity())
                .addTime(LocalDateTime.ofEpochSecond(cartItem.getAddTime() / 1000, 0, ZoneOffset.of("+8")))
                .updateTime(LocalDateTime.ofEpochSecond(cartItem.getUpdateTime() / 1000, 0, ZoneOffset.of("+8")));

                // 根据商品数据设置商品信息
        Object productInfo = productMap.get(cartItem.getProductId().toString());
        if (productInfo != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> product = (Map<String, Object>) productInfo;
                
                // 检查商品状态：1-上架，0-下架
                Integer status = extractInteger(product, "status");
                boolean isAvailable = status != null && status == 1;
                
                if (!isAvailable) {
                    log.info("购物车中的商品已下架: productId={}, status={}", cartItem.getProductId(), status);
                }
                
                builder.available(isAvailable);
                
                // 设置商品基本信息
                builder.title(extractString(product, "title"));
                builder.coverImg(extractString(product, "coverImg"));
                
                // 设置价格信息
                BigDecimal price = extractBigDecimal(product, "price");
                if (price != null) {
                    builder.price(price);
                    builder.subtotal(price.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
                }
                
                // 设置库存信息
                Integer stock = extractInteger(product, "stock");
                builder.stock(stock);
                
            } catch (Exception e) {
                log.error("解析商品数据失败: productId={}", cartItem.getProductId(), e);
                builder.available(false);
            }
        } else {
            // 商品不存在或已删除
            log.warn("购物车中的商品不存在: productId={}", cartItem.getProductId());
            builder.available(false);
        }

        return builder.build();
    }

    /**
     * 计算购物车统计信息
     */
    private CartStatistics calculateStatistics(List<CartItemVO> itemVOs) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;
        int validCount = 0;
        int invalidCount = 0;

        for (CartItemVO itemVO : itemVOs) {
            totalQuantity += itemVO.getQuantity();

            if (itemVO.getAvailable()) {
                validCount++;
                if (itemVO.getPrice() != null) {
                    totalAmount = totalAmount.add(itemVO.getSubtotal());
                }
            } else {
                invalidCount++;
            }
        }

        return CartStatistics.builder()
                .totalQuantity(totalQuantity)
                .totalAmount(totalAmount)
                .validCount(validCount)
                .invalidCount(invalidCount)
                .build();
    }

    /**
     * 购物车统计信息内部类
     */
    @Data
    @Builder
    private static class CartStatistics {
        private int totalQuantity;
        private BigDecimal totalAmount;
        private int validCount;
        private int invalidCount;
    }

    /**
     * 安全地从Map中提取String值
     */
    private String extractString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 安全地从Map中提取Integer值
     */
    private Integer extractInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        try {
            return value instanceof Integer ? (Integer) value : Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法解析Integer值: key={}, value={}", key, value);
            return null;
        }
    }

    /**
     * 安全地从Map中提取BigDecimal值
     */
    private BigDecimal extractBigDecimal(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        try {
            return value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法解析BigDecimal值: key={}, value={}", key, value);
            return null;
        }
    }
}
