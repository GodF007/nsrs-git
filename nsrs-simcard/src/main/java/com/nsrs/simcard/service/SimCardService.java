package com.nsrs.simcard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.simcard.dto.SimCardBatchOperationRequest;
import com.nsrs.simcard.dto.SimCardDetailDTO;
import com.nsrs.simcard.entity.SimCard;
import com.nsrs.simcard.model.dto.SimCardDTO;
import com.nsrs.simcard.model.query.SimCardQuery;
import com.nsrs.simcard.vo.SimCardVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * SIM Card Service Interface
 */
public interface SimCardService extends IService<SimCard> {
    
    /**
     * Paginated Query SIM Card List
     *
     * @param current Current Page Number
     * @param size Page Size
     * @param iccid ICCID
     * @param imsi IMSI
     * @param status Status
     * @param batchId Batch ID
     * @param orgId Organization ID
     * @return Paginated Result
     */
    PageResult<SimCardDetailDTO> page(long current, long size, String iccid, 
                                    String imsi, Integer status, Long batchId, Long orgId);
    
    /**
     * Get SIM Card Details by ID
     *
     * @param id SIM Card ID
     * @return SIM Card Details
     */
    SimCardDetailDTO getById(Long id);
    
    /**
     * Get SIM Card Details by ICCID
     *
     * @param iccid ICCID
     * @return SIM Card Details
     */
    SimCardDetailDTO getByIccid(String iccid);
    
    /**
     * Batch Import SIM Cards
     *
     * @param simCards SIM Card List
     * @param batchId Batch ID
     * @return Whether Successful
     */
    boolean batchImport(List<SimCard> simCards, Long batchId);
    
    /**
     * Update SIM Card Status
     *
     * @param id SIM Card ID
     * @param status Status
     * @return Whether Successful
     */
    boolean updateStatus(Long id, Integer status);
    
    /**
     * Batch Update SIM Card Status
     *
     * @param ids SIM Card ID List
     * @param status Status
     * @return Whether Successful
     */
    boolean batchUpdateStatus(List<Long> ids, Integer status);
    
    /**
     * Batch Update SIM Card Status by ICCID (Sharding Friendly)
     * 推荐在分表环境下使用此方法
     *
     * @param iccids ICCID List
     * @param status Status
     * @return Whether Successful
     */
    boolean batchUpdateStatusByIccids(List<String> iccids, Integer status);
    
    /**
     * Get SIM Card Count under Specified Organization
     *
     * @param orgId Organization ID
     * @return SIM Card Count
     */
    long countByOrgId(Long orgId);
    
    /**
     * Get SIM Card Count under Specified Batch
     *
     * @param batchId Batch ID
     * @return SIM Card Count
     */
    long countByBatchId(Long batchId);
    
    /**
     * Query SIM Card List under Specified Batch
     *
     * @param batchId Batch ID
     * @return SIM Card List
     */
    List<SimCardDetailDTO> listByBatchId(Long batchId);
    
    /**
     * Update SIM Card Remark
     *
     * @param id SIM Card ID
     * @param remark Remark
     * @return Whether Successful
     */
    boolean updateRemark(Long id, String remark);
    
    /**
     * Paginated Query SIM Cards
     * 
     * @param page Page Number
     * @param size Page Size
     * @param params Query Parameters
     * @return Paginated Result
     */
    PageResult<SimCard> getPage(int page, int size, Map<String, Object> params);
    
    /**
     * Get SIM Card Details
     * 
     * @param cardId Card ID
     * @return SIM Card Details
     */
    SimCardDetailDTO getSimCardDetail(Long cardId);
    
    /**
     * Get SIM Card by ICCID
     * 
     * @param iccid ICCID
     * @return SIM Card
     */
    SimCard getSimCardByIccid(String iccid);
    
    /**
     * Get SIM Card by ID (Sharding Compatible)
     * 【分表查询警告】仅通过ID查询在分表环境下可能存在问题
     * 
     * @param cardId SIM Card ID
     * @return SIM Card
     */
    SimCard getSimCardById(Long cardId);
    
    /**
     * Batch Operation SIM Cards
     * 
     * @param request Batch Operation Request
     * @param userId Operation User ID
     * @return Operation Result
     */
    boolean batchOperation(SimCardBatchOperationRequest request, Long userId);
    

    
    /**
     * Count SIM Card Status
     * 
     * @param params Query Parameters
     * @return Status Statistics
     */
    Map<String, Object> countByStatus(Map<String, Object> params);
    
