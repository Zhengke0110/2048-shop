package fun.timu.shop.product.model.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

/**
 * 商品表
 *
 * @TableName product
 */
@TableName(value = "product")
@Data
public class ProductDO implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 封面图
     */
    private String coverImg;

    /**
     * 商品详情
     */
    private String detail;

    /**
     * 商品分类ID
     */
    private Long categoryId;

    /**
     * 原价
     */
    private BigDecimal oldPrice;

    /**
     * 现价
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 锁定库存
     */
    private Integer lockStock;

    /**
     * 销售数量
     */
    private Integer salesCount;

    /**
     * 排序权重，数值越大越靠前
     */
    private Integer sort;

    /**
     * 商品状态：0-下架，1-上架
     */
    private Integer status;

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