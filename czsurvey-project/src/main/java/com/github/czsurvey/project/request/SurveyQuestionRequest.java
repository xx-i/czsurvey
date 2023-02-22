package com.github.czsurvey.project.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.czsurvey.common.valication.group.Group;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author YanYu
 */
@Data
public class SurveyQuestionRequest {

    @NotNull(message = "问卷ID不能为null", groups = {Group.Create.class, Group.Update.class})
    private Long surveyId;

    @NotBlank(message = "问题key不能为空", groups = {Group.Create.class, Group.Update.class})
    private String questionKey;

    @NotBlank(message = "分页key不能为空", groups = {Group.Create.class})
    private String pageKey;

    private JsonNode title;

    private JsonNode description;

    @NotBlank(message = "问题分类不能为空", groups = {Group.Create.class})
    private String type;

    private Boolean required;

    private JsonNode additionalInfo;

    @NotEmpty(message = "排序后的pageKey列表不能为空", groups = {Group.Create.class})
    private List<String> sortedPageKeys;

    @NotEmpty(message = "排序后的questionKey列表不能为空", groups = {Group.Create.class})
    private List<String> sortedQuestionKeys;
}
