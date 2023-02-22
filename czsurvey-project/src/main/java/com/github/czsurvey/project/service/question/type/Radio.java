package com.github.czsurvey.project.service.question.type;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.czsurvey.common.exception.InvalidQuestionSettingException;
import com.github.czsurvey.project.entity.SurveyQuestion;
import com.github.czsurvey.project.service.question.InputModeQuestionType;
import com.github.czsurvey.project.service.question.type.result.RadioResult;
import com.github.czsurvey.project.service.question.type.setting.RadioSetting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class Radio implements InputModeQuestionType<RadioSetting, RadioResult> {

    public static final String TYPE = "RADIO";

    @Override
    public Object getStatistics(RadioSetting setting, Map<String, Object> params) {
        return null;
    }

    @Override
    public void validateSetting(RadioSetting setting) {
        if (setting == null) {
            throw new InvalidQuestionSettingException("问题设置不能为空");
        }
        List<RadioSetting.Option> options = setting.getOptions();
        long distinctIdOptionCount = options.stream().map(RadioSetting.Option::getId).distinct().count();
        if (distinctIdOptionCount != options.size()) {
            throw new InvalidQuestionSettingException("选项ID不能重复");
        }
        if (setting.getReference()) {
            if (StrUtil.isEmpty(setting.getRefQuestionKey())) {
                throw new InvalidQuestionSettingException("引用的问题ID不能为空");
            }
        } else {
            if (options.size() == 0) {
                throw new InvalidQuestionSettingException("至少添加一个选项");
            }
        }
    }

    @Override
    public void validateResult(RadioSetting setting, RadioResult result, Map<String, SurveyQuestion> questionMap, Map<String, JsonNode> answerMap) {
        // todo: 校验单选题结果是否合法
    }

    @Override
    public String getQuestionType() {
        return TYPE;
    }
}
