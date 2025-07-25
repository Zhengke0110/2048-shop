package fun.timu.shop.user.components;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String uploadUserImg(MultipartFile file);
}
