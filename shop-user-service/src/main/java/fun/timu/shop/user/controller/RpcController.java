package fun.timu.shop.user.controller;

import fun.timu.shop.common.enums.BizCodeEnum;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.user.model.VO.AddressVO;
import fun.timu.shop.user.service.AddressService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务 RPC 接口控制器
 * 专门处理服务间调用的接口
 */
@Slf4j
@RestController
@RequestMapping("/api/user/v1/rpc")
@RequiredArgsConstructor
public class RpcController {

    private final AddressService addressService;

    /**
     * RPC - 根据ID获取收货地址详情
     * 该接口用于其他服务查询用户收货地址的详细信息
     *
     * @param addressId 收货地址ID
     * @param request   HTTP请求对象，用于获取调用方信息
     * @return 收货地址详情
     */
    @GetMapping("/address/detail/{addressId}")
    public JsonData getAddressById(@PathVariable Long addressId, HttpServletRequest request) {
        // 检查是否为RPC调用
        String rpcSource = request.getHeader("RPC-Source");

        // 允许订单服务等合法服务调用
        if (!"shop-order-service".equals(rpcSource) && !"shop-product-service".equals(rpcSource)) {
            log.warn("非法的RPC调用来源: {}", rpcSource);
            return JsonData.buildError("非法的RPC调用");
        }

        if (addressId == null || addressId <= 0) {
            return JsonData.buildError("地址ID必须大于0");
        }

        log.info("RPC接口被调用 - 查询收货地址详情: addressId={}, rpcSource={}", addressId, rpcSource);

        try {
            AddressVO addressVO = addressService.detail(addressId);
            return addressVO == null ?
                    JsonData.buildResult(BizCodeEnum.ADDRESS_NO_EXITS) :
                    JsonData.buildSuccess(addressVO);
        } catch (Exception e) {
            log.error("查询收货地址详情失败: addressId={}", addressId, e);
            return JsonData.buildError("查询收货地址详情失败: " + e.getMessage());
        }
    }

    // 如果有需要，可以在这里添加其他RPC接口

}