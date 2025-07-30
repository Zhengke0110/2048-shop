package fun.timu.shop.order.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import fun.timu.shop.common.enums.*;
import fun.timu.shop.common.exception.BizException;
import fun.timu.shop.common.interceptor.LoginInterceptor;
import fun.timu.shop.common.model.LoginUser;
import fun.timu.shop.common.model.OrderMessage;
import fun.timu.shop.common.util.CommonUtil;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.common.request.LockCouponRecordRequest;
import fun.timu.shop.coupon.model.VO.CouponRecordVO;
import fun.timu.shop.order.config.RabbitMQConfig;
import fun.timu.shop.order.controller.request.ConfirmOrderRequest;
import fun.timu.shop.common.request.LockProductRequest;
import fun.timu.shop.common.request.OrderItemRequest;
import fun.timu.shop.order.feign.CouponFeignService;
import fun.timu.shop.order.feign.ProductFeignService;
import fun.timu.shop.order.feign.UserFeignService;
import fun.timu.shop.order.manager.ProductOrderItemManager;
import fun.timu.shop.order.manager.ProductOrderManager;
import fun.timu.shop.order.model.DO.ProductOrderDO;
import fun.timu.shop.order.model.DO.ProductOrderItemDO;
import fun.timu.shop.order.model.VO.ProductOrderAddressVO;
import fun.timu.shop.order.model.VO.ProductOrderItemVO;
import fun.timu.shop.order.service.CartService;
import fun.timu.shop.order.service.ProductOrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhengke
 * @description 针对表【product_order(订单表)】的数据库操作Service实现
 * @createDate 2025-07-29 10:49:11
 */
@Slf4j
@Service
@AllArgsConstructor
public class ProductOrderServiceImpl implements ProductOrderService {
    private final ProductOrderManager orderManager;
    private final ProductOrderItemManager orderItemManager;
    private final CouponFeignService couponFeignService;
    private final ProductFeignService productFeignService;
    private final UserFeignService userFeignService;
    private final CartService cartService;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQConfig rabbitMQConfig;


    @Override
    public JsonData confirmOrder(ConfirmOrderRequest orderRequest) {

        LoginUser loginUser = LoginInterceptor.threadLocal.get();

        String orderOutTradeNo = CommonUtil.getStringNumRandom(32);


        //获取收货地址详情
        ProductOrderAddressVO addressVO = this.getUserAddress(orderRequest.getAddressId());


        log.info("收货地址信息:{}", addressVO);

        //获取用户加入购物车的商品
        List<Long> productIdList = orderRequest.getProductIdList();

        JsonData cartItemDate = cartService.confirmCartItems(productIdList);

        // 使用FastJSON进行类型转换
        List<ProductOrderItemVO> orderItemList = null;
        if (cartItemDate.getData() != null) {
            orderItemList = JSON.parseObject(JSON.toJSONString(cartItemDate.getData()), new TypeReference<List<ProductOrderItemVO>>() {
            });
        }

        log.info("获取的商品:{}", orderItemList);
        if (orderItemList == null || orderItemList.isEmpty()) {
            //购物车商品不存在
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_CART_ITEM_NOT_EXIST);
        }

        //验证价格，减去商品优惠券
        this.checkPrice(orderItemList, orderRequest);

        //锁定优惠券
        this.lockCouponRecords(orderRequest, orderOutTradeNo);

        //锁定库存
        this.lockProductStocks(orderItemList, orderOutTradeNo);


        //创建订单
        ProductOrderDO productOrderDO = this.saveProductOrder(orderRequest, loginUser, orderOutTradeNo, addressVO);

        //创建订单项
        this.saveProductOrderItems(orderOutTradeNo, productOrderDO.getId(), orderItemList);

        //发送延迟消息，用于自动关单
        OrderMessage orderMessage = new OrderMessage();
        orderMessage.setOutTradeNo(orderOutTradeNo);
        rabbitTemplate.convertAndSend(rabbitMQConfig.getEventExchange(), rabbitMQConfig.getOrderCloseDelayRoutingKey(), orderMessage);


        //创建支付  TODO
        log.info("创建订单成功，outTradeNo:{}", orderOutTradeNo);


        return null;
    }

    @Override
    public String queryProductOrderState(String outTradeNo) {
        ProductOrderDO productOrderDO = orderManager.selectOne(outTradeNo);
        if (productOrderDO == null) {
            return "";
        } else {
            return productOrderDO.getState();
        }
    }

    @Override
    public boolean closeProductOrder(OrderMessage orderMessage) {
        return false;
    }

    private ProductOrderAddressVO getUserAddress(long addressId) {

        JsonData addressData = userFeignService.getAddressById(addressId);

        if (addressData.getCode() != 0) {
            log.error("获取收获地址失败,msg:{}", addressData);
            throw new BizException(BizCodeEnum.ADDRESS_NO_EXITS);
        }

        // 使用FastJSON将JsonData中的data转换为指定类型
        ProductOrderAddressVO addressVO = JSON.parseObject(JSON.toJSONString(addressData.getData()), ProductOrderAddressVO.class);

        return addressVO;
    }

    private void checkPrice(List<ProductOrderItemVO> orderItemList, ConfirmOrderRequest orderRequest) {

        //统计商品总价格
        BigDecimal realPayAmount = new BigDecimal("0");
        if (orderItemList != null) {
            for (ProductOrderItemVO orderItemVO : orderItemList) {
                BigDecimal itemRealPayAmount = orderItemVO.getTotalAmount();
                realPayAmount = realPayAmount.add(itemRealPayAmount);
            }
        }

        //获取优惠券，判断是否可以使用
        CouponRecordVO couponRecordVO = getCartCouponRecord(orderRequest.getCouponRecordId());

        //计算购物车价格，是否满足优惠券满减条件
        if (couponRecordVO != null) {

            //计算是否满足满减
            if (realPayAmount.compareTo(couponRecordVO.getConditionPrice()) < 0) {
                throw new BizException(BizCodeEnum.ORDER_CONFIRM_COUPON_FAIL);
            }
            if (couponRecordVO.getPrice().compareTo(realPayAmount) > 0) {
                realPayAmount = BigDecimal.ZERO;

            } else {
                realPayAmount = realPayAmount.subtract(couponRecordVO.getPrice());
            }

        }

        if (realPayAmount.compareTo(orderRequest.getRealPayAmount()) != 0) {
            log.error("订单验价失败：{}", orderRequest);
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_PRICE_FAIL);
        }
    }

    private CouponRecordVO getCartCouponRecord(Long couponRecordId) {

        if (couponRecordId == null || couponRecordId < 0) {
            return null;
        }

        JsonData couponData = couponFeignService.getCouponRecordById(couponRecordId);

        if (couponData.getCode() != 0) {
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_COUPON_FAIL);
        }

        if (couponData.getCode() == 0) {

            // 使用FastJSON进行类型转换
            CouponRecordVO couponRecordVO = JSON.parseObject(JSON.toJSONString(couponData.getData()), CouponRecordVO.class);

            if (!couponAvailable(couponRecordVO)) {
                log.error("优惠券使用失败");
                throw new BizException(BizCodeEnum.COUPON_UNAVAILABLE);
            }
            return couponRecordVO;
        }

        return null;
    }

    private boolean couponAvailable(CouponRecordVO couponRecordVO) {

        if (couponRecordVO.getUseState().equalsIgnoreCase(CouponStateEnum.NEW.name())) {
            long currentTimestamp = CommonUtil.getCurrentTimestamp();
            long end = couponRecordVO.getEndTime().getTime();
            long start = couponRecordVO.getStartTime().getTime();
            if (currentTimestamp >= start && currentTimestamp <= end) {
                return true;
            }
        }
        return false;
    }

    private void lockCouponRecords(ConfirmOrderRequest orderRequest, String orderOutTradeNo) {
        List<Long> lockCouponRecordIds = new ArrayList<>();
        if (orderRequest.getCouponRecordId() > 0) {
            lockCouponRecordIds.add(orderRequest.getCouponRecordId());

            LockCouponRecordRequest lockCouponRecordRequest = new LockCouponRecordRequest();
            lockCouponRecordRequest.setOrderOutTradeNo(orderOutTradeNo);
            lockCouponRecordRequest.setLockCouponRecordIds(lockCouponRecordIds);

            //发起锁定优惠券请求
            JsonData jsonData = couponFeignService.lockCouponRecords(lockCouponRecordRequest);
            if (jsonData.getCode() != 0) {
                throw new BizException(BizCodeEnum.COUPON_RECORD_LOCK_FAIL);
            }
        }

    }

    private void lockProductStocks(List<ProductOrderItemVO> orderItemList, String orderOutTradeNo) {

        List<OrderItemRequest> itemRequestList = orderItemList.stream().map(obj -> {
            OrderItemRequest request = new OrderItemRequest();
            request.setBuyNum(obj.getBuyNum());
            request.setProductId(obj.getProductId());
            return request;
        }).collect(Collectors.toList());

        LockProductRequest lockProductRequest = new LockProductRequest();
        lockProductRequest.setOrderOutTradeNo(orderOutTradeNo);
        lockProductRequest.setOrderItemList(itemRequestList);

        JsonData jsonData = productFeignService.lockProductStock(lockProductRequest);
        if (jsonData.getCode() != 0) {
            log.error("锁定商品库存失败：{}", lockProductRequest);
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_LOCK_PRODUCT_FAIL);
        }
    }


    private ProductOrderDO saveProductOrder(ConfirmOrderRequest orderRequest, LoginUser loginUser, String orderOutTradeNo, ProductOrderAddressVO addressVO) {

        ProductOrderDO productOrderDO = new ProductOrderDO();
        productOrderDO.setUserId(loginUser.getId());
        productOrderDO.setHeadImg(loginUser.getHeadImg());
        productOrderDO.setNickname(loginUser.getName());

        productOrderDO.setOutTradeNo(orderOutTradeNo);
        productOrderDO.setCreateTime(new Date());
        productOrderDO.setDel(0);
        productOrderDO.setOrderType(OrderTypeEnum.DAILY.name());

        //实际支付的价格
        productOrderDO.setPayAmount(orderRequest.getRealPayAmount());

        //总价，未使用优惠券的价格
        productOrderDO.setTotalAmount(orderRequest.getTotalAmount());
        productOrderDO.setState(OrderStateEnum.NEW.name());
        OrderTypeEnum.valueOf(orderRequest.getPayType()).name();
        productOrderDO.setPayType(OrderPayTypeEnum.valueOf(orderRequest.getPayType()).name());

        productOrderDO.setReceiverAddress(JSON.toJSONString(addressVO));

        boolean inserted = orderManager.insert(productOrderDO);
        if (!inserted) {
            log.error("保存订单失败：{}", productOrderDO);
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_SAVE_ORDER_FAIL);
        }
        return productOrderDO;

    }

    private void saveProductOrderItems(String orderOutTradeNo, Long orderId, List<ProductOrderItemVO> orderItemList) {


        List<ProductOrderItemDO> list = orderItemList.stream().map(obj -> {
            ProductOrderItemDO itemDO = new ProductOrderItemDO();
            itemDO.setBuyNum(obj.getBuyNum());
            itemDO.setProductId(obj.getProductId());
            itemDO.setProductImg(obj.getProductImg());
            itemDO.setProductName(obj.getProductName());

            itemDO.setOutTradeNo(orderOutTradeNo);
            itemDO.setCreateTime(new Date());

            //单价
            itemDO.setAmount(obj.getAmount());
            //总价
            itemDO.setTotalAmount(obj.getTotalAmount());
            itemDO.setProductOrderId(orderId);
            return itemDO;
        }).collect(Collectors.toList());


        orderItemManager.insertBatch(list);
    }
}




