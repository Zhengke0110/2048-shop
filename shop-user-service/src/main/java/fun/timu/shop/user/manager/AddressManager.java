package fun.timu.shop.user.manager;

import fun.timu.shop.user.model.DO.AddressDO;

import java.util.List;

public interface AddressManager {
    AddressDO selectOne(Long id, Long userId);

    AddressDO selectOne(Long userId, int status);

    int update(AddressDO defaultAddressDO);

    int insert(AddressDO addressDO);

    int deleteById(int addressId, Long userId);

    List<AddressDO> selectListByUserId(Long userId);
}
