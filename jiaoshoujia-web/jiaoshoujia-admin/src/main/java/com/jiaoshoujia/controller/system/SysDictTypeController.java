package com.jiaoshoujia.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jiaoshoujia.common.annotation.Log;
import com.jiaoshoujia.common.core.PageResult;
import com.jiaoshoujia.common.core.R;
import com.jiaoshoujia.common.enums.BusinessType;
import com.jiaoshoujia.system.domain.SysDictType;
import com.jiaoshoujia.system.service.ISysDictTypeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/dict/type")
public class SysDictTypeController {

    private final ISysDictTypeService dictTypeService;

    public SysDictTypeController(ISysDictTypeService dictTypeService) {
        this.dictTypeService = dictTypeService;
    }

    private static final int MAX_PAGE_SIZE = 100;

    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping("/list")
    public R<PageResult<SysDictType>> list(SysDictType dictType,
                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysDictType> page = new Page<>(pageNum, Math.min(pageSize, MAX_PAGE_SIZE));
        Page<SysDictType> result = dictTypeService.selectDictTypePage(page, dictType);
        return R.ok(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @PreAuthorize("hasAuthority('system:dict:query')")
    @GetMapping("/{dictId}")
    public R<SysDictType> getInfo(@PathVariable Long dictId) {
        return R.ok(dictTypeService.selectDictTypeById(dictId));
    }

    @PreAuthorize("hasAuthority('system:dict:add')")
    @Log(title = "字典类型", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@RequestBody SysDictType dictType) {
        return dictTypeService.insertDictType(dictType) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:dict:edit')")
    @Log(title = "字典类型", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody SysDictType dictType) {
        return dictTypeService.updateDictType(dictType) > 0 ? R.ok() : R.fail();
    }

    @PreAuthorize("hasAuthority('system:dict:remove')")
    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{dictIds}")
    public R<Void> remove(@PathVariable Long[] dictIds) {
        return dictTypeService.deleteDictTypeByIds(dictIds) > 0 ? R.ok() : R.fail();
    }

    @GetMapping("/optionselect")
    public R<List<SysDictType>> optionselect() {
        return R.ok(dictTypeService.selectDictTypeAll());
    }
}
