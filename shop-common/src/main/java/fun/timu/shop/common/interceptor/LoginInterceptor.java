package fun.timu.shop.common.interceptor;


import fun.timu.shop.common.enums.BizCodeEnum;
import fun.timu.shop.common.model.LoginUser;
import fun.timu.shop.common.util.CommonUtil;
import fun.timu.shop.common.util.JWTUtil;
import fun.timu.shop.common.util.JsonData;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {


    public static ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 从Authorization头获取token，支持Bearer格式
        String accessToken = getTokenFromRequest(request);
        
        if (StringUtils.isNotBlank(accessToken)) {
            // 2. 验证Access Token
            Claims claims = JWTUtil.checkAccessToken(accessToken);
            if (claims == null) {
                // Token无效或已过期
                log.warn("Access Token验证失败, token: {}", accessToken.substring(0, Math.min(accessToken.length(), 20)));
                CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.TOKEN_INVALID));
                return false;
            }

            // 3. 检查Token是否过期
            if (JWTUtil.isTokenExpired(claims)) {
                log.warn("Access Token已过期, userId: {}", claims.get("id"));
                CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.TOKEN_EXPIRED));
                return false;
            }

            // 4. 提取用户信息
            LoginUser loginUser = JWTUtil.extractLoginUser(claims);
            if (loginUser == null) {
                log.warn("无法从Token中提取用户信息");
                CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.TOKEN_INVALID));
                return false;
            }

            // 5. 设置到ThreadLocal
            threadLocal.set(loginUser);
            log.debug("用户认证成功, userId: {}, name: {}", loginUser.getId(), loginUser.getName());
            return true;
        }

        // 6. 没有提供Token
        log.warn("请求缺少Access Token, URI: {}", request.getRequestURI());
        CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN));
        return false;
    }

    /**
     * 从请求中获取Token
     * 支持多种方式：Authorization头、token参数
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 1. 优先从Authorization头获取
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(authHeader)) {
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7); // 移除"Bearer "前缀
            }
            // 直接返回Authorization头的值
            return authHeader;
        }

        // 2. 从token头获取（兼容旧版本）
        String tokenHeader = request.getHeader("token");
        if (StringUtils.isNotBlank(tokenHeader)) {
            return tokenHeader;
        }

        // 3. 从请求参数获取（兼容性支持，不推荐）
        return request.getParameter("token");
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理ThreadLocal，防止内存泄漏
        threadLocal.remove();
    }

    /**
     * 获取当前登录用户
     * 
     * @return 当前登录用户，如果未登录返回null
     */
    public static LoginUser getCurrentUser() {
        return threadLocal.get();
    }

    /**
     * 获取当前登录用户ID
     * 
     * @return 当前登录用户ID，如果未登录返回null
     */
    public static Long getCurrentUserId() {
        LoginUser user = threadLocal.get();
        return user != null ? user.getId() : null;
    }
}
