package com.github.czsurvey.project.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.czsurvey.common.exception.BadRequestAlertException;
import com.github.czsurvey.common.exception.InternalServerErrorException;
import com.github.czsurvey.common.exception.InvalidParameterException;
import com.github.czsurvey.extra.security.model.LoginUser;
import com.github.czsurvey.project.entity.QSurveyQuestion;
import com.github.czsurvey.project.entity.Survey;
import com.github.czsurvey.project.entity.SurveyPage;
import com.github.czsurvey.project.entity.SurveyQuestion;
import com.github.czsurvey.project.repository.SurveyLogicRepository;
import com.github.czsurvey.project.repository.SurveyPageRepository;
import com.github.czsurvey.project.repository.SurveyQuestionRepository;
import com.github.czsurvey.project.repository.SurveyRepository;
import com.github.czsurvey.project.request.SurveyQuestionRequest;
import com.github.czsurvey.project.request.SwapQuestionRequest;
import com.github.czsurvey.project.service.question.QuestionTypeUtil;
import com.github.czsurvey.project.service.question.type.CheckBox;
import com.github.czsurvey.project.service.question.type.setting.ReferenceSetting;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author YanYu
 */
@Service
@RequiredArgsConstructor
public class SurveyQuestionService {

    private final SurveyQuestionRepository surveyQuestionRepository;

    private final SurveyRepository surveyRepository;

    private final SurveyPageRepository surveyPageRepository;

    private final SurveyLogicRepository surveyLogicRepository;

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 添加或修改问题
     * @param questionRequest 问题
     * @param isUpdate 是否是修改
     * @return 问题
     */
    @Transactional(rollbackFor = Exception.class)
    public SurveyQuestion createOrUpdateSurveyQuestion(SurveyQuestionRequest questionRequest, boolean isUpdate) {
        Long surveyId = questionRequest.getSurveyId();
        if (!surveyRepository.existsEffectiveSurveyByIdAndUserId(surveyId, LoginUser.me().getId())) {
            throw new BadRequestAlertException("问卷不存在", Survey.entityName(), "survey_not_exists");
        }
        Optional<SurveyQuestion> target = surveyQuestionRepository.findBySurveyIdAndQuestionKey(surveyId, questionRequest.getQuestionKey());
        if (isUpdate && target.isEmpty()) {
            throw new BadRequestAlertException("要修改的问题不存在", SurveyQuestion.entityName(), "question_not_exist");
        } else if (!isUpdate && target.isPresent()) {
            throw new BadRequestAlertException("questionKey已经存在", SurveyQuestion.entityName(), "question_key_already_existed");
        }
        SurveyQuestion question = isUpdate ? target.get() : new SurveyQuestion();
        BeanUtil.copyProperties(questionRequest, question, CopyOptions.create().ignoreNullValue());

        JsonNode setting = QuestionTypeUtil.validateAndConvertQuestionSetting(question.getType(), question.getAdditionalInfo());
        question.setAdditionalInfo(setting);

        // 添加问题，则对本页的问题重新排序
        if (!isUpdate) {
            List<String> sortedQuestionKeys = questionRequest.getSortedQuestionKeys();
            int questionIndex = sortedQuestionKeys.indexOf(question.getQuestionKey());
            if (questionIndex == -1) {
                throw new InvalidParameterException("sortedQuestionKeys中找不到questionKey: " + questionRequest.getQuestionKey(), SurveyQuestion.entityName());
            }
            question.setOrderNum(questionIndex);
            for (int i = questionIndex + 1; i < sortedQuestionKeys.size(); ++i) {
                QSurveyQuestion qSurveyQuestion = QSurveyQuestion.surveyQuestion;
                jpaQueryFactory
                    .update(qSurveyQuestion)
                    .where(qSurveyQuestion.surveyId.eq(surveyId).and(qSurveyQuestion.questionKey.eq(sortedQuestionKeys.get(i))))
                    .set(qSurveyQuestion.orderNum, i)
                    .execute();
            }
        }

        // 如果是新的一页，则插入页面记录（默认只能在页面的最后插入新的一页）
        SurveyPage currentPage = surveyPageRepository
            .findBySurveyIdAndPageKey(surveyId, question.getPageKey())
            .orElseGet(() -> {
                int pageIndex = questionRequest.getSortedPageKeys().indexOf(questionRequest.getPageKey());
                if (pageIndex == -1) {
                    throw new InvalidParameterException("sortedPageKeys中找不到pageKey: " + questionRequest.getPageKey(), SurveyQuestion.entityName());
                }
                Integer maxOrderNum = surveyPageRepository.findMaxPageOrderNumBySurveyId(surveyId);
                return surveyPageRepository.save(new SurveyPage(surveyId, questionRequest.getPageKey(), maxOrderNum + 1));
            });

        // 如果该题目类型可以引用多选题，则校验引用的内容是否合法
        if (QuestionTypeUtil.isReferenceQuestionType(question.getType())) {
            ReferenceSetting referenceSetting = (ReferenceSetting) QuestionTypeUtil.settingTreeToValue(question.getType(), setting);
            if (referenceSetting.isReference()) {
                SurveyQuestion refQuestion = surveyQuestionRepository
                    .findBySurveyIdAndQuestionKey(surveyId, referenceSetting.getRefQuestionKey())
                    .orElseThrow(() -> new BadRequestAlertException("引用的问题不存在", SurveyQuestion.entityName(), "id_not_found"));
                if (!CheckBox.TYPE.equals(refQuestion.getType())) {
                    throw new InvalidParameterException("只能引用多选题型", SurveyQuestion.entityName());
                }
                SurveyPage refPage = surveyPageRepository.findBySurveyIdAndPageKey(surveyId, refQuestion.getPageKey())
                    .orElseThrow(() -> new InternalServerErrorException("引用题目的分页不存在"));
                if (
                    refPage.getOrderNum() > currentPage.getOrderNum()
                    || (
                        refPage.getOrderNum().equals(currentPage.getOrderNum())
                        && refQuestion.getOrderNum() > question.getOrderNum()
                    )
                ) {
                    throw new InvalidParameterException("引用的题目必须在当前题目之前", SurveyQuestion.entityName());
                }
            }
        }

        // 如果修改了选择题并且选择题删除了某些选项，则校验删除的选项是否会对已有的逻辑产生影响
        if (isUpdate && QuestionTypeUtil.isChoiceQuestion(question.getType())) {
            Set<String> dependsOptions = surveyLogicRepository.findLogicDependsOptions(question.getSurveyId(), question.getQuestionKey());
            if (dependsOptions.size() > 0) {
                Set<String> questionOptions = QuestionTypeUtil.getChoiceQuestionOptionIdSet(question.getAdditionalInfo());
                if (!questionOptions.containsAll(dependsOptions)) {
                    throw new BadRequestAlertException("删除选项失败，选项被逻辑引用", SurveyQuestion.entityName(), "logic_dependent");
                }
            }
        }
        return surveyQuestionRepository.save(question);
    }

