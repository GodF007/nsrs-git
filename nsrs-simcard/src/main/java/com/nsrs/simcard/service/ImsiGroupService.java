package com.nsrs.simcard.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.simcard.entity.ImsiGroup;
import com.nsrs.simcard.model.dto.ImsiGroupDTO;
import com.nsrs.simcard.model.query.ImsiGroupQuery;

import java.util.List;

/**
 * IMSI组服务接口
 */
public interface ImsiGroupService extends IService<ImsiGroup> {
    
    /**
     * 分页查询IMSI组列表
     *
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<ImsiGroupDTO> pageImsiGroup(PageRequest<ImsiGroupQuery> request);
    
    /**
     * 获取IMSI组详情
     *
     * @param groupId 组ID
     * @return IMSI组详情
     */
    ImsiGroupDTO getImsiGroupDetail(Long groupId);
    
    /**
     * 添加IMSI组
     *
     * @param groupDTO IMSI组DTO
     * @return 是否成功
     */
    boolean addImsiGroup(ImsiGroupDTO groupDTO);
    
    /**
     * 修改IMSI组
     *
     * @param groupDTO IMSI组DTO
     * @return 是否成功
     */
    boolean updateImsiGroup(ImsiGroupDTO groupDTO);
    
    /**
     * 删除IMSI组
     *
     * @param groupId 组ID
     * @return 是否成功
     */
    boolean deleteImsiGroup(Long groupId);
    
    /**
     * 获取所有启用的IMSI组
     *
     * @return IMSI组列表
     */
    List<ImsiGroupDTO> listAllImsiGroup();
    
    /**
     * 验证IMSI组是否可用
     *
     * @param groupId 组ID
     * @return 可用则返回IMSI组，否则返回null
     */
    ImsiGroup validateImsiGroup(Long groupId);
    
    /**
     * 更新IMSI组使用计数
     *
     * @param groupId 组ID
     * @param increment 增量
     * @return 是否成功
     */
    boolean updateUsedCount(Long groupId, int increment);
    
    /**
     * 更新IMSI组可用数量
     *
     * @param groupId 组ID
     * @param increment 增量
     * @return 是否成功
     */
    boolean updateAvailableCount(Long groupId, int increment);
}