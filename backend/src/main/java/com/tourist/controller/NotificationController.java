package com.tourist.controller;

import com.tourist.annotation.CurrentUser;
import com.tourist.annotation.CurrentUserInfo;
import com.tourist.common.PageResult;
import com.tourist.common.Result;
import com.tourist.entity.Notification;
import com.tourist.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public Result<PageResult<Notification>> getMyNotifications(
            @CurrentUser CurrentUserInfo currentUser,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(notificationService.getMyNotifications(currentUser.getUserId(), page, pageSize));
    }

    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(@CurrentUser CurrentUserInfo currentUser) {
        return Result.success(notificationService.getUnreadCount(currentUser.getUserId()));
    }

    @PutMapping("/{id}/read")
    public Result<String> markAsRead(@PathVariable Long id, @CurrentUser CurrentUserInfo currentUser) {
        notificationService.markAsRead(currentUser.getUserId(), id);
        return Result.success("已标记为已读");
    }

    @PutMapping("/read-all")
    public Result<String> markAllAsRead(@CurrentUser CurrentUserInfo currentUser) {
        notificationService.markAllAsRead(currentUser.getUserId());
        return Result.success("全部已读");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteNotification(@PathVariable Long id, @CurrentUser CurrentUserInfo currentUser) {
        notificationService.deleteNotification(currentUser.getUserId(), id);
        return Result.success("已删除");
    }
}
