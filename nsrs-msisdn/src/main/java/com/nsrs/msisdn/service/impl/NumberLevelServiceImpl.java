package com.nsrs.msisdn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.msisdn.entity.NumberPattern;
import com.nsrs.msisdn.entity.NumberResource;
import com.nsrs.msisdn.entity.NumberLevel;
import com.nsrs.msisdn.mapper.NumberLevelMapper;
import com.nsrs.msisdn.mapper.NumberResourceMapper;
import com.nsrs.msisdn.service.NumberLevelService;
import com.nsrs.msisdn.service.NumberPatternService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 号码级别服务实现类
 */
@Service
public class NumberLevelServiceImpl extends ServiceImpl<NumberLevelMapper, NumberLevel> implements NumberLevelService {

    @Autowired
    private NumberPatternService numberPatternService;
    
    @Autowired
    private NumberResourceMapper numberResourceMapper;

    @Override
    public IPage<NumberLevel> pageList(Page<NumberLevel> page, String levelName, String levelCode, Integer status) {
        LambdaQueryWrapper<NumberLevel> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(levelName)) {
            queryWrapper.like(NumberLevel::getLevelName, levelName);
        }
        if (StringUtils.isNotBlank(levelCode)) {
            queryWrapper.like(NumberLevel::getLevelCode, levelCode);
        }
        if (status != null) {
            queryWrapper.eq(NumberLevel::getStatus, status);
        }
        queryWrapper.orderByAsc(NumberLevel::getLevelCode);
        return page(page, queryWrapper);
    }

    @Override
    public NumberLevel getDetail(Long levelId) {
        return getById(levelId);
    }

    @Override
    public NumberLevel getByLevelCode(String levelCode) {
        if (StringUtils.isBlank(levelCode)) {
            return null;
        }
        LambdaQueryWrapper<NumberLevel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberLevel::getLevelCode, levelCode);
        return getOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(NumberLevel numberLevel) {
        // 检查代码是否已存在
        NumberLevel existLevel = getByLevelCode(numberLevel.getLevelCode());
        if (existLevel != null) {
            throw new RuntimeException("Number level code already exists");
        }
        
        numberLevel.setCreateTime(new Date());
        numberLevel.setUpdateTime(new Date());
        return save(numberLevel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(NumberLevel numberLevel) {
        // 检查代码是否已存在（排除自己）
        LambdaQueryWrapper<NumberLevel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberLevel::getLevelCode, numberLevel.getLevelCode())
                   .ne(NumberLevel::getLevelId, numberLevel.getLevelId());
        if (count(queryWrapper) > 0) {
            throw new RuntimeException("Number level code already exists");
        }
        
        numberLevel.setUpdateTime(new Date());
        return updateById(numberLevel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long levelId) {
        // 检查是否被号码模式关联
        long patternCount = numberPatternService.count(
            new LambdaQueryWrapper<NumberPattern>()
                .eq(NumberPattern::getLevelId, levelId)
        );
        if (patternCount > 0) {
            throw new RuntimeException("Cannot delete number level: it is referenced by " + patternCount + " number pattern(s)");
        }
        
        // 检查是否被号码资源关联
        long resourceCount = numberResourceMapper.selectCount(
            new LambdaQueryWrapper<NumberResource>()
                .eq(NumberResource::getLevelId, levelId)
        );
        if (resourceCount > 0) {
            throw new RuntimeException("Cannot delete number level: it is referenced by " + resourceCount + " number resource(s)");
        }
        
        return removeById(levelId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enable(Long levelId) {
        NumberLevel numberLevel = getById(levelId);
        if (numberLevel == null) {
            return false;
        }
        numberLevel.setStatus(1); // Enable
        numberLevel.setUpdateTime(new Date());
        return updateById(numberLevel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disable(Long levelId) {
        NumberLevel numberLevel = getById(levelId);
        if (numberLevel == null) {
            return false;
        }
        numberLevel.setStatus(0); // Disable
        numberLevel.setUpdateTime(new Date());
        return updateById(numberLevel);
    }

    @Override
    public List<NumberLevel> listAllEnabled() {
        LambdaQueryWrapper<NumberLevel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberLevel::getStatus, 1); // Only query enabled status
        queryWrapper.orderByAsc(NumberLevel::getLevelCode);
        return list(queryWrapper);
    }

    @Override
    public List<NumberLevel> listNeedApproval() {
        LambdaQueryWrapper<NumberLevel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberLevel::getNeedApproval, 1); // Need approval
        queryWrapper.eq(NumberLevel::getStatus, 1); // Enabled status
        queryWrapper.orderByAsc(NumberLevel::getLevelCode);
        return list(queryWrapper);
    }
}