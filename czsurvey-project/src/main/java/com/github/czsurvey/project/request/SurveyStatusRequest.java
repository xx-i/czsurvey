package com.github.czsurvey.project.request;

import com.github.czsurvey.project.entity.enums.ProjectStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SurveyStatusRequest {

    @NotNull
    private Long surveyId;

    @NotNull
    private ProjectStatus status;
}
