package com.jiaoshoujia.controller.system;

import com.jiaoshoujia.common.annotation.Log;
import com.jiaoshoujia.common.core.PageResult;
import com.jiaoshoujia.common.core.R;
import com.jiaoshoujia.common.enums.BusinessType;
import com.jiaoshoujia.generator.domain.GenTable;
import com.jiaoshoujia.generator.service.IGenTableService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tool/gen")
public class GenController {

    private static final int MAX_PAGE_SIZE = 100;

    private final IGenTableService genTableService;

    public GenController(IGenTableService genTableService) {
        this.genTableService = genTableService;
    }

    @PreAuthorize("hasAuthority('tool:gen:list')")
    @GetMapping("/list")
    public R<PageResult<GenTable>> list(GenTable genTable,
                                        @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        List<GenTable> list = genTableService.selectGenTableList(genTable);
        int size = Math.min(pageSize, MAX_PAGE_SIZE);
        int fromIndex = Math.min((Math.max(pageNum, 1) - 1) * size, list.size());
        int toIndex = Math.min(fromIndex + size, list.size());
        return R.ok(PageResult.of(list.size(), list.subList(fromIndex, toIndex)));
    }

    @PreAuthorize("hasAuthority('tool:gen:import')")
    @GetMapping("/db/list")
    public R<List<Map<String, String>>> dbList() {
        return R.ok(genTableService.selectDbTableList());
    }

    @PreAuthorize("hasAuthority('tool:gen:import')")
    @Log(title = "代码生成", businessType = BusinessType.IMPORT)
    @PostMapping("/importTable")
    public R<Void> importTable(@RequestBody Map<String, List<String>> body) {
        List<String> tableNames = body.get("tableNames");
        if (tableNames == null || tableNames.isEmpty()) {
            return R.fail("请选择要导入的表");
        }
        genTableService.importTable(tableNames.toArray(String[]::new));
        return R.ok();
    }

    @PreAuthorize("hasAuthority('tool:gen:preview')")
    @GetMapping("/preview/{tableId}")
    public R<Map<String, String>> preview(@PathVariable(name = "tableId") Long tableId) {
        return R.ok(genTableService.previewCode(tableId));
    }

    @PreAuthorize("hasAuthority('tool:gen:code')")
    @Log(title = "代码生成", businessType = BusinessType.EXPORT)
    @GetMapping("/generate/{tableId}")
    public void generate(HttpServletResponse response, @PathVariable(name = "tableId") Long tableId) throws IOException {
        byte[] data = genTableService.generateCode(tableId);
        String fileName = URLEncoder.encode("generated-code.zip", StandardCharsets.UTF_8);
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        response.getOutputStream().write(data);
    }

    @PreAuthorize("hasAuthority('tool:gen:remove')")
    @Log(title = "代码生成", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tableIds}")
    public R<Void> remove(@PathVariable(name = "tableIds") Long[] tableIds) {
        genTableService.deleteGenTableByIds(tableIds);
        return R.ok();
    }
}
