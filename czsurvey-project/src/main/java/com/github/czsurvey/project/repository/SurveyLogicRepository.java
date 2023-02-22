package com.github.czsurvey.project.repository;

import com.github.czsurvey.project.entity.SurveyLogic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface SurveyLogicRepository extends JpaRepository<SurveyLogic, Long> {

    List<SurveyLogic> findBySurveyId(Long surveyId);

    /**
     * 查找逻辑条件中某个问题所依赖的所有选项
     * @param surveyId 问卷ID
     * @param questionKey questionKey
     * @return 选项Set
     */
    @Query(value = """
        select
        	distinct t2.option_id
        from t_survey_logic t1
        inner join
        json_table(
            t1.conditions,
            '$[*]'
            columns (
        	    option_id varchar(20) path '$.optionId',
        	    question_key varchar(20) path '$.questionKey'
            )
        ) t2
        where t1.survey_id = :surveyId and t2.question_key = :questionKey
    """, nativeQuery = true)
    Set<String> findLogicDependsOptions(Long surveyId, String questionKey);

    @Query(value = """
        select
            distinct t1.id
        from t_survey_logic t1
        inner join
        json_table(
            t1.conditions,
            '$[*]'
            columns (question_key varchar(20) path '$.questionKey')
        ) t2
        where t1.survey_id = :surveyId and t2.question_key = :questionKey
    """, nativeQuery = true)
    List<Long> findLogicIdsByConditionQuestionKey(@Param("surveyId") Long surveyId, @Param("questionKey") String questionKey);

    @Query(value = """
        select
            count(distinct t1.id)
        from t_survey_logic t1
        inner join
        json_table(
            t1.conditions,
            '$[*]'
            columns (question_key varchar(20) path '$.questionKey')
        ) t2
        where t1.survey_id = :surveyId and t2.question_key = :questionKey
    """, nativeQuery = true)
    Long countByConditionQuestionKey(@Param("surveyId") Long surveyId, @Param("questionKey") String questionKey);

    @Query(value = """
        select
            count(distinct t1.id)
        from t_survey_logic t1
        inner join
        json_table(
            t1.conditions,
            '$[*]'
            columns (question_key varchar(20) path '$.questionKey')
        ) t2
        where t1.survey_id = :surveyId and t2.question_key in :questionKeys
    """, nativeQuery = true)
    Long countByConditionQuestionKeys(@Param("surveyId") Long surveyId, @Param("questionKeys") Collection<String> questionKeys);

    @Query(value = """
        select
            distinct t1.id
        from t_survey_logic t1
        inner join
        json_table(
            t1.question_keys,
            '$[*]'
            columns (question_key varchar(20) path '$')
        ) t2 where t1.survey_id = :surveyId and t2.question_key = :resultQuestionKey
    """, nativeQuery = true)
    List<Long> findLogicIdsByResultQuestionKey(Long surveyId, String resultQuestionKey);

    @Query(value = """
        select
            count(distinct t1.id)
        from t_survey_logic t1
        inner join
        json_table(
            t1.question_keys,
            '$[*]'
            columns (question_key varchar(20) path '$')
        ) t2 where t1.survey_id = :surveyId and t2.question_key = :resultQuestionKey
    """, nativeQuery = true)
    Long countByResultQuestionKey(Long surveyId, String resultQuestionKey);

    @Query(value = """
        select
            count(distinct t1.id)
        from t_survey_logic t1
        inner join
        json_table(
            t1.question_keys,
            '$[*]'
            columns (question_key varchar(20) path '$')
        ) t2 where t1.survey_id = :surveyId and t2.question_key in :resultQuestionKeys
    """, nativeQuery = true)
    Long countByResultQuestionKeys(Long surveyId, Collection<String> resultQuestionKeys);

    @Query(value = """
        select
            distinct t2.question_key
        from t_survey_logic t1
        inner join
        json_table(
            t1.question_keys,
            '$[*]'
            columns (question_key varchar(20) path '$')
        ) t2 where t1.id in :logicIds
    """, nativeQuery = true)
    Set<String> findQuestionKeySetByLogicIds(@Param("logicIds") List<Long> logicIds);

    @Query(value = """
        select
            distinct t2.question_key
        from t_survey_logic t1
        inner join
        json_table(
            t1.conditions,
            '$[*]'
            columns (question_key varchar(20) path '$.questionKey')
        ) t2
        where t1.id in :logicIds
    """, nativeQuery = true)
    Set<String> findConditionQuestionKeySetByIdIn(@Param("logicIds") List<Long> logicIds);
}
