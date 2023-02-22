package com.github.czsurvey.common.exception;

import java.net.URI;

public class ErrorConstant {

    public static final String PROBLEM_BASE_URL = "https://www.jhipster.tech/problem";

    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/problemWithMessage");

    public static final URI INVALID_QUESTION_SETTING = URI.create(PROBLEM_BASE_URL + "/invalidQuestionSetting");

    public static final URI INTERNAL_SERVER_ERROR = URI.create(PROBLEM_BASE_URL + "/internalServerError");
}
