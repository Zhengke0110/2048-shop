package fun.timu.shop.common.util;

import fun.timu.shop.common.model.LoginUser;
import fun.timu.shop.common.model.TokenPairVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Slf4j
public class JWTUtil {

    /**
     * Access Token 过期时间：1小时（毫秒）
     */
    private static final long ACCESS_TOKEN_EXPIRE_TIME_MS = 1000L * 60 * 60;

    /**
     * Refresh Token 过期时间：30天（毫秒）
     */
    private static final long REFRESH_TOKEN_EXPIRE_TIME_MS = 1000L * 60 * 60 * 24 * 30;

    /**
     * JWT 签名密钥（实际项目中应从配置文件或环境变量读取）
     * 为了增强安全性，这里使用Base64编码
     */
    private static final String SECRET_KEY = generateSecretKey();

    /**
     * Access Token 令牌前缀
     */
    private static final String ACCESS_TOKEN_PREFIX = "2048shop";

    /**
     * Refresh Token 令牌前缀
     */
    private static final String REFRESH_TOKEN_PREFIX = "2048refresh";

    /**
     * JWT Subject 标识
     */
    private static final String JWT_SUBJECT = "timu";

    /**
     * JWT解析器实例（复用以提高性能）
     */
    private static final JwtParser JWT_PARSER = Jwts.parser().setSigningKey(SECRET_KEY);

    /**
     * 生成更安全的密钥
     * 在实际生产环境中，应该从安全的配置源获取
     */
    private static String generateSecretKey() {
        // 使用原始密钥作为种子，但进行Base64编码增强
        String originalSecret = "timu.fun";
        return Base64.getEncoder().encodeToString(originalSecret.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * 生成Token对（Access Token + Refresh Token）
     *
     * @param loginUser 登录用户信息
     * @return Token对
     */
    public static TokenPairVO generateTokenPair(LoginUser loginUser) {
        if (loginUser == null) {
            throw new IllegalArgumentException("loginUser对象不能为空");
        }

        String accessToken = generateAccessToken(loginUser);
        String refreshToken = generateRefreshToken(loginUser);

        return new TokenPairVO(
                accessToken,
                refreshToken,
                ACCESS_TOKEN_EXPIRE_TIME_MS / 1000,
                REFRESH_TOKEN_EXPIRE_TIME_MS / 1000,
                "Bearer"
        );
    }

    /**
     * 生成Access Token
     *
     * @param loginUser 登录用户信息
     * @return Access Token
     */
    public static String generateAccessToken(LoginUser loginUser) {
        return generateToken(loginUser, ACCESS_TOKEN_EXPIRE_TIME_MS, ACCESS_TOKEN_PREFIX, "access");
    }

    /**
     * 生成Refresh Token
     *
     * @param loginUser 登录用户信息
     * @return Refresh Token
     */
    public static String generateRefreshToken(LoginUser loginUser) {
        return generateToken(loginUser, REFRESH_TOKEN_EXPIRE_TIME_MS, REFRESH_TOKEN_PREFIX, "refresh");
    }

    /**
     * 通用Token生成方法
     *
     * @param loginUser  用户信息
     * @param expireTime 过期时间（毫秒）
     * @param prefix     Token前缀
     * @param tokenType  Token类型
     * @return 生成的Token
     */
    private static String generateToken(LoginUser loginUser, long expireTime, String prefix, String tokenType) {
        try {
            long currentTimeMillis = System.currentTimeMillis();
            Date now = new Date(currentTimeMillis);
            Date expiration = new Date(currentTimeMillis + expireTime);

            String jti = UUID.randomUUID().toString(); // JWT ID，用于标识唯一性

            String token = Jwts.builder()
                    .setSubject(JWT_SUBJECT)
                    .setIssuedAt(now)
                    .setExpiration(expiration)
                    .setId(jti)
                    // 用户信息载荷
                    .claim("head_img", loginUser.getHeadImg())
                    .claim("id", loginUser.getId())
                    .claim("name", loginUser.getName())
                    .claim("mail", loginUser.getMail())
                    .claim("token_type", tokenType)
                    .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                    .compact();

            return prefix + token;

        } catch (Exception e) {
            log.error("生成{}失败, loginUser: {}", tokenType, loginUser, e);
            throw new RuntimeException(tokenType + "生成失败", e);
        }
    }


    /**
     * 校验并解析Access Token
     *
     * @param token 待校验的Access Token
     * @return 解析成功返回Claims对象，失败返回null
     */
    public static Claims checkAccessToken(String token) {
        return checkToken(token, ACCESS_TOKEN_PREFIX, "access");
    }

    /**
     * 校验并解析Refresh Token
     *
     * @param token 待校验的Refresh Token
     * @return 解析成功返回Claims对象，失败返回null
     */
    public static Claims checkRefreshToken(String token) {
        return checkToken(token, REFRESH_TOKEN_PREFIX, "refresh");
    }

    /**
     * 校验并解析JWT令牌（兼容旧版本）
     *
     * @param token 待校验的JWT令牌
     * @return 解析成功返回Claims对象，失败返回null
     */
    public static Claims checkJWT(String token) {
        return checkAccessToken(token);
    }

    /**
     * 通用Token校验方法
     *
     * @param token     待校验的Token
     * @param prefix    Token前缀
     * @param tokenType Token类型
     * @return 解析成功返回Claims对象，失败返回null
     */
    private static Claims checkToken(String token, String prefix, String tokenType) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("{}为空或无效", tokenType);
            return null;
        }

        if (!token.startsWith(prefix)) {
            log.warn("{}前缀不正确, token: {}", tokenType, token.substring(0, Math.min(token.length(), 20)));
            return null;
        }

        try {
            // 移除前缀并解析
            String actualToken = token.substring(prefix.length());
            Claims claims = JWT_PARSER.parseClaimsJws(actualToken).getBody();

            // 验证Token类型
            String claimTokenType = (String) claims.get("token_type");
            if (!tokenType.equals(claimTokenType)) {
                log.warn("Token类型不匹配，期望: {}, 实际: {}", tokenType, claimTokenType);
                return null;
            }

            return claims;

        } catch (JwtException e) {
            log.warn("{}解析失败: {}, token: {}", tokenType, e.getMessage(),
                    token.substring(0, Math.min(token.length(), 20)));
            return null;
        } catch (Exception e) {
            log.error("{}校验过程中发生异常", tokenType, e);
            return null;
        }
    }

    /**
     * 检查Token是否过期
     *
     * @param claims Token声明
     * @return true-已过期，false-未过期
     */
    public static boolean isTokenExpired(Claims claims) {
        if (claims == null) {
            return true;
        }
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.before(new Date());
    }

    /**
     * 从Claims中提取用户信息
     *
     * @param claims Token声明
     * @return 登录用户信息
     */
    public static LoginUser extractLoginUser(Claims claims) {
        if (claims == null) {
            return null;
        }

        try {
            LoginUser loginUser = new LoginUser();
            loginUser.setId(claims.get("id", Long.class));
            loginUser.setName(claims.get("name", String.class));
            loginUser.setHeadImg(claims.get("head_img", String.class));
            loginUser.setMail(claims.get("mail", String.class));
            return loginUser;
        } catch (Exception e) {
            log.error("提取用户信息失败", e);
            return null;
        }
    }

    /**
     * 获取Token的JTI（JWT ID）
     *
     * @param claims Token声明
     * @return JTI
     */
    public static String getTokenId(Claims claims) {
        return claims != null ? claims.getId() : null;
    }

}
