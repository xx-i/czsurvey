package com.github.czsurvey.project.service.question.type;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.czsurvey.project.entity.SurveyQuestion;
import com.github.czsurvey.project.service.question.InputModeQuestionType;
import com.github.czsurvey.project.service.question.type.setting.InputSetting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author YanYu
 */
@Component
@RequiredArgsConstructor
public class Input implements InputModeQuestionType<InputSetting, String>  {

    public static final String TYPE = "INPUT";

    @Override
    public void validateResult(InputSetting setting, String result, Map<String, SurveyQuestion> questionMap, Map<String, JsonNode> answerMap) {
        // todo: 校验输入框结果参数是否合法
    }

    @Override
    public void validateSetting(InputSetting setting) {
        // todo: 校验输入框设置参数是否合法
    }

    @Override
    public Object getStatistics(InputSetting setting, Map<String, Object> params) {
        return null;
    }

    @Override
    public String getQuestionType() {
        return TYPE;
    }
}
