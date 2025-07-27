package fun.timu.shop.product.model.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 轮播图表
 *
 * @TableName banner
 */
@TableName(value = "banner")
@Data
public class BannerDO implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
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
    private String targetType;

    /**
     * 目标ID，根据target_type关联对应表的ID
     */
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
    private Date startTime;

    /**
     * 轮播结束时间
     */
    private Date endTime;

    /**
     * 点击统计
     */
    private Integer clickCount;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除标记：0-未删除，1-已删除
     */
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}