package fun.timu.shop.user.components.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fun.timu.shop.common.model.LoginUser;
import fun.timu.shop.user.components.RefreshTokenManager;
import fun.timu.shop.user.model.RefreshTokenInfo;
import fun.timu.shop.user.model.TokenFamilyInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RefreshTokenManagerImpl implements RefreshTokenManager {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * RefreshToken Redis Key前缀
     */
    private static final String REFRESH_TOKEN_KEY_PREFIX = "user:refresh:";

    /**
     * Token家族 Redis Key前缀
     */
    private static final String TOKEN_FAMILY_KEY_PREFIX = "user:token:family:";

    /**
     * RefreshToken过期时间（秒）
     */
    private static final long REFRESH_TOKEN_EXPIRE_SECONDS = 30 * 24 * 60 * 60; // 30天


    public RefreshTokenManagerImpl(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 存储RefreshToken信息
     *
     * @param userId    用户ID
     * @param tokenId   Token ID (JTI)
     * @param loginUser 用户信息
     * @param familyId  Token家族ID
     */
    @Override
    public void storeRefreshToken(Long userId, String tokenId, LoginUser loginUser, String familyId) {
        try {
            RefreshTokenInfo tokenInfo = new RefreshTokenInfo();
            tokenInfo.setUserId(userId);
            tokenInfo.setTokenId(tokenId);
            tokenInfo.setLoginUser(loginUser);
            tokenInfo.setFamilyId(familyId);
            tokenInfo.setCreateTime(System.currentTimeMillis());

            String key = buildRefreshTokenKey(userId, tokenId);
            String value = objectMapper.writeValueAsString(tokenInfo);

            redisTemplate.opsForValue().set(key, value, REFRESH_TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);

            // 更新Token家族信息
            updateTokenFamily(userId, familyId, tokenId);

            log.info("存储RefreshToken成功, userId: {}, tokenId: {}", userId, tokenId);
        } catch (Exception e) {
            log.error("存储RefreshToken失败, userId: {}, tokenId: {}", userId, tokenId, e);
            throw new RuntimeException("存储RefreshToken失败", e);
        }
    }

    /**
     * 验证并获取RefreshToken信息
     *
     * @param userId  用户ID
     * @param tokenId Token ID
     * @return RefreshToken信息，不存在返回null
     */
    @Override
    public RefreshTokenInfo validateRefreshToken(Long userId, String tokenId) {
        try {
            String key = buildRefreshTokenKey(userId, tokenId);
            String value = redisTemplate.opsForValue().get(key);

            if (value == null) {
                log.warn("RefreshToken不存在, userId: {}, tokenId: {}", userId, tokenId);
                return null;
            }

            RefreshTokenInfo tokenInfo = objectMapper.readValue(value, RefreshTokenInfo.class);
            log.info("验证RefreshToken成功, userId: {}, tokenId: {}", userId, tokenId);
            return tokenInfo;

        } catch (Exception e) {
            log.error("验证RefreshToken失败, userId: {}, tokenId: {}", userId, tokenId, e);
            return null;
        }
    }

    /**
     * 删除RefreshToken
     *
     * @param userId  用户ID
     * @param tokenId Token ID
     */
    @Override
    public void removeRefreshToken(Long userId, String tokenId) {
        try {
            String key = buildRefreshTokenKey(userId, tokenId);
            redisTemplate.delete(key);
            log.info("删除RefreshToken成功, userId: {}, tokenId: {}", userId, tokenId);
        } catch (Exception e) {
            log.error("删除RefreshToken失败, userId: {}, tokenId: {}", userId, tokenId, e);
        }
    }

    /**
     * 删除用户所有RefreshToken（登出或安全清理）
     *
     * @param userId 用户ID
     */
    @Override
    public void removeAllRefreshTokens(Long userId) {
        try {
            // 获取Token家族信息
            String familyKey = buildTokenFamilyKey(userId);
            String familyInfo = redisTemplate.opsForValue().get(familyKey);

            if (familyInfo != null) {
                // 删除所有相关的RefreshToken
                String pattern = buildRefreshTokenKey(userId, "*");
                redisTemplate.delete(redisTemplate.keys(pattern));

                // 删除Token家族信息
                redisTemplate.delete(familyKey);
            }

            log.info("删除用户所有RefreshToken成功, userId: {}", userId);
        } catch (Exception e) {
            log.error("删除用户所有RefreshToken失败, userId: {}", userId, e);
        }
    }

    /**
     * 检测Token家族异常使用（安全检查）
     *
     * @param userId   用户ID
     * @param familyId 家族ID
     * @param tokenId  当前Token ID
     * @return true-检测到异常，false-正常
     */
    @Override
    public boolean detectTokenFamilyAnomaly(Long userId, String familyId, String tokenId) {
        try {
            String familyKey = buildTokenFamilyKey(userId);
            String currentFamilyInfo = redisTemplate.opsForValue().get(familyKey);

            if (currentFamilyInfo == null) {
                log.warn("Token家族信息不存在, userId: {}, familyId: {}", userId, familyId);
                return true;
            }

            // 这里可以实现更复杂的异常检测逻辑
            // 例如：检查Token使用频率、地理位置变化等
            return false;

        } catch (Exception e) {
            log.error("检测Token家族异常失败, userId: {}, familyId: {}", userId, familyId, e);
            return true;
        }
    }

    /**
     * 更新Token家族信息
     */
    private void updateTokenFamily(Long userId, String familyId, String tokenId) {
        try {
            String familyKey = buildTokenFamilyKey(userId);
            TokenFamilyInfo familyInfo = new TokenFamilyInfo();
            familyInfo.setFamilyId(familyId);
            familyInfo.setLatestTokenId(tokenId);
            familyInfo.setUpdateTime(System.currentTimeMillis());

            String value = objectMapper.writeValueAsString(familyInfo);
            redisTemplate.opsForValue().set(familyKey, value, REFRESH_TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);

        } catch (JsonProcessingException e) {
            log.error("更新Token家族信息失败, userId: {}, familyId: {}", userId, familyId, e);
        }
    }

    /**
     * 构建RefreshToken Redis Key
     */
    private String buildRefreshTokenKey(Long userId, String tokenId) {
        return REFRESH_TOKEN_KEY_PREFIX + userId + ":" + tokenId;
    }

    /**
     * 构建Token家族 Redis Key
     */
    private String buildTokenFamilyKey(Long userId) {
        return TOKEN_FAMILY_KEY_PREFIX + userId;
    }
}
