package com.jiaoshoujia.generator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiaoshoujia.generator.domain.GenTable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface GenTableMapper extends BaseMapper<GenTable> {

    GenTable selectGenTableById(@Param("id") Long id);

    List<Map<String, String>> selectDbTableList();

    List<Map<String, String>> selectDbTableColumnsByName(@Param("tableName") String tableName);
}
