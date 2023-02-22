package com.github.czsurvey.project.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SwapQuestionRequest {

    @NotNull
    private Long surveyId;

    @NotBlank
    private String sourceQuestionKey;

    @NotBlank
    private String targetQuestionKey;
}
