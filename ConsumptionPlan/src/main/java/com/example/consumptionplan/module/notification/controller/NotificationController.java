package com.example.consumptionplan.module.notification.controller;

import com.example.consumptionplan.common.Result;
import com.example.consumptionplan.module.notification.entity.Notification;
import com.example.consumptionplan.module.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public Result<List<Notification>> listNotifications() {
        Long userId = getCurrentUserId();
        return Result.success(notificationService.listNotifications(userId));
    }

    @PutMapping("/notifications/read-all")
    public Result<Void> markAllRead() {
        Long userId = getCurrentUserId();
        notificationService.markAllRead(userId);
        return Result.success();
    }

    @GetMapping("/notifications/unread-count")
    public Result<Long> getUnreadCount() {
        Long userId = getCurrentUserId();
        return Result.success(notificationService.getUnreadCount(userId));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
