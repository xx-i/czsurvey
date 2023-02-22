package com.github.czsurvey.project.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.czsurvey.extra.data.converter.JsonNodeConverter;
import com.github.czsurvey.project.entity.audit.DateAudit;
import com.github.czsurvey.project.entity.enums.ProjectStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;

/**
 * @author YanYu
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "t_survey")
public class Survey extends DateAudit {

    @Id
    @GenericGenerator(name = "snowflake", strategy = "com.github.czsurvey.extra.data.generator.SnowflakeIdGenerator")
    @GeneratedValue(generator = "snowflake")
    private Long id;

    private String title;

    @Convert(converter = JsonNodeConverter.class)
    private JsonNode instruction;

    @Convert(converter = JsonNodeConverter.class)
    private JsonNode conclusion;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Survey survey)) return false;
        return Objects.equals(id, survey.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static String entityName() {
        return "survey";
    }
}
