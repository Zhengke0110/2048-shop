package fun.timu.shop.user.components.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectResult;
import fun.timu.shop.common.util.CommonUtil;
import fun.timu.shop.user.components.FileService;
import fun.timu.shop.user.config.OSSConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    private final OSSConfig ossConfig;

    // 支持的图片格式
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp");

    // 最大文件大小：5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // 日期格式化器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public FileServiceImpl(OSSConfig ossConfig) {
        this.ossConfig = ossConfig;
    }

    @Override
    public String uploadUserImg(MultipartFile file) {
        // 参数验证
        if (!validateFile(file)) {
            return null;
        }

        // 构建文件路径
        String newFileName = buildFileName(file.getOriginalFilename());

        // 上传文件
        return uploadToOSS(file, newFileName);
    }

    /**
     * 验证上传文件
     */
    private boolean validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.error("上传文件为空");
            return false;
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            log.error("文件大小超过限制，最大允许{}MB", MAX_FILE_SIZE / 1024 / 1024);
            return false;
        }

        // 检查文件名
        String originalFileName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFileName)) {
            log.error("文件名为空");
            return false;
        }

        // 检查文件扩展名
        String extension = getFileExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            log.error("不支持的文件格式：{}", extension);
            return false;
        }

        return true;
    }

    /**
     * 构建文件存储路径
     */
    private String buildFileName(String originalFileName) {
        // 获取文件扩展名
        String extension = getFileExtension(originalFileName);

        // 生成日期文件夹
        String folder = DATE_FORMATTER.format(LocalDateTime.now());

        // 生成唯一文件名
        String fileName = CommonUtil.generateUUID();

        // 拼装完整路径：user/2024/07/25/uuid.jpg
        return "user/" + folder + "/" + fileName + extension;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex) : "";
    }

    /**
     * 上传文件到OSS
     */
    private String uploadToOSS(MultipartFile file, String fileName) {
        OSS ossClient = null;
        try {
            // 创建OSS客户端
            ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );

            // 上传文件
            PutObjectResult result = ossClient.putObject(
                    ossConfig.getBucketname(),
                    fileName,
                    file.getInputStream()
            );

            if (result != null) {
                String imageUrl = buildImageUrl(fileName);
                log.info("文件上传成功，URL：{}", imageUrl);
                return imageUrl;
            } else {
                log.error("文件上传失败，OSS返回结果为空");
                return null;
            }

        } catch (IOException e) {
            log.error("文件上传失败，IO异常：{}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("文件上传失败，未知异常：{}", e.getMessage(), e);
            return null;
        } finally {
            // 确保OSS客户端被关闭
            if (ossClient != null) {
                try {
                    ossClient.shutdown();
                } catch (Exception e) {
                    log.warn("关闭OSS客户端失败：{}", e.getMessage());
                }
            }
        }
    }

    /**
     * 构建图片访问URL
     */
    private String buildImageUrl(String fileName) {
        return "https://" + ossConfig.getBucketname() + "." + ossConfig.getEndpoint() + "/" + fileName;
    }
}
