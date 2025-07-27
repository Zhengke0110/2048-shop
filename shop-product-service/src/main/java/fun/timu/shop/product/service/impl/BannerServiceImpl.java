package fun.timu.shop.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.timu.shop.product.manager.BannerManager;
import fun.timu.shop.product.model.DO.BannerDO;
import fun.timu.shop.product.model.VO.BannerVO;
import fun.timu.shop.product.service.BannerService;
import fun.timu.shop.product.mapper.BannerMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhengke
 * @description 针对表【banner(轮播图表)】的数据库操作Service实现
 * @createDate 2025-07-27 16:38:53
 */
@Service
public class BannerServiceImpl implements BannerService {
    private final BannerManager bannerManager;

    public BannerServiceImpl(BannerManager bannerManager) {
        this.bannerManager = bannerManager;
    }

    @Override
    public List<BannerVO> list() {
        return List.of();
    }
}




