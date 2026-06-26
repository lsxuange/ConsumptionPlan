package com.example.consumptionplan.module.feedback.controller;

import com.example.consumptionplan.common.Result;
import com.example.consumptionplan.module.feedback.dto.FeedbackDTO;
import com.example.consumptionplan.module.feedback.entity.Feedback;
import com.example.consumptionplan.module.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/feedback")
    public Result<Void> submitFeedback(@RequestBody @Valid FeedbackDTO dto) {
        Long userId = getCurrentUserId();
        feedbackService.submitFeedback(userId, dto);
        return Result.success();
    }

    @GetMapping("/feedback/my")
    public Result<List<Feedback>> listMyFeedback() {
        Long userId = getCurrentUserId();
        return Result.success(feedbackService.listMyFeedback(userId));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
