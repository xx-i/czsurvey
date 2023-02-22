package com.github.czsurvey.project.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashSet;

/**
 * @author YanYu
 */
@Data
public class SortingSurveyQuestionRequest {

    @NotNull(message = "问卷ID不能为空")
    private Long surveyId;

    @NotEmpty(message = "排序的问题key列表不能为空")
    private LinkedHashSet<String> sortedQuestionKeys;
}
