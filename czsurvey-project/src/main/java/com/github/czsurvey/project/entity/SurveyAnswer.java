package com.github.czsurvey.project.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.czsurvey.extra.data.converter.JsonNodeConverter;
import com.github.czsurvey.project.entity.audit.DateAudit;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@Entity
@Table(name = "t_survey_answer")
public class SurveyAnswer extends DateAudit {

    @Id
    @GenericGenerator(name = "snowflake", strategy = "com.github.czsurvey.extra.data.generator.SnowflakeIdGenerator")
    @GeneratedValue(generator = "snowflake")
    private Long id;

    private Long surveyId;

    @Convert(converter = JsonNodeConverter.class)
    private JsonNode answer;

    private String ua;

    private Long duration;

    @Column(name = "is_anonymously")
    private Boolean anonymously;

    private String ip;

    private String ipCity;

    private String ipProvince;

    private String browser;

    private String os;

    private String platform;

    @Column(name = "is_valid")
    private Boolean valid;

    private Long answererId;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SurveyAnswer that = (SurveyAnswer) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static String entityName() {
        return "surveyAnswer";
    }
}
