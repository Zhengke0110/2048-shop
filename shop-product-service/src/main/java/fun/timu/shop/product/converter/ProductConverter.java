package fun.timu.shop.product.converter;

import fun.timu.shop.product.controller.request.ProductCreateRequest;
import fun.timu.shop.product.controller.request.ProductUpdateRequest;
import fun.timu.shop.product.model.DO.ProductDO;
import fun.timu.shop.product.model.VO.ProductVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 商品数据转换器
 * @author zhengke
 */
@Component
public class ProductConverter {

    /**
     * 将创建请求转换为DO对象
     */
    public ProductDO convertCreateRequestToDO(ProductCreateRequest request) {
        return Optional.ofNullable(request)
                .map(this::buildProductDOFromCreateRequest)
                .orElse(null);
    }

    /**
     * 将更新请求转换为DO对象
     */
    public ProductDO convertUpdateRequestToDO(Long id, ProductUpdateRequest request) {
        return Optional.ofNullable(request)
                .map(req -> buildProductDOFromUpdateRequest(id, req))
                .orElse(null);
    }

    /**
     * 转换DO为VO
     */
    public ProductVO convertToVO(ProductDO productDO) {
        return Optional.ofNullable(productDO)
                .map(this::buildProductVO)
                .orElse(null);
    }

    /**
     * 批量转换DO为VO
     */
    public List<ProductVO> convertToVOList(List<ProductDO> productDOList) {
        if (productDOList == null || productDOList.isEmpty()) {
            return List.of();
        }

        return productDOList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 构建ProductDO从CreateRequest
     */
    private ProductDO buildProductDOFromCreateRequest(ProductCreateRequest request) {
        ProductDO productDO = new ProductDO();
        
        // 复制基础字段
        BeanUtils.copyProperties(request, productDO, "status");
        
        // 转换枚举字段
        if (request.getStatus() != null) {
            productDO.setStatus(request.getStatus().getCode());
        }
        
        return productDO;
    }

    /**
     * 构建ProductDO从UpdateRequest
     */
    private ProductDO buildProductDOFromUpdateRequest(Long id, ProductUpdateRequest request) {
        ProductDO productDO = new ProductDO();
        productDO.setId(id);
        
        // 复制基础字段
        BeanUtils.copyProperties(request, productDO, "status");
        
        // 转换枚举字段
        if (request.getStatus() != null) {
            productDO.setStatus(request.getStatus().getCode());
        }
        
        return productDO;
    }

    /**
     * 构建ProductVO
     */
    private ProductVO buildProductVO(ProductDO productDO) {
        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(productDO, productVO);
        return productVO;
    }
}
