package fun.timu.shop.user.mapper;

import fun.timu.shop.user.model.DO.UserDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhengke
 * @description 针对表【user】的数据库操作Mapper
 * @createDate 2025-07-25 10:19:00
 * @Entity fun.timu.shop.user.model.DO.User
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

}




