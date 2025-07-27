package fun.timu.shop.order.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.timu.shop.order.manager.CartManager;
import fun.timu.shop.order.mapper.CartMapper;
import fun.timu.shop.order.model.DO.CartDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车Manager实现类
 * 数据访问层实现，负责与数据库交互
 *
 * @author zhengke
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartManagerImpl extends ServiceImpl<CartMapper, CartDO> implements CartManager {

    private final CartMapper cartMapper;

    @Override
    public List<CartDO> selectByUserId(Long userId) {
        return cartMapper.selectByUserId(userId);
    }

    @Override
    public CartDO selectByUserIdAndProductId(Long userId, Long productId) {
        return cartMapper.selectByUserIdAndProductId(userId, productId);
    }

    @Override
    public int insertOrUpdate(Long userId, Long productId, Integer quantity) {
        return cartMapper.insertOrUpdate(userId, productId, quantity);
    }

    @Override
    public int deleteBatchByUserIdAndProductIds(Long userId, List<Long> productIds) {
        return cartMapper.deleteBatchByUserIdAndProductIds(userId, productIds);
    }

    @Override
    public int deleteByUserId(Long userId) {
        return cartMapper.deleteByUserId(userId);
    }

    @Override
    public List<CartDO> selectHotUserCartData(Integer days) {
        return cartMapper.selectHotUserCartData(days);
    }

    @Override
    public List<CartDO> selectAllCartData(Integer offset, Integer limit) {
        return cartMapper.selectAllCartData(offset, limit);
    }

    @Override
    public Long countAllCartData() {
        return cartMapper.countAllCartData();
    }
}
