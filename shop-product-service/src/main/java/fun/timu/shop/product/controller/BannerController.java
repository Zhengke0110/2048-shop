package fun.timu.shop.product.controller;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.product.controller.request.BannerQueryRequest;
import fun.timu.shop.common.enums.BannerPositionEnum;
import fun.timu.shop.product.service.BannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 轮播图查询控制器
 * 普通用户可以访问的轮播图相关接口
 *
 * @author zhengke
 */
@Slf4j
@RestController
@RequestMapping("/api/product/v1/banner")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    /**
     * 查询轮播图列表
     *
     * @param queryRequest
     * @return
     */
    @PostMapping("/list")
    public JsonData list(@RequestBody(required = false) BannerQueryRequest queryRequest) {
        // 如果没有传查询条件，使用默认条件
        if (queryRequest == null) {
            queryRequest = new BannerQueryRequest();
        }

        return bannerService.list(queryRequest);
    }

    /**
     * 根据位置查询轮播图列表
     *
     * @param position
     * @return
     */
    @GetMapping("/list/{position}")
    public JsonData listByPosition(@PathVariable String position) {
        BannerQueryRequest queryRequest = new BannerQueryRequest();
        BannerPositionEnum positionEnum = BannerPositionEnum.getByCode(position);
        if (positionEnum == null) {
            return JsonData.buildError("无效的位置参数：" + position);
        }
        queryRequest.setPosition(positionEnum);

        return bannerService.list(queryRequest);
    }

    /**
     * 获取首页轮播图列表
     *
     * @return
     */
    @GetMapping("/home")
    public JsonData getHomeBanners() {
        BannerQueryRequest queryRequest = new BannerQueryRequest();
        queryRequest.setPosition(BannerPositionEnum.HOME);
        queryRequest.setOnlyActive(true); // 只获取有效时间内的轮播图

        return bannerService.list(queryRequest);
    }

    /**
     * 根据ID获取轮播图详情
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public JsonData getById(@PathVariable Integer id) {
        return bannerService.getById(id);
    }

    /**
     * 根据ID获取轮播图详情（仅用于点击统计）
     *
     * @param id
     * @return
     */
    @PostMapping("/{id}/click")
    public JsonData incrementClickCount(@PathVariable Integer id) {
        return bannerService.incrementClickCount(id);
    }
}
