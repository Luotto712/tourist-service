package com.tourist.service;

import com.tourist.common.PageResult;
import com.tourist.entity.Notification;

public interface NotificationService {

    /**
     * Get paginated notifications for a user.
     */
    PageResult<Notification> getMyNotifications(Long userId, int page, int pageSize);

    /**
     * Get the count of unread notifications for a user.
     */
    int getUnreadCount(Long userId);

    /**
     * Mark a single notification as read.
     */
    void markAsRead(Long userId, Long notificationId);

    /**
     * Mark all notifications as read for a user.
     */
    void markAllAsRead(Long userId);

    /**
     * Send a notification to a specific user.
     */
    void sendNotification(Long userId, String title, String content, String type, Long relatedId);

    void deleteNotification(Long userId, Long notificationId);
}
