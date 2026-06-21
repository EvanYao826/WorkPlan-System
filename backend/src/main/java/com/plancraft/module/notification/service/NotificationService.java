package com.plancraft.module.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plancraft.common.exception.BusinessException;
import com.plancraft.module.notification.entity.Notification;
import com.plancraft.module.notification.mapper.NotificationMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService extends ServiceImpl<NotificationMapper, Notification> {

    /**
     * 发送通知
     */
    public void sendNotification(Long userId, String title, String content,
                                  String type, Long relatedId, String relatedType) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        save(notification);
    }

    /**
     * 分页查询通知列表（按用户 + 已读状态筛选）
     */
    public IPage<Notification> listNotifications(Long userId, Integer isRead,
                                                   int page, int size) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        wrapper.eq(isRead != null, Notification::getIsRead, isRead);
        wrapper.orderByDesc(Notification::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 标记单条已读
     */
    public void markAsRead(Long notificationId, Long userId) {
        boolean updated = lambdaUpdate()
                .eq(Notification::getId, notificationId)
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1)
                .set(Notification::getReadTime, LocalDateTime.now())
                .update();
        if (!updated) {
            throw new BusinessException("通知不存在或已读");
        }
    }

    /**
     * 全部标记已读
     */
    public void markAllAsRead(Long userId) {
        lambdaUpdate()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1)
                .set(Notification::getReadTime, LocalDateTime.now())
                .update();
    }

    /**
     * 获取未读数量
     */
    public long getUnreadCount(Long userId) {
        return count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }

    /**
     * 清除已读通知
     */
    public void clearReadNotifications(Long userId) {
        remove(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 1));
    }
}
