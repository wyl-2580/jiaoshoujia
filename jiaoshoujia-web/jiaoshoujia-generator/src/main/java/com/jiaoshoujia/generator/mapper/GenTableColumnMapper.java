package com.jiaoshoujia.generator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiaoshoujia.generator.domain.GenTableColumn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GenTableColumnMapper extends BaseMapper<GenTableColumn> {

    List<GenTableColumn> selectGenTableColumnListByTableId(@Param("tableId") Long tableId);

    int deleteGenTableColumnByTableIds(@Param("tableIds") Long[] tableIds);
}
