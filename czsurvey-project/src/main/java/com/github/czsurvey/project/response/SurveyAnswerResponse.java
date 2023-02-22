package com.github.czsurvey.project.response;

import com.github.czsurvey.project.entity.SurveyAnswer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAnswerResponse {

    private SurveyAnswer surveyAnswer;

    private UserSummary answerer;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserSummary {

        private String nickName;

        private String realName;

        private String phone;

        private String avatar;
    }
}
