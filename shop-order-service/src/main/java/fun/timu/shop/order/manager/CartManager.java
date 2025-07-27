package fun.timu.shop.order.manager;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.timu.shop.order.model.DO.CartDO;

import java.util.List;

/**
 * 购物车Manager接口
 * 数据访问层，负责与数据库交互
 *
 * @author zhengke
 */
public interface CartManager extends IService<CartDO> {

    /**
     * 根据用户ID获取购物车列表
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<CartDO> selectByUserId(Long userId);

    /**
     * 根据用户ID和商品ID获取购物车项
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 购物车项
     */
    CartDO selectByUserIdAndProductId(Long userId, Long productId);

    /**
     * 插入或更新购物车项
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @param quantity  数量
     * @return 影响行数
     */
    int insertOrUpdate(Long userId, Long productId, Integer quantity);

    /**
     * 批量删除购物车项
     *
     * @param userId     用户ID
     * @param productIds 商品ID列表
     * @return 影响行数
     */
    int deleteBatchByUserIdAndProductIds(Long userId, List<Long> productIds);

    /**
     * 根据用户ID删除所有购物车项
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(Long userId);

    /**
     * 获取热点用户的购物车数据（最近N天有更新的）
     *
     * @param days 天数
     * @return 购物车列表
     */
    List<CartDO> selectHotUserCartData(Integer days);

    /**
     * 获取所有购物车数据（分页）
     *
     * @param offset 偏移量
     * @param limit  限制数量
     * @return 购物车列表
     */
    List<CartDO> selectAllCartData(Integer offset, Integer limit);

    /**
     * 获取购物车数据总数
     *
     * @return 总数
     */
    Long countAllCartData();
}
