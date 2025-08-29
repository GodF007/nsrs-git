package com.nsrs.simcard.controller;

import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.model.CommonResult;
import com.nsrs.simcard.model.dto.OrganizationDTO;
import com.nsrs.simcard.model.query.OrganizationQuery;
import com.nsrs.simcard.model.vo.OrganizationTreeVO;
import com.nsrs.simcard.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import java.util.List;

/**
 * Organization Controller
 */
@Tag(name = "Organization Management", description = "Organization CRUD operations and tree structure management")
@RestController
@RequestMapping("/simcard/organization")
public class OrganizationController {
    
    @Autowired
    private OrganizationService organizationService;
    
    /**
     * 分页查询组织列表
     */
    @PostMapping("/page")
    @Operation(summary = "Paginated Query Organization List")
    public CommonResult<PageResult<OrganizationDTO>> page(@Valid @RequestBody PageRequest<OrganizationQuery> request) {
        PageResult<OrganizationDTO> pageResult = organizationService.pageOrganization(request);
        return CommonResult.success(pageResult);
    }
    
    /**
     * Get Organization Details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Organization Details")
    public CommonResult<OrganizationDTO> getDetail(@Parameter(description = "Organization ID") @PathVariable Long id) {
        OrganizationDTO dto = organizationService.getOrganizationDetail(id);
        return dto != null ? CommonResult.success(dto) : CommonResult.failed("Organization not found");
    }
    
    /**
     * Get Organization by Code
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "Get Organization by Code")
    public CommonResult<OrganizationDTO> getByCode(@Parameter(description = "Organization Code") @PathVariable String code) {
        OrganizationDTO dto = organizationService.getOrganizationByCode(code);
        return dto != null ? CommonResult.success(dto) : CommonResult.failed("Organization not found");
    }
    
    /**
     * Get Organization Tree
     */
    @GetMapping("/tree")
    @Operation(summary = "Get Organization Tree")
    public CommonResult<List<OrganizationTreeVO>> getTree(@Parameter(description = "Parent Organization ID") @RequestParam(required = false) Long parentId) {
        // 直接从Service获取树形结构，避免重复构建
        List<OrganizationTreeVO> treeList = organizationService.getOrganizationTreeStructure(parentId);
        return CommonResult.success(treeList);
    }
    
    /**
     * Add Organization
     */
    @PostMapping("/add")
    @Operation(summary = "Add Organization")
    public CommonResult<Void> add(@Valid @RequestBody OrganizationDTO organizationDTO) {
        boolean result = organizationService.addOrganization(organizationDTO);
        return result ? CommonResult.success() : CommonResult.failed("Add failed");
    }
    
    /**
     * Update Organization
     */
    @PutMapping("/update")
    @Operation(summary = "Update Organization")
    public CommonResult<Void> update(@Valid @RequestBody OrganizationDTO organizationDTO) {
        boolean result = organizationService.updateOrganization(organizationDTO);
        return result ? CommonResult.success() : CommonResult.failed("Update failed");
    }
    
    /**
     * Delete Organization
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Organization")
    public CommonResult<Void> delete(@Parameter(description = "Organization ID") @PathVariable Long id) {
        boolean result = organizationService.deleteOrganization(id);
        return result ? CommonResult.success() : CommonResult.failed("Delete failed");
    }
    
    /**
     * Update Organization Status
     */
    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "Update Organization Status")
    public CommonResult<Void> updateStatus(
            @Parameter(description = "Organization ID") @PathVariable Long id, 
            @Parameter(description = "Status: 0-Disabled, 1-Enabled") @PathVariable Integer status) {
        boolean result = organizationService.updateOrganizationStatus(id, status);
        return result ? CommonResult.success() : CommonResult.failed("Update status failed");
    }

}