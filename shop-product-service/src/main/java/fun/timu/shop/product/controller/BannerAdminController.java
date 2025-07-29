package fun.timu.shop.product.controller;

import fun.timu.shop.common.enums.BannerStatusEnum;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.product.controller.request.BannerCreateRequest;
import fun.timu.shop.product.controller.request.BannerQueryRequest;
import fun.timu.shop.product.controller.request.BannerUpdateRequest;
import fun.timu.shop.product.service.BannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 轮播图管理控制器
 * 管理员专用，包含轮播图的增删改查操作
 * 需要管理员权限才能访问
 *
 * @author zhengke
 */
@Slf4j
@RestController
@RequestMapping("/api/product/v1/admin/banner")
@RequiredArgsConstructor
public class BannerAdminController {

    private final BannerService bannerService;

    /**
     * 管理员查询轮播图列表
     * 可以查看所有状态的轮播图，包括未激活的
     *
     * @param queryRequest 查询条件
     * @return 轮播图列表
     */
    @PostMapping("/list")
    public JsonData adminList(@RequestBody(required = false) BannerQueryRequest queryRequest) {
        // 如果没有传查询条件，使用默认条件
        if (queryRequest == null) {
            queryRequest = new BannerQueryRequest();
        }
        // 管理员可以查看所有状态的轮播图，包括未激活的
        queryRequest.setOnlyActive(false);

        return bannerService.list(queryRequest);
    }

    /**
     * 创建轮播图
     *
     * @param createRequest
     * @return
     */
    @PostMapping("")
    public JsonData create(@RequestBody BannerCreateRequest createRequest) {
        return bannerService.create(createRequest);
    }

    /**
     * 更新轮播图
     *
     * @param id            轮播图ID
     * @param updateRequest 更新请求对象
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public JsonData update(@PathVariable Integer id, @RequestBody BannerUpdateRequest updateRequest) {
        return bannerService.update(id, updateRequest);
    }

    /**
     * 删除轮播图
     *
     * @param id 轮播图ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public JsonData delete(@PathVariable Integer id) {
        return bannerService.delete(id);
    }

    /**
     * 启用轮播图
     *
     * @param id 轮播图ID
     * @return 启用结果
     */
    @PutMapping("/{id}/enable")
    public JsonData enable(@PathVariable Integer id) {
        return bannerService.updateStatus(id, BannerStatusEnum.ENABLED.getCode());
    }

    /**
     * 禁用轮播图
     *
     * @param id 轮播图ID
     * @return 禁用结果
     */
    @PutMapping("/{id}/disable")
    public JsonData disable(@PathVariable Integer id) {
        return bannerService.updateStatus(id, BannerStatusEnum.DISABLED.getCode());
    }

    /**
     * 批量删除轮播图
     *
     * @param ids 轮播图ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public JsonData batchDelete(@RequestBody List<Integer> ids) {
        return bannerService.batchDelete(ids);
    }

    /**
     * 获取Banner详情
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public JsonData getById(@PathVariable Integer id) {
        return bannerService.getById(id);
    }
}
