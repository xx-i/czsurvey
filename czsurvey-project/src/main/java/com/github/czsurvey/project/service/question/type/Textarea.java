package com.github.czsurvey.project.service.question.type;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.czsurvey.project.entity.SurveyQuestion;
import com.github.czsurvey.project.service.question.InputModeQuestionType;
import com.github.czsurvey.project.service.question.type.setting.TextareaSetting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author YanYu
 */
@Component
@RequiredArgsConstructor
public class Textarea implements InputModeQuestionType<TextareaSetting, String> {

    public static final String TYPE = "TEXTAREA";

    @Override
    public void validateSetting(TextareaSetting setting) {
        // todo: 校验文本框设置参数是否合法
    }

    @Override
    public void validateResult(TextareaSetting setting, String result, Map<String, SurveyQuestion> questionMap, Map<String, JsonNode> answerMap) {
        // todo: 校验文本框结果参数是否合法
    }

    @Override
    public Object getStatistics(TextareaSetting setting, Map<String, Object> params) {
        return null;
    }

    @Override
    public String getQuestionType() {
        return TYPE;
    }
}
