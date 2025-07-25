package fun.timu.shop.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.timu.shop.common.components.IdGeneratorComponent;
import fun.timu.shop.common.enums.AddressStatusEnum;
import fun.timu.shop.common.enums.BizCodeEnum;
import fun.timu.shop.common.interceptor.LoginInterceptor;
import fun.timu.shop.common.model.LoginUser;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.user.controller.request.AddressAddReqeust;
import fun.timu.shop.user.manager.AddressManager;
import fun.timu.shop.user.mapper.AddressMapper;
import fun.timu.shop.user.model.DO.AddressDO;
import fun.timu.shop.user.model.VO.AddressVO;
import fun.timu.shop.user.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author zhengke
 * @description 针对表【address(电商-公司收发货地址表)】的数据库操作Service实现
 * @createDate 2025-07-25 10:19:00
 */
@Slf4j
@Service
public class AddressServiceImpl implements AddressService {
    private final AddressManager addressManager;
    private final IdGeneratorComponent idGeneratorComponent;

    public AddressServiceImpl(AddressManager addressManager, IdGeneratorComponent idGeneratorComponent) {
        this.addressManager = addressManager;
        this.idGeneratorComponent = idGeneratorComponent;
    }

    @Override
    public AddressVO detail(Long id) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();

        AddressDO addressDO = addressManager.selectOne(id, loginUser.getId());
        if (addressDO == null) {
            return null;
        }
        AddressVO addressVO = new AddressVO();
        BeanUtils.copyProperties(addressDO, addressVO);
        return addressVO;
    }

    @Override
    public JsonData add(AddressAddReqeust addressAddReqeust) {

        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        AddressDO addressDO = new AddressDO();
        
        // 生成分布式ID
        Long addressId = idGeneratorComponent.generateId();
        addressDO.setId(addressId);
        
        addressDO.setCreateTime(new Date());
        addressDO.setUserId(loginUser.getId());

        BeanUtils.copyProperties(addressAddReqeust, addressDO);


        //是否有默认收货地址
        if (addressDO.getDefaultStatus() == AddressStatusEnum.DEFAULT_STATUS.getStatus()) {
            //查找数据库是否有默认地址
            AddressDO defaultAddressDO = addressManager.selectOne(loginUser.getId(), AddressStatusEnum.DEFAULT_STATUS.getStatus());

            if (defaultAddressDO != null) {
                //修改为非默认收货地址
                defaultAddressDO.setDefaultStatus(AddressStatusEnum.COMMON_STATUS.getStatus());
                addressManager.update(defaultAddressDO);
            }
        }

        int rows = addressManager.insert(addressDO);

        log.info("新增收货地址:rows={},data={},生成的地址ID:{}", rows, addressDO, addressId);
        return rows > 0 ? JsonData.buildSuccess() : JsonData.buildResult(BizCodeEnum.ADDRESS_ADD_FAIL);
    }

    @Override
    public JsonData del(int addressId) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        int row = addressManager.deleteById(addressId, loginUser.getId());
        return row > 0 ? JsonData.buildSuccess() : JsonData.buildResult(BizCodeEnum.ADDRESS_DEL_FAIL);
    }

    @Override
    public List<AddressVO> listUserAllAddress() {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        List<AddressDO> addressDOS = addressManager.selectListByUserId(loginUser.getId());
        if (addressDOS == null || addressDOS.isEmpty()) {
            return List.of();
        }
        return addressDOS.stream().map(addressDO -> {
            AddressVO addressVO = new AddressVO();
            BeanUtils.copyProperties(addressDO, addressVO);
            return addressVO;
        }).toList();
    }
}




