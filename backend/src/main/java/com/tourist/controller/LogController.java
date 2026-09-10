package com.tourist.controller;

import com.tourist.common.PageResult;
import com.tourist.common.Result;
import com.tourist.entity.OperationLog;
import com.tourist.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping
    public Result<PageResult<OperationLog>> queryLogs(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(operationLogService.queryLogs(module, action, page, pageSize));
    }
}
