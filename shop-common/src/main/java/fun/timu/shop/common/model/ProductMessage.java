package fun.timu.shop.common.model;

import lombok.Data;

/**
 * 商品库存消息
 * @author zhengke
 */
@Data
public class ProductMessage {

    /**
     * 消息id
     */
    private Long messageId;

    /**
     * 订单号
     */
    private String outTradeNo;

    /**
     * 商品任务ID
     */
    private Long taskId;
}
