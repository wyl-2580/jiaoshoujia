package com.jiaoshoujia.common.core;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private long total;

    private List<T> rows;

    public PageResult() {
    }

    public PageResult(long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }

    public static <T> PageResult<T> of(long total, List<T> rows) {
        return new PageResult<>(total, rows);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }
}
