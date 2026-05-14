package com.jiaoshoujia.system.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaoshoujia.common.utils.StringUtils;
import com.jiaoshoujia.system.domain.SysDictData;
import com.jiaoshoujia.system.mapper.SysDictDataMapper;
import com.jiaoshoujia.system.service.ISysDictDataService;
import org.springframework.stereotype.Service;

@Service
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements ISysDictDataService {

    @Override
    public Page<SysDictData> selectDictDataPage(Page<SysDictData> page, SysDictData dictData) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotEmpty(dictData.getDictType()), SysDictData::getDictType, dictData.getDictType())
                .like(StringUtils.isNotEmpty(dictData.getDictLabel()), SysDictData::getDictLabel, dictData.getDictLabel())
                .eq(dictData.getStatus() != null, SysDictData::getStatus, dictData.getStatus())
                .orderByAsc(SysDictData::getDictSort);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public SysDictData selectDictDataById(Long dictCode) {
        return getById(dictCode);
    }

    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        return lambdaQuery()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 0)
                .orderByAsc(SysDictData::getDictSort)
                .list();
    }

    @Override
    public int insertDictData(SysDictData dictData) {
        save(dictData);
        return 1;
    }

    @Override
    public int updateDictData(SysDictData dictData) {
        updateById(dictData);
        return 1;
    }

    @Override
    public int deleteDictDataByIds(Long[] ids) {
        removeBatchByIds(List.of(ids));
        return 1;
    }
}
