package com.github.czsurvey.project.service.question;

import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

/**
 * 题型
 * @param <T> 题型设置
 */
@Validated
public interface QuestionType<T> {

    /**
     * 校验题型设置
     * @param setting 题型设置
     */
    default void validateSetting(@Valid T setting) {}

    /**
     * 获取题型唯一标识
     * @return 题型标识
     */
    String getQuestionType();
}
