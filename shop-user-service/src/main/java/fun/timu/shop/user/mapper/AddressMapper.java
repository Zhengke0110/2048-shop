package fun.timu.shop.user.mapper;

import fun.timu.shop.user.model.DO.AddressDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhengke
 * @description 针对表【address(电商-公司收发货地址表)】的数据库操作Mapper
 * @createDate 2025-07-25 10:19:00
 * @Entity fun.timu.shop.user.model.DO.Address
 */
@Mapper
public interface AddressMapper extends BaseMapper<AddressDO> {

}




