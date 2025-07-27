package fun.timu.shop.product.controller.request;

import fun.timu.shop.product.enums.BannerPositionEnum;
import fun.timu.shop.product.enums.BannerStatusEnum;
import lombok.Data;

import java.util.Date;

/**
 * 轮播图查询请求类
 *
 * @author zhengke
 */
@Data
public class BannerQueryRequest {

    /**
     * 显示位置：HOME-首页轮播，CATEGORY-分类页轮播，PRODUCT-商品页轮播
     */
    private BannerPositionEnum position;

    /**
     * 启用状态：0-禁用，1-启用
     */
    private BannerStatusEnum status;

    /**
     * 是否只查询有效时间内的轮播图
     */
    private Boolean onlyActive = false;

    /**
     * 最小权重
     */
    private Integer minWeight;

    /**
     * 最大权重
     */
    private Integer maxWeight;

    /**
     * 查询开始时间
     */
    private Date startTime;

    /**
     * 查询结束时间
     */
    private Date endTime;

    /**
     * 排序字段：weight-权重，create_time-创建时间，click_count-点击数
     */
    private String orderBy = "weight";

    /**
     * 排序方向：ASC-升序，DESC-降序
     */
    private String orderDirection = "DESC";

    /**
     * 页码（从1开始）
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;
}
