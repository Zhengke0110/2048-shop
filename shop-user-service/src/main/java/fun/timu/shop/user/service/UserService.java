package fun.timu.shop.user.service;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.user.controller.request.UserLoginRequest;
import fun.timu.shop.user.controller.request.UserRegisterRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author zhengke
 * @description 针对表【user】的数据库操作Service
 * @createDate 2025-07-25 10:19:00
 */
public interface UserService {

    JsonData register(UserRegisterRequest registerRequest);

    JsonData login(UserLoginRequest userLoginRequest);

    JsonData uploadUserImg(MultipartFile file);
}
