package fun.timu.shop.user.service.impl;

import fun.timu.shop.common.constant.CacheKey;
import fun.timu.shop.common.enums.BizCodeEnum;
import fun.timu.shop.common.enums.SendCodeEnum;
import fun.timu.shop.common.exception.BizException;
import fun.timu.shop.common.util.CheckUtil;
import fun.timu.shop.common.util.CommonUtil;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.user.components.MailService;
import fun.timu.shop.user.service.NotifyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class NotifyServiceImpl implements NotifyService {
    private final MailService mailService;

    private final StringRedisTemplate redisTemplate;

    public NotifyServiceImpl(MailService mailService, StringRedisTemplate redisTemplate) {
        this.mailService = mailService;
        this.redisTemplate = redisTemplate;
    }


    /**
     * 验证码的标题
     */
    private static final String SUBJECT = "2048 Shop 验证码";

    /**
     * 验证码的内容
     */
    private static final String CONTENT = "您的验证码是%s,有效时间是60秒,打死也不要告诉任何人";

    /**
     * 验证码10分钟有效
     */
    private static final int CODE_EXPIRED = 60 * 1000 * 10;

    @Override
    public JsonData sendCode(SendCodeEnum sendCodeEnum, String to) {
        // 参数校验
        if (sendCodeEnum == null) {
            log.error("发送验证码失败：验证码类型不能为空");
            return JsonData.buildResult(BizCodeEnum.CODE_TO_ERROR);
        }

        if (StringUtils.isBlank(to)) {
            log.error("发送验证码失败：接收地址不能为空");
            return JsonData.buildResult(BizCodeEnum.CODE_TO_ERROR);
        }

        // 验证接收地址格式
        if (!CheckUtil.isEmail(to) && !CheckUtil.isPhone(to)) {
            log.error("发送验证码失败：接收地址格式不正确, to: {}", to);
            return JsonData.buildResult(BizCodeEnum.CODE_TO_ERROR);
        }

        String cacheKey = String.format(CacheKey.CHECK_CODE_KEY, sendCodeEnum.name(), to);

        try {
            String cacheValue = redisTemplate.opsForValue().get(cacheKey);

            //如果不为空，则判断是否60秒内重复发送
            if (StringUtils.isNotBlank(cacheValue)) {
                long ttl = Long.parseLong(cacheValue.split("_")[1]);
                //当前时间戳-验证码发送时间戳，如果小于60秒，则不给重复发送
                if (CommonUtil.getCurrentTimestamp() - ttl < 1000 * 60) {
                    log.info("重复发送验证码,时间间隔:{} 秒", (CommonUtil.getCurrentTimestamp() - ttl) / 1000);
                    return JsonData.buildResult(BizCodeEnum.CODE_LIMITED);
                }
            }

            //拼接验证码 2322_324243232424324
            String code = CommonUtil.getRandomCode(6);
            String value = code + "_" + CommonUtil.getCurrentTimestamp();

            // 先存储验证码，再发送，避免发送失败但验证码已存储的情况
            redisTemplate.opsForValue().set(cacheKey, value, CODE_EXPIRED, TimeUnit.MILLISECONDS);

            if (CheckUtil.isEmail(to)) {
                try {
                    //邮箱验证码
                    mailService.sendMail(to, SUBJECT, String.format(CONTENT, code));
                    log.info("邮箱验证码发送成功, to: {}, 验证码类型: {}", to, sendCodeEnum.name());
                    return JsonData.buildSuccess();
                } catch (BizException e) {
                    // 发送失败，删除已存储的验证码
                    redisTemplate.delete(cacheKey);
                    log.error("邮箱验证码发送失败, to: {}, 错误: {}", to, e.getMsg());
                    return JsonData.buildResult(BizCodeEnum.CODE_SEND_FAIL);
                }
            } else if (CheckUtil.isPhone(to)) {
                try {
                    //短信验证码 - 这里需要实现短信发送逻辑
                    // TODO: 实现短信发送服务
                    log.warn("短信验证码发送功能暂未实现, to: {}", to);
                    // 发送失败，删除已存储的验证码
                    redisTemplate.delete(cacheKey);
                    return JsonData.buildResult(BizCodeEnum.CODE_SEND_FAIL);
                } catch (Exception e) {
                    // 发送失败，删除已存储的验证码
                    redisTemplate.delete(cacheKey);
                    log.error("短信验证码发送失败, to: {}, 错误: {}", to, e.getMessage(), e);
                    return JsonData.buildResult(BizCodeEnum.CODE_SEND_FAIL);
                }
            }

        } catch (Exception e) {
            log.error("验证码发送过程中发生异常, to: {}, 验证码类型: {}, 异常: {}", to, sendCodeEnum.name(), e.getMessage(), e);
            // 发生异常时，尝试清除可能已存储的验证码
            try {
                redisTemplate.delete(cacheKey);
            } catch (Exception deleteException) {
                log.warn("删除验证码缓存失败: {}", deleteException.getMessage());
            }
            return JsonData.buildResult(BizCodeEnum.CODE_SEND_FAIL);
        }

        return JsonData.buildResult(BizCodeEnum.CODE_TO_ERROR);
    }
}
