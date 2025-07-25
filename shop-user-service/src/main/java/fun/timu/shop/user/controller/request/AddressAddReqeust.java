package fun.timu.shop.user.controller.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import fun.timu.shop.common.validation.ValidPhone;

@Data
public class AddressAddReqeust {


    /**
     * 是否默认收货地址：0->否；1->是
     */
    @JsonProperty("default_status")
    @Min(value = 0, message = "默认状态值无效")
    @Max(value = 1, message = "默认状态值无效")
    private Integer defaultStatus;

    /**
     * 收发货人姓名
     */
    @JsonProperty("receive_name")
    @NotBlank(message = "收货人姓名不能为空")
    @Length(min = 2, max = 64, message = "收货人姓名长度必须在2-64字符之间")
    private String receiveName;

    /**
     * 收货人电话
     */
    @NotBlank(message = "收货人电话不能为空")
    @ValidPhone(message = "手机号格式不正确")
    private String phone;

    /**
     * 省/直辖市
     */
    @NotBlank(message = "省/直辖市不能为空")
    @Length(min = 1, max = 32, message = "省/直辖市长度必须在1-32字符之间")
    private String province;

    /**
     * 市
     */
    @NotBlank(message = "市不能为空")
    @Length(min = 1, max = 32, message = "市长度必须在1-32字符之间")
    private String city;

    /**
     * 区
     */
    @NotBlank(message = "区不能为空")
    @Length(min = 1, max = 32, message = "区长度必须在1-32字符之间")
    private String region;

    /**
     * 详细地址
     */
    @JsonProperty("detail_address")
    @NotBlank(message = "详细地址不能为空")
    @Length(min = 5, max = 200, message = "详细地址长度必须在5-200字符之间")
    private String detailAddress;
}
