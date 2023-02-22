package com.github.czsurvey.project.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SurveyAnswerQueryRequest {

    private Long surveyId;

    private String nickName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Boolean valid;
}
