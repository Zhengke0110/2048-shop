package fun.timu.shop.product.controller.request;

import fun.timu.shop.product.enums.BannerPositionEnum;
import fun.timu.shop.product.enums.BannerStatusEnum;
import fun.timu.shop.product.enums.BannerTargetTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * 轮播图创建请求类
 *
 * @author zhengke
 */
@Data
public class BannerCreateRequest {

    /**
     * 轮播图标题
     */
    @NotBlank(message = "轮播图标题不能为空")
    private String title;

    /**
     * 轮播图片URL
     */
    @NotBlank(message = "轮播图片URL不能为空")
    private String img;

    /**
     * 跳转URL
     */
    private String url;

    /**
     * 显示位置：HOME-首页轮播，CATEGORY-分类页轮播，PRODUCT-商品页轮播
     */
    @NotNull(message = "显示位置不能为空")
    private BannerPositionEnum position;

    /**
     * 跳转类型：URL-外部链接，PRODUCT-商品详情，CATEGORY-分类页，PAGE-页面
     */
    private BannerTargetTypeEnum targetType;

    /**
     * 排序权重，数字越大优先级越高
     */
    private Integer weight;

    /**
     * 启用状态：0-禁用，1-启用
     */
    private BannerStatusEnum status;

    /**
     * 生效开始时间
     */
    private Date startTime;

    /**
     * 生效结束时间
     */
    private Date endTime;
}
