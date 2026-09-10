package com.tourist.controller;

import com.tourist.annotation.CurrentUser;
import com.tourist.annotation.CurrentUserInfo;
import com.tourist.annotation.RequireRole;
import com.tourist.common.PageResult;
import com.tourist.common.Result;
import com.tourist.common.RoleConstants;
import com.tourist.dto.request.EmergencyInfoRequest;
import com.tourist.entity.EmergencyInfo;
import com.tourist.service.EmergencyInfoService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/emergency-info")
public class EmergencyInfoController {

    @Resource
    private EmergencyInfoService emergencyInfoService;

    @PostMapping
    @RequireRole(RoleConstants.PLATFORM_ADMIN)
    public Result<EmergencyInfo> publish(@CurrentUser CurrentUserInfo currentUser,
                                         @Valid @RequestBody EmergencyInfoRequest request) {
        return Result.success(emergencyInfoService.publish(currentUser.getUserId(), request));
    }

    @GetMapping
    @RequireRole(RoleConstants.PLATFORM_ADMIN)
    public Result<PageResult<EmergencyInfo>> adminList(@RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(emergencyInfoService.adminList(page, pageSize));
    }

    @GetMapping("/public")
    @RequireRole(RoleConstants.TOURIST)
    public Result<List<EmergencyInfo>> publicList() {
        return Result.success(emergencyInfoService.publicList());
    }

    @GetMapping("/pending")
    @RequireRole(RoleConstants.APPROVER)
    public Result<List<EmergencyInfo>> pendingList() {
        return Result.success(emergencyInfoService.pendingList());
    }

    @GetMapping("/{id}")
    @RequireRole({RoleConstants.TOURIST, RoleConstants.PLATFORM_ADMIN, RoleConstants.APPROVER})
    public Result<EmergencyInfo> detail(@PathVariable Long id) {
        return Result.success(emergencyInfoService.detail(id));
    }

    @PutMapping("/{id}")
    @RequireRole(RoleConstants.PLATFORM_ADMIN)
    public Result<String> update(@PathVariable Long id, @Valid @RequestBody EmergencyInfoRequest request) {
        emergencyInfoService.update(id, request);
        return Result.success("修改成功");
    }

    @DeleteMapping("/{id}")
    @RequireRole(RoleConstants.PLATFORM_ADMIN)
    public Result<String> delete(@PathVariable Long id) {
        emergencyInfoService.delete(id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/approve")
    @RequireRole(RoleConstants.APPROVER)
    public Result<String> approve(@PathVariable Long id) {
        emergencyInfoService.approve(id);
        return Result.success("审批通过");
    }

    @PutMapping("/{id}/reject")
    @RequireRole(RoleConstants.APPROVER)
    public Result<String> reject(@PathVariable Long id) {
        emergencyInfoService.reject(id);
        return Result.success("审批驳回");
    }
}
