package com.tourist.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourist.common.PageResult;
import com.tourist.entity.OperationLog;
import com.tourist.mapper.OperationLogMapper;
import com.tourist.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Override
    public PageResult<OperationLog> queryLogs(String module, String action, Integer page, Integer pageSize) {
        int pageNum = page != null ? page : 1;
        int pageSizeVal = pageSize != null ? pageSize : 10;

        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (module != null && !module.trim().isEmpty()) {
            wrapper.eq(OperationLog::getModule, module.trim());
        }
        if (action != null && !action.trim().isEmpty()) {
            wrapper.eq(OperationLog::getAction, action.trim());
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);

        IPage<OperationLog> iPage = operationLogMapper.selectPage(new Page<>(pageNum, pageSizeVal), wrapper);
        return PageResult.of(iPage);
    }

    @Override
    public void log(String module, String action, Long targetId, String detail, Long userId, String username, String ip) {
        OperationLog log = new OperationLog();
        log.setModule(module);
        log.setAction(action);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setUserId(userId);
        log.setUsername(username);
        log.setIp(ip);
        log.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(log);
    }
}
