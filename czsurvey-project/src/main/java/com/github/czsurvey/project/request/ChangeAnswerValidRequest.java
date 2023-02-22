package com.github.czsurvey.project.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ChangeAnswerValidRequest {

    @NotNull
    private Long answerId;

    @NotNull
    private Boolean valid;
}
