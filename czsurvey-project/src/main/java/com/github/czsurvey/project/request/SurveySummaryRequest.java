package com.github.czsurvey.project.request;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author YanYu
 */
@Data
public class SurveySummaryRequest {

    @NotEmpty(message = "问卷标题不能为空")
    private String title;

    private JsonNode instruction;

    private JsonNode conclusion;
}
