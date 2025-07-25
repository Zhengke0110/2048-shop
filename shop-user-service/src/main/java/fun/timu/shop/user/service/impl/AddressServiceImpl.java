package fun.timu.shop.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.timu.shop.common.components.DistributedLockComponent;
import fun.timu.shop.common.components.IdGeneratorComponent;
import fun.timu.shop.common.enums.AddressStatusEnum;
import fun.timu.shop.common.enums.BizCodeEnum;
import fun.timu.shop.common.exception.BizException;
import fun.timu.shop.common.interceptor.LoginInterceptor;
import fun.timu.shop.common.model.LoginUser;
import fun.timu.shop.common.util.DistributedLock;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.user.controller.request.AddressAddReqeust;
import fun.timu.shop.user.manager.AddressManager;
import fun.timu.shop.user.model.DO.AddressDO;
import fun.timu.shop.user.model.VO.AddressVO;
import fun.timu.shop.user.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final DistributedLockComponent distributedLockComponent;

    public AddressServiceImpl(AddressManager addressManager, IdGeneratorComponent idGeneratorComponent, DistributedLockComponent distributedLockComponent) {
        this.addressManager = addressManager;
        this.idGeneratorComponent = idGeneratorComponent;
        this.distributedLockComponent = distributedLockComponent;
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
    @Transactional(rollbackFor = Exception.class)
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

        // 3. 检查用户地址数量限制
        checkUserAddressLimit(loginUser.getId());

        // 4. 使用分布式锁处理默认地址并发问题
        String lockKey = distributedLockComponent.generateDefaultAddressLockKey(loginUser.getId());

        try {
            return distributedLockComponent.executeWithLock(lockKey, () -> {
                return doAddAddress(addressAddReqeust, loginUser);
            });
        } catch (DistributedLock.LockException e) {
            log.error("获取默认地址锁失败, userId: {}", loginUser.getId(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        } catch (Exception e) {
            log.error("新增地址异常, userId: {}", loginUser.getId(), e);
            throw new BizException(BizCodeEnum.ADDRESS_ADD_FAIL);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonData del(Long addressId) {
        // 1. 参数验证
        if (addressId == null || addressId <= 0) {
            log.warn("删除地址ID参数无效: {}", addressId);
            throw new BizException(BizCodeEnum.ADDRESS_PARAM_ERROR);
        }

        // 2. 用户身份验证
        LoginUser loginUser = getCurrentLoginUser();

        // 3. 先查询要删除的地址信息，确认归属权和是否为默认地址
        AddressDO addressToDelete = addressManager.selectOne(addressId, loginUser.getId());
        if (addressToDelete == null) {
            log.warn("地址不存在或无权限访问, addressId: {}, userId: {}", addressId, loginUser.getId());
            return JsonData.buildResult(BizCodeEnum.ADDRESS_NO_EXITS);
        }

        // 4. 如果是默认地址，需要使用分布式锁
        boolean isDefaultAddress = AddressStatusEnum.DEFAULT_STATUS.getStatus() == addressToDelete.getDefaultStatus();
        if (isDefaultAddress) {
            String lockKey = distributedLockComponent.generateDefaultAddressLockKey(loginUser.getId());
            try {
                return distributedLockComponent.executeWithLock(lockKey, () -> {
                    return doDeleteAddress(addressId, loginUser, true);
                });
            } catch (DistributedLock.LockException e) {
                log.error("获取默认地址锁失败, userId: {}, addressId: {}", loginUser.getId(), addressId, e);
                throw new BizException(BizCodeEnum.SYSTEM_ERROR);
            } catch (Exception e) {
                log.error("删除默认地址异常, userId: {}, addressId: {}", loginUser.getId(), addressId, e);
                throw new BizException(BizCodeEnum.ADDRESS_DEL_FAIL);
            }
        } else {
            // 非默认地址直接删除，无需加锁
            return doDeleteAddress(addressId, loginUser, false);
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
     * 执行删除地址的核心逻辑
     *
     * @param addressId        地址ID
     * @param loginUser        登录用户
     * @param isDefaultAddress 是否为默认地址
     * @return 操作结果
     */
    private JsonData doDeleteAddress(Long addressId, LoginUser loginUser, boolean isDefaultAddress) {
        try {
            // 执行删除操作
            int row = addressManager.deleteById(addressId, loginUser.getId());

            if (row > 0) {
                log.info("删除收货地址成功: userId={}, addressId={}, isDefault={}", loginUser.getId(), addressId, isDefaultAddress);

                // 如果删除的是默认地址，可以考虑自动设置一个新的默认地址
                if (isDefaultAddress) {
                    autoSetNewDefaultAddress(loginUser.getId(), addressId);
                }

                return JsonData.buildSuccess();
            } else {
                log.warn("删除收货地址失败，地址不存在或无权限: userId={}, addressId={}", loginUser.getId(), addressId);
                return JsonData.buildResult(BizCodeEnum.ADDRESS_NO_EXITS);
            }
        } catch (Exception e) {
            log.error("删除地址异常, userId: {}, addressId: {}", loginUser.getId(), addressId, e);
            throw new BizException(BizCodeEnum.ADDRESS_DEL_FAIL);
        }
    }


    /**
     * 自动设置新的默认地址（当原默认地址被删除时）
     *
     * @param userId           用户ID
     * @param deletedAddressId 被删除的地址ID
     */
    private void autoSetNewDefaultAddress(Long userId, Long deletedAddressId) {
        try {
            // 查询用户的其他地址
            List<AddressDO> userAddresses = addressManager.selectListByUserId(userId);

            if (userAddresses != null && !userAddresses.isEmpty()) {
                // 找到第一个非被删除的地址，设置为默认地址
                AddressDO newDefaultAddress = userAddresses.stream().filter(addr -> !addr.getId().equals(deletedAddressId)).findFirst().orElse(null);

                if (newDefaultAddress != null) {
                    newDefaultAddress.setDefaultStatus(AddressStatusEnum.DEFAULT_STATUS.getStatus());
                    int updateRows = addressManager.update(newDefaultAddress);
                    log.info("自动设置新默认地址成功: userId={}, newDefaultAddressId={}, updateRows={}", userId, newDefaultAddress.getId(), updateRows);
                }
            }
        } catch (Exception e) {
            // 自动设置默认地址失败不影响删除操作，仅记录日志
            log.warn("自动设置新默认地址失败: userId={}, deletedAddressId={}", userId, deletedAddressId, e);
        }
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
     * 检查用户地址数量限制
     *
     * @param userId 用户ID
     * @throws BizException 如果超出限制
     */
    private void checkUserAddressLimit(Long userId) {
        // 查询用户现有地址数量
        List<AddressDO> existingAddresses = addressManager.selectListByUserId(userId);
        int addressCount = existingAddresses != null ? existingAddresses.size() : 0;

        // 限制用户最多创建20个地址
        final int MAX_ADDRESS_COUNT = 20;
        if (addressCount >= MAX_ADDRESS_COUNT) {
            log.warn("用户地址数量超出限制, userId: {}, count: {}", userId, addressCount);
            throw new BizException(BizCodeEnum.ADDRESS_PARAM_ERROR);
        }
    }

    /**
     * 执行新增地址的核心逻辑（在锁内执行）
     *
     * @param addressAddReqeust 地址请求
     * @param loginUser         登录用户
     * @return 操作结果
     */
    private JsonData doAddAddress(AddressAddReqeust addressAddReqeust, LoginUser loginUser) throws Exception {
        AddressDO addressDO = new AddressDO();

        // 生成分布式ID
        Long addressId = idGeneratorComponent.generateId();
        addressDO.setId(addressId);

        addressDO.setCreateTime(new Date());
        addressDO.setUserId(loginUser.getId());

        BeanUtils.copyProperties(addressAddReqeust, addressDO);

        // 验证并处理默认地址状态（在锁内，避免并发问题）
        Integer defaultStatus = addressDO.getDefaultStatus();
        if (defaultStatus != null && defaultStatus.equals(AddressStatusEnum.DEFAULT_STATUS.getStatus())) {
            // 查找数据库是否有默认地址
            AddressDO defaultAddressDO = addressManager.selectOne(loginUser.getId(), AddressStatusEnum.DEFAULT_STATUS.getStatus());

            if (defaultAddressDO != null) {
                // 修改为非默认收货地址
                defaultAddressDO.setDefaultStatus(AddressStatusEnum.COMMON_STATUS.getStatus());
                int updateRows = addressManager.update(defaultAddressDO);
                log.debug("更新原默认地址状态, userId: {}, oldDefaultAddressId: {}, updateRows: {}", loginUser.getId(), defaultAddressDO.getId(), updateRows);
            }
        }

        int rows = addressManager.insert(addressDO);

        log.info("新增收货地址成功: userId={}, addressId={}, rows={}", loginUser.getId(), addressId, rows);
        return rows > 0 ? JsonData.buildSuccess() : JsonData.buildResult(BizCodeEnum.ADDRESS_ADD_FAIL);
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
        if (defaultStatus != null && !defaultStatus.equals(AddressStatusEnum.DEFAULT_STATUS.getStatus()) && !defaultStatus.equals(AddressStatusEnum.COMMON_STATUS.getStatus())) {
            throw new BizException(BizCodeEnum.ADDRESS_PARAM_ERROR);
        }
    }
}




