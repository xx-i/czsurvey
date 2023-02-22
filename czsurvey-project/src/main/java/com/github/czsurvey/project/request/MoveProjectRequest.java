package com.github.czsurvey.project.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MoveProjectRequest {

    @NotNull
    private Long projectId;

    @NotNull
    private Long to;
}