    /**
     * Paginated Query SIM Card List
     *
     * @param request Query Conditions
     * @return Paginated Result
     */
    PageResult<SimCardDTO> pageCard(PageRequest<SimCardQuery> request);
    

    
    /**
     * Get SIM Card Details by ICCID
     *
     * @param iccid ICCID
     * @return SIM Card Details
     */
    SimCardDTO getCardByIccid(String iccid);
    
    /**
     * Add SIM Card
     *
     * @param cardDTO SIM Card DTO
     * @return Whether Successful
     */
    boolean addCard(SimCardDTO cardDTO);
    
    /**
     * Update SIM Card
     *
     * @param cardDTO SIM Card DTO
     * @return Whether Successful
     */
    boolean updateCard(SimCardDTO cardDTO);
    

    

    

    
    /**
     * Batch Allocate SIM Cards
     *
     * @param cardIds List of SIM Card IDs
     * @param orgId Organization ID
     * @param operatorUserId Operator User ID
     * @return Operation Result
     */
    boolean allocateCards(List<Long> cardIds, Long orgId, Long operatorUserId);

    /**
     * Batch Allocate SIM Cards by ICCIDs (分表友好)
     *
     * @param iccids List of ICCIDs
     * @param orgId Organization ID
     * @param operatorUserId Operator User ID
     * @return Operation Result
     */
    boolean allocateCardsByIccids(List<String> iccids, Long orgId, Long operatorUserId);

    /**
     * Batch Recycle SIM Cards
     *
     * @param cardIds List of SIM Card IDs
     * @param operatorUserId Operator User ID
     * @return Operation Result
     */
    boolean recycleCards(List<Long> cardIds, Long operatorUserId);

    /**
     * Batch Recycle SIM Cards by ICCIDs (分表友好)
     *
     * @param iccids List of ICCIDs
     * @param operatorUserId Operator User ID
     * @return Operation Result
     */
    boolean recycleCardsByIccids(List<String> iccids, Long operatorUserId);
    
    /**
     * Delete SIM Card by ICCID
     *
     * @param iccid ICCID
     * @return Whether Successful
     */
    boolean deleteSimCardByIccid(String iccid);
    
    /**
     * Update SIM Card by ICCID
     *
     * @param iccid ICCID
     * @param simCard SIM Card Entity
     * @return Whether Successful
     */
    boolean updateSimCardByIccid(String iccid, SimCard simCard);
    
    /**
     * Activate SIM Card by ICCID
     *
     * @param iccid ICCID
     * @param operatorUserId Operator User ID
     * @return Whether Successful
     */
    boolean activateCardByIccid(String iccid, Long operatorUserId);
    
    /**
     * Deactivate SIM Card by ICCID
     *
     * @param iccid ICCID
     * @param operatorUserId Operator User ID
     * @return Whether Successful
     */
    boolean deactivateCardByIccid(String iccid, Long operatorUserId);
    
    /**
     * Update SIM Card Status by ICCID
     *
     * @param iccid ICCID
     * @param status Status
     * @return Whether Successful
     */
    boolean updateStatusByIccid(String iccid, Integer status);
    
    /**
     * Count SIM Cards (Grouped by Card Type)
     *
     * @param query Query Conditions
     * @return Statistics Result
     */
    Map<String, Object> countByCardType(SimCardQuery query);
    
    /**
     * Count SIM Cards (Grouped by Organization)
     *
     * @param query Query Conditions
     * @return Statistics Result
     */
    Map<String, Object> countByOrganization(SimCardQuery query);
    
    /**
     * Batch Import SIM Cards from Excel
     *
     * @param simCards SIM Card List
     * @param batchId Batch ID
     * @param operatorUserId Operator User ID
     * @return Import Result
     */
    Map<String, Object> batchImportFromExcel(List<SimCard> simCards, Long batchId, Long operatorUserId);

    /**
     * Query SIM Cards for Export
     *
     * @param params Query Parameters
     * @return SIM Card List for Export
     */
    List<SimCard> queryForExport(Map<String, Object> params);

    /**
     * Query SIM Cards for Export
     *
     * @param queryParams Query Parameters
     * @return SIM Card List for Export
     */
    List<SimCard> queryForExport(SimCardQuery queryParams);
}