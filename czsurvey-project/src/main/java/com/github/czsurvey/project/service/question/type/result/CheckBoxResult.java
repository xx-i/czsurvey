package com.github.czsurvey.project.service.question.type.result;

import lombok.Data;

import java.util.List;

@Data
public class CheckBoxResult {

    private List<String> value;

    private String otherText;
}
