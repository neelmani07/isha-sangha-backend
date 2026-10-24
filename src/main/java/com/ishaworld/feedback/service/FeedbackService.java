package com.ishaworld.feedback.service;

import com.ishaworld.common.api.PageResponse;
import com.ishaworld.exception.NotFoundException;
import com.ishaworld.feedback.dto.FeedbackRequest;
import com.ishaworld.feedback.dto.FeedbackResponse;
import com.ishaworld.feedback.entity.Feedback;
import com.ishaworld.feedback.mapper.FeedbackMapper;
import com.ishaworld.feedback.repository.FeedbackRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackMapper feedbackMapper;

    public FeedbackService(FeedbackRepository feedbackRepository, FeedbackMapper feedbackMapper) {
        this.feedbackRepository = feedbackRepository;
        this.feedbackMapper = feedbackMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<FeedbackResponse> findAll(Pageable pageable) {
        return PageResponse.from(feedbackRepository.findAll(pageable).map(feedbackMapper::toResponse));
    }

    @Transactional
    public FeedbackResponse submit(FeedbackRequest request) {
        Feedback feedback = new Feedback();
        feedback.setName(request.name());
        feedback.setEmail(request.email());
        feedback.setType(request.type());
        feedback.setMessage(request.message());
        return feedbackMapper.toResponse(feedbackRepository.save(feedback));
    }

    @Transactional
    public void delete(Long id) {
        if (!feedbackRepository.existsById(id)) {
            throw new NotFoundException("Feedback not found");
        }
        feedbackRepository.deleteById(id);
    }
}

