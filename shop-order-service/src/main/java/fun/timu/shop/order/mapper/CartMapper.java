package fun.timu.shop.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.timu.shop.order.model.DO.CartDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 购物车Mapper接口
 *
 * @author zhengke
 */
@Mapper
public interface CartMapper extends BaseMapper<CartDO> {

    /**
     * 根据用户ID获取购物车列表
     */
    List<CartDO> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和商品ID获取购物车项
     */
    CartDO selectByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 插入或更新购物车项（如果存在则更新数量，不存在则插入）
     */
    int insertOrUpdate(@Param("userId") Long userId, @Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 批量删除购物车项
     */
    int deleteBatchByUserIdAndProductIds(@Param("userId") Long userId, @Param("productIds") List<Long> productIds);

    /**
     * 根据用户ID删除所有购物车项
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 获取热点用户的购物车数据（最近N天有更新的）
     */
    List<CartDO> selectHotUserCartData(@Param("days") Integer days);

    /**
     * 获取所有购物车数据（分页）
     */
    List<CartDO> selectAllCartData(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 获取购物车数据总数
     */
    Long countAllCartData();
}
