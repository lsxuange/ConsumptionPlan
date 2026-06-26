package com.example.consumptionplan.module.notification.service;

import com.example.consumptionplan.module.notification.entity.Notification;

import java.util.List;

public interface NotificationService {

    void send(Long userId, String title, String content);

    List<Notification> listNotifications(Long userId);

    void markAllRead(Long userId);

    Long getUnreadCount(Long userId);
}
