package fun.timu.shop.user.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import fun.timu.shop.common.enums.AddressStatusEnum;
import fun.timu.shop.common.enums.DelFlagEnum;
import fun.timu.shop.user.manager.AddressManager;
import fun.timu.shop.user.mapper.AddressMapper;
import fun.timu.shop.user.model.DO.AddressDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class AddressManagerImpl implements AddressManager {
    private final AddressMapper addressMapper;

    public AddressManagerImpl(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    @Override
    public AddressDO selectOne(Long id, Long userId) {
        // MyBatis-Plus会自动添加 del_flag = 0 的条件
        QueryWrapper<AddressDO> queryWrapper = new QueryWrapper<AddressDO>()
                .eq("id", id)
                .eq("user_id", userId);
        
        AddressDO addressDO = addressMapper.selectOne(queryWrapper);
        log.debug("查询地址详情: id={}, userId={}, result={}", id, userId, addressDO != null);
        return addressDO;
    }

    @Override
    public AddressDO selectOne(Long userId, int status) {
        // MyBatis-Plus会自动添加 del_flag = 0 的条件
        QueryWrapper<AddressDO> queryWrapper = new QueryWrapper<AddressDO>()
                .eq("user_id", userId)
                .eq("default_status", status);
        
        AddressDO defaultAddressDO = addressMapper.selectOne(queryWrapper);
        log.debug("查询用户默认地址: userId={}, status={}, result={}", userId, status, defaultAddressDO != null);
        return defaultAddressDO;
    }

    @Override
    public int update(AddressDO addressDO) {
        // MyBatis-Plus会自动添加 del_flag = 0 的条件，并自动填充更新时间
        UpdateWrapper<AddressDO> updateWrapper = new UpdateWrapper<AddressDO>()
                .eq("id", addressDO.getId());
        
        int rows = addressMapper.update(addressDO, updateWrapper);
        log.debug("更新地址信息: id={}, rows={}", addressDO.getId(), rows);
        return rows;
    }

    @Override
    public int insert(AddressDO addressDO) {
        // MyBatis-Plus会自动填充创建时间、更新时间和删除标记
        int rows = addressMapper.insert(addressDO);
        log.debug("插入地址记录: id={}, rows={}", addressDO.getId(), rows);
        return rows;
    }

    @Override
    public int deleteById(Long addressId, Long userId) {
        // 使用MyBatis-Plus的逻辑删除功能
        // 它会自动将 del_flag 设置为 1 并更新 update_time
        QueryWrapper<AddressDO> queryWrapper = new QueryWrapper<AddressDO>()
                .eq("id", addressId)
                .eq("user_id", userId);
        
        int rows = addressMapper.delete(queryWrapper);
        log.debug("软删除地址: addressId={}, userId={}, rows={}", addressId, userId, rows);
        return rows;
    }

    @Override
    public List<AddressDO> selectListByUserId(Long userId) {
        // MyBatis-Plus会自动添加 del_flag = 0 的条件
        QueryWrapper<AddressDO> queryWrapper = new QueryWrapper<AddressDO>()
                .eq("user_id", userId)
                .orderByDesc("default_status") // 默认地址排在前面
                .orderByDesc("create_time");   // 创建时间倒序
        
        List<AddressDO> list = addressMapper.selectList(queryWrapper);
        log.debug("查询用户地址列表: userId={}, count={}", userId, list != null ? list.size() : 0);
        return list;
    }
}
