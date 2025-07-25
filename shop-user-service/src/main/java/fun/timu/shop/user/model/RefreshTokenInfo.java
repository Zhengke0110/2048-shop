package fun.timu.shop.user.model;

import fun.timu.shop.common.model.LoginUser;
import lombok.Data;

/**
 * RefreshToken信息实体
 *
 * @author zhengke
 */
@Data
public class RefreshTokenInfo {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * Token唯一标识(JTI)
     */
    private String tokenId;
    
    /**
     * 用户信息
     */
    private LoginUser loginUser;
    
    /**
     * Token家族ID
     */
    private String familyId;
    
    /**
     * 创建时间
     */
    private Long createTime;
}
