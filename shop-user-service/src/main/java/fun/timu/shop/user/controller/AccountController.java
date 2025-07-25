package fun.timu.shop.user.controller;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.user.controller.request.RefreshTokenRequest;
import fun.timu.shop.user.controller.request.UserLoginRequest;
import fun.timu.shop.user.controller.request.UserRegisterRequest;
import fun.timu.shop.user.model.VO.UserVO;
import fun.timu.shop.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/user/v1/account")
public class AccountController {
    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 上传用户头像
     *
     * @param file
     * @return
     */
    @PostMapping("upload")
    public JsonData uploadUserImg(@RequestPart("file") MultipartFile file) {
        return userService.uploadUserImg(file);
    }

    /**
     * 注册用户
     *
     * @param registerRequest
     * @return
     */
    @PostMapping("register")
    public JsonData register(@RequestBody UserRegisterRequest registerRequest) {
        return userService.register(registerRequest);
    }

    /**
     * 登录用户
     *
     * @param userLoginRequest
     * @return
     */
    @PostMapping("login")
    public JsonData login(@RequestBody UserLoginRequest userLoginRequest) {
        return userService.login(userLoginRequest);
    }

    /**
     * 刷新Token
     *
     * @param refreshTokenRequest 刷新Token请求
     * @return 新的Token对
     */
    @PostMapping("refresh")
    public JsonData refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        return userService.refreshToken(refreshTokenRequest);
    }

    /**
     * 用户登出
     *
     * @param refreshToken 刷新Token（可选，从Header或Body获取）
     * @return 操作结果
     */
    @PostMapping("logout")
    public JsonData logout(@RequestHeader(value = "Refresh-Token", required = false) String refreshToken) {
        return userService.logout(refreshToken);
    }

    /**
     * 获取用户详情
     *
     * @return 用户详情
     */
    @GetMapping("detail")
    public JsonData detail() {
        UserVO userVO = userService.findUserDetail();
        return JsonData.buildSuccess(userVO);
    }

}
