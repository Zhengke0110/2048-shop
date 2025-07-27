package fun.timu.shop.product.service;

import fun.timu.shop.product.model.DO.BannerDO;
import fun.timu.shop.product.model.VO.BannerVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author zhengke
 * @description 针对表【banner(轮播图表)】的数据库操作Service
 * @createDate 2025-07-27 16:38:53
 */
public interface BannerService {
    List<BannerVO> list();
}
