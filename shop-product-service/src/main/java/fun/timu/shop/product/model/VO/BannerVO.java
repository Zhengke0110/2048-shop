package fun.timu.shop.product.model.VO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * 轮播图视图对象
 */
@Data
public class BannerVO {

    /**
     * 轮播图ID
     */
    private Integer id;

    /**
     * 轮播图标题
     */
    private String title;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 图片
     */
    private String img;

    /**
     * 跳转地址
     */
    private String url;

    /**
     * 跳转类型：URL-外部链接，PRODUCT-商品详情，CATEGORY-分类页，PAGE-页面
     */
    @JsonProperty("target_type")
    private String targetType;

    /**
     * 目标ID，根据target_type关联对应表的ID
     */
    @JsonProperty("target_id")
    private Long targetId;

    /**
     * 显示位置：HOME-首页轮播，CATEGORY-分类页轮播，PRODUCT-商品页轮播
     */
    private String position;

    /**
     * 权重，数值越大越靠前
     */
    private Integer weight;

    /**
     * 启用状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 轮播开始时间
     */
    @JsonProperty("start_time")
    private Date startTime;

    /**
     * 轮播结束时间
     */
    @JsonProperty("end_time")
    private Date endTime;

    /**
     * 点击统计
     */
    @JsonProperty("click_count")
    private Integer clickCount;
}
