package com.github.czsurvey.project.repository;

import com.github.czsurvey.project.entity.SurveyQuestion;
import com.github.czsurvey.project.response.SurveyQuestionResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author YanYu
 */
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long>  {

    List<SurveyQuestion> findBySurveyId(Long surveyId);

    List<SurveyQuestion> findBySurveyIdOrderByOrderNum(Long surveyId);

    Optional<SurveyQuestion> findBySurveyIdAndQuestionKey(Long surveyId, String questionKey);

    Optional<SurveyQuestion> findBySurveyIdAndQuestionKeyAndOrderNumBefore(Long surveyId, String questionKey, Integer orderNum);

    @Query(value = """
        select new com.github.czsurvey.project.response.SurveyQuestionResponse(t1, t2.orderNum)
        from SurveyQuestion t1
        left join SurveyPage t2 on t1.surveyId = t2.surveyId and t1.pageKey = t2.pageKey
        where t1.surveyId = :surveyId
    """)
    List<SurveyQuestionResponse> findSurveyQuestionResponseBySurveyId(Long surveyId);

    Long countBySurveyIdAndTypeNotIn(Long surveyId, Collection<String> types);

    void deleteAllBySurveyIdIn(List<Long> surveyIds);

    List<SurveyQuestion> findBySurveyIdAndPageKey(Long surveyId, String pageKey);

    Long countBySurveyIdAndPageKey(Long surveyId, String pageKey);

    @Query(value = """
        select count(*) from t_survey_question t1
        left join t_survey_page t2 on t1.survey_id = t2.survey_id and t1.page_key = t2.page_key
        where t1.survey_id = :surveyId
        and t1.additional_info->'$.refQuestionKey' = :questionKey
        and (t2.order_num < :pageOrderNum or (t2.order_num = :pageOrderNum and t1.order_num <= :orderNum))
    """, nativeQuery = true)
    Long countReferencedQuestionByOrderBefore(
        @Param("surveyId") Long surveyId,
        @Param("questionKey") String questionKey,
        @Param("pageOrderNum") Integer pageOrderNum,
        @Param("orderNum") Integer orderNum
    );

    @Query(value = """
        select count(*) from t_survey_question t1
        left join t_survey_page t2 on t1.survey_id = t2.survey_id and t1.page_key = t2.page_key
        where t1.survey_id = :surveyId
        and t1.additional_info->'$.refQuestionKey' = :questionKey
    """, nativeQuery = true)
    Long countReferencedQuestionBySurveyIdAndQuestionKey(@Param("surveyId") Long surveyId, @Param("questionKey") String questionKey);

    @Query(value = """
        select t1.question_key from t_survey_question t1
        left join t_survey_page t2 on t1.survey_id = t2.survey_id and t1.page_key = t2.page_key
        where t1.survey_id = :surveyId
        and t1.additional_info->'$.refQuestionKey' in :questionKeys
    """, nativeQuery = true)
    List<String> findQuestionKeyByRefQuestionKeyIn(@Param("surveyId") Long surveyId, @Param("questionKeys") Collection<String> questionKeys);

    @Query(value = """
        select count(*)
        from t_survey_question t1
        left join t_survey_page t2 on t1.survey_id = t2.survey_id and t1.page_key = t2.page_key
        where t1.question_key in :questionKeys
        and (t2.order_num < :pageOrderNum or (t2.order_num = :pageOrderNum and t1.order_num <= :orderNum))
    """, nativeQuery = true)
    Long countByQuestionKeyInAndOrderBefore(
        @Param("questionKeys") Set<String> questionKeys,
        @Param("pageOrderNum") Integer pageOrderNum,
        @Param("orderNum") Integer orderNum
    );

    @Query(value = """
        select count(*)
        from t_survey_question t1
        left join t_survey_page t2 on t1.survey_id = t2.survey_id and t1.page_key = t2.page_key
        where t1.question_key in :questionKeys
        and (t2.order_num > :pageOrderNum or (t2.order_num = :pageOrderNum || t1.order_num > :orderNum))
    """, nativeQuery = true)
    Long countByQuestionKeyInAndOrderAfter(
        @Param("questionKeys") Set<String> questionKeys,
        @Param("pageOrderNum") Integer pageOrderNum,
        @Param("orderNum") Integer orderNum
    );
}
