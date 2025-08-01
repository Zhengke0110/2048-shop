package fun.timu.shop.order.mapper;

import fun.timu.shop.order.model.DO.ProductOrderDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author zhengke
 * @description 针对表【product_order(订单表)】的数据库操作Mapper
 * @createDate 2025-07-29 10:49:11
 * @Entity fun.timu.shop.order.model.DO.ProductOrder
 */
public interface ProductOrderMapper extends BaseMapper<ProductOrderDO> {
    void updateOrderPayState(@Param("outTradeNo") String outTradeNo, @Param("newState") String newState, @Param("oldState") String oldState);
}




