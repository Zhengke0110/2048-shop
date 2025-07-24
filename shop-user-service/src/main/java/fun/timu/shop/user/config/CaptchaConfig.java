package fun.timu.shop.user.config;


import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 验证码配置类
 * <p>
 * 该配置类用于生成高安全性验证码，参考银行业验证码标准设计
 * 包含图片样式、文字配置、安全干扰等多维度配置项
 */
@Configuration
public class CaptchaConfig {

    /**
     * 配置高安全性验证码生成器
     *
     * @return DefaultKaptcha 验证码生成器实例
     */
    @Bean
    @Qualifier("captchaProducer")
    public DefaultKaptcha defaultKaptcha() {

        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Properties properties = new Properties();

        // ===== 验证码图片基础配置 =====
        // 设置图片边框及样式
        properties.setProperty(Constants.KAPTCHA_BORDER, "yes");
        properties.setProperty(Constants.KAPTCHA_BORDER_COLOR, "47,79,79");
        properties.setProperty(Constants.KAPTCHA_BORDER_THICKNESS, "2");

        // 设置验证码图片尺寸
        properties.setProperty(Constants.KAPTCHA_IMAGE_WIDTH, "200");
        properties.setProperty(Constants.KAPTCHA_IMAGE_HEIGHT, "80");

        // 设置背景颜色渐变效果
        properties.setProperty(Constants.KAPTCHA_BACKGROUND_CLR_FROM, "240,248,255");
        properties.setProperty(Constants.KAPTCHA_BACKGROUND_CLR_TO, "230,230,250");

        // ===== 验证码文字配置 =====
        // 设置验证码字符长度
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "5");

        // 设置字体样式及大小
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_NAMES, "Arial,Helvetica,Times New Roman,Georgia,Verdana");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_SIZE, "40");

        // 设置文字颜色
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_COLOR, "25,25,112");

        // 设置字符间距
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_SPACE, "8");

        // 设置验证码字符集（排除易混淆字符）
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_STRING, "ABCDEFGHJKMNPQRSTUVWXYZ23456789");

        // ===== 安全干扰配置 =====
        // 设置干扰线颜色
        properties.setProperty(Constants.KAPTCHA_NOISE_COLOR, "169,169,169");
        // 设置噪点干扰实现方式
        properties.setProperty(Constants.KAPTCHA_NOISE_IMPL, "com.google.code.kaptcha.impl.DefaultNoise");

        // ===== 图片特效配置 =====
        // 设置图片变形效果实现方式
        properties.setProperty(Constants.KAPTCHA_OBSCURIFICATOR_IMPL, "com.google.code.kaptcha.impl.ShadowGimpy");

        // 设置文字渲染器实现方式
        properties.setProperty(Constants.KAPTCHA_WORDRENDERER_IMPL, "com.google.code.kaptcha.text.impl.DefaultWordRenderer");

        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
