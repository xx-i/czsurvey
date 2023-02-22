package com.github.czsurvey.common.exception;

/**
 * @author YanYu
 */
public class InvalidQuestionSettingException extends BadRequestAlertException {

    public InvalidQuestionSettingException(String message) {
        super(ErrorConstant.INVALID_QUESTION_SETTING, message, "surveyQuestionSetting", "invalid_question_setting");
    }
}
