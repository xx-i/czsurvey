package com.github.czsurvey.common.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

/**
 * @author YanYu
 */
public class InternalServerErrorException extends AbstractThrowableProblem {

    public InternalServerErrorException(String message) {
        super(ErrorConstant.INTERNAL_SERVER_ERROR, message, Status.INTERNAL_SERVER_ERROR);
    }
}
