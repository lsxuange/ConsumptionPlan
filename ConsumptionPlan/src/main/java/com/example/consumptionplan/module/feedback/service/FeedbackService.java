package com.example.consumptionplan.module.feedback.service;

import com.example.consumptionplan.module.feedback.dto.FeedbackDTO;
import com.example.consumptionplan.module.feedback.entity.Feedback;

import java.util.List;

public interface FeedbackService {

    void submitFeedback(Long userId, FeedbackDTO dto);

    List<Feedback> listMyFeedback(Long userId);
}
