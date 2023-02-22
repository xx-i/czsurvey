package com.github.czsurvey.project.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author YanYu
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SurveyPagePrimaryKey implements Serializable {

    private Long surveyId;

    private String pageKey;
}
