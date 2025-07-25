package fun.timu.shop.user.components.impl;

import fun.timu.shop.common.enums.BizCodeEnum;
import fun.timu.shop.common.exception.BizException;
import fun.timu.shop.user.components.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;


    @Value("${spring.mail.from}")
    private String from;

    public MailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    @Override
    public void sendMail(String to, String subject, String content) {
        // 参数校验
        if (!StringUtils.hasText(to)) {
            log.error("邮件发送失败：收件人地址为空");
            throw new BizException(BizCodeEnum.CODE_TO_ERROR.getCode(), "收件人地址不能为空");
        }
        
        if (!StringUtils.hasText(subject)) {
            log.error("邮件发送失败：邮件主题为空");
            throw new BizException(BizCodeEnum.CODE_SEND_FAIL.getCode(), "邮件主题不能为空");
        }
        
        if (!StringUtils.hasText(content)) {
            log.error("邮件发送失败：邮件内容为空");
            throw new BizException(BizCodeEnum.CODE_SEND_FAIL.getCode(), "邮件内容不能为空");
        }

        try {
            //创建一个邮箱消息对象
            SimpleMailMessage message = new SimpleMailMessage();

            //配置邮箱发送人
            message.setFrom(from);

            //邮件的收件人
            message.setTo(to);

            //邮件的主题
            message.setSubject(subject);

            //邮件的内容
            message.setText(content);

            mailSender.send(message);

            log.info("邮件发送成功 - 收件人: {}, 主题: {}", to, subject);
            
        } catch (MailException e) {
            log.error("邮件发送失败 - 收件人: {}, 主题: {}, 错误信息: {}", to, subject, e.getMessage(), e);
            throw new BizException(BizCodeEnum.CODE_SEND_FAIL.getCode(), "邮件发送失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("邮件发送异常 - 收件人: {}, 主题: {}, 异常信息: {}", to, subject, e.getMessage(), e);
            throw new BizException(BizCodeEnum.CODE_SEND_FAIL.getCode(), "邮件发送异常，请稍后重试");
        }
    }
}
