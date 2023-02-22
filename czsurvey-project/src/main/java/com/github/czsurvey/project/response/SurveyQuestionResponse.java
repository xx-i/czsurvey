package com.github.czsurvey.project.response;

import com.github.czsurvey.project.entity.SurveyQuestion;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SurveyQuestionResponse {

    private SurveyQuestion question;

    private Integer pageOrderNum;

    public SurveyQuestionResponse(SurveyQuestion question, Integer pageOrderNum) {
        this.question = question;
        this.pageOrderNum = pageOrderNum;
    }
}