    @Transactional
    public void swapQuestionDisplayOrder(SwapQuestionRequest swapQuestionRequest) {
        Long surveyId = swapQuestionRequest.getSurveyId();
        if (!surveyRepository.existsEffectiveSurveyByIdAndUserId(surveyId, LoginUser.me().getId())) {
            throw new BadRequestAlertException("问卷不存在", Survey.entityName(), "survey_not_exists");
        }
        SurveyQuestion sourceQuestion = surveyQuestionRepository.findBySurveyIdAndQuestionKey(surveyId, swapQuestionRequest.getSourceQuestionKey())
            .orElseThrow(() -> new InvalidParameterException("问题key: " + swapQuestionRequest.getSourceQuestionKey() + "不存在", SurveyQuestion.entityName()));
        SurveyQuestion targetQuestion = surveyQuestionRepository.findBySurveyIdAndQuestionKey(surveyId, swapQuestionRequest.getTargetQuestionKey())
            .orElseThrow(() -> new InvalidParameterException("问题key: " + swapQuestionRequest.getTargetQuestionKey() + "不存在", SurveyQuestion.entityName()));
        SurveyPage sourcePage = surveyPageRepository.findBySurveyIdAndPageKey(surveyId, sourceQuestion.getPageKey())
            .orElseThrow(() -> new InternalServerErrorException("问题所在的分页不存在"));
        SurveyPage targetPage = surveyPageRepository.findBySurveyIdAndPageKey(surveyId, targetQuestion.getPageKey())
            .orElseThrow(() -> new InternalServerErrorException("问题所在的分页不存在"));

        SurveyQuestion frontQuestion;
        SurveyQuestion backQuestion;
        SurveyPage frontPage;
        SurveyPage backPage;
        if (
            (sourcePage.getOrderNum() < targetPage.getOrderNum()) ||
            (sourcePage.getOrderNum().equals(targetPage.getOrderNum()) && sourceQuestion.getOrderNum() < targetQuestion.getOrderNum())
        ) {
            frontQuestion = sourceQuestion;
            backQuestion = targetQuestion;
            frontPage = sourcePage;
            backPage = targetPage;
        } else {
            frontQuestion = targetQuestion;
            backQuestion = sourceQuestion;
            frontPage = targetPage;
            backPage = sourcePage;
        }
        // 在前面的问题向后移动需要判断
        // 1. 该问题是否被引用，如果被引用则移动后的位置是否在所引用的题目的位置之前
        // 2. 该问题是否作为逻辑的条件，如果作为逻辑的条件，则移动后的位置是否在作为逻辑的结果的题目之前
        if (
            frontQuestion.getType().equals(CheckBox.TYPE) &&
            surveyQuestionRepository.countReferencedQuestionByOrderBefore(
                surveyId,
                frontQuestion.getQuestionKey(),
                backPage.getOrderNum(),
                backQuestion.getOrderNum()
            ) > 0
        ) {
            throw new InvalidParameterException("操作失败,被引用的题目必须在引用的题目之前", SurveyQuestion.entityName());
        }
        List<Long> frontLogicIds = surveyLogicRepository.findLogicIdsByConditionQuestionKey(surveyId, frontQuestion.getQuestionKey());
        if (frontLogicIds.size() > 0) {
            Set<String> resultQuestionKeySet = surveyLogicRepository.findQuestionKeySetByLogicIds(frontLogicIds);
            Long quesCount = surveyQuestionRepository.countByQuestionKeyInAndOrderBefore(resultQuestionKeySet, backPage.getOrderNum(), backQuestion.getOrderNum());
            if (quesCount > 0) {
                throw new InvalidParameterException("操作失败，逻辑的条件必须在逻辑的结果之前", SurveyQuestion.entityName());
            }
        }

        // 在后面的问题向前移动需要判断
        // 1. 该问题是否引用了其它问题，如果引用了其它问题，则移动后是否在引用的问题之后
        // 2. 该问题是否作为逻辑的结果，如果作为逻辑的结果，则移动后是否在逻辑的条件之后
        if (QuestionTypeUtil.getReferenceQuestionType().contains(backQuestion.getType())) {
            ReferenceSetting referenceSetting = (ReferenceSetting) QuestionTypeUtil.settingTreeToValue(backQuestion.getType(), backQuestion.getAdditionalInfo());
            if (referenceSetting.isReference()) {
                SurveyQuestion refQuestion = surveyQuestionRepository.findBySurveyIdAndQuestionKey(surveyId, referenceSetting.getRefQuestionKey())
                    .orElseThrow(() -> new InternalServerErrorException("找不到引用的问题"));
                SurveyPage refPage = surveyPageRepository.findBySurveyIdAndPageKey(surveyId, refQuestion.getPageKey())
                    .orElseThrow(() -> new InternalServerErrorException("引用题目的分页不存在"));
                if (
                    frontPage.getOrderNum() < refPage.getOrderNum() ||
                    (frontPage.getOrderNum().equals(refPage.getOrderNum()) && frontQuestion.getOrderNum() < refQuestion.getOrderNum())
                ) {
                    throw new InvalidParameterException("操作失败,被引用的题目必须在引用的题目之前", SurveyQuestion.entityName());
                }
            }
        }
        List<Long> backLogicIds = surveyLogicRepository.findLogicIdsByResultQuestionKey(frontQuestion.getSurveyId(), frontQuestion.getQuestionKey());
        if (backLogicIds.size() > 0) {
            Set<String> conditionQuesKeys = surveyLogicRepository.findConditionQuestionKeySetByIdIn(backLogicIds);
            Long quesCount = surveyQuestionRepository.countByQuestionKeyInAndOrderAfter(conditionQuesKeys, frontQuestion.getOrderNum(), frontQuestion.getOrderNum());
            if (quesCount > 0) {
                throw new InvalidParameterException("操作失败，逻辑的条件必须在逻辑的结果之前", SurveyQuestion.entityName());
            }
        }

        Integer tempOrderNum;
        tempOrderNum = sourceQuestion.getOrderNum();
        sourceQuestion.setOrderNum(targetQuestion.getOrderNum());
        targetQuestion.setOrderNum(tempOrderNum);
        String tempPageKey;
        tempPageKey = sourceQuestion.getPageKey();
        sourceQuestion.setPageKey(targetPage.getPageKey());
        targetPage.setPageKey(tempPageKey);
        surveyQuestionRepository.save(sourceQuestion);
        surveyQuestionRepository.save(targetQuestion);
    }

