package fun.timu.shop.order.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.timu.shop.order.manager.ProductOrderManager;
import fun.timu.shop.order.mapper.ProductOrderMapper;
import fun.timu.shop.order.model.DO.ProductOrderDO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ProductOrderManagerImpl extends ServiceImpl<ProductOrderMapper, ProductOrderDO> implements ProductOrderManager {
    private final ProductOrderMapper productOrderMapper;

    @Override
    public ProductOrderDO selectOne(String outTradeNo) {
        return productOrderMapper.selectOne(new QueryWrapper<ProductOrderDO>().eq("out_trade_no", outTradeNo));
    }
}
