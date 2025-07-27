package fun.timu.shop.product.manager;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.timu.shop.product.model.DO.BannerDO;
import fun.timu.shop.product.controller.request.BannerQueryRequest;

import java.util.List;

public interface BannerManager extends IService<BannerDO> {
    
    /**
     * 根据查询条件获取轮播图列表
     * @param queryRequest 查询条件
     * @return 轮播图列表
     */
    List<BannerDO> listByQuery(BannerQueryRequest queryRequest);
    
    /**
     * 根据ID获取未删除的轮播图
     * @param id 轮播图ID
     * @return 轮播图信息
     */
    BannerDO getByIdNotDeleted(Integer id);
    
    /**
     * 增加点击统计
     * @param id 轮播图ID
     * @return 是否成功
     */
    boolean incrementClickCount(Integer id);
    
    /**
     * 逻辑删除轮播图
     * @param id 轮播图ID
     * @return 是否成功
     */
    boolean logicDeleteById(Integer id);
    
    /**
     * 批量逻辑删除轮播图
     * @param ids 轮播图ID列表
     * @return 是否成功
     */
    boolean logicDeleteBatchByIds(List<Integer> ids);
    
    /**
     * 更新轮播图状态
     * @param id 轮播图ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatusById(Integer id, Integer status);
}
