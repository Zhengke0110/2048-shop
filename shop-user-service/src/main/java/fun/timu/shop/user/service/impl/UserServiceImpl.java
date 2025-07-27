package fun.timu.shop.user.service.impl;

import fun.timu.shop.user.client.CouponRpcClient;
import fun.timu.shop.common.components.IdGeneratorComponent;
import fun.timu.shop.common.enums.BizCodeEnum;
import fun.timu.shop.common.enums.SendCodeEnum;
import fun.timu.shop.common.interceptor.LoginInterceptor;
import fun.timu.shop.common.model.LoginUser;
import fun.timu.shop.common.model.TokenPairVO;
import fun.timu.shop.common.util.CommonUtil;
import fun.timu.shop.common.util.JWTUtil;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.user.components.FileService;
import fun.timu.shop.user.controller.request.RefreshTokenRequest;
import fun.timu.shop.user.controller.request.UserLoginRequest;
import fun.timu.shop.user.controller.request.UserRegisterRequest;
import fun.timu.shop.user.components.RefreshTokenManager;
import fun.timu.shop.user.manager.UserManager;
import fun.timu.shop.user.model.DO.UserDO;
import fun.timu.shop.user.model.RefreshTokenInfo;
import fun.timu.shop.user.model.VO.UserVO;
import fun.timu.shop.user.service.NotifyService;
import fun.timu.shop.user.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.UUID;

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
    private final RefreshTokenManager refreshTokenManager;
    private final IdGeneratorComponent idGeneratorComponent;
    private final CouponRpcClient couponRpcClient;

    public UserServiceImpl(FileService fileService, NotifyService notifyService, UserManager userManager, StringRedisTemplate redisTemplate, RefreshTokenManager refreshTokenManager, IdGeneratorComponent idGeneratorComponent, CouponRpcClient couponRpcClient) {
        this.fileService = fileService;
        this.notifyService = notifyService;
        this.userManager = userManager;
        this.refreshTokenManager = refreshTokenManager;
        this.idGeneratorComponent = idGeneratorComponent;
        this.couponRpcClient = couponRpcClient;
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

        // 生成分布式ID
        Long userId = idGeneratorComponent.generateId();
        userDO.setId(userId);
        
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
            log.info("rows:{},注册成功:{}, 生成的用户ID:{}", rows, userDO.toString(), userId);

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
                //登录成功,生成双Token
                LoginUser loginUser = LoginUser.builder().build();
                BeanUtils.copyProperties(userDO, loginUser);

                // 生成Token对
                TokenPairVO tokenPair = JWTUtil.generateTokenPair(loginUser);

                // 提取Refresh Token的JTI并存储到Redis
                Claims refreshClaims = JWTUtil.checkRefreshToken(tokenPair.getRefreshToken());
                if (refreshClaims != null) {
                    String tokenId = JWTUtil.getTokenId(refreshClaims);
                    String familyId = UUID.randomUUID().toString(); // 生成Token家族ID
                    refreshTokenManager.storeRefreshToken(loginUser.getId(), tokenId, loginUser, familyId);
                }

                return JsonData.buildSuccess(tokenPair);
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

    @Override
    public UserVO findUserDetail() {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        UserDO userDO = userManager.selectOne(loginUser.getId());
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userDO, userVO);
        return userVO;
    }

    /**
     * 刷新Token
     *
     * @param refreshTokenRequest 刷新Token请求
     * @return 新的Token对
     */
    @Override
    public JsonData refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        // 1. 验证Refresh Token格式和签名
        Claims claims = JWTUtil.checkRefreshToken(refreshToken);
        if (claims == null) {
            return JsonData.buildResult(BizCodeEnum.REFRESH_TOKEN_INVALID);
        }

        // 2. 检查Token是否过期
        if (JWTUtil.isTokenExpired(claims)) {
            return JsonData.buildResult(BizCodeEnum.REFRESH_TOKEN_EXPIRED);
        }

        // 3. 从Claims中提取用户信息
        LoginUser loginUser = JWTUtil.extractLoginUser(claims);
        if (loginUser == null || loginUser.getId() == null) {
            return JsonData.buildResult(BizCodeEnum.REFRESH_TOKEN_INVALID);
        }

        // 4. 验证Redis中的Refresh Token
        String tokenId = JWTUtil.getTokenId(claims);
        RefreshTokenInfo tokenInfo = refreshTokenManager.validateRefreshToken(loginUser.getId(), tokenId);
        if (tokenInfo == null) {
            return JsonData.buildResult(BizCodeEnum.REFRESH_TOKEN_NOT_FOUND);
        }

        // 5. 安全检查：检测Token家族异常
        if (refreshTokenManager.detectTokenFamilyAnomaly(loginUser.getId(), tokenInfo.getFamilyId(), tokenId)) {
            // 检测到异常，清理所有Token
            refreshTokenManager.removeAllRefreshTokens(loginUser.getId());
            return JsonData.buildResult(BizCodeEnum.REFRESH_TOKEN_INVALID);
        }

        // 6. 删除旧的Refresh Token（Token轮转）
        refreshTokenManager.removeRefreshToken(loginUser.getId(), tokenId);

        // 7. 生成新的Token对
        TokenPairVO newTokenPair = JWTUtil.generateTokenPair(loginUser);

        // 8. 存储新的Refresh Token
        Claims newRefreshClaims = JWTUtil.checkRefreshToken(newTokenPair.getRefreshToken());
        if (newRefreshClaims != null) {
            String newTokenId = JWTUtil.getTokenId(newRefreshClaims);
            refreshTokenManager.storeRefreshToken(loginUser.getId(), newTokenId, loginUser, tokenInfo.getFamilyId());
        }

        log.info("刷新Token成功, userId: {}, oldTokenId: {}, newTokenId: {}", loginUser.getId(), tokenId, JWTUtil.getTokenId(newRefreshClaims));

        return JsonData.buildSuccess(newTokenPair);
    }

    /**
     * 登出
     *
     * @param refreshToken 刷新Token
     * @return 操作结果
     */
    @Override
    public JsonData logout(String refreshToken) {
        if (StringUtils.isBlank(refreshToken)) {
            return JsonData.buildSuccess(); // 没有Token也算登出成功
        }

        // 验证Refresh Token
        Claims claims = JWTUtil.checkRefreshToken(refreshToken);
        if (claims == null) {
            return JsonData.buildSuccess(); // Token无效也算登出成功
        }

        // 提取用户信息
        LoginUser loginUser = JWTUtil.extractLoginUser(claims);
        if (loginUser != null && loginUser.getId() != null) {
            // 清理用户所有Token
            refreshTokenManager.removeAllRefreshTokens(loginUser.getId());
            log.info("用户登出成功, userId: {}", loginUser.getId());
        }

        return JsonData.buildSuccess();
    }

    /**
     * 用户注册，初始化福利信息
     * 为新用户发放注册福利优惠券
     *
     * @param userDO 新注册的用户信息
     */
    private void userRegisterInitTask(UserDO userDO) {
        try {
            log.info("开始为新用户发放福利: userId={}, email={}", userDO.getId(), userDO.getMail());
            
            // 调用优惠券服务的新用户福利发放接口
            // 具体发放什么优惠券由优惠券服务决定，用户服务只负责触发
            JsonData result = couponRpcClient.grantNewUserBenefits(userDO.getId());
            
            if (result != null && result.getCode() == 0) {
                log.info("新用户福利发放成功: userId={}, result={}", userDO.getId(), result.getData());
                
                // 可以在这里添加其他初始化任务，比如：
                // - 发送欢迎邮件
                // - 初始化用户积分
                // - 记录用户注册来源
                // - 添加到用户成长体系等
                
            } else {
                log.warn("新用户福利发放失败: userId={}, error={}", 
                        userDO.getId(), result != null ? result.getMsg() : "未知错误");
            }
            
        } catch (Exception e) {
            log.error("新用户初始化任务执行失败: userId={}", userDO.getId(), e);
            // 注意：这里不应该抛出异常，避免影响用户注册流程
            // 福利发放失败不应该导致注册失败
        }
    }

}




