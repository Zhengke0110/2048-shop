package fun.timu.shop.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户服务 RPC 接口控制器
 * 专门处理服务间调用的接口
 */
@Slf4j
@RestController
@RequestMapping("/api/user/v1/rpc")
@RequiredArgsConstructor
public class RpcController {

    // 如果有需要，可以在这里添加RPC接口
    // 目前用户服务主要是调用其他服务的RPC接口，而不是提供RPC接口

}