package com.jiaoshoujia.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiaoshoujia.common.annotation.Log;
import com.jiaoshoujia.common.core.PageResult;
import com.jiaoshoujia.common.core.R;
import com.jiaoshoujia.common.enums.BusinessType;
import com.jiaoshoujia.system.domain.SysDictData;
import com.jiaoshoujia.system.service.ISysDictDataService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/dict/data")
public class SysDictDataController {

    private final ISysDictDataService dictDataService;

    public SysDictDataController(ISysDictDataService dictDataService) {
        this.dictDataService = dictDataService;
    }

    private static final int MAX_PAGE_SIZE = 100;

    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping("/list")
    public R<PageResult<SysDictData>> list(SysDictData dictData,
                                            @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<SysDictData> page = new Page<>(pageNum, Math.min(pageSize, MAX_PAGE_SIZE));
        Page<SysDictData> result = dictDataService.selectDictDataPage(page, dictData);
        return R.ok(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @PreAuthorize("hasAuthority('system:dict:query')")
    @GetMapping("/{dictCode}")
    public R<SysDictData> getInfo(@PathVariable(name = "dictCode") Long dictCode) {
        return R.ok(dictDataService.selectDictDataById(dictCode));
    }

    @GetMapping("/type/{dictType}")
    public R<List<SysDictData>> dictType(@PathVariable(name = "dictType") String dictType) {
        return R.ok(dictDataService.selectDictDataByType(dictType));
    }

    @PreAuthorize("hasAuthority('system:dict:add')")
    @Log(title = "字典数据", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@RequestBody SysDictData dictData) {
        return dictDataService.insertDictData(dictData) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:dict:edit')")
    @Log(title = "字典数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody SysDictData dictData) {
        return dictDataService.updateDictData(dictData) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:dict:remove')")
    @Log(title = "字典数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/{dictCodes}")
    public R<Void> remove(@PathVariable(name = "dictCodes") Long[] dictCodes) {
        return dictDataService.deleteDictDataByIds(dictCodes) > 0 ? R.ok() : R.fail();
    }
}
