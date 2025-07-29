package fun.timu.shop.coupon.service;

import fun.timu.shop.common.model.CouponRecordMessage;
import fun.timu.shop.common.util.JsonData;
import fun.timu.shop.coupon.controller.request.LockCouponRecordRequest;
import fun.timu.shop.coupon.model.VO.CouponRecordVO;

import java.util.Map;

/**
 * @author zhengke
 * @description 针对表【coupon_record】的数据库操作Service
 * @createDate 2025-07-26 11:16:47
 */
public interface CouponRecordService {
    /**
     * 分页查询领劵记录
     *
     * @param page
     * @param size
     * @return
     */
    JsonData page(int page, int size);

    /**
     * 根据id查询详情
     *
     * @param recordId
     * @return
     */
    JsonData findById(long recordId);

    /**
     * 锁定优惠券
     *
     * @param recordRequest
     * @return
     */
    JsonData lockCouponRecords(LockCouponRecordRequest recordRequest);


    /**
     * 释放优惠券记录
     *
     * @param recordMessage
     * @return
     */
    boolean releaseCouponRecord(CouponRecordMessage recordMessage);
}
