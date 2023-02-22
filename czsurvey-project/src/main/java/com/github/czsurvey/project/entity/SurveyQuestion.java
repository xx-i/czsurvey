package com.github.czsurvey.project.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.czsurvey.extra.data.converter.JsonNodeConverter;
import com.github.czsurvey.project.entity.audit.DateAudit;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author YanYu
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "t_survey_question")
public class SurveyQuestion extends DateAudit {

    @Id
    @GenericGenerator(name = "snowflake", strategy = "com.github.czsurvey.extra.data.generator.SnowflakeIdGenerator")
    @GeneratedValue(generator = "snowflake")
    private Long id;

    @NotNull(message = "问卷ID不能为空")
    private Long surveyId;

    @NotEmpty(message = "问卷Key不能为空")
    private String questionKey;

    private String pageKey;

    @Convert(converter = JsonNodeConverter.class)
    private JsonNode title;

    @Convert(converter = JsonNodeConverter.class)
    private JsonNode description;

    @NotEmpty(message = "问题类型不能为空")
    private String type;

    private Boolean required;

    @Convert(converter = JsonNodeConverter.class)
    private JsonNode additionalInfo;

    private Integer orderNum;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SurveyQuestion that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static String entityName() {
        return "surveyQuestion";
    }
}
