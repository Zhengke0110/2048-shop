package fun.timu.shop.order.model.VO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单商品项视图对象
 * 用于前端展示订单商品详情
 *
 * @author zhengke
 */
@Data
public class ProductOrderItemVO {
    
    /**
     * 主键ID
     */
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

    /**
     * 商品分类名称（扩展字段，前端友好显示）
     */
    private String categoryName;

    /**
     * 优惠金额（原价与实际成交价的差额）
     */
    private BigDecimal discountAmount;

    /**
     * 商品规格信息（如颜色、尺寸等）
     */
    private String productSpec;

    /**
     * 商品状态描述（如有库存、缺货等）
     */
    private String productStatus;
}
