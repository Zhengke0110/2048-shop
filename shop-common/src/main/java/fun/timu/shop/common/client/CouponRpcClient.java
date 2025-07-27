package fun.timu.shop.common.client;

import fun.timu.shop.common.components.HttpRpcClient;
import fun.timu.shop.common.util.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 优惠券服务RPC客户端
 * 用于调用优惠券相关的远程服务
 *
 * @author zhengke
 */
@Slf4j
@Component
public class CouponRpcClient {

    private final HttpRpcClient httpRpcClient;

    @Value("${rpc.coupon.service.url:http://localhost:9002}")
    private String couponServiceUrl;

    public CouponRpcClient(HttpRpcClient httpRpcClient) {
        this.httpRpcClient = httpRpcClient;
    }

    /**
     * RPC调用 - 新用户注册福利发放
     * 为新注册用户发放所有配置的福利优惠券
     *
     * @param userId 用户ID
     * @return 发放结果
     */
    public JsonData grantNewUserBenefits(Long userId) {
        String url = couponServiceUrl + "/api/coupon/v1/coupon/rpc/new-user-benefits";
        
        Map<String, Object> request = new HashMap<>();
        request.put("userId", userId);
        
        // 添加RPC标识头
        Map<String, String> headers = new HashMap<>();
        headers.put("RPC-Source", "user-service");
        headers.put("Content-Type", "application/json");
        
        log.info("发送RPC请求 - 新用户注册福利发放: userId={}", userId);
        
        JsonData result = httpRpcClient.postWithHeaders(url, request, headers);
        
        if (result != null && result.getCode() == 0) {
            log.info("新用户注册福利发放成功: userId={}, result={}", userId, result.getData());
        } else {
            log.error("新用户注册福利发放失败: userId={}, result={}", userId, result);
        }
        
        return result;
    }
}
