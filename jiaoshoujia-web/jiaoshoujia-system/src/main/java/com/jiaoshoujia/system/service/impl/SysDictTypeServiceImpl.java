package com.jiaoshoujia.system.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaoshoujia.common.utils.StringUtils;
import com.jiaoshoujia.system.domain.SysDictData;
import com.jiaoshoujia.system.domain.SysDictType;
import com.jiaoshoujia.system.mapper.SysDictDataMapper;
import com.jiaoshoujia.system.mapper.SysDictTypeMapper;
import com.jiaoshoujia.system.service.ISysDictTypeService;
import org.springframework.stereotype.Service;

@Service
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements ISysDictTypeService {

    private final SysDictDataMapper dictDataMapper;

    public SysDictTypeServiceImpl(SysDictDataMapper dictDataMapper) {
        this.dictDataMapper = dictDataMapper;
    }

    @Override
    public Page<SysDictType> selectDictTypePage(Page<SysDictType> page, SysDictType dictType) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(dictType.getDictName()), SysDictType::getDictName, dictType.getDictName())
                .like(StringUtils.isNotEmpty(dictType.getDictType()), SysDictType::getDictType, dictType.getDictType())
                .eq(dictType.getStatus() != null, SysDictType::getStatus, dictType.getStatus())
                .ge(StringUtils.isNotEmpty(dictType.getBeginTime()), SysDictType::getCreateTime, dictType.getBeginTime())
                .le(StringUtils.isNotEmpty(dictType.getEndTime()), SysDictType::getCreateTime, endOfDay(dictType.getEndTime()))
                .orderByDesc(SysDictType::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    private String endOfDay(String endTime) {
        if (StringUtils.isEmpty(endTime) || endTime.length() > 10) {
            return endTime;
        }
        return endTime + " 23:59:59";
    }

    @Override
    public SysDictType selectDictTypeById(Long dictId) {
        return getById(dictId);
    }

    @Override
    public List<SysDictType> selectDictTypeAll() {
        return list();
    }

    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        return dictDataMapper.selectList(
                new LambdaQueryWrapper<SysDictData>()
                        .eq(SysDictData::getDictType, dictType)
                        .eq(SysDictData::getStatus, 0)
                        .orderByAsc(SysDictData::getDictSort));
    }

    @Override
    public int insertDictType(SysDictType dictType) {
        save(dictType);
        return 1;
    }

    @Override
    public int updateDictType(SysDictType dictType) {
        updateById(dictType);
        return 1;
    }

    @Override
    public int deleteDictTypeByIds(Long[] ids) {
        removeBatchByIds(List.of(ids));
        return 1;
    }
}
