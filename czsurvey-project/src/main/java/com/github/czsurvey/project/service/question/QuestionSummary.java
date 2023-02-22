package com.github.czsurvey.project.service.question;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.czsurvey.project.entity.SurveyQuestion;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionSummary {

    private String questionKey;

    private String type;

    private JsonNode description;

    private Boolean required;

    private Integer orderNum;

    private Object setting;

    public QuestionSummary(SurveyQuestion question) {
        this.questionKey = question.getQuestionKey();
        this.type = question.getType();
        this.description = question.getDescription();
        this.required = question.getRequired();
        this.orderNum = question.getOrderNum();
    }
}
