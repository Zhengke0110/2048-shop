package fun.timu.shop.user.components;

import fun.timu.shop.common.model.LoginUser;
import fun.timu.shop.user.model.RefreshTokenInfo;

/**
 * RefreshToken管理器接口
 * 负责RefreshToken的Redis存储、验证和管理
 *
 * @author zhengke
 */
public interface RefreshTokenManager {

    /**
     * 存储RefreshToken信息
     *
     * @param userId    用户ID
     * @param tokenId   Token ID (JTI)
     * @param loginUser 用户信息
     * @param familyId  Token家族ID
     */
    void storeRefreshToken(Long userId, String tokenId, LoginUser loginUser, String familyId);

    /**
     * 验证并获取RefreshToken信息
     *
     * @param userId  用户ID
     * @param tokenId Token ID
     * @return RefreshToken信息，不存在返回null
     */
    RefreshTokenInfo validateRefreshToken(Long userId, String tokenId);

    /**
     * 删除RefreshToken
     *
     * @param userId  用户ID
     * @param tokenId Token ID
     */
    void removeRefreshToken(Long userId, String tokenId);

    /**
     * 删除用户所有RefreshToken（登出或安全清理）
     *
     * @param userId 用户ID
     */
    void removeAllRefreshTokens(Long userId);

    /**
     * 检测Token家族异常使用（安全检查）
     *
     * @param userId   用户ID
     * @param familyId 家族ID
     * @param tokenId  当前Token ID
     * @return true-检测到异常，false-正常
     */
    boolean detectTokenFamilyAnomaly(Long userId, String familyId, String tokenId);
}
