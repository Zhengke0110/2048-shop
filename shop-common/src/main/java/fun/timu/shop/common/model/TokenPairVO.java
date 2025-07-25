package fun.timu.shop.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token对响应VO
 *
 * @author zhengke
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenPairVO {

    /**
     * 访问令牌
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * 刷新令牌
     */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * 访问令牌过期时间(秒)
     */
    @JsonProperty("expires_in")
    private Long expiresIn;

    /**
     * 刷新令牌过期时间(秒)
     */
    @JsonProperty("refresh_expires_in")
    private Long refreshExpiresIn;

    /**
     * 令牌类型
     */
    @JsonProperty("token_type")
    private String tokenType = "Bearer";

}
