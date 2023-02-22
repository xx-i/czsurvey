package com.github.czsurvey.project.entity;

import com.github.czsurvey.project.entity.audit.DateAudit;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Objects;

/**
 * 问卷分页
 * @author YanYu
 */
@Getter
@Setter
@ToString
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "t_survey_page")
@IdClass(SurveyPagePrimaryKey.class)
public class SurveyPage extends DateAudit {

    @Id
    private Long surveyId;

    @Id
    private String pageKey;

    private Integer orderNum;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SurveyPage that)) return false;
        return surveyId.equals(that.surveyId) && pageKey.equals(that.pageKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(surveyId, pageKey);
    }

    public static String entityName() {
        return "survey_page";
    }
}