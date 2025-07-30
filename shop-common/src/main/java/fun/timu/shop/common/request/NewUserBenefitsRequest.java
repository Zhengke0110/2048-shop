package fun.timu.shop.common.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新用户福利发放请求类
 *
 * @author zhengke
 */
@Data
public class NewUserBenefitsRequest {
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @JsonProperty("user_id")
    private Long userId;
}
