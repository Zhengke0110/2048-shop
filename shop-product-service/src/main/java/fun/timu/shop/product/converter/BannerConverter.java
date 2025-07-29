package fun.timu.shop.product.converter;

import fun.timu.shop.common.enums.BannerPositionEnum;
import fun.timu.shop.common.enums.BannerStatusEnum;
import fun.timu.shop.common.enums.BannerTargetTypeEnum;
import fun.timu.shop.product.controller.request.BannerCreateRequest;
import fun.timu.shop.product.controller.request.BannerUpdateRequest;
import fun.timu.shop.product.model.DO.BannerDO;
import fun.timu.shop.product.model.VO.BannerVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 轮播图数据转换器
 *
 * @author zhengke
 */
@Component
public class BannerConverter {

    /**
     * 将创建请求转换为DO对象
     */
    public BannerDO convertCreateRequestToDO(BannerCreateRequest request) {
        return Optional.ofNullable(request)
                .map(this::buildBannerDOFromCreateRequest)
                .orElse(null);
    }

    /**
     * 将更新请求转换为DO对象
     */
    public BannerDO convertUpdateRequestToDO(Integer id, BannerUpdateRequest request) {
        return Optional.ofNullable(request)
                .map(req -> buildBannerDOFromUpdateRequest(id, req))
                .orElse(null);
    }

    /**
     * 转换DO为VO
     */
    public BannerVO convertToVO(BannerDO bannerDO) {
        return Optional.ofNullable(bannerDO)
                .map(this::buildBannerVO)
                .orElse(null);
    }

    /**
     * 批量转换DO为VO
     */
    public List<BannerVO> convertToVOList(List<BannerDO> bannerDOList) {
        if (bannerDOList == null || bannerDOList.isEmpty()) {
            return List.of();
        }

        return bannerDOList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 构建BannerDO从CreateRequest
     */
    private BannerDO buildBannerDOFromCreateRequest(BannerCreateRequest request) {
        BannerDO bannerDO = new BannerDO();

        // 复制基础字段
        BeanUtils.copyProperties(request, bannerDO, "position", "targetType", "status");

        // 转换枚举字段
        setEnumFields(bannerDO, request::getPosition, request::getTargetType, request::getStatus);

        return bannerDO;
    }

    /**
     * 构建BannerDO从UpdateRequest
     */
    private BannerDO buildBannerDOFromUpdateRequest(Integer id, BannerUpdateRequest request) {
        BannerDO bannerDO = new BannerDO();
        bannerDO.setId(id);

        // 复制基础字段
        BeanUtils.copyProperties(request, bannerDO, "position", "targetType", "status");

        // 转换枚举字段
        setEnumFields(bannerDO, request::getPosition, request::getTargetType, request::getStatus);

        return bannerDO;
    }

    /**
     * 构建BannerVO
     */
    private BannerVO buildBannerVO(BannerDO bannerDO) {
        BannerVO bannerVO = new BannerVO();
        BeanUtils.copyProperties(bannerDO, bannerVO);
        return bannerVO;
    }

    /**
     * 设置枚举字段的通用方法
     */
    private <T> void setEnumFields(BannerDO bannerDO,
                                   java.util.function.Supplier<T> positionSupplier,
                                   java.util.function.Supplier<T> targetTypeSupplier,
                                   java.util.function.Supplier<T> statusSupplier) {

        Optional.ofNullable(positionSupplier.get())
                .map(pos -> ((BannerPositionEnum) pos).getCode())
                .ifPresent(bannerDO::setPosition);

        Optional.ofNullable(targetTypeSupplier.get())
                .map(type -> ((BannerTargetTypeEnum) type).getCode())
                .ifPresent(bannerDO::setTargetType);

        Optional.ofNullable(statusSupplier.get())
                .map(status -> ((BannerStatusEnum) status).getCode())
                .ifPresent(bannerDO::setStatus);
    }
}