    @Transactional
    public void deleteQuestion(Long surveyId, String questionKey) {
        if (!surveyRepository.existsEffectiveSurveyByIdAndUserId(surveyId, LoginUser.me().getId())) {
            throw new BadRequestAlertException("问卷不存在", Survey.entityName(), "survey_not_exists");
        }
        SurveyQuestion question = surveyQuestionRepository.findBySurveyIdAndQuestionKey(surveyId, questionKey)
            .orElseThrow(() -> new BadRequestAlertException("要删除的问题不存在", SurveyQuestion.entityName()));

        if (surveyQuestionRepository.countBySurveyIdAndPageKey(surveyId, question.getPageKey()) == 1) {
            throw new BadRequestAlertException("每一页至少需要一个题目", SurveyQuestion.entityName());
        }

        if (question.getType().equals(CheckBox.TYPE)) {
            Long refQuesCount = surveyQuestionRepository.countReferencedQuestionBySurveyIdAndQuestionKey(surveyId, questionKey);
            if (refQuesCount > 0) {
                throw new BadRequestAlertException("该问题被其它问题引用，不能被删除", SurveyQuestion.entityName());
            }
        }
        // 逻辑条件中包含该问题的数量
        Long conditionLogicCount = surveyLogicRepository.countByConditionQuestionKey(surveyId, questionKey);
        if (conditionLogicCount > 0) {
            throw new BadRequestAlertException("该问题被逻辑条件引用，不能被删除", SurveyQuestion.entityName());
        }
        // 逻辑结果中包含该问题的数量
        Long resultLogicCount = surveyLogicRepository.countByResultQuestionKey(surveyId, questionKey);
        if (resultLogicCount > 0) {
            throw new BadRequestAlertException("该问题被逻辑结果引用，不能被删除", SurveyQuestion.entityName());
        }
        surveyQuestionRepository.delete(question);
    }
}
