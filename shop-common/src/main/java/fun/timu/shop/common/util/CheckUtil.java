package fun.timu.shop.common.util;

import java.util.regex.Pattern;

public class CheckUtil {

    /**
     * 邮箱验证正则表达式
     * 支持常见的邮箱格式验证
     */
    private static final Pattern MAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    /**
     * 手机号验证正则表达式
     * 支持中国大陆手机号码格式（包含所有主要号段）
     * 13x, 14x, 15x, 16x, 17x, 18x, 19x
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^1[3-9]\\d{9}$"
    );

    /**
     * 验证邮箱格式是否正确
     *
     * @param email 待验证的邮箱地址
     * @return true-格式正确，false-格式错误或为空
     */
    public static boolean isEmail(String email) {
        return isNotBlank(email) && MAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 验证手机号格式是否正确
     *
     * @param phone 待验证的手机号码
     * @return true-格式正确，false-格式错误或为空
     */
    public static boolean isPhone(String phone) {
        return isNotBlank(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 检查字符串是否不为空且不为空白
     *
     * @param str 待检查的字符串
     * @return true-不为空且不为空白，false-为null、空字符串或只包含空白字符
     */
    private static boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

}
