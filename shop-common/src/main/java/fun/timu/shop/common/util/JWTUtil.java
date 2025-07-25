package fun.timu.shop.common.util;

import fun.timu.shop.common.model.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Slf4j
public class JWTUtil {

    /**
     * token 过期时间：7天（毫秒）
     * 正常是7天，方便测试我们改为70天
     */
    private static final long EXPIRE_TIME_MS = 1000L * 60 * 60 * 24 * 7 * 10;

    /**
     * JWT 签名密钥（实际项目中应从配置文件或环境变量读取）
     * 为了增强安全性，这里使用Base64编码
     */
    private static final String SECRET_KEY = generateSecretKey();

    /**
     * JWT 令牌前缀
     */
    private static final String TOKEN_PREFIX = "2048shop";

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
     * 根据用户信息生成JWT令牌
     *
     * @param loginUser 登录用户信息
     * @return 生成的JWT令牌
     * @throws IllegalArgumentException 如果loginUser为null
     */
    public static String geneJsonWebToken(LoginUser loginUser) {
        if (loginUser == null) {
            throw new IllegalArgumentException("loginUser对象不能为空");
        }

        try {
            long currentTimeMillis = System.currentTimeMillis();
            Date now = new Date(currentTimeMillis);
            Date expiration = new Date(currentTimeMillis + EXPIRE_TIME_MS);

            String token = Jwts.builder()
                    .setSubject(JWT_SUBJECT)
                    .setIssuedAt(now)
                    .setExpiration(expiration)
                    // 用户信息载荷
                    .claim("head_img", loginUser.getHeadImg())
                    .claim("id", loginUser.getId())
                    .claim("name", loginUser.getName())
                    .claim("mail", loginUser.getMail())
                    .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                    .compact();

            return TOKEN_PREFIX + token;

        } catch (Exception e) {
            log.error("生成JWT令牌失败, loginUser: {}", loginUser, e);
            throw new RuntimeException("JWT令牌生成失败", e);
        }
    }


    /**
     * 校验并解析JWT令牌
     *
     * @param token 待校验的JWT令牌
     * @return 解析成功返回Claims对象，失败返回null
     */
    public static Claims checkJWT(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("JWT令牌为空或无效");
            return null;
        }

        if (!token.startsWith(TOKEN_PREFIX)) {
            log.warn("JWT令牌前缀不正确, token: {}", token.substring(0, Math.min(token.length(), 20)));
            return null;
        }

        try {
            // 移除前缀并解析
            String actualToken = token.substring(TOKEN_PREFIX.length());
            return JWT_PARSER.parseClaimsJws(actualToken).getBody();

        } catch (JwtException e) {
            log.warn("JWT令牌解析失败: {}, token: {}", e.getMessage(),
                    token.substring(0, Math.min(token.length(), 20)));
            return null;
        } catch (Exception e) {
            log.error("JWT令牌校验过程中发生异常", e);
            return null;
        }
    }

}
