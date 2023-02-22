package com.github.czsurvey.project.response;

import com.github.czsurvey.project.entity.Survey;
import com.github.czsurvey.project.entity.SurveyLogic;
import com.github.czsurvey.project.entity.SurveySetting;
import lombok.Data;

import java.util.List;

/**
 * @author YanYu
 */
@Data
public class SurveyDetailResponse {

    private Survey survey;

    private List<SurveyPageResponse> pages;

    private List<SurveyLogic> logics;

    private SurveySetting settings;
}
