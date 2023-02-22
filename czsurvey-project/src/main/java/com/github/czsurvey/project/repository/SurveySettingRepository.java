package com.github.czsurvey.project.repository;

import com.github.czsurvey.project.entity.SurveySetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author YanYu
 */
public interface SurveySettingRepository extends JpaRepository<SurveySetting, Long> {

    Optional<SurveySetting> findBySurveyId(Long surveyId);
}
