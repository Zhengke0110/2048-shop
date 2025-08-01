package fun.timu.shop.user.service;

import fun.timu.shop.common.enums.SendCodeEnum;
import fun.timu.shop.common.util.JsonData;

public interface NotifyService {
    /**
     * 发送验证码
     *
     * @param sendCodeEnum
     * @param to
     * @return
     */
    JsonData sendCode(SendCodeEnum sendCodeEnum, String to);

    /**
     * 校验验证码
     *
     * @param sendCodeEnum
     * @param to
     * @param code
     * @return
     */
    boolean checkCode(SendCodeEnum sendCodeEnum, String to, String code);
}
