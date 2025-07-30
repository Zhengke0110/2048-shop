package fun.timu.shop.order.mapper;

import fun.timu.shop.order.model.DO.ProductOrderItemDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author zhengke
* @description 针对表【product_order_item(订单商品表)】的数据库操作Mapper
* @createDate 2025-07-29 10:49:11
* @Entity fun.timu.shop.order.model.DO.ProductOrderItem
*/
public interface ProductOrderItemMapper extends BaseMapper<ProductOrderItemDO> {

    void insertBatch( @Param("orderItemList") List<ProductOrderItemDO> list);
}




