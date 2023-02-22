package com.github.czsurvey.project.entity;

import com.github.czsurvey.project.entity.enums.LogicConditionExpression;
import lombok.Data;

@Data
public class LogicCondition {

    private String questionKey;

    private LogicConditionExpression expression;

    private String optionId;
}
