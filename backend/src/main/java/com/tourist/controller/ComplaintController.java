package com.tourist.controller;

import com.tourist.annotation.CurrentUser;
import com.tourist.annotation.CurrentUserInfo;
import com.tourist.annotation.RequireRole;
import com.tourist.common.PageResult;
import com.tourist.common.Result;
import com.tourist.common.RoleConstants;
import com.tourist.dto.request.ComplaintAssignRequest;
import com.tourist.dto.request.ComplaintProcessRequest;
import com.tourist.dto.request.ComplaintRateRequest;
import com.tourist.dto.request.ComplaintReplyRequest;
import com.tourist.dto.request.ComplaintSubmitRequest;
import com.tourist.dto.response.ComplaintDetailResponse;
import com.tourist.entity.ComplaintReply;
import com.tourist.entity.TouristComplaint;
import com.tourist.service.ComplaintService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    @Resource
    private ComplaintService complaintService;

    @PostMapping
    @RequireRole(RoleConstants.TOURIST)
    public Result<TouristComplaint> submit(@CurrentUser CurrentUserInfo currentUser,
                                           @Valid @RequestBody ComplaintSubmitRequest request) {
        return Result.success(complaintService.submit(currentUser.getUserId(), request));
    }

    @PostMapping("/{id}/reply")
    @RequireRole({RoleConstants.TOURIST, RoleConstants.PLATFORM_ADMIN, RoleConstants.APPROVER, RoleConstants.COMPLAINT_HANDLER})
    public Result<ComplaintReply> reply(@CurrentUser CurrentUserInfo currentUser,
                                        @PathVariable Long id,
                                        @Valid @RequestBody ComplaintReplyRequest request) {
        return Result.success(complaintService.reply(currentUser.getUserId(), id, request));
    }

    @GetMapping("/mine")
    @RequireRole(RoleConstants.TOURIST)
    public Result<PageResult<TouristComplaint>> mine(@CurrentUser CurrentUserInfo currentUser,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(complaintService.myComplaints(currentUser.getUserId(), page, pageSize));
    }

    @GetMapping("/approved")
    @RequireRole(RoleConstants.PLATFORM_ADMIN)
    public Result<List<TouristComplaint>> approved() {
        return Result.success(complaintService.approvedList());
    }

    @GetMapping("/confirmed")
    @RequireRole(RoleConstants.PLATFORM_ADMIN)
    public Result<List<TouristComplaint>> confirmed() {
        return Result.success(complaintService.confirmedList());
    }

    @GetMapping("/pending")
    @RequireRole(RoleConstants.COMPLAINT_HANDLER)
    public Result<PageResult<TouristComplaint>> pending(@CurrentUser CurrentUserInfo currentUser,
                                                        @RequestParam(defaultValue = "1") Integer page,
                                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(complaintService.pendingByHandler(currentUser.getUserId(), page, pageSize));
    }

    @GetMapping("/for-approval")
    @RequireRole(RoleConstants.APPROVER)
    public Result<PageResult<TouristComplaint>> forApproval(@RequestParam(defaultValue = "1") Integer page,
                                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(complaintService.pendingForApproval(page, pageSize));
    }

    @DeleteMapping("/{id}")
    @RequireRole(RoleConstants.TOURIST)
    public Result<String> delete(@CurrentUser CurrentUserInfo currentUser, @PathVariable Long id) {
        complaintService.deleteComplaint(currentUser.getUserId(), id);
        return Result.success("删除成功");
    }

    @GetMapping("/{id}")
    @RequireRole({RoleConstants.TOURIST, RoleConstants.PLATFORM_ADMIN, RoleConstants.APPROVER,
            RoleConstants.COMPLAINT_HANDLER, RoleConstants.HOTEL_ADMIN})
    public Result<ComplaintDetailResponse> detail(@PathVariable Long id) {
        return Result.success(complaintService.detail(id));
    }

    @PutMapping("/{id}/approve")
    @RequireRole(RoleConstants.APPROVER)
    public Result<String> approve(@CurrentUser CurrentUserInfo currentUser,
                                  @PathVariable Long id,
                                  @RequestBody(required = false) Map<String, String> body) {
        complaintService.approve(currentUser.getUserId(), id, comment(body));
        return Result.success("审批通过");
    }

    @PutMapping("/{id}/reject")
    @RequireRole(RoleConstants.APPROVER)
    public Result<String> reject(@CurrentUser CurrentUserInfo currentUser,
                                 @PathVariable Long id,
                                 @RequestBody(required = false) Map<String, String> body) {
        complaintService.reject(currentUser.getUserId(), id, comment(body));
        return Result.success("审批驳回");
    }

    @PutMapping("/{id}/assign")
    @RequireRole(RoleConstants.PLATFORM_ADMIN)
    public Result<String> assign(@PathVariable Long id,
                                 @Valid @RequestBody ComplaintAssignRequest request) {
        complaintService.assign(id, request.getHandlerId());
        return Result.success("分派成功");
    }

    @PostMapping("/{id}/process")
    @RequireRole(RoleConstants.COMPLAINT_HANDLER)
    public Result<ComplaintReply> process(@CurrentUser CurrentUserInfo currentUser,
                                          @PathVariable Long id,
                                          @Valid @RequestBody ComplaintProcessRequest request) {
        return Result.success(complaintService.process(currentUser.getUserId(), id, request.getResult()));
    }

    @PutMapping("/{id}/confirm")
    @RequireRole(RoleConstants.TOURIST)
    public Result<String> confirm(@CurrentUser CurrentUserInfo currentUser, @PathVariable Long id) {
        complaintService.confirm(currentUser.getUserId(), id);
        return Result.success("已确认处理意见");
    }

    @PutMapping("/{id}/close")
    @RequireRole(RoleConstants.PLATFORM_ADMIN)
    public Result<String> close(@PathVariable Long id) {
        complaintService.close(id);
        return Result.success("已结案");
    }

    @PostMapping("/{id}/rate")
    @RequireRole(RoleConstants.TOURIST)
    public Result<String> rate(@CurrentUser CurrentUserInfo currentUser,
                               @PathVariable Long id,
                               @Valid @RequestBody ComplaintRateRequest request) {
        complaintService.rate(currentUser.getUserId(), id, request.getRating());
        return Result.success("评价成功");
    }

    private String comment(Map<String, String> body) {
        return body == null ? null : body.get("comment");
    }
}
