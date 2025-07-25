package fun.timu.shop.user.components;

public interface MailService {
    void sendMail(String to, String subject, String content);
}
