package fun.timu.shop.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.timu.shop.common.components.IdGeneratorComponent;
import fun.timu.shop.common.enums.AddressStatusEnum;
import fun.timu.shop.common.enums.BizCodeEnum;
import fun.timu.shop.common.exception.BizException;
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
import org.springframework.util.StringUtils;

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
        // 1. 参数验证
        if (id == null || id <= 0) {
            log.warn("地址ID参数无效: {}", id);
            throw new BizException(BizCodeEnum.ADDRESS_PARAM_ERROR);
        }
        
        // 2. 用户身份验证
        LoginUser loginUser = getCurrentLoginUser();

        // 3. 查询地址并验证权限
        AddressDO addressDO = addressManager.selectOne(id, loginUser.getId());
        if (addressDO == null) {
            log.warn("地址不存在或无权限访问, addressId: {}, userId: {}", id, loginUser.getId());
            return null;
        }
        
        AddressVO addressVO = new AddressVO();
        BeanUtils.copyProperties(addressDO, addressVO);
        return addressVO;
    }

    @Override
    public JsonData add(AddressAddReqeust addressAddReqeust) {
        // 1. 参数验证
        if (addressAddReqeust == null) {
            log.warn("新增地址请求参数为空");
            throw new BizException(BizCodeEnum.ADDRESS_PARAM_ERROR);
        }
        
        // 验证必要字段
        validateAddressRequest(addressAddReqeust);
        
        // 2. 用户身份验证
        LoginUser loginUser = getCurrentLoginUser();
        
        AddressDO addressDO = new AddressDO();
        
        // 生成分布式ID
        Long addressId = idGeneratorComponent.generateId();
        addressDO.setId(addressId);
        
        addressDO.setCreateTime(new Date());
        addressDO.setUserId(loginUser.getId());

        BeanUtils.copyProperties(addressAddReqeust, addressDO);

        // 验证并处理默认地址状态
        Integer defaultStatus = addressDO.getDefaultStatus();
        if (defaultStatus != null && defaultStatus.equals(AddressStatusEnum.DEFAULT_STATUS.getStatus())) {
            //查找数据库是否有默认地址
            AddressDO defaultAddressDO = addressManager.selectOne(loginUser.getId(), AddressStatusEnum.DEFAULT_STATUS.getStatus());

            if (defaultAddressDO != null) {
                //修改为非默认收货地址
                defaultAddressDO.setDefaultStatus(AddressStatusEnum.COMMON_STATUS.getStatus());
                addressManager.update(defaultAddressDO);
            }
        }

        int rows = addressManager.insert(addressDO);

        log.info("新增收货地址成功: userId={}, addressId={}, rows={}", loginUser.getId(), addressId, rows);
        return rows > 0 ? JsonData.buildSuccess() : JsonData.buildResult(BizCodeEnum.ADDRESS_ADD_FAIL);
    }

    @Override
    public JsonData del(Long addressId) {
        // 1. 参数验证
        if (addressId == null || addressId <= 0) {
            log.warn("删除地址ID参数无效: {}", addressId);
            throw new BizException(BizCodeEnum.ADDRESS_PARAM_ERROR);
        }
        
        // 2. 用户身份验证
        LoginUser loginUser = getCurrentLoginUser();
        
        // 3. 执行删除操作
        int row = addressManager.deleteById(addressId, loginUser.getId());
        
        if (row > 0) {
            log.info("删除收货地址成功: userId={}, addressId={}", loginUser.getId(), addressId);
            return JsonData.buildSuccess();
        } else {
            log.warn("删除收货地址失败，地址不存在或无权限: userId={}, addressId={}", loginUser.getId(), addressId);
            return JsonData.buildResult(BizCodeEnum.ADDRESS_NO_EXITS);
        }
    }

    @Override
    public List<AddressVO> listUserAllAddress() {
        // 用户身份验证
        LoginUser loginUser = getCurrentLoginUser();
        
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
    
    /**
     * 获取当前登录用户，并进行身份验证
     * 
     * @return 登录用户信息
     * @throws BizException 如果用户未登录
     */
    private LoginUser getCurrentLoginUser() {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        if (loginUser == null || loginUser.getId() == null) {
            log.warn("用户未登录或登录信息无效");
            throw new BizException(BizCodeEnum.ACCOUNT_UNLOGIN);
        }
        return loginUser;
    }
    
    /**
     * 验证新增地址请求参数
     * 
     * @param request 地址请求参数
     * @throws BizException 如果参数验证失败
     */
    private void validateAddressRequest(AddressAddReqeust request) {
        if (!StringUtils.hasText(request.getReceiveName())) {
            throw new BizException(BizCodeEnum.ADDRESS_PARAM_ERROR);
        }
        if (!StringUtils.hasText(request.getPhone())) {
            throw new BizException(BizCodeEnum.ADDRESS_PARAM_ERROR);
        }
        if (!StringUtils.hasText(request.getProvince())) {
            throw new BizException(BizCodeEnum.ADDRESS_PARAM_ERROR);
        }
        if (!StringUtils.hasText(request.getCity())) {
            throw new BizException(BizCodeEnum.ADDRESS_PARAM_ERROR);
        }
        if (!StringUtils.hasText(request.getRegion())) {
            throw new BizException(BizCodeEnum.ADDRESS_PARAM_ERROR);
        }
        if (!StringUtils.hasText(request.getDetailAddress())) {
            throw new BizException(BizCodeEnum.ADDRESS_PARAM_ERROR);
        }
        
        // 验证默认状态参数
        Integer defaultStatus = request.getDefaultStatus();
        if (defaultStatus != null && 
            !defaultStatus.equals(AddressStatusEnum.DEFAULT_STATUS.getStatus()) && 
            !defaultStatus.equals(AddressStatusEnum.COMMON_STATUS.getStatus())) {
            throw new BizException(BizCodeEnum.ADDRESS_PARAM_ERROR);
        }
    }
}




