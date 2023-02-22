package com.github.czsurvey.project.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class BatchChangeAnswerValidRequest {

    @NotEmpty
    private List<Long> ids;

    @NotNull
    private Boolean valid;
}
