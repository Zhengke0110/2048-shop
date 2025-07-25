package fun.timu.shop.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class CommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final int MAX_IP_LENGTH = 15;
    private static final String DIGITS = "0123456789";
    private static final String ALL_CHAR_NUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 获取客户端真实IP地址
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址，获取失败时返回空字符串
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        String ipAddress = null;
        try {
            // 优先从代理头中获取真实IP
            ipAddress = getIpFromHeader(request, "x-forwarded-for");
            if (isValidIp(ipAddress)) {
                return extractFirstIp(ipAddress);
            }

            ipAddress = getIpFromHeader(request, "Proxy-Client-IP");
            if (isValidIp(ipAddress)) {
                return ipAddress;
            }

            ipAddress = getIpFromHeader(request, "WL-Proxy-Client-IP");
            if (isValidIp(ipAddress)) {
                return ipAddress;
            }

            // 从远程地址获取
            ipAddress = request.getRemoteAddr();
            if (LOCALHOST_IPV4.equals(ipAddress) || LOCALHOST_IPV6.equals(ipAddress)) {
                // 获取本机真实IP
                try {
                    InetAddress inet = InetAddress.getLocalHost();
                    ipAddress = inet.getHostAddress();
                } catch (UnknownHostException e) {
                    logger.warn("Failed to get local host address", e);
                    return LOCALHOST_IPV4;
                }
            }

        } catch (Exception e) {
            logger.error("Failed to get IP address from request", e);
            return "";
        }
        return ipAddress != null ? ipAddress : "";
    }

    /**
     * 从请求头中获取IP
     */
    private static String getIpFromHeader(HttpServletRequest request, String headerName) {
        return request.getHeader(headerName);
    }

    /**
     * 验证IP是否有效
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip);
    }

    /**
     * 提取多个IP中的第一个（真实客户端IP）
     */
    private static String extractFirstIp(String ipAddress) {
        if (ipAddress != null && ipAddress.length() > MAX_IP_LENGTH && ipAddress.contains(",")) {
            return ipAddress.substring(0, ipAddress.indexOf(",")).trim();
        }
        return ipAddress;
    }


    /**
     * SHA-256哈希加密（推荐使用，比MD5更安全）
     *
     * @param data 待加密的字符串
     * @return 加密后的十六进制字符串（大写），加密失败时返回null
     */
    public static String sha256(String data) {
        return hash(data, "SHA-256");
    }

    /**
     * 验证密码是否正确
     *
     * @param rawPassword 原始密码
     * @param salt 盐值
     * @param encodedPassword 已加密的密码
     * @return 密码是否匹配
     */
    public static boolean verifyPassword(String rawPassword, String salt, String encodedPassword) {
        if (rawPassword == null || salt == null || encodedPassword == null) {
            return false;
        }
        String saltedPassword = rawPassword + salt;
        String hashedPassword = sha256(saltedPassword);
        return encodedPassword.equals(hashedPassword);
    }

    /**
     * 通用哈希方法
     */
    private static String hash(String data, String algorithm) {
        if (data == null) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] array = md.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Hash algorithm {} not available", algorithm, e);
            return null;
        }
    }


    /**
     * 生成指定长度的数字验证码
     *
     * @param length 验证码长度，必须大于0
     * @return 指定长度的数字验证码字符串
     * @throws IllegalArgumentException 当length <= 0时抛出
     */
    public static String getRandomCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(DIGITS.charAt(ThreadLocalRandom.current().nextInt(DIGITS.length())));
        }
        return sb.toString();
    }


    /**
     * 获取当前时间戳
     *
     * @return
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }


    /**
     * 生成uuid
     *
     * @return
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32);
    }


    /**
     * 获取随机长度的字符串（包含数字和字母）
     *
     * @param length 生成字符串的长度，必须大于0
     * @return 指定长度的随机字符串
     * @throws IllegalArgumentException 当length <= 0时抛出
     */
    public static String getStringNumRandom(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        StringBuilder saltString = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            saltString.append(ALL_CHAR_NUM.charAt(ThreadLocalRandom.current().nextInt(ALL_CHAR_NUM.length())));
        }
        return saltString.toString();
    }


    /**
     * 响应JSON数据给前端
     *
     * @param response HTTP响应对象，不能为null
     * @param obj      要序列化为JSON的对象
     * @throws IllegalArgumentException 当response为null时抛出
     */
    public static void sendJsonMessage(HttpServletResponse response, Object obj) {
        if (response == null) {
            throw new IllegalArgumentException("HttpServletResponse cannot be null");
        }

        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        try (PrintWriter writer = response.getWriter()) {
            String jsonString = OBJECT_MAPPER.writeValueAsString(obj);
            writer.print(jsonString);
            response.flushBuffer();
        } catch (IOException e) {
            logger.error("Failed to send JSON response to client", e);
            // 设置错误状态码
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

}