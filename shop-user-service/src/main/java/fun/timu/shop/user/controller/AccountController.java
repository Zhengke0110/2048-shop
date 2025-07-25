package fun.timu.shop.user.controller;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.user.controller.request.UserLoginRequest;
import fun.timu.shop.user.controller.request.UserRegisterRequest;
import fun.timu.shop.user.service.UserService;
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

}
