package com.tourist.service;

import com.tourist.common.PageResult;
import com.tourist.entity.OperationLog;

public interface OperationLogService {

    /**
     * Query operation logs with optional module and action filters.
     */
    PageResult<OperationLog> queryLogs(String module, String action, Integer page, Integer pageSize);

    /**
     * Record an operation log entry.
     */
    void log(String module, String action, Long targetId, String detail, Long userId, String username, String ip);
}
