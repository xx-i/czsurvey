package com.github.czsurvey.project.repository;

import com.github.czsurvey.project.entity.SurveyPage;
import com.github.czsurvey.project.entity.SurveyPagePrimaryKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * @author YanYu
 */
public interface SurveyPageRepository extends JpaRepository<SurveyPage, SurveyPagePrimaryKey> {

    List<SurveyPage> findBySurveyIdOrderByOrderNum(Long surveyId);

    Optional<SurveyPage> findBySurveyIdAndPageKey(Long surveyId, String pageKey);

    Long countBySurveyId(Long surveyId);

    @Query(value = " select max(order_num) from t_survey_page where survey_id = :surveyId", nativeQuery = true)
    Integer findMaxPageOrderNumBySurveyId(@Param("surveyId") Long surveyId);
}
