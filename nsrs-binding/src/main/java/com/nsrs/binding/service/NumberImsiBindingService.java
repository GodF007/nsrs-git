package com.nsrs.binding.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.binding.entity.NumberImsiBinding;
import com.nsrs.binding.query.NumberImsiBindingQuery;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.model.CommonResult;

import java.util.List;
import java.util.Map;

/**
 * 号码与IMSI绑定服务接口
 */
public interface NumberImsiBindingService extends IService<NumberImsiBinding> {

    /**
     * 分页查询绑定关系
     *
     * @param request 分页查询请求
     * @return 分页结果
     */
    PageResult<NumberImsiBinding> page(PageRequest<NumberImsiBindingQuery> request);

    /**
     * 绑定号码和IMSI
     *
     * @param number 号码
     * @param imsi IMSI号码
     * @param iccid ICCID
     * @param orderId 订单ID
     * @param bindingType 绑定类型
     * @param operatorUserId 操作用户ID
     * @param remark 备注
     * @return 绑定结果
     */
    CommonResult<Void> bind(String number, String imsi, String iccid, Long orderId,
                        Integer bindingType, Long operatorUserId, String remark);

    /**
     * 根据号码解绑
     *
     * @param number 号码
     * @param operatorUserId 操作用户ID
     * @param remark 备注
     * @return 解绑结果
     */
    CommonResult<Void> unbindByNumber(String number, Long operatorUserId, String remark);

    /**
     * 批量绑定
     *
     * @param bindingList 绑定列表
     * @param operatorUserId 操作用户ID
     * @return 成功绑定数量
     */
    CommonResult<Integer> batchBind(List<NumberImsiBinding> bindingList, Long operatorUserId);


    
    /**
     * 批量解绑（新版本）
     *
     * @param request 批量解绑请求
     * @return 成功解绑数量
     */
    CommonResult<Integer> batchUnbindV2(com.nsrs.binding.dto.BatchUnbindRequest request);

    /**
     * 根据号码获取绑定关系
     *
     * @param number 号码
     * @return 绑定关系
     */
    NumberImsiBinding getByNumber(String number);

    /**
     * 根据IMSI获取绑定关系
     *
     * @param imsi IMSI号码
     * @return 绑定关系
     */
    NumberImsiBinding getByImsi(String imsi);

    /**
     * 根据订单ID获取绑定关系列表
     *
     * @param orderId 订单ID
     * @return 绑定关系列表
     */
    List<NumberImsiBinding> getByOrderId(Long orderId);

    /**
     * 检查号码是否已绑定
     *
     * @param number 号码
     * @return 是否已绑定
     */
    boolean isNumberBound(String number);



    /**
     * 检查ICCID是否已绑定
     * 通过ICCID判断SIM卡资源是否被绑定，这是正确的业务逻辑
     *
     * @param iccid ICCID
     * @return 是否已绑定
     */
    boolean isIccidBound(String iccid);

    /**
     * 统计绑定数量
     *
     * @param params 查询参数
     * @return 统计结果
     */
    Map<String, Object> countBindings(Map<String, Object> params);

    /**
     * 前缀查询绑定关系（支持分表路由优化）
     *
     * @param page 分页参数
     * @param numberPrefix 号码前缀
     * @param params 其他查询参数
     * @return 分页结果
     */
    PageResult<NumberImsiBinding> prefixQuery(com.baomidou.mybatisplus.core.metadata.IPage<NumberImsiBinding> page, String numberPrefix, Map<String, Object> params);
}