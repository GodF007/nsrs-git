package com.nsrs.simcard.controller;

import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.model.CommonResult;
import com.nsrs.simcard.model.dto.SimCardOperationDTO;
import com.nsrs.simcard.model.query.SimCardOperationQuery;
import com.nsrs.simcard.service.SimCardOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import java.util.List;
import java.util.Map;

/**
 * SIM Card Operation Controller
 */
@Tag(name = "SIM Card Operation Management", description = "SIM card operation record query and management")
@RestController
@RequestMapping("/simcard/operation")
public class SimCardOperationController {
    
    @Autowired
    private SimCardOperationService operationService;
    
    /**
     * Paginated Query SIM Card Operation Record List
     * 
     * @param request Query conditions
     * @return Paginated results
     */
    @PostMapping("/page")
    @Operation(summary = "Paginated Query SIM Card Operation Record List")
    public CommonResult<PageResult<SimCardOperationDTO>> page(@Valid @RequestBody PageRequest<SimCardOperationQuery> request) {
        PageResult<SimCardOperationDTO> pageResult = operationService.pageOperation(request);
        return CommonResult.success(pageResult);
    }
    
    /**
     * Get SIM Card Operation Record Details
     * 
     * @param id Operation ID
     * @return Operation record details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get SIM Card Operation Record Details")
    @Deprecated
    public CommonResult<SimCardOperationDTO> getDetail(@Parameter(description = "Operation Record ID") @PathVariable Long id) {
        SimCardOperationDTO dto = operationService.getOperationDetail(id);
        return dto != null ? CommonResult.success(dto) : CommonResult.failed("Operation record not found");
    }
    
    /**
     * Get Operation Records for Specified SIM Card
     * 
     * @param cardId SIM Card ID
     * @return Operation record list
     */
    @GetMapping("/card/{cardId}")
    @Operation(summary = "Get Operation Records for Specified SIM Card")
    public CommonResult<List<SimCardOperationDTO>> getOperationsByCardId(@Parameter(description = "SIM Card ID") @PathVariable Long cardId) {
        List<SimCardOperationDTO> operationList = operationService.getOperationsByCardId(cardId);
        return CommonResult.success(operationList);
    }
    
    /**
     * Get Operation Records for Specified ICCID
     * 
     * @param iccid ICCID
     * @return Operation record list
     */
    @GetMapping("/iccid/{iccid}")
    @Operation(summary = "Get Operation Records for Specified ICCID")
    public CommonResult<List<SimCardOperationDTO>> getOperationsByIccid(@Parameter(description = "ICCID") @PathVariable String iccid) {
        List<SimCardOperationDTO> operationList = operationService.getOperationsByIccid(iccid);
        return CommonResult.success(operationList);
    }
    
    /**
     * Get Recent Operation Records
     * 
     * @param limit Quantity limit, default is 10
     * @return Operation record list
     */
    @GetMapping("/recent")
    @Operation(summary = "Get Recent Operation Records")
    public CommonResult<List<SimCardOperationDTO>> getRecentOperations(@Parameter(description = "Quantity limit") @RequestParam(defaultValue = "10") int limit) {
        List<SimCardOperationDTO> operationList = operationService.getRecentOperations(limit);
        return CommonResult.success(operationList);
    }
    
    /**
     * Count Operations by Type
     * 
     * @param beginDate Start date
     * @param endDate End date
     * @return Operation type statistics
     */
    @GetMapping("/count/type")
    @Operation(summary = "Count Operations by Type")
    public CommonResult<List<Object>> countByOperationType(
            @Parameter(description = "Start date") @RequestParam(required = false) String beginDate,
            @Parameter(description = "End date") @RequestParam(required = false) String endDate) {
        List<Object> countList = operationService.countByOperationType(beginDate, endDate);
        return CommonResult.success(countList);
    }
    
    /**
     * Count Operation Type Distribution by Conditions
     */
    @PostMapping("/count/type/condition")
    @Operation(summary = "(暂时没用)Count Operation Type Distribution by Conditions")
    @Deprecated
    public CommonResult<Map<String, Object>> countByOperationType(@Valid @RequestBody SimCardOperationQuery query) {
        Map<String, Object> countMap = operationService.countByOperationType(query);
        return CommonResult.success(countMap);
    }
    
    /**
     * Count Operation Result Status Distribution by Conditions
     */
    @PostMapping("/count/status/condition")
    @Operation(summary = "(暂时没用)Count Operation Result Status Distribution by Conditions")
    @Deprecated
    public CommonResult<Map<String, Object>> countByResultStatus(@Valid @RequestBody SimCardOperationQuery query) {
        Map<String, Object> countMap = operationService.countByResultStatus(query);
        return CommonResult.success(countMap);
    }

}