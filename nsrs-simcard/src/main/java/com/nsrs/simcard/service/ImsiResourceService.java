package com.nsrs.simcard.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.simcard.entity.ImsiResource;
import com.nsrs.simcard.model.dto.ImsiResourceDTO;
import com.nsrs.simcard.model.query.ImsiResourceQuery;
import com.nsrs.simcard.model.request.ImsiGenerateRequest;

import java.util.List;

/**
 * IMSI资源服务接口
 */
public interface ImsiResourceService extends IService<ImsiResource> {
    
    /**
     * 分页查询IMSI资源列表
     *
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<ImsiResourceDTO> pageImsiResource(PageRequest<ImsiResourceQuery> request);
    
    /**
     * 获取IMSI资源详情
     *
     * @param imsiId IMSI ID
     * @return IMSI资源详情
     */
    ImsiResourceDTO getImsiResourceDetail(Long imsiId);
    
    /**
     * 根据IMSI号码获取IMSI资源详情
     *
     * @param imsi IMSI号码
     * @return IMSI资源详情
     */
    ImsiResourceDTO getImsiResourceByImsi(String imsi);
    
    /**
     * 根据IMSI号码获取资源详情
     *
     * @param imsi IMSI号码
     * @return IMSI资源详情
     */
    ImsiResourceDTO getImsiResourceDetailByImsi(String imsi);
    
    /**
     * 添加IMSI资源
     *
     * @param resourceDTO IMSI资源DTO
     * @return 是否成功
     */
    boolean addImsiResource(ImsiResourceDTO resourceDTO);
    
    /**
     * 修改IMSI资源 (已废弃)
     *
     * @param imsiResourceDTO IMSI资源DTO
     * @return 是否成功
     */
    @Deprecated
    boolean updateImsiResource(ImsiResourceDTO imsiResourceDTO);
    
    /**
     * 根据IMSI号码修改IMSI资源
     *
     * @param imsiResourceDTO IMSI资源DTO
     * @return 是否成功
     */
    boolean updateImsiResourceByImsi(ImsiResourceDTO imsiResourceDTO);
    
    /**
     * 删除IMSI资源 (已废弃)
     *
     * @param imsiId IMSI ID
     * @return 是否成功
     */
    @Deprecated
    boolean deleteImsiResource(Long imsiId);
    
    /**
     * 根据IMSI号码删除资源
     *
     * @param imsi IMSI号码
     * @return 是否成功
     */
    boolean deleteImsiResourceByImsi(String imsi);
    
    /**
     * 更新IMSI资源状态 (已废弃)
     *
     * @param imsiId IMSI ID
     * @param status 状态
     * @return 是否成功
     */
    @Deprecated
    boolean updateImsiStatus(Long imsiId, Integer status);
    
    /**
     * 根据IMSI号码更新状态
     *
     * @param imsi IMSI号码
     * @param status 状态
     * @return 是否成功
     */
    boolean updateImsiStatusByImsi(String imsi, Integer status);
    
    /**
     * 生成IMSI资源
     *
     * @param request 生成请求
     * @return 生成的IMSI资源列表
     */
    List<ImsiResourceDTO> generateImsi(ImsiGenerateRequest request);
    
    /**
     * 批量更新IMSI资源状态 (已废弃)
     *
     * @param imsiIds IMSI ID列表
     * @param status 状态
     * @return 是否成功
     */
    @Deprecated
    boolean batchUpdateImsiStatus(List<Long> imsiIds, Integer status);
    
    /**
     * 根据IMSI号码批量更新状态
     *
     * @param imsiList IMSI号码列表
     * @param status 状态
     * @return 是否成功
     */
    boolean batchUpdateImsiStatusByImsi(List<String> imsiList, Integer status);
    
    /**
     * 根据组ID获取IMSI资源列表
     *
     * @param groupId 组ID
     * @return IMSI资源列表
     */
    List<ImsiResourceDTO> listImsiByGroupId(Long groupId);
    
    /**
     * 获取指定组的可用IMSI数量
     *
     * @param groupId 组ID
     * @return 可用IMSI数量
     */
    int countAvailableImsiByGroupId(Long groupId);

    /**
     * 批量导入IMSI资源
     *
     * @param dataList IMSI资源列表
     * @return 导入结果
     */
    boolean batchImportImsiResource(List<ImsiResource> dataList);

    /**
     * 查询需要导出的IMSI资源数据
     *
     * @param queryParams 查询参数
     * @return IMSI资源列表
     */
    List<ImsiResource> queryImsiResourceForExport(ImsiResourceQuery queryParams);
}