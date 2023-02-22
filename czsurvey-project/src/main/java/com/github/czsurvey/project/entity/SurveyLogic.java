package com.github.czsurvey.project.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.czsurvey.extra.data.converter.JsonNodeConverter;
import com.github.czsurvey.project.entity.audit.DateAudit;
import com.github.czsurvey.project.entity.enums.LogicExpression;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author YanYu
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "t_survey_logic")
public class SurveyLogic extends DateAudit {

    @Id
    @GenericGenerator(name = "snowflake", strategy = "com.github.czsurvey.extra.data.generator.SnowflakeIdGenerator")
    @GeneratedValue(generator = "snowflake")
    private Long id;

    @NotNull(message = "问卷ID不能为空")
    private Long surveyId;

    @NotNull(message = "逻辑表达式不能为空")
    @Enumerated(EnumType.STRING)
    private LogicExpression expression;

    @NotNull(message = "条件列表不能为空")
    @Convert(converter = JsonNodeConverter.class)
    private JsonNode conditions;

    @NotNull(message = "问题key列表不能为空")
    @Convert(converter = JsonNodeConverter.class)
    private JsonNode questionKeys;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SurveyLogic that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static String entityName() {
        return "surveyLogic";
    }
}
