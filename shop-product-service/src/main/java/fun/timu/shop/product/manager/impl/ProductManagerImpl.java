package fun.timu.shop.product.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import fun.timu.shop.product.controller.request.ProductQueryRequest;
import fun.timu.shop.common.enums.DelFlagEnum;
import fun.timu.shop.common.enums.ProductStatusEnum;
import fun.timu.shop.product.manager.ProductManager;
import fun.timu.shop.product.mapper.ProductMapper;
import fun.timu.shop.product.model.DO.ProductDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 商品管理器实现类
 *
 * @author zhengke
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductManagerImpl implements ProductManager {

    private final ProductMapper productMapper;

    @Override
    public List<ProductDO> listByQuery(ProductQueryRequest queryRequest) {
        LambdaQueryWrapper<ProductDO> wrapper = new LambdaQueryWrapper<>();

        // 基础过滤条件
        wrapper.eq(ProductDO::getDelFlag, DelFlagEnum.NOT_DELETED.getFlag());

        // 根据查询条件构建where条件
        if (queryRequest != null) {
            // 标题模糊查询
            if (StringUtils.hasText(queryRequest.getTitle())) {
                wrapper.like(ProductDO::getTitle, queryRequest.getTitle());
            }

            // 分类过滤
            if (queryRequest.getCategoryId() != null) {
                wrapper.eq(ProductDO::getCategoryId, queryRequest.getCategoryId());
            }

            // 状态过滤
            if (queryRequest.getStatus() != null) {
                wrapper.eq(ProductDO::getStatus, queryRequest.getStatus().getCode());
            } else {
                // 默认只查询上架商品
                wrapper.eq(ProductDO::getStatus, ProductStatusEnum.ONLINE.getCode());
            }

            // 价格范围过滤
            if (queryRequest.getMinPrice() != null) {
                wrapper.ge(ProductDO::getPrice, queryRequest.getMinPrice());
            }
            if (queryRequest.getMaxPrice() != null) {
                wrapper.le(ProductDO::getPrice, queryRequest.getMaxPrice());
            }

            // 只查询有库存商品
            if (Boolean.TRUE.equals(queryRequest.getOnlyInStock())) {
                wrapper.gt(ProductDO::getStock, 0);
            }

            // 排序处理
            String orderBy = queryRequest.getOrderBy();
            String orderDirection = queryRequest.getOrderDirection();
            boolean isAsc = "ASC".equalsIgnoreCase(orderDirection);

            if (StringUtils.hasText(orderBy)) {
                switch (orderBy.toLowerCase()) {
                    case "price":
                        wrapper.orderBy(true, isAsc, ProductDO::getPrice);
                        break;
                    case "sales_count":
                        wrapper.orderBy(true, isAsc, ProductDO::getSalesCount);
                        break;
                    case "create_time":
                        wrapper.orderBy(true, isAsc, ProductDO::getCreateTime);
                        break;
                    case "sort":
                    default:
                        wrapper.orderBy(true, isAsc, ProductDO::getSort);
                        break;
                }
            }
        }

        return productMapper.selectList(wrapper);
    }

    @Override
    public ProductDO getByIdNotDeleted(Long id) {
        LambdaQueryWrapper<ProductDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductDO::getId, id)
                .eq(ProductDO::getDelFlag, DelFlagEnum.NOT_DELETED.getFlag());
        return productMapper.selectOne(wrapper);
    }

    @Override
    public ProductDO selectById(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public List<ProductDO> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<ProductDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ProductDO::getId, ids)
                .eq(ProductDO::getDelFlag, DelFlagEnum.NOT_DELETED.getFlag());
        return productMapper.selectList(wrapper);
    }

    @Override
    public boolean save(ProductDO productDO) {
        return productMapper.insert(productDO) > 0;
    }

    @Override
    public boolean updateById(ProductDO productDO) {
        return productMapper.updateById(productDO) > 0;
    }

    @Override
    public boolean logicDeleteById(Long id) {
        LambdaUpdateWrapper<ProductDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ProductDO::getId, id)
                .set(ProductDO::getDelFlag, DelFlagEnum.DELETED.getFlag());
        return productMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean logicDeleteBatchByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        LambdaUpdateWrapper<ProductDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(ProductDO::getId, ids)
                .set(ProductDO::getDelFlag, DelFlagEnum.DELETED.getFlag());
        return productMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean updateStatusById(Long id, Integer status) {
        LambdaUpdateWrapper<ProductDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ProductDO::getId, id)
                .eq(ProductDO::getDelFlag, DelFlagEnum.NOT_DELETED.getFlag())
                .set(ProductDO::getStatus, status);
        return productMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        LambdaUpdateWrapper<ProductDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(ProductDO::getId, ids)
                .eq(ProductDO::getDelFlag, DelFlagEnum.NOT_DELETED.getFlag())
                .set(ProductDO::getStatus, status);
        return productMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean decreaseStock(Long id, Integer quantity) {
        return productMapper.decreaseStock(id, quantity) > 0;
    }

    @Override
    public boolean increaseStock(Long id, Integer quantity) {
        return productMapper.increaseStock(id, quantity) > 0;
    }

    @Override
    public boolean lockStock(Long id, Integer quantity) {
        return productMapper.lockStock(id, quantity) > 0;
    }

    @Override
    public boolean releaseLockStock(Long id, Integer quantity) {
        return productMapper.releaseLockStock(id, quantity) > 0;
    }

    @Override
    public boolean updateSalesCount(Long id, Integer salesCount) {
        LambdaUpdateWrapper<ProductDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ProductDO::getId, id)
                .eq(ProductDO::getDelFlag, DelFlagEnum.NOT_DELETED.getFlag())
                .set(ProductDO::getSalesCount, salesCount);
        return productMapper.update(null, wrapper) > 0;
    }
}
