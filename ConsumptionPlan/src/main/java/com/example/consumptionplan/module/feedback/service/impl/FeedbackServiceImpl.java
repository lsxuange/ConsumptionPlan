package com.example.consumptionplan.module.feedback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.consumptionplan.module.feedback.dto.FeedbackDTO;
import com.example.consumptionplan.module.feedback.entity.Feedback;
import com.example.consumptionplan.module.feedback.mapper.FeedbackMapper;
import com.example.consumptionplan.module.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackMapper feedbackMapper;

    @Override
    public void submitFeedback(Long userId, FeedbackDTO dto) {
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setContent(dto.getContent());
        feedback.setStatus(0);
        feedbackMapper.insert(feedback);
    }

    @Override
    public List<Feedback> listMyFeedback(Long userId) {
        return feedbackMapper.selectList(new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getUserId, userId)
                .orderByDesc(Feedback::getCreateTime));
    }
}
