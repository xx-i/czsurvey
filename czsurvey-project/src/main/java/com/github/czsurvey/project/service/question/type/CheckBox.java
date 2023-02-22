package com.github.czsurvey.project.service.question.type;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.czsurvey.common.exception.InvalidQuestionSettingException;
import com.github.czsurvey.project.entity.SurveyQuestion;
import com.github.czsurvey.project.service.question.InputModeQuestionType;
import com.github.czsurvey.project.service.question.type.result.CheckBoxResult;
import com.github.czsurvey.project.service.question.type.setting.CheckBoxSetting;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author YanYu
 */
@Component
public class CheckBox implements InputModeQuestionType<CheckBoxSetting, CheckBoxResult> {

    public static final String TYPE = "CHECKBOX";

    @Override
    public void validateSetting(CheckBoxSetting setting) {
        if (setting == null) {
            throw new InvalidQuestionSettingException("问题设置不能为空");
        }
        List<CheckBoxSetting.Option> options = setting.getOptions();
        long distinctIdOptionCount = options.stream().map(CheckBoxSetting.Option::getId).distinct().count();
        if (distinctIdOptionCount != options.size()) {
            throw new InvalidQuestionSettingException("选项ID不能重复");
        }
        if (setting.getMinLength() != null) {
            if (setting.getMinLength() < 1) {
                throw new InvalidQuestionSettingException("最少选择数量不能小于1");
            }
            if (setting.getMinLength() > options.size()) {
                throw new InvalidQuestionSettingException("最少选择数量不能大于选项总数");
            }
        }
        if (setting.getMaxLength() != null) {
            if (setting.getMaxLength() < 1) {
                throw new InvalidQuestionSettingException("最多选择数量不能小于1");
            }
            if (setting.getMaxLength() > options.size()) {
                throw new InvalidQuestionSettingException("最多选择数量不能大于选项总数");
            }
        }
        if (
            setting.getMaxLength() != null
            && setting.getMinLength() != null
            && setting.getMinLength() > setting.getMaxLength()
        ) {
            throw new InvalidQuestionSettingException("最少选择数量不能大于最大选择数量");
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
    public void validateResult(CheckBoxSetting setting, CheckBoxResult result, Map<String, SurveyQuestion> questionMap, Map<String, JsonNode> answerMap) {
        // todo: 校验多选题结果是否合法
    }

    @Override
    public Object getStatistics(CheckBoxSetting setting, Map<String, Object> params) {
        return null;
    }

    @Override
    public String getQuestionType() {
        return TYPE;
    }
}
