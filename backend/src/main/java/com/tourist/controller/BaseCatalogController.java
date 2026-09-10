package com.tourist.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourist.annotation.RequireRole;
import com.tourist.common.BusinessException;
import com.tourist.common.PageResult;
import com.tourist.common.Result;
import com.tourist.common.ResultCode;
import com.tourist.common.RoleConstants;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

/**
 * 查询类子系统通用底座：游客只读（page/detail）+ 平台管理员维护（create/update/delete）。
 * 每个查询模块只需提供 {@link #mapper()} 与可选的 {@link #buildWrapper(String)}。
 * 这是「深模块」：CRUD + RBAC + 分页逻辑集中一处，被 5 个查询模块复用。
 */
public abstract class BaseCatalogController<T> {

    protected abstract BaseMapper<T> mapper();

    /** 子类可按关键词搜索（如 name LIKE）；默认无搜索 */
    protected LambdaQueryWrapper<T> buildWrapper(String keyword) {
        return new LambdaQueryWrapper<>();
    }

    @GetMapping
    @RequireRole({RoleConstants.TOURIST, RoleConstants.PLATFORM_ADMIN, RoleConstants.HOTEL_ADMIN, RoleConstants.APPROVER, RoleConstants.COMPLAINT_HANDLER})
    public Result<PageResult<T>> page(@RequestParam(required = false) String keyword,
                                      @RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<T> iPage = mapper().selectPage(new Page<>(page, pageSize), buildWrapper(keyword));
        return Result.success(PageResult.of(iPage));
    }

    @GetMapping("/{id}")
    @RequireRole({RoleConstants.TOURIST, RoleConstants.PLATFORM_ADMIN, RoleConstants.HOTEL_ADMIN, RoleConstants.APPROVER, RoleConstants.COMPLAINT_HANDLER})
    public Result<T> detail(@PathVariable Long id) {
        T entity = mapper().selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资源不存在");
        }
        return Result.success(entity);
    }

    @PostMapping
    @RequireRole(RoleConstants.PLATFORM_ADMIN)
    public Result<T> create(@RequestBody T body) {
        mapper().insert(body);
        return Result.success(body);
    }

    @PutMapping("/{id}")
    @RequireRole(RoleConstants.PLATFORM_ADMIN)
    public Result<String> update(@PathVariable Long id, @RequestBody T body) {
        setId(body, id);
        mapper().updateById(body);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @RequireRole(RoleConstants.PLATFORM_ADMIN)
    public Result<String> delete(@PathVariable Long id) {
        mapper().deleteById(id);
        return Result.success("删除成功");
    }

    private void setId(T body, Long id) {
        try {
            Method method = body.getClass().getMethod("setId", Long.class);
            method.invoke(body, id);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "更新失败");
        }
    }
}
