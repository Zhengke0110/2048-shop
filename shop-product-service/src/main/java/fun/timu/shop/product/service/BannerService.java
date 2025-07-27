package fun.timu.shop.product.service;

import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.product.controller.request.BannerCreateRequest;
import fun.timu.shop.product.controller.request.BannerQueryRequest;
import fun.timu.shop.product.controller.request.BannerUpdateRequest;

import java.util.List;

/**
 * @author zhengke
 * @description 针对表【banner(轮播图表)】的数据库操作Service
 * @createDate 2025-07-27 16:38:53
 */
public interface BannerService {
    
    /**
     * 查询轮播图列表
     * @param queryRequest 查询条件
     * @return JsonData格式的响应
     */
    JsonData list(BannerQueryRequest queryRequest);
    
    /**
     * 根据ID获取轮播图详情
     * @param id 轮播图ID
     * @return JsonData格式的响应
     */
    JsonData getById(Integer id);
    
    /**
     * 增加点击统计
     * @param id 轮播图ID
     * @return JsonData格式的响应
     */
    JsonData incrementClickCount(Integer id);
    
    /**
     * 创建轮播图
     * @param createRequest 轮播图创建请求
     * @return JsonData格式的响应
     */
    JsonData create(BannerCreateRequest createRequest);
    
    /**
     * 更新轮播图
     * @param id 轮播图ID
     * @param updateRequest 轮播图更新请求
     * @return JsonData格式的响应
     */
    JsonData update(Integer id, BannerUpdateRequest updateRequest);
    
    /**
     * 删除轮播图（物理删除）
     * @param id 轮播图ID
     * @return JsonData格式的响应
     */
    JsonData delete(Integer id);
    
    /**
     * 批量删除轮播图（物理删除）
     * @param ids 轮播图ID列表
     * @return JsonData格式的响应
     */
    JsonData batchDelete(List<Integer> ids);
    
    /**
     * 更新轮播图状态
     * @param id 轮播图ID
     * @param status 状态：0-禁用，1-启用
     * @return JsonData格式的响应
     */
    JsonData updateStatus(Integer id, Integer status);
}
