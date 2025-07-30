package fun.timu.shop.product.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import fun.timu.shop.product.manager.ProductTaskManager;
import fun.timu.shop.product.mapper.ProductTaskMapper;
import fun.timu.shop.product.model.DO.ProductTaskDO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class ProductTaskManagerImpl implements ProductTaskManager {
    private final ProductTaskMapper taskMapper;

    @Override
    public boolean insert(ProductTaskDO productTaskDO) {
        return taskMapper.insert(productTaskDO) > 0;
    }

    @Override
    public ProductTaskDO selectById(Long id) {
        return taskMapper.selectOne(
                new QueryWrapper<ProductTaskDO>().eq("id", id)
        );
    }

    @Override
    public boolean updateEntity(ProductTaskDO productTaskDO, Long taskId) {
        return taskMapper.update(productTaskDO,
                new UpdateWrapper<ProductTaskDO>().eq("id", taskId)
        ) > 0;
    }
}
