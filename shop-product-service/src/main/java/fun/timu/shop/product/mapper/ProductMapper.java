package fun.timu.shop.product.mapper;

import fun.timu.shop.product.model.DO.ProductDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author zhengke
 * @description 针对表【product(商品表)】的数据库操作Mapper
 * @createDate 2025-07-27 16:38:53
 * @Entity fun.timu.shop.product.model.DO.Product
 */
public interface ProductMapper extends BaseMapper<ProductDO> {

    /**
     * 扣减库存
     *
     * @param id       商品ID
     * @param quantity 扣减数量
     * @return 影响行数
     */
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 增加库存
     *
     * @param id       商品ID
     * @param quantity 增加数量
     * @return 影响行数
     */
    int increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 锁定库存
     *
     * @param id       商品ID
     * @param quantity 锁定数量
     * @return 影响行数
     */
    int lockStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 释放锁定库存（将锁定库存转为可用库存）
     *
     * @param id       商品ID
     * @param quantity 释放数量
     * @return 影响行数
     */
    int releaseLockStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}




