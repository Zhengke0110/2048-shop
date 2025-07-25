package fun.timu.shop.user.model;

import lombok.Data;

/**
 * Token家族信息实体
 *
 * @author zhengke
 */
@Data
public class TokenFamilyInfo {
    
    /**
     * 家族ID
     */
    private String familyId;
    
    /**
     * 最新的Token ID
     */
    private String latestTokenId;
    
    /**
     * 更新时间
     */
    private Long updateTime;
}
