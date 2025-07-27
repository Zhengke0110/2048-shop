package fun.timu.shop.product.mapper;

import fun.timu.shop.product.model.DO.BannerDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author zhengke
 * @description 针对表【banner(轮播图表)】的数据库操作Mapper
 * @createDate 2025-07-27 16:38:53
 * @Entity fun.timu.shop.product.model.DO.Banner
 */
public interface BannerMapper extends BaseMapper<BannerDO> {

    /**
     * 根据位置获取启用的轮播图列表（按权重排序）
     *
     * @param position 位置
     * @return 轮播图列表
     */
    List<BannerDO> selectByPosition(@Param("position") String position);

    /**
     * 获取有效时间内的轮播图
     *
     * @param position 位置
     * @return 轮播图列表
     */
    List<BannerDO> selectActiveByPosition(@Param("position") String position);

    /**
     * 增加点击统计
     *
     * @param id 轮播图ID
     * @return 影响行数
     */
    int incrementClickCount(@Param("id") Integer id);

    /**
     * 批量更新状态
     *
     * @param ids    ID列表
     * @param status 状态
     * @return 影响行数
     */
    int batchUpdateStatus(@Param("ids") List<Integer> ids, @Param("status") Integer status);
}




