package fun.timu.shop.product.service.impl;

import fun.timu.shop.common.enums.BannerStatusEnum;
import fun.timu.shop.common.enums.DelFlagEnum;
import fun.timu.shop.product.manager.BannerManager;
import fun.timu.shop.product.model.DO.BannerDO;
import fun.timu.shop.product.model.VO.BannerVO;
import fun.timu.shop.product.controller.request.BannerCreateRequest;
import fun.timu.shop.product.controller.request.BannerQueryRequest;
import fun.timu.shop.product.controller.request.BannerUpdateRequest;
import fun.timu.shop.product.converter.BannerConverter;

import fun.timu.shop.common.exception.BizException;
import fun.timu.shop.common.enums.BizCodeEnum;
import fun.timu.shop.common.interceptor.LoginInterceptor;
import fun.timu.shop.common.model.LoginUser;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.product.service.BannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhengke
 * @description 针对表【banner(轮播图表)】的数据库操作Service实现
 * @createDate 2025-07-27 16:38:53
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerManager bannerManager;
    private final BannerConverter bannerConverter;

    @Override
    public JsonData list(BannerQueryRequest queryRequest) {
        try {
            List<BannerDO> bannerDOList = bannerManager.listByQuery(queryRequest);
            List<BannerVO> bannerVOList = bannerConverter.convertToVOList(bannerDOList);
            return JsonData.buildSuccess(bannerVOList);
        } catch (BizException e) {
            log.error("查询轮播图列表失败: {}", e.getMessage());
            return JsonData.buildError(e.getMessage());
        } catch (Exception e) {
            log.error("查询轮播图列表异常", e);
            return JsonData.buildError("查询轮播图列表失败");
        }
    }

    @Override
    public JsonData getById(Integer id) {
        try {
            if (id == null || id <= 0) {
                return JsonData.buildError("轮播图ID不能为空或无效");
            }

            BannerDO bannerDO = bannerManager.getByIdNotDeleted(id);
            if (bannerDO == null) {
                return JsonData.buildError("轮播图不存在");
            }

            BannerVO bannerVO = bannerConverter.convertToVO(bannerDO);
            return JsonData.buildSuccess(bannerVO);
        } catch (BizException e) {
            log.error("获取轮播图详情失败: {}", e.getMessage());
            return JsonData.buildError(e.getMessage());
        } catch (Exception e) {
            log.error("获取轮播图详情异常", e);
            return JsonData.buildError("获取轮播图详情失败");
        }
    }

    @Override
    public JsonData incrementClickCount(Integer id) {
        try {
            // 权限校验：点击统计无需特殊权限，但需要登录
            validateLogin();

            if (id == null || id <= 0) {
                return JsonData.buildError("轮播图ID不能为空或无效");
            }

            boolean success = bannerManager.incrementClickCount(id);
            if (success) {
                return JsonData.buildSuccess("点击统计成功");
            } else {
                return JsonData.buildError("点击统计失败，轮播图可能不存在");
            }
        } catch (BizException e) {
            log.error("轮播图点击统计失败: {}", e.getMessage());
            return JsonData.buildError(e.getMessage());
        } catch (Exception e) {
            log.error("轮播图点击统计异常", e);
            return JsonData.buildError("点击统计失败");
        }
    }

    @Override
    public JsonData create(BannerCreateRequest createRequest) {
        try {
            // 权限校验：创建轮播图需要管理员权限
            LoginUser currentUser = validateAdminPermission();

            if (createRequest == null) {
                return JsonData.buildError("轮播图数据不能为空");
            }

            // 将Request转换为DO对象
            BannerDO bannerDO = bannerConverter.convertCreateRequestToDO(createRequest);

            // 设置默认值
            if (bannerDO.getWeight() == null) {
                bannerDO.setWeight(0);
            }
            if (bannerDO.getStatus() == null) {
                bannerDO.setStatus(BannerStatusEnum.ENABLED.getCode());
            }
            if (bannerDO.getClickCount() == null) {
                bannerDO.setClickCount(0);
            }
            if (bannerDO.getDelFlag() == null) {
                bannerDO.setDelFlag(DelFlagEnum.NOT_DELETED.getFlag());
            }

            // 设置创建人信息
            bannerDO.setCreatorId(currentUser.getId());

            log.info("管理员创建轮播图, 操作人: {}, 轮播图标题: {}", currentUser.getName(), bannerDO.getTitle());
            boolean success = bannerManager.save(bannerDO);
            if (success) {
                return JsonData.buildSuccess("创建轮播图成功");
            } else {
                return JsonData.buildError("创建轮播图失败");
            }
        } catch (BizException e) {
            log.error("创建轮播图失败: {}", e.getMessage());
            return JsonData.buildError(e.getMessage());
        } catch (Exception e) {
            log.error("创建轮播图异常", e);
            return JsonData.buildError("创建轮播图失败");
        }
    }

    @Override
    public JsonData update(Integer id, BannerUpdateRequest updateRequest) {
        try {
            // 权限校验：更新轮播图需要管理员权限
            LoginUser currentUser = validateAdminPermission();

            if (id == null || id <= 0) {
                return JsonData.buildError("轮播图ID不能为空或无效");
            }

            if (updateRequest == null) {
                return JsonData.buildError("轮播图数据不能为空");
            }

            // 验证轮播图是否存在
            BannerDO existingBanner = bannerManager.getByIdNotDeleted(id);
            if (existingBanner == null) {
                return JsonData.buildError("轮播图不存在或已被删除");
            }

            // 将Request转换为DO对象
            BannerDO bannerDO = bannerConverter.convertUpdateRequestToDO(id, updateRequest);

            log.info("管理员更新轮播图, 操作人: {}, 轮播图ID: {}", currentUser.getName(), id);
            boolean success = bannerManager.updateById(bannerDO);
            if (success) {
                return JsonData.buildSuccess("更新轮播图成功");
            } else {
                return JsonData.buildError("更新轮播图失败");
            }
        } catch (BizException e) {
            log.error("更新轮播图失败: {}", e.getMessage());
            return JsonData.buildError(e.getMessage());
        } catch (Exception e) {
            log.error("更新轮播图异常", e);
            return JsonData.buildError("更新轮播图失败");
        }
    }

    @Override
    public JsonData delete(Integer id) {
        try {
            // 权限校验：删除轮播图需要管理员权限
            LoginUser currentUser = validateAdminPermission();

            if (id == null || id <= 0) {
                return JsonData.buildError("轮播图ID不能为空或无效");
            }

            // 验证轮播图是否存在
            BannerDO existingBanner = bannerManager.getByIdNotDeleted(id);
            if (existingBanner == null) {
                return JsonData.buildError("轮播图不存在或已被删除");
            }

            log.info("管理员删除轮播图, 操作人: {}, 轮播图ID: {}", currentUser.getName(), id);
            boolean success = bannerManager.logicDeleteById(id);
            if (success) {
                return JsonData.buildSuccess("删除轮播图成功");
            } else {
                return JsonData.buildError("删除轮播图失败");
            }
        } catch (BizException e) {
            log.error("删除轮播图失败: {}", e.getMessage());
            return JsonData.buildError(e.getMessage());
        } catch (Exception e) {
            log.error("删除轮播图异常", e);
            return JsonData.buildError("删除轮播图失败");
        }
    }

    @Override
    public JsonData batchDelete(List<Integer> ids) {
        try {
            // 权限校验：批量删除轮播图需要管理员权限
            LoginUser currentUser = validateAdminPermission();

            if (ids == null || ids.isEmpty()) {
                return JsonData.buildError("轮播图ID列表不能为空");
            }

            log.info("管理员批量删除轮播图, 操作人: {}, 删除数量: {}", currentUser.getName(), ids.size());
            boolean success = bannerManager.logicDeleteBatchByIds(ids);
            if (success) {
                return JsonData.buildSuccess("批量删除轮播图成功");
            } else {
                return JsonData.buildError("批量删除轮播图失败");
            }
        } catch (BizException e) {
            log.error("批量删除轮播图失败: {}", e.getMessage());
            return JsonData.buildError(e.getMessage());
        } catch (Exception e) {
            log.error("批量删除轮播图异常", e);
            return JsonData.buildError("批量删除轮播图失败");
        }
    }

    @Override
    public JsonData updateStatus(Integer id, Integer status) {
        try {
            // 权限校验：更新轮播图状态需要管理员权限
            LoginUser currentUser = validateAdminPermission();

            if (id == null || id <= 0) {
                return JsonData.buildError("轮播图ID不能为空或无效");
            }

            if (!BannerStatusEnum.isValid(status)) {
                return JsonData.buildError("轮播图状态无效");
            }

            // 验证轮播图是否存在
            BannerDO existingBanner = bannerManager.getByIdNotDeleted(id);
            if (existingBanner == null) {
                return JsonData.buildError("轮播图不存在或已被删除");
            }

            BannerStatusEnum bannerStatus = BannerStatusEnum.getByCode(status);
            String statusDesc = bannerStatus != null ? bannerStatus.getDesc() : "未知状态";
            log.info("管理员更新轮播图状态, 操作人: {}, 轮播图ID: {}, 状态: {}",
                    currentUser.getName(), id, statusDesc);

            boolean success = bannerManager.updateStatusById(id, status);
            if (success) {
                return JsonData.buildSuccess(statusDesc + "轮播图成功");
            } else {
                return JsonData.buildError(statusDesc + "轮播图失败");
            }
        } catch (BizException e) {
            log.error("更新轮播图状态失败: {}", e.getMessage());
            return JsonData.buildError(e.getMessage());
        } catch (Exception e) {
            log.error("更新轮播图状态异常", e);
            return JsonData.buildError("更新轮播图状态失败");
        }
    }

    private LoginUser validateLogin() {
        LoginUser currentUser = LoginInterceptor.threadLocal.get();
        if (currentUser == null) {
            throw new BizException(BizCodeEnum.ACCOUNT_UNLOGIN);
        }
        return currentUser;
    }

    private LoginUser validateAdminPermission() {
        LoginUser currentUser = validateLogin();

        if (!isAdmin(currentUser)) {
            log.warn("用户权限不足, userId: {}, userName: {}", currentUser.getId(), currentUser.getName());
            throw new BizException(BizCodeEnum.ACCOUNT_FORBIDDEN);
        }

        return currentUser;
    }

    private boolean isAdmin(LoginUser user) {
        if (user == null) {
            return false;
        }

        // 方案1: 根据用户ID判断（超级管理员）
        if (user.getId().equals(1L)) {
            return true;
        }

        // 方案2: 根据用户名判断
        if ("admin".equals(user.getName()) || user.getName().startsWith("admin_")) {
            return true;
        }

        // 方案3: 根据邮箱域名判断
        if (user.getMail() != null && user.getMail().endsWith("@admin.com")) {
            return true;
        }

        // TODO: 方案4: 从数据库查询用户角色信息（推荐）
        // 可以通过注入UserRoleService来查询用户角色
        // UserRoleDO userRole = userRoleService.getByUserId(user.getId());
        // return userRole != null && "ADMIN".equals(userRole.getRoleCode());

        return false;
    }
}




