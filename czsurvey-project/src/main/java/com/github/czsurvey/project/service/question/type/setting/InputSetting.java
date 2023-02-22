package com.github.czsurvey.project.service.question.type.setting;

import lombok.Data;

/**
 * @author YanYu
 */
@Data
public class InputSetting {

    private InputType type;

    private Integer maxLength;

    public static enum InputType {
        NUMBER,

        EMAIL,

        CHINESE,

        ENGLISH,

        URL,

        ID_CARD,

        PHONE
    }
}
