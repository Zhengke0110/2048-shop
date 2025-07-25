package fun.timu.shop.user.controller.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import fun.timu.shop.common.validation.ValidEmail;

@Data
public class UserLoginRequest {

    @NotBlank(message = "邮箱不能为空")
    @ValidEmail(message = "邮箱格式不正确")
    private String mail;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度必须在8-32位之间")
    private String pwd;
}
