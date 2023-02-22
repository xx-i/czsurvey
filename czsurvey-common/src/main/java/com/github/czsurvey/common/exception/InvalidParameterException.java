package com.github.czsurvey.common.exception;

/**
 * @author YanYu
 */
public class InvalidParameterException extends BadRequestAlertException {

    public InvalidParameterException(String message, String entityName) {
        super(ErrorConstant.INVALID_QUESTION_SETTING, message, entityName, "param_validation_failed");
    }
}
