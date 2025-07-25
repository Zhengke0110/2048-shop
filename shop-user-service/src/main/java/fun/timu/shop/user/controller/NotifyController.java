package fun.timu.shop.user.controller;

import com.google.code.kaptcha.Producer;
import fun.timu.shop.common.enums.SendCodeEnum;
import fun.timu.shop.common.util.CommonUtil;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.common.enums.BizCodeEnum;
import fun.timu.shop.user.service.NotifyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/user/v1/notify")
public class NotifyController {

    private final Producer captchaProducer;

    private final StringRedisTemplate redisTemplate;

    private final NotifyService notifyService;

    public NotifyController(Producer captchaProducer, StringRedisTemplate redisTemplate, NotifyService notifyService) {
        this.captchaProducer = captchaProducer;
        this.redisTemplate = redisTemplate;
        this.notifyService = notifyService;
    }

    /**
     * 图形验证码有效期5分钟
     */
    private static final long CAPTCHA_CODE_EXPIRED = 60 * 1000 * 5;

    /**
     * 获取图形验证码
     *
     * @param request
     * @param response
     */
    @GetMapping("captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {

        String captchaText = captchaProducer.createText();
        log.info("图形验证码:{}", captchaText);

        //存储
        redisTemplate.opsForValue().set(getCaptchaKey(request), captchaText, CAPTCHA_CODE_EXPIRED, TimeUnit.MILLISECONDS);

        BufferedImage bufferedImage = captchaProducer.createImage(captchaText);
        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            ImageIO.write(bufferedImage, "jpg", outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            log.error("获取图形验证码异常:{}", e);
        }
    }

    /**
     * 发送注册验证码
     *
     * @param to
     * @param captcha
     * @param request
     * @return
     */
    @GetMapping("sendCode")
    public JsonData sendRegisterCode(@RequestParam(value = "to", required = true) String to, @RequestParam(value = "captcha", required = true) String captcha, HttpServletRequest request) {

        String key = getCaptchaKey(request);
        String cacheCaptcha = redisTemplate.opsForValue().get(key);

        //匹配图形验证码是否一样
        if (captcha != null && cacheCaptcha != null && captcha.equalsIgnoreCase(cacheCaptcha)) {
            //成功
            redisTemplate.delete(key);
            JsonData jsonData = notifyService.sendCode(SendCodeEnum.USER_REGISTER, to);
            return jsonData;

        } else {
            return JsonData.buildResult(BizCodeEnum.CODE_CAPTCHA_ERROR);
        }

    }


    /**
     * 获取缓存的key
     *
     * @param request
     * @return
     */
    private String getCaptchaKey(HttpServletRequest request) {

        String ip = CommonUtil.getIpAddr(request);
        String userAgent = request.getHeader("User-Agent");

        String key = "user-service:captcha:" + CommonUtil.sha256(ip + userAgent);

        log.info("ip={}", ip);
        log.info("userAgent={}", userAgent);
        log.info("key={}", key);

        return key;

    }
}
