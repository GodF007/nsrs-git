package com.nsrs.simcard.controller;

import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.model.CommonResult;
import com.nsrs.simcard.model.dto.ImsiGroupDTO;
import com.nsrs.simcard.model.query.ImsiGroupQuery;
import com.nsrs.simcard.service.ImsiGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import java.util.List;

/**
 * IMSI Group Controller
 */
@RestController
@RequestMapping("/simcard/imsi-group")
@Tag(name = "IMSI Group Management")
public class ImsiGroupController {
    
    @Autowired
    private ImsiGroupService imsiGroupService;
    
    /**
     * 分页查询IMSI组列表
     */
    @PostMapping("/page")
    @Operation(summary = "Paginated Query IMSI Group List")
    public CommonResult<PageResult<ImsiGroupDTO>> page(@Valid @RequestBody PageRequest<ImsiGroupQuery> request) {
        PageResult<ImsiGroupDTO> pageResult = imsiGroupService.pageImsiGroup(request);
        return CommonResult.success(pageResult);
    }
    
    /**
     * Get IMSI Group Details
     */
    @GetMapping("/{groupId}")
    @Operation(summary = "Get IMSI Group Details")
    public CommonResult<ImsiGroupDTO> getDetail(@Parameter(description = "Group ID") @PathVariable Long groupId) {
        ImsiGroupDTO groupDTO = imsiGroupService.getImsiGroupDetail(groupId);
        return groupDTO != null ? CommonResult.success(groupDTO) : CommonResult.failed("IMSI group not found");
    }
    
    /**
     * Add IMSI Group
     */
    @PostMapping("/add")
    @Operation(summary = "Add IMSI Group")
    public CommonResult<Void> add(@Valid @RequestBody ImsiGroupDTO groupDTO) {
        return imsiGroupService.addImsiGroup(groupDTO) ? CommonResult.success() : CommonResult.failed("Failed to add IMSI group");
    }
    
    /**
     * Update IMSI Group
     */
    @PutMapping("/update")
    @Operation(summary = "Update IMSI Group")
    public CommonResult<Void> update(@Valid @RequestBody ImsiGroupDTO groupDTO) {
        return imsiGroupService.updateImsiGroup(groupDTO) ? CommonResult.success() : CommonResult.failed("Failed to update IMSI group");
    }
    
    /**
     * Delete IMSI Group
     */
    @DeleteMapping("/{groupId}")
    @Operation(summary = "Delete IMSI Group")
    public CommonResult<Void> delete(@Parameter(description = "Group ID") @PathVariable Long groupId) {
        return imsiGroupService.deleteImsiGroup(groupId) ? CommonResult.success() : CommonResult.failed("Failed to delete IMSI group");
    }
    
    /**
     * Get All IMSI Groups
     */
    @GetMapping("/list")
    @Operation(summary = "Get All IMSI Groups")
    public CommonResult<List<ImsiGroupDTO>> list() {
        List<ImsiGroupDTO> list = imsiGroupService.listAllImsiGroup();
        return CommonResult.success(list);
    }
}