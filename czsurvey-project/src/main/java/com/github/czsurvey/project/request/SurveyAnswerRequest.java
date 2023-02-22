package com.github.czsurvey.project.request;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class SurveyAnswerRequest {

    private Long id;

    @NotNull(message = "问卷ID不能未空")
    private Long surveyId;

    @NotNull(message = "问卷回答不能为空")
    private Map<String, JsonNode> answer;

    @NotNull(message = "问卷开始时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;
}
