package com.github.czsurvey.project.response;

import com.github.czsurvey.project.entity.SurveyQuestion;
import lombok.Data;

import java.util.List;

/**
 * @author YanYu
 */
@Data
public class SurveyPageResponse {

    private String pageKey;

    private Integer orderNum;

    private List<SurveyQuestion> questions;
}
