package com.nsrs.msisdn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.msisdn.entity.Region;
import com.nsrs.msisdn.mapper.RegionMapper;
import com.nsrs.msisdn.service.RegionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 区域服务实现类
 */
@Service
public class RegionServiceImpl extends ServiceImpl<RegionMapper, Region> implements RegionService {

    @Override
    public IPage<Region> pageList(Page<Region> page, String regionCode, String regionName, Integer regionType, Integer status) {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(regionCode)) {
            queryWrapper.like(Region::getRegionCode, regionCode);
        }
        if (StringUtils.isNotBlank(regionName)) {
            queryWrapper.like(Region::getRegionName, regionName);
        }
        if (regionType != null) {
            queryWrapper.eq(Region::getRegionType, regionType);
        }
        if (status != null) {
            queryWrapper.eq(Region::getStatus, status);
        }
        queryWrapper.orderByAsc(Region::getRegionCode);
        return page(page, queryWrapper);
    }

    @Override
    public Region getDetail(Long regionId) {
        return getById(regionId);
    }

    @Override
    public Region getByRegionCode(String regionCode) {
        if (StringUtils.isBlank(regionCode)) {
            return null;
        }
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Region::getRegionCode, regionCode);
        return getOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(Region region) {
        region.setCreateTime(new Date());
        region.setUpdateTime(new Date());
        return save(region);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Region region) {
        region.setUpdateTime(new Date());
        return updateById(region);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long regionId) {
        // Check if there are sub-regions
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Region::getParentId, regionId);
        long count = count(queryWrapper);
        if (count > 0) {
            throw new RuntimeException("Cannot delete region with existing sub-regions");
        }
        return removeById(regionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enable(Long regionId) {
        Region region = getById(regionId);
        if (region == null) {
            return false;
        }
        region.setStatus(1); // 1 means enabled
        region.setUpdateTime(new Date());
        return updateById(region);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disable(Long regionId) {
        Region region = getById(regionId);
        if (region == null) {
            return false;
        }
        region.setStatus(0); // 0 means disabled
        region.setUpdateTime(new Date());
        return updateById(region);
    }

    @Override
    public List<Region> listByParentId(Long parentId) {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Region::getParentId, parentId);
        queryWrapper.eq(Region::getStatus, 1); // Only query enabled regions
        queryWrapper.orderByAsc(Region::getRegionCode);
        return list(queryWrapper);
    }

    @Override
    public List<Region> listRegionTree(Long parentId, Integer regionType) {
        // 只查询启用状态的区域
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Region::getStatus, 1); // 只查询启用状态
        queryWrapper.orderByAsc(Region::getRegionCode);
        List<Region> allRegions = list(queryWrapper);
        
        if (allRegions.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Filter by conditions
        List<Region> filteredRegions = allRegions;
        if (regionType != null) {
            filteredRegions = filteredRegions.stream()
                    .filter(region -> region.getRegionType().equals(regionType))
                    .collect(Collectors.toList());
        }
        
        // Build tree structure
        if (parentId == null) {
            // Get all top-level regions
            return buildTree(filteredRegions, null);
        } else {
            // Get sub-region tree for specified parent ID
            return buildTree(filteredRegions, parentId);
        }
    }
    
    /**
     * Build region tree
     *
     * @param regions region list
     * @param parentId parent region ID
     * @return region tree
     */
    private List<Region> buildTree(List<Region> regions, Long parentId) {
        List<Region> tree = new ArrayList<>();
        for (Region region : regions) {
            if (Objects.equals(parentId, region.getParentId())) {
                tree.add(region);
            }
        }
        // Recursively find sub-regions
        for (Region parent : tree) {
            List<Region> children = buildTree(regions, parent.getRegionId());
            parent.setChildren(children);
        }
        return tree;
    }
    
    @Override
    public List<Region> listAllEnabled() {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Region::getStatus, 1); // Only query enabled status
        queryWrapper.orderByAsc(Region::getRegionCode);
        return list(queryWrapper);
    }
}