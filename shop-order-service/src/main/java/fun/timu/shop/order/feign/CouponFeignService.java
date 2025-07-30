package fun.timu.shop.order.feign;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.common.request.LockCouponRecordRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 优惠券服务Feign客户端
 * 用于调用优惠券服务的远程接口
 *
 * @author zhengke
 */
@FeignClient(name = "shop-coupon-service", path = "/api/coupon/v1/rpc")
public interface CouponFeignService {

    /**
     * RPC - 锁定优惠券记录（下单时调用）
     * 该接口用于订单服务在下单时锁定用户选择的优惠券
     *
     * @param request 锁定优惠券记录请求
     * @return 锁定结果
     */
    @PostMapping("/lock")
    JsonData lockCouponRecords(@RequestBody LockCouponRecordRequest request);

    /**
     * RPC - 根据ID查询优惠券记录详情
     * 该接口用于订单服务查询优惠券记录的详细信息
     *
     * @param recordId 优惠券记录ID
     * @return 优惠券记录详情
     */
    @GetMapping("/detail/{recordId}")
    JsonData getCouponRecordById(@PathVariable("recordId") Long recordId);
}
