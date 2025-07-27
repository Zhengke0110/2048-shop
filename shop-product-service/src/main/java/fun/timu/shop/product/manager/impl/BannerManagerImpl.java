package fun.timu.shop.product.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.timu.shop.product.enums.BannerStatusEnum;
import fun.timu.shop.product.enums.DelFlagEnum;
import fun.timu.shop.product.manager.BannerManager;
import fun.timu.shop.product.mapper.BannerMapper;
import fun.timu.shop.product.model.DO.BannerDO;
import fun.timu.shop.product.controller.request.BannerQueryRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BannerManagerImpl extends ServiceImpl<BannerMapper, BannerDO> implements BannerManager {

    @Override
    public List<BannerDO> listByQuery(BannerQueryRequest queryRequest) {
        LambdaQueryWrapper<BannerDO> wrapper = new LambdaQueryWrapper<>();

        // 基础过滤条件
        wrapper.eq(BannerDO::getDelFlag, DelFlagEnum.NOT_DELETED.getCode());

        // 根据查询条件构建where条件
        if (queryRequest != null) {
            // 位置过滤
            if (queryRequest.getPosition() != null) {
                wrapper.eq(BannerDO::getPosition, queryRequest.getPosition().getCode());
            }

            // 状态过滤
            if (queryRequest.getStatus() != null) {
                wrapper.eq(BannerDO::getStatus, queryRequest.getStatus().getCode());
            } else {
                // 默认只查询启用状态
                wrapper.eq(BannerDO::getStatus, BannerStatusEnum.ENABLED.getCode());
            }

            // 权重范围过滤
            if (queryRequest.getMinWeight() != null) {
                wrapper.ge(BannerDO::getWeight, queryRequest.getMinWeight());
            }
            if (queryRequest.getMaxWeight() != null) {
                wrapper.le(BannerDO::getWeight, queryRequest.getMaxWeight());
            }

            // 时间范围过滤
            if (queryRequest.getStartTime() != null) {
                wrapper.ge(BannerDO::getCreateTime, queryRequest.getStartTime());
            }
            if (queryRequest.getEndTime() != null) {
                wrapper.le(BannerDO::getCreateTime, queryRequest.getEndTime());
            }

            // 如果需要只查询有效时间内的轮播图
            if (queryRequest.getOnlyActive() != null && queryRequest.getOnlyActive()) {
                Date now = new Date();
                wrapper.le(BannerDO::getStartTime, now)
                        .ge(BannerDO::getEndTime, now);
            }

            wrapper.eq(BannerDO::getStatus, BannerStatusEnum.ENABLED.getCode());

            // 排序处理
            String orderBy = queryRequest.getOrderBy();
            String orderDirection = queryRequest.getOrderDirection();
            boolean isDesc = "DESC".equalsIgnoreCase(orderDirection);

            if ("weight".equals(orderBy)) {
                wrapper.orderBy(true, !isDesc, BannerDO::getWeight);
            } else if ("create_time".equals(orderBy)) {
                wrapper.orderBy(true, !isDesc, BannerDO::getCreateTime);
            } else if ("click_count".equals(orderBy)) {
                wrapper.orderBy(true, !isDesc, BannerDO::getClickCount);
            } else {
                // 默认按权重降序，创建时间降序
                wrapper.orderByDesc(BannerDO::getWeight)
                        .orderByDesc(BannerDO::getCreateTime);
            }
        } else {
            // 没有查询条件时的默认行为
            wrapper.eq(BannerDO::getStatus, BannerStatusEnum.ENABLED.getCode())
                    .orderByDesc(BannerDO::getWeight)
                    .orderByDesc(BannerDO::getCreateTime);
        }

        return list(wrapper);
    }

    @Override
    public BannerDO getByIdNotDeleted(Integer id) {
        LambdaQueryWrapper<BannerDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BannerDO::getId, id)
                .eq(BannerDO::getDelFlag, DelFlagEnum.NOT_DELETED.getCode());
        return getOne(wrapper);
    }

    @Override
    public boolean incrementClickCount(Integer id) {
        return baseMapper.incrementClickCount(id) > 0;
    }

    @Override
    public boolean logicDeleteById(Integer id) {
        LambdaUpdateWrapper<BannerDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(BannerDO::getId, id)
                .set(BannerDO::getDelFlag, DelFlagEnum.DELETED.getCode());
        return update(wrapper);
    }

    @Override
    public boolean logicDeleteBatchByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        LambdaUpdateWrapper<BannerDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(BannerDO::getId, ids)
                .set(BannerDO::getDelFlag, DelFlagEnum.DELETED.getCode());
        return update(wrapper);
    }

    @Override
    public boolean updateStatusById(Integer id, Integer status) {
        LambdaUpdateWrapper<BannerDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(BannerDO::getId, id)
                .eq(BannerDO::getDelFlag, DelFlagEnum.NOT_DELETED.getCode())
                .set(BannerDO::getStatus, status);
        return update(wrapper);
    }
}
