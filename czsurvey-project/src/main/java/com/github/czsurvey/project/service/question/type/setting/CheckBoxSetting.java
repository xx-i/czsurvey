package com.github.czsurvey.project.service.question.type.setting;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author YanYu
 */
@Data
public class CheckBoxSetting {

    @NotEmpty(message = "至少添加一个选项")
    private List<@Valid Option> options;

    @NotNull(message = "选项是否随机显示不能为空")
    private Boolean random;

    private Integer minLength;

    private Integer maxLength;

    @NotNull(message = "选项是否引用不能为空")
    private Boolean reference;

    private String refQuestionKey;

    @Data
    public static class Option {

        @NotEmpty(message = "选项ID不能为空")
        private String id;

        @NotNull(message = "选项标签不能为空")
        private JsonNode label;

        @NotNull(message = "是否固定不能为空")
        private Boolean fixed;

        @NotNull(message = "是否其它选项不能为空")
        private Boolean otherOption;

        @NotNull(message = "是否为互斥选项不能为空")
        private Boolean exclusive;
    }
}
