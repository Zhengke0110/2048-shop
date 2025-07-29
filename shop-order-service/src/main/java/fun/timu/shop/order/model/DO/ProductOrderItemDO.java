package fun.timu.shop.order.model.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 订单商品表
 * @TableName product_order_item
 */
@TableName(value ="product_order_item")
@Data
public class ProductOrderItemDO implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID，关联product_order表
     */
    private Long productOrderId;

    /**
     * 订单号，冗余字段便于查询
     */
    private String outTradeNo;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productImg;

    /**
     * 商品分类ID，下单时商品所属分类
     */
    private Long categoryId;

    /**
     * 商品原价，下单时的原价
     */
    private BigDecimal oldPrice;

    /**
     * 购买数量
     */
    private Integer buyNum;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 购物项商品总价格
     */
    private BigDecimal totalAmount;

    /**
     * 购物项商品单价（实际成交价）
     */
    private BigDecimal amount;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}