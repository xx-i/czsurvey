package com.github.czsurvey.project.repository;

import com.github.czsurvey.project.entity.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {

    Long countBySurveyIdAndAnswererId(Long surveyId, Long answerId);

    Long countBySurveyIdAndAnswererIdAndCreateTimeAfter(Long surveyId, Long answerId, LocalDateTime afterTime);

    Long countBySurveyIdAndIp(Long surveyId, String ip);

    Long countBySurveyIdAndIpAndCreateTimeAfter(Long surveyId, String ip, LocalDateTime afterTime);

    Long countBySurveyId(Long surveyId);

    Long countBySurveyIdAndCreateTimeAfter(Long surveyId, LocalDateTime afterTime);

    @Query("select avg(s.duration) from SurveyAnswer s where s.surveyId = :surveyId")
    Integer getAvgDurationBySurveyId(@Param("surveyId") Long surveyId);

    SurveyAnswer findTopBySurveyIdAndAnswererIdOrderByCreateTimeDesc(Long surveyId, Long answerId);

    @Modifying
    @Query("update SurveyAnswer a set a.valid = :valid where a.id in :ids")
    void batchUpdateSurveyAnswers(@Param("ids") List<Long> ids, @Param("valid") boolean valid);

    void deleteAllBySurveyId(Long surveyId);

    Long countBySurveyIdAndEndedAtAfter(Long survey, LocalDateTime endedAt);

    void deleteAllBySurveyIdIn(List<Long> surveyIds);
}
