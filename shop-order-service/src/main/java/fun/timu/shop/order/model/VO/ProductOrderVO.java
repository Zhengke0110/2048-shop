package fun.timu.shop.order.model.VO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单视图对象
 * 用于前端展示订单信息
 *
 * @author zhengke
 */
@Data
public class ProductOrderVO {
    
    /**
     * 订单ID主键
     */
    private Long id;

    /**
     * 订单唯一标识
     */
    private String outTradeNo;

    /**
     * NEW 未支付订单,PAY已经支付订单,CANCEL超时取消订单
     */
    private String state;

    /**
     * 订单生成时间
     */
    private Date createTime;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单实际支付价格
     */
    private BigDecimal payAmount;

    /**
     * 支付类型，微信-银行-支付宝
     */
    private String payType;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String headImg;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 订单类型 DAILY普通单，PROMOTION促销订单
     */
    private String orderType;

    /**
     * 收货地址信息（解析后的对象，而不是JSON字符串）
     */
    private ProductOrderAddressVO receiverAddress;

    /**
     * 订单商品项列表
     */
    private List<ProductOrderItemVO> orderItemList;

    /**
     * 订单状态描述（前端友好显示）
     */
    private String stateDesc;

    /**
     * 支付类型描述（前端友好显示）
     */
    private String payTypeDesc;

    /**
     * 订单类型描述（前端友好显示）
     */
    private String orderTypeDesc;
}
