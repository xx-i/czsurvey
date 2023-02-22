package com.github.czsurvey.project.service.question.type.setting;

import lombok.Data;

/**
 * @author YanYu
 */
@Data
public class SelectSetting {

    private Boolean random;

    @Data
    public static class Option {

        private String id;

        private String label;

        private Boolean fixed;
    }
}
