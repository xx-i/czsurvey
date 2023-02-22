package com.github.czsurvey.project.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.czsurvey.common.exception.BadRequestAlertException;
import com.github.czsurvey.common.exception.InvalidParameterException;
import com.github.czsurvey.extra.security.model.LoginUser;
import com.github.czsurvey.project.entity.LogicCondition;
import com.github.czsurvey.project.entity.Survey;
import com.github.czsurvey.project.entity.SurveyLogic;
import com.github.czsurvey.project.entity.SurveyQuestion;
import com.github.czsurvey.project.repository.SurveyLogicRepository;
import com.github.czsurvey.project.repository.SurveyQuestionRepository;
import com.github.czsurvey.project.repository.SurveyRepository;
import com.github.czsurvey.project.response.SurveyQuestionResponse;
import com.github.czsurvey.project.service.question.QuestionTypeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyLogicService {

    private final SurveyLogicRepository surveyLogicRepository;

    private final SurveyRepository surveyRepository;

    private final SurveyQuestionRepository surveyQuestionRepository;

    private final ObjectMapper objectMapper;

    public SurveyLogic createOrUpdateSurveyLogic(SurveyLogic surveyLogic) {
        if (!surveyRepository.existsEffectiveSurveyByIdAndUserId(surveyLogic.getSurveyId(), LoginUser.me().getId())) {
            throw new BadRequestAlertException("问卷不存在", Survey.entityName(), "survey_not_exists");
        }
        if (surveyLogic.getId() != null) {
            SurveyLogic previous = surveyLogicRepository.findById(surveyLogic.getId())
                .orElseThrow(() -> new BadRequestAlertException("要修改的逻辑不存在", SurveyLogic.entityName(), "survey_logic_not_exists"));
            surveyLogic.setCreateTime(previous.getCreateTime());
        }
        JavaType conditionsType = objectMapper.constructType(new TypeReference<List<LogicCondition>>() {});
        JavaType questionKeysType = objectMapper.constructType(new TypeReference<List<String>>() {});
        List<LogicCondition> conditions;
        List<String> questionKeys;
        try {
            conditions = objectMapper.treeToValue(surveyLogic.getConditions(), conditionsType);
            questionKeys = objectMapper.treeToValue(surveyLogic.getQuestionKeys(), questionKeysType);
        } catch (Exception ignore) {
            throw new InvalidParameterException("条件列表或问题列表参数错误", SurveyLogic.entityName());
        }

        if (ObjectUtil.isEmpty(conditions)) {
            throw new InvalidParameterException("condition不能为空", SurveyLogic.entityName());
        }
        if (ObjectUtil.isEmpty(questionKeys)) {
            throw new InvalidParameterException("questionKeys不能为空", SurveyLogic.entityName());
        }
        List<SurveyQuestionResponse> questions = surveyQuestionRepository.findSurveyQuestionResponseBySurveyId(surveyLogic.getSurveyId());
        Map<String, SurveyQuestionResponse> questionsMap = questions.stream().collect(Collectors.toMap(e -> e.getQuestion().getQuestionKey(), e -> e));

        Integer maxPageOrderNum = -1;
        Integer maxOrderNum = -1;

        for (LogicCondition condition : conditions) {
            if (StrUtil.isEmpty(condition.getQuestionKey())) {
                throw new InvalidParameterException("条件列表中，questionKey参数不能为空", SurveyLogic.entityName());
            }
            SurveyQuestionResponse questionResp = questionsMap.get(condition.getQuestionKey());
            if (questionResp == null) {
                throw new InvalidParameterException("条件列表中，questionKey: " + condition.getQuestionKey() + " 不存在", SurveyLogic.entityName());
            }
            if (condition.getExpression() == null) {
                throw new InvalidParameterException("条件列表中，expression参数不能为空", SurveyLogic.entityName());
            }
            SurveyQuestion question = questionResp.getQuestion();
            if (!QuestionTypeUtil.isChoiceQuestion(question.getType())) {
                throw new InvalidParameterException("条件列表中，作为条件的问题必须为选择题类型", SurveyLogic.entityName());
            }
            if (StrUtil.isEmpty(condition.getOptionId())) {
                throw new InvalidParameterException("条件列表中，optionId参数不能为空", SurveyLogic.entityName());
            }
            if (!QuestionTypeUtil.getChoiceQuestionOptionIdSet(question.getAdditionalInfo()).contains(condition.getOptionId())) {
                throw new InvalidParameterException("条件列表中，questionKey: " + " 的问题中，不存在optionId为" + condition.getOptionId() + " 的选项", SurveyLogic.entityName());
            }
            if (questionResp.getPageOrderNum() >= maxOrderNum) {
                maxOrderNum = questionResp.getPageOrderNum();
                maxOrderNum = question.getOrderNum() > maxOrderNum ? question.getOrderNum() : maxOrderNum;
            }
        }

        for (String questionKey : questionKeys) {
            if (StrUtil.isEmpty(questionKey)) {
                throw new InvalidParameterException("要显示的问题key列表中，不能存在空属性", SurveyLogic.entityName());
            }
            SurveyQuestionResponse questionResp = questionsMap.get(questionKey);
            if (questionResp == null) {
                throw new InvalidParameterException("要显示的问题key列表中，questionKey: " + questionKey + " 不存在", SurveyLogic.entityName());
            }
            SurveyQuestion question = questionResp.getQuestion();
            if (questionResp.getPageOrderNum() < maxPageOrderNum
                || (questionResp.getPageOrderNum().equals(maxPageOrderNum) && question.getOrderNum() <= maxOrderNum)) {
                throw new InvalidParameterException("作为结果的问题必须在作为条件的问题之后", SurveyLogic.entityName());
            }
        }

        return surveyLogicRepository.save(surveyLogic);
    }

    public void removeById(Long id) {
        SurveyLogic logic = surveyLogicRepository.findById(id)
            .orElseThrow(() -> new BadRequestAlertException("要刪除的逻辑不存在", Survey.entityName(), "survey_logic_not_exists"));
        if (!surveyRepository.existsEffectiveSurveyByIdAndUserId(logic.getSurveyId(), LoginUser.me().getId())) {
            throw new BadRequestAlertException("要刪除的逻辑不存在", Survey.entityName(), "survey_logic_not_exists");
        }
        surveyLogicRepository.deleteById(id);
    }
}
