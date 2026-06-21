package com.plancraft.module.notification.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.plancraft.common.result.R;
import com.plancraft.module.notification.entity.Notification;
import com.plancraft.module.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "通知管理", description = "通知列表、未读数、标记已读、清除")
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 查询通知列表（当前用户）
     */
    @GetMapping("/list")
    public R<IPage<Notification>> list(
            @RequestParam(required = false) Integer isRead,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        Long userId = (Long) authentication.getDetails();
        IPage<Notification> result = notificationService.listNotifications(userId, isRead, page, size);
        return R.ok(result);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public R<Map<String, Long>> unreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        long count = notificationService.getUnreadCount(userId);
        return R.ok(Map.of("count", count));
    }

    /**
     * 标记单条已读
     */
    @PutMapping("/{id}/read")
    public R<Void> markAsRead(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        notificationService.markAsRead(id, userId);
        return R.ok();
    }

    /**
     * 全部标记已读
     */
    @PutMapping("/read-all")
    public R<Void> markAllAsRead(Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        notificationService.markAllAsRead(userId);
        return R.ok();
    }

    /**
     * 清除已读通知
     */
    @DeleteMapping("/clear-read")
    public R<Void> clearRead(Authentication authentication) {
        Long userId = (Long) authentication.getDetails();
        notificationService.clearReadNotifications(userId);
        return R.ok();
    }
}
