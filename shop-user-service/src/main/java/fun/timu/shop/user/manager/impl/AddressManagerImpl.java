package fun.timu.shop.user.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import fun.timu.shop.common.enums.AddressStatusEnum;
import fun.timu.shop.user.manager.AddressManager;
import fun.timu.shop.user.mapper.AddressMapper;
import fun.timu.shop.user.model.DO.AddressDO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AddressManagerImpl implements AddressManager {
    private final AddressMapper addressMapper;

    public AddressManagerImpl(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    @Override
    public AddressDO selectOne(Long id, Long userId) {
        AddressDO addressDO = addressMapper.selectOne(new QueryWrapper<AddressDO>().eq("id", id).eq("user_id", userId));
        return addressDO;
    }

    @Override
    public AddressDO selectOne(Long userId, int status) {
        AddressDO defaultAddressDO = addressMapper.selectOne(new QueryWrapper<AddressDO>()
                .eq("user_id", userId)
                .eq("default_status", AddressStatusEnum.DEFAULT_STATUS.getStatus()));

        return defaultAddressDO;
    }

    @Override
    public int update(AddressDO defaultAddressDO) {
        return addressMapper.update(defaultAddressDO, new QueryWrapper<AddressDO>().eq("id", defaultAddressDO.getId()));
    }

    @Override
    public int insert(AddressDO addressDO) {
        return addressMapper.insert(addressDO);
    }

    @Override
    public int deleteById(Long addressId, Long userId) {
        addressMapper.delete(new QueryWrapper<AddressDO>().eq("id", addressId).eq("user_id", userId));
        return 0;
    }

    @Override
    public List<AddressDO> selectListByUserId(Long userId) {
        List<AddressDO> list = addressMapper.selectList(new QueryWrapper<AddressDO>().eq("user_id", userId));
        return list;
    }
}
