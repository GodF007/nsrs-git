package com.nsrs.msisdn.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.msisdn.dto.NumberResourceDTO;
import com.nsrs.msisdn.entity.NumberResource;
import com.nsrs.msisdn.vo.NumberResourceVO;

import java.util.List;
import java.util.Map;

/**
 * 号码资源服务接口
 */
public interface NumberResourceService extends IService<NumberResource> {
    
    /**
     * 分页查询号码资源
     *
     * @param page 分页参数
     * @param dto 查询条件
     * @return 分页结果
     */
    IPage<NumberResourceVO> pageQuery(IPage<NumberResource> page, NumberResourceDTO dto);
    
    /**
     * 查询单个号码资源
     *
     * @param number 号码
     * @return 号码资源
     */
    NumberResourceVO getByNumber(String number);
    
    /**
     * 新增号码资源
     *
     * @param dto 号码资源信息
     * @return 是否成功
     */
    boolean add(NumberResourceDTO dto);
    
    /**
     * 修改号码资源
     *
     * @param number 号码
     * @param dto 号码资源信息
     * @return 是否成功
     */
    boolean update(String number, NumberResourceDTO dto);
    
    /**
     * 删除号码资源
     *
     * @param number 号码
     * @return 是否成功
     */
    boolean delete(String number);
    
    /**
     * 更新号码状态
     *
     * @param number 号码
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(String number, Integer status);
    
    /**
     * 批量更新号码状态
     *
     * @param numbers 号码列表
     * @param status 状态
     * @return 是否成功
     */
    boolean batchUpdateNumberStatusByNumber(List<String> numbers, Integer status);
    
    /**
     * 跨表查询号码资源
     *
     * @param page 分页参数
     * @param dto 查询条件
     * @return 分页结果
     */
    IPage<NumberResourceVO> crossTableQuery(IPage<NumberResource> page, NumberResourceDTO dto);
    
    /**
     * 前缀模糊查询号码资源（指定分表查询，性能优化）
     *
     * @param page 分页参数
     * @param dto 查询条件
     * @return 分页结果
     */
    IPage<NumberResourceVO> prefixQuery(IPage<NumberResource> page, NumberResourceDTO dto);
    
    /**
     * 创建号码分表
     *
     * @param prefix 号码前缀
     * @return 是否成功
     */
    boolean createTable(String prefix);
    
    /**
     * 自动分类号码
     *
     * @param number 号码
     * @return 是否成功
     */
    boolean autoClassify(String number);
    
    /**
     * 批量自动分类号码
     *
     * @param segmentId 号码段ID
     * @return 成功分类数量
     */
    int batchAutoClassify(Long segmentId);
    
    /**
     * 预留号码
     *
     * @param number 号码
     * @param remark 备注
     * @return 是否成功
     */
    boolean reserve(String number, String remark);
    
    /**
     * 分配号码
     *
     * @param number 号码
     * @param attributiveOrg 归属组织
     * @param remark 备注
     * @return 是否成功
     */
    boolean assign(String number, String attributiveOrg, String remark);
    
    /**
     * 激活号码
     *
     * @param number 号码
     * @param iccid SIM卡ICCID
     * @param remark 备注
     * @return 是否成功
     */
    boolean activate(String number, String iccid, String remark);
    
    /**
     * 冻结号码
     *
     * @param number 号码
     * @param remark 备注
     * @return 是否成功
     */
    boolean freeze(String number, String remark);
    
    /**
     * 解冻号码
     *
     * @param number 号码
     * @param remark 备注
     * @return 是否成功
     */
    boolean unfreeze(String number, String remark);
    
    /**
     * 释放号码
     *
     * @param number 号码
     * @param remark 备注
     * @return 是否成功
     */
    boolean release(String number, String remark);
    
    /**
     * 回收号码
     *
     * @param number 号码
     * @param remark 备注
     * @return 是否成功
     */
    boolean recycle(String number, String remark);
    
    /**
     * 批量操作号码
     *
     * @param numbers 号码列表
     * @param operationType 操作类型
     * @param remark 备注
     * @return 成功操作数量
     */
    int batchOperation(List<String> numbers, Integer operationType, String remark);
    
    /**
     * 根据号码获取号码详情
     *
     * @param number 号码
     * @return 号码详情
     */
    NumberResource getBasicInfo(String number);
    
    /**
     * 获取号码统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getStatistics();
    
    /**
     * 查询导出数据
     *
     * @param queryDTO 查询条件
     * @return 数据列表
     */
    List<NumberResource> queryForExport(NumberResourceDTO queryDTO);

    /**
     * 批量导入号码资源
     *
     * @param dataList 数据列表
     * @return 是否成功
     */
    boolean batchImport(List<NumberResource> dataList);
}