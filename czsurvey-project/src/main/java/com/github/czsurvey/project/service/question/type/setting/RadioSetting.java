package com.github.czsurvey.project.service.question.type.setting;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RadioSetting implements ReferenceSetting {

    @NotNull(message = "选项列表不能为null")
    private List<@Valid Option> options;

    @NotNull(message = "选项是否随机显示不能为空")
    private Boolean random;

    @NotNull(message = "选项是否引用不能为空")
    private Boolean reference;

    private String refQuestionKey;

    @Override
    public Boolean isReference() {
        return reference;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Option {

        @NotEmpty(message = "选项ID不能为空")
        private String id;

        @NotNull(message = "选项标签不能为空")
        private JsonNode label;

        @NotNull(message = "是否固定不能为空")
        private Boolean fixed;

        @NotNull(message = "是否其它选项不能为空")
        private Boolean otherOption;
    }
}
