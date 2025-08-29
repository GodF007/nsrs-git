package com.nsrs.msisdn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.msisdn.entity.NumberPattern;
import com.nsrs.msisdn.entity.NumberResource;
import com.nsrs.msisdn.mapper.NumberPatternMapper;
import com.nsrs.msisdn.mapper.NumberResourceMapper;
import com.nsrs.msisdn.service.NumberPatternService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 号码模式服务实现类
 */
@Service
public class NumberPatternServiceImpl extends ServiceImpl<NumberPatternMapper, NumberPattern> implements NumberPatternService {

    @Autowired
    private NumberResourceMapper numberResourceMapper;

    @Override
    public IPage<NumberPattern> pageList(Page<NumberPattern> page, String patternName, Long levelId, Integer status) {
        LambdaQueryWrapper<NumberPattern> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(patternName)) {
            queryWrapper.like(NumberPattern::getPatternName, patternName);
        }
        if (levelId != null) {
            queryWrapper.eq(NumberPattern::getLevelId, levelId);
        }
        if (status != null) {
            queryWrapper.eq(NumberPattern::getStatus, status);
        }
        queryWrapper.orderByAsc(NumberPattern::getPatternId);
        return page(page, queryWrapper);
    }

    @Override
    public NumberPattern getDetail(Long patternId) {
        return getById(patternId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(NumberPattern numberPattern) {
        // 验证正则表达式的有效性
        try {
            Pattern.compile(numberPattern.getExpression());
        } catch (Exception e) {
            throw new RuntimeException("Invalid regular expression: " + e.getMessage());
        }
        
        numberPattern.setCreateTime(new Date());
        numberPattern.setUpdateTime(new Date());
        return save(numberPattern);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(NumberPattern numberPattern) {
        // 验证正则表达式的有效性
        try {
            Pattern.compile(numberPattern.getExpression());
        } catch (Exception e) {
            throw new RuntimeException("Invalid regular expression: " + e.getMessage());
        }
        
        numberPattern.setUpdateTime(new Date());
        return updateById(numberPattern);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long patternId) {
        // 检查是否被号码资源关联
        long resourceCount = numberResourceMapper.selectCount(
            new LambdaQueryWrapper<NumberResource>()
                .eq(NumberResource::getPatternId, patternId)
        );
        if (resourceCount > 0) {
            throw new RuntimeException("Cannot delete number pattern: it is referenced by " + resourceCount + " number resource(s)");
        }
        
        return removeById(patternId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enable(Long patternId) {
        NumberPattern numberPattern = getById(patternId);
        if (numberPattern == null) {
            return false;
        }
        numberPattern.setStatus(1); // Enable
        numberPattern.setUpdateTime(new Date());
        return updateById(numberPattern);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disable(Long patternId) {
        NumberPattern numberPattern = getById(patternId);
        if (numberPattern == null) {
            return false;
        }
        numberPattern.setStatus(0); // Disable
        numberPattern.setUpdateTime(new Date());
        return updateById(numberPattern);
    }

    @Override
    public List<NumberPattern> listByLevelId(Long levelId) {
        LambdaQueryWrapper<NumberPattern> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberPattern::getLevelId, levelId);
        queryWrapper.eq(NumberPattern::getStatus, 1); // Only query enabled status
        queryWrapper.orderByAsc(NumberPattern::getPatternId);
        return list(queryWrapper);
    }

    @Override
    public List<NumberPattern> listAllEnabled() {
        LambdaQueryWrapper<NumberPattern> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberPattern::getStatus, 1); // Only query enabled status
        queryWrapper.orderByAsc(NumberPattern::getPatternId);
        return list(queryWrapper);
    }

    @Override
    public boolean validateNumber(String number, Long patternId) {
        if (StringUtils.isBlank(number) || patternId == null) {
            return false;
        }
        
        NumberPattern numberPattern = getById(patternId);
        if (numberPattern == null || StringUtils.isBlank(numberPattern.getExpression())) {
            return false;
        }
        
        try {
            Pattern pattern = Pattern.compile(numberPattern.getExpression());
            return pattern.matcher(number).matches();
        } catch (Exception e) {
            return false;
        }
    }
}