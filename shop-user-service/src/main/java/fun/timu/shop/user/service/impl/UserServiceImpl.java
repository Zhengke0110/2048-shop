package fun.timu.shop.user.service.impl;

import fun.timu.shop.common.enums.BizCodeEnum;
import fun.timu.shop.common.enums.SendCodeEnum;
import fun.timu.shop.common.model.LoginUser;
import fun.timu.shop.common.util.CommonUtil;
import fun.timu.shop.common.util.JWTUtil;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.user.components.FileService;
import fun.timu.shop.user.controller.request.UserLoginRequest;
import fun.timu.shop.user.controller.request.UserRegisterRequest;
import fun.timu.shop.user.manager.UserManager;
import fun.timu.shop.user.model.DO.UserDO;
import fun.timu.shop.user.service.NotifyService;
import fun.timu.shop.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

/**
 * @author zhengke
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2025-07-25 10:19:00
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final FileService fileService;
    private final NotifyService notifyService;
    private final UserManager userManager;
    private final StringRedisTemplate redisTemplate;

    public UserServiceImpl(FileService fileService, NotifyService notifyService, UserManager userManager, StringRedisTemplate redisTemplate) {
        this.fileService = fileService;
        this.notifyService = notifyService;
        this.userManager = userManager;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 用户注册
     *
     * @param registerRequest
     * @return
     */
    @Override
    public JsonData register(UserRegisterRequest registerRequest) {
        boolean checkCode = false;
        //校验验证码
        if (StringUtils.isNotBlank(registerRequest.getMail())) {
            checkCode = notifyService.checkCode(SendCodeEnum.USER_REGISTER, registerRequest.getMail(), registerRequest.getCode());
        }

        if (!checkCode) {
            return JsonData.buildResult(BizCodeEnum.CODE_ERROR);
        }


        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(registerRequest, userDO);

        userDO.setCreateTime(new Date());
        userDO.setSlogan("人生需要动态规划，学习需要贪心算法");

        //设置密码 生成盐值
        String salt = CommonUtil.getStringNumRandom(16);
        userDO.setSecret(salt);

        //密码+盐处理，使用SHA-256加密
        String saltedPassword = registerRequest.getPwd() + salt;
        String cryptPwd = CommonUtil.sha256(saltedPassword);
        userDO.setPwd(cryptPwd);

        if (userManager.checkUnique(userDO.getMail())) {

            int rows = userManager.insert(userDO);
            log.info("rows:{},注册成功:{}", rows, userDO.toString());

            //新用户注册成功，初始化信息，发放福利等 TODO
            userRegisterInitTask(userDO);
            return JsonData.buildSuccess();
        } else {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_REPEAT);
        }
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @return
     */
    @Override
    public JsonData login(UserLoginRequest userLoginRequest) {
        List<UserDO> userDOList = userManager.selectList(userLoginRequest.getMail());
        if (userDOList != null && userDOList.size() == 1) {
            //已经注册
            UserDO userDO = userDOList.get(0);

            // 使用SHA-256验证密码
            boolean passwordMatch = CommonUtil.verifyPassword(userLoginRequest.getPwd(), userDO.getSecret(), userDO.getPwd());

            if (passwordMatch) {
                //登录成功,生成token TODO

                LoginUser loginUser = new LoginUser();
                BeanUtils.copyProperties(userDO, loginUser);

                String accessToken = JWTUtil.geneJsonWebToken(loginUser);
                // accessToken
                // accessToken的过期时间
                // UUID生成一个token
                //String refreshToken = CommonUtil.generateUUID();
                //redisTemplate.opsForValue().set(refreshToken,"1",1000*60*60*24*30);

                return JsonData.buildSuccess(accessToken);
            } else {
                return JsonData.buildResult(BizCodeEnum.ACCOUNT_PWD_ERROR);
            }
        } else {
            // 用户不存在
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNREGISTER);
        }
    }

    @Override
    public JsonData uploadUserImg(MultipartFile file) {
        String result = fileService.uploadUserImg(file);
        return result != null ? JsonData.buildSuccess(result) : JsonData.buildResult(BizCodeEnum.FILE_UPLOAD_USER_IMG_FAIL);
    }

    /**
     * 用户注册，初始化福利信息 TODO
     *
     * @param userDO
     */
    private void userRegisterInitTask(UserDO userDO) {

    }

}




