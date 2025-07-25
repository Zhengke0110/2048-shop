package fun.timu.shop.user.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import fun.timu.shop.common.validation.ValidEmail;

@Data
public class UserRegisterRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Length(min = 2, max = 32, message = "用户名长度必须在2-32位之间")
    private String name;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度必须在8-32位之间")
    private String pwd;

    @JsonProperty("head_img")
    @Length(max = 524, message = "头像URL长度不能超过524字符")
    private String headImg;

    @Length(max = 128, message = "个人标语长度不能超过128字符")
    private String slogan;

    @Min(value = 0, message = "性别值无效")
    @Max(value = 2, message = "性别值无效")
    private Integer sex;

    @NotBlank(message = "邮箱不能为空")
    @ValidEmail(message = "邮箱格式不正确")
    private String mail;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,8}$", message = "验证码格式不正确")
    private String code;
}
