package com.github.czsurvey.project.service.question;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.czsurvey.project.entity.SurveyQuestion;

import java.util.Map;

/**
 * 需要用户填写答案的题型
 * @param <T> 题目设置
 * @param <R> 题目填写结果
 */
public interface InputModeQuestionType<T, R> extends QuestionType<T> {

    /**
     * 获取统计结果
     * @param params 参数
     * @param setting 题目设置
     * @return 统计结果
     */
    Object getStatistics(T setting, Map<String, Object> params);

    /**
     * 校验结果是否满足题目设置
     * @param setting 题目设置
     * @param result 题目填写结果
     * @param questionMap 问题Map
     * @param answerMap 回答Map
     */
    default void validateResult(
        T setting,
        R result,
        Map<String, SurveyQuestion> questionMap,
        Map<String, JsonNode> answerMap
    ) {

    }

    /**
     * 题目结果与excel导出列的映射
     * @param setting 题目设置
     * @param result 题目填写结果
     * @return excel导出的结果
     */
    default String exportResult(T setting, R result) {
        return result.toString();
    }
}

