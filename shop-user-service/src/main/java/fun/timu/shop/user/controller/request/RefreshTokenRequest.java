package fun.timu.shop.user.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * 刷新Token请求
 *
 * @author zhengke
 */
@Data
public class RefreshTokenRequest {

    /**
     * 刷新令牌
     */
    @NotBlank(message = "刷新令牌不能为空")
    @JsonProperty("refresh_token")
    private String refreshToken;

}
