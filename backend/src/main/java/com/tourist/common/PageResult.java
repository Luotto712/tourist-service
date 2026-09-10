package com.tourist.common;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.Collections;
import java.util.List;

public class PageResult<T> {

    private long total;
    private List<T> list;

    public PageResult() {
    }

    public PageResult(long total, List<T> list) {
        this.total = total;
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    /**
     * Create PageResult from total and list.
     */
    public static <T> PageResult<T> of(long total, List<T> list) {
        return new PageResult<>(total, list != null ? list : Collections.emptyList());
    }

    /**
     * Create PageResult from MyBatis-Plus IPage.
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        if (page == null) {
            return new PageResult<>(0, Collections.emptyList());
        }
        return new PageResult<>(page.getTotal(), page.getRecords() != null ? page.getRecords() : Collections.emptyList());
    }

}
