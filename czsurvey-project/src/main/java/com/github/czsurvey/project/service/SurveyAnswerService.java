package com.github.czsurvey.project.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.czsurvey.common.costant.CommonConstant;
import com.github.czsurvey.common.exception.BadRequestAlertException;
import com.github.czsurvey.common.exception.InternalServerErrorException;
import com.github.czsurvey.common.exception.InvalidParameterException;
import com.github.czsurvey.common.payload.AddrInfo;
import com.github.czsurvey.common.util.IpUtil;
import com.github.czsurvey.common.util.PaginationUtil;
import com.github.czsurvey.common.util.WebUtil;
import com.github.czsurvey.extra.security.model.LoginUser;
import com.github.czsurvey.project.entity.*;
import com.github.czsurvey.project.entity.enums.LogicExpression;
import com.github.czsurvey.project.entity.enums.SurveyStatus;
import com.github.czsurvey.project.repository.*;
import com.github.czsurvey.project.request.BatchChangeAnswerValidRequest;
import com.github.czsurvey.project.request.ChangeAnswerValidRequest;
import com.github.czsurvey.project.request.SurveyAnswerQueryRequest;
import com.github.czsurvey.project.request.SurveyAnswerRequest;
import com.github.czsurvey.project.response.SurveyAnswerResponse;
import com.github.czsurvey.project.service.question.QuestionTypeUtil;
import com.github.czsurvey.project.service.question.type.CheckBox;
import com.github.czsurvey.project.service.question.type.Radio;
import com.github.czsurvey.project.service.question.type.Select;
import com.github.czsurvey.project.service.question.type.result.CheckBoxResult;
import com.github.czsurvey.project.service.question.type.result.RadioResult;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyAnswerService {

    private final SurveyAnswerRepository surveyAnswerRepository;

    private final SurveyService surveyService;

    private final SurveyRepository surveyRepository;

    private final SurveyLogicRepository surveyLogicRepository;

    private final SurveyQuestionRepository surveyQuestionRepository;

    private final SurveySettingRepository surveySettingRepository;

    private final JPAQueryFactory jpaQueryFactory;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public SurveyAnswer  createOrUpdateSurveyAnswer(SurveyAnswerRequest surveyAnswerRequest) {
        SurveySetting setting = surveySettingRepository.findBySurveyId(surveyAnswerRequest.getSurveyId())
            .orElseThrow(() -> new InternalServerErrorException("找不到该问卷的配置，问卷ID: " + surveyAnswerRequest.getSurveyId()));
        SurveyStatus surveyStatus = surveyService.getSurveyAnswerState(surveyAnswerRequest.getSurveyId());
        boolean isNeedUpdate = SurveyStatus.ALREADY_ANSWERED_BUT_CAD_MODIFY.equals(surveyStatus)
            || SurveyStatus.EXCEED_LIMIT_BUT_CAN_MODIFY.equals(surveyStatus);
        if (!SurveyStatus.NORMAL.equals(surveyStatus) && !isNeedUpdate) {
            throw new  BadRequestAlertException("问卷状态校验失败, 当前状态: " + surveyStatus, Survey.entityName(), "error_status");
        }
        Map<String, JsonNode> answerMap = surveyAnswerRequest.getAnswer();
        List<SurveyLogic> logics = surveyLogicRepository.findBySurveyId(surveyAnswerRequest.getSurveyId());
        Map<String, Set<Long>> neededLogicMap = getNeededLogicMap(logics);
        Map<Long, SurveyLogic> logicMap = logics.stream().collect(Collectors.toMap(SurveyLogic::getId, e -> e));
        List<SurveyQuestion> questions = surveyQuestionRepository.findBySurveyId(surveyAnswerRequest.getSurveyId());
        Map<String, SurveyQuestion> questionMap = questions.stream().collect(Collectors.toMap(SurveyQuestion::getQuestionKey, e -> e));
        for (SurveyQuestion question : questions) {
            if (!QuestionTypeUtil.getIsInputModeQuestion(question.getType())) {
                answerMap.remove(question.getQuestionKey());
                continue;
            }
            boolean isRequired = checkQuestionIsRequired(question, neededLogicMap, logicMap, answerMap, questionMap);
            if (isRequired && answerMap.get(question.getQuestionKey()) == null) {
                throw new InvalidParameterException("问题: " + question.getQuestionKey() + " 需要填写", SurveyAnswer.entityName());
            }
            JsonNode value = QuestionTypeUtil.validateAndConvertQuestionResult(question, questionMap, answerMap);
            if (answerMap.containsKey(question.getQuestionKey())) {
                answerMap.put(question.getQuestionKey(), value);
            }
        }
        SurveyAnswer surveyAnswer = new SurveyAnswer();
        if (isNeedUpdate) {
            surveyAnswer = surveyAnswerRepository.findTopBySurveyIdAndAnswererIdOrderByCreateTimeDesc(surveyAnswerRequest.getSurveyId(), LoginUser.me().getId());
        }
        surveyAnswer.setSurveyId(surveyAnswerRequest.getSurveyId());
        surveyAnswer.setAnswer(objectMapper.valueToTree(surveyAnswerRequest.getAnswer()));
        surveyAnswer.setStartedAt(surveyAnswerRequest.getStartedAt());
        surveyAnswer.setEndedAt(LocalDateTime.now());
        surveyAnswer.setAnonymously(setting.getAnonymously());
        Duration duration = Duration.between(surveyAnswer.getStartedAt(), surveyAnswer.getEndedAt());
        surveyAnswer.setDuration(duration.toSeconds());
        setUaInfo(surveyAnswer, surveyAnswerRequest.getSurveyId());
        setIpInfo(surveyAnswer, surveyAnswerRequest.getSurveyId());
        if (setting.getLoginRequired()) {
            surveyAnswer.setAnswererId(LoginUser.me().getId());
        }
        surveyAnswer.setValid(true);
        return surveyAnswerRepository.save(surveyAnswer);
    }

    public Page<SurveyAnswerResponse> pageAnswers(SurveyAnswerQueryRequest surveyAnswerQueryRequest, Pageable pageable) {
        if (!surveyRepository.existsEffectiveSurveyByIdAndUserId(surveyAnswerQueryRequest.getSurveyId(), LoginUser.me().getId())) {
            throw new BadRequestAlertException("问卷:" + surveyAnswerQueryRequest.getSurveyId() + "不存在", Project.entityName(), "survey_not_found");
        }
        QSurveyAnswer qSurveyAnswer = QSurveyAnswer.surveyAnswer;
        QUser qUser = QUser.user;
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(qSurveyAnswer.surveyId.eq(surveyAnswerQueryRequest.getSurveyId()));
        if (StrUtil.isNotBlank(surveyAnswerQueryRequest.getNickName())) {
            booleanBuilder.and(qUser.nickname.like("%" + surveyAnswerQueryRequest.getNickName() + "%"))
                .and(qSurveyAnswer.anonymously.eq(false));
        }
        if (surveyAnswerQueryRequest.getStartTime() != null) {
            booleanBuilder.and(qSurveyAnswer.endedAt.after(surveyAnswerQueryRequest.getStartTime()));
        }
        if (surveyAnswerQueryRequest.getEndTime() != null) {
            booleanBuilder.and(qSurveyAnswer.endedAt.before(surveyAnswerQueryRequest.getEndTime()));
        }
        if (surveyAnswerQueryRequest.getValid() != null) {
            booleanBuilder.and(qSurveyAnswer.valid.eq(surveyAnswerQueryRequest.getValid()));
        }
        var userSummaryExpr = Projections.constructor(SurveyAnswerResponse.UserSummary.class, qUser.nickname, qUser.realName, qUser.phone, qUser.avatar);
        var surveyAnswerResponseExpr = Projections.constructor(SurveyAnswerResponse.class, qSurveyAnswer, userSummaryExpr);
        JPAQuery<SurveyAnswerResponse> query = jpaQueryFactory.select(surveyAnswerResponseExpr)
            .from(qSurveyAnswer)
            .leftJoin(qUser)
            .on(qSurveyAnswer.answererId.eq(qUser.id))
            .where(booleanBuilder.getValue())
            .orderBy(qSurveyAnswer.endedAt.desc());
        return PaginationUtil.page(query, pageable, SurveyAnswer.class, answer -> {
            if (answer.getSurveyAnswer().getAnonymously()) {
                answer.setAnswerer(new SurveyAnswerResponse.UserSummary());
            }
            return answer;
        });
    }

    public void changeAnswerIsValid(ChangeAnswerValidRequest request) {
        InvalidParameterException exception = new InvalidParameterException("要修改的回答不存在, id: " + request.getAnswerId(), SurveyAnswer.entityName());
        SurveyAnswer answer = surveyAnswerRepository.findById(request.getAnswerId()).orElseThrow(() -> exception);
        if (!surveyRepository.existsEffectiveSurveyByIdAndUserId(answer.getSurveyId(), LoginUser.me().getId())) {
            throw exception;
        }
        answer.setValid(request.getValid());
        surveyAnswerRepository.save(answer);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchChangeAnswerIdValid(BatchChangeAnswerValidRequest request) {
        List<Long> surveyIdList = surveyAnswerRepository.findAllById(request.getIds())
            .stream()
            .map(SurveyAnswer::getSurveyId)
            .toList();
        Set<Long> surveyIdSet = new HashSet<>(surveyIdList);
        if (surveyIdSet.size() > 1) {
            throw new InvalidParameterException("不能批量修改多个问卷中的回答", SurveyAnswer.entityName());
        }
        if (surveyIdSet.size() == 0) {
            throw new InvalidParameterException("批量修改的回答不存在", SurveyAnswer.entityName());
        }
        if (!surveyRepository.existsEffectiveSurveyByIdAndUserId(surveyIdList.get(0), LoginUser.me().getId())) {
            throw new InvalidParameterException("批量修改的回答不存在", SurveyAnswer.entityName());
        }
        surveyAnswerRepository.batchUpdateSurveyAnswers(request.getIds(), request.getValid());
    }

    public void deleteAnswer(Long answerId) {
        InvalidParameterException exception = new InvalidParameterException("删除的回答不存在, id: " + answerId, SurveyAnswer.entityName());
        SurveyAnswer answer = surveyAnswerRepository.findById(answerId).orElseThrow(() -> exception);
        if (!surveyRepository.existsEffectiveSurveyByIdAndUserId(answer.getSurveyId(), LoginUser.me().getId())) {
            throw exception;
        }
        surveyAnswerRepository.delete(answer);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteAnswers(List<Long> ids) {
        List<Long> surveyIdList = surveyAnswerRepository.findAllById(ids)
            .stream()
            .map(SurveyAnswer::getSurveyId)
            .toList();
        Set<Long> surveyIdSet = new HashSet<>(surveyIdList);
        if (surveyIdSet.size() > 1) {
            throw new InvalidParameterException("不能批量删除多个问卷中的回答", SurveyAnswer.entityName());
        }
        if (surveyIdSet.size() == 0) {
            throw new InvalidParameterException("批量删除的回答不存在", SurveyAnswer.entityName());
        }
        if (!surveyRepository.existsEffectiveSurveyByIdAndUserId(surveyIdList.get(0), LoginUser.me().getId())) {
            throw new InvalidParameterException("批量删除的回答不存在", SurveyAnswer.entityName());
        }
        surveyAnswerRepository.deleteAllById(ids);
    }

    @SneakyThrows
    private Map<String, Set<Long>> getNeededLogicMap(List<SurveyLogic> logics) {
        Map<String, Set<Long>> neededLogicMap = new HashMap<>();
        JavaType questionKeysType = objectMapper.constructType(new TypeReference<List<String>>() {});
        for (SurveyLogic logic : logics) {
            List<String> asResultQuestionKeys = objectMapper.treeToValue(logic.getQuestionKeys(), questionKeysType);
            for (String questionKey : asResultQuestionKeys) {
                if(neededLogicMap.containsKey(questionKey)) {
                    neededLogicMap.get(questionKey).add(logic.getId());
                } else {
                    Set<Long> logicIdSet = new HashSet<>();
                    logicIdSet.add(logic.getId());
                    neededLogicMap.put(questionKey, logicIdSet);
                }
            }
        }
        return neededLogicMap;
    }


    @Transactional(rollbackFor = Exception.class)
    public void clearAllSurveyAnswerData(Long surveyId) {
        if (!surveyRepository.existsEffectiveSurveyByIdAndUserId(surveyId, LoginUser.me().getId())) {
            throw new BadRequestAlertException("问卷:" + surveyId + "不存在", Project.entityName(), "survey_not_found");
        }
        surveyAnswerRepository.deleteAllBySurveyId(surveyId);
    }

    @SneakyThrows
    private boolean checkQuestionIsRequired(
        SurveyQuestion question,
        Map<String, Set<Long>> neededLogicMap,
        Map<Long, SurveyLogic> logicMap,
        Map<String, JsonNode> answerMap,
        Map<String, SurveyQuestion> questionMap
    ) {
        if (!question.getRequired()) {
            return false;
        }
        Set<Long> logicIdSet = neededLogicMap.get(question.getQuestionKey());
        if (logicIdSet == null) {
            return question.getRequired();
        }
        if (!QuestionTypeUtil.getIsInputModeQuestion(question.getType())) {
            return false;
        }
        for (Long logicId : logicIdSet) {
            SurveyLogic logic = logicMap.get(logicId);
            JavaType conditionsType = objectMapper.constructType(new TypeReference<List<LogicCondition>>() {});
            List<LogicCondition> conditions = objectMapper.treeToValue(logic.getConditions(), conditionsType);
            if (logic.getExpression().equals(LogicExpression.ALL)) {
                for (LogicCondition condition : conditions) {
                    SurveyQuestion conditionQuestion = questionMap.get(condition.getQuestionKey());
                    JsonNode answerJsonNode = answerMap.get(condition.getQuestionKey());
                    if (answerJsonNode == null) {
                        return false;
                    }
                    if (Radio.TYPE.equals(conditionQuestion.getType())) {
                        RadioResult radioResult = objectMapper.treeToValue(answerJsonNode, RadioResult.class);
                        if (!condition.getOptionId().equals(radioResult.getValue())) {
                            return false;
                        }
                    } else if (CheckBox.TYPE.equals(conditionQuestion.getType())) {
                        CheckBoxResult checkBoxResult = objectMapper.treeToValue(answerJsonNode, CheckBoxResult.class);
                        if (!checkBoxResult.getValue().contains(condition.getOptionId())) {
                            return false;
                        }
                    } else if (Select.TYPE.equals(conditionQuestion.getType())) {
                        String selectResult = objectMapper.treeToValue(answerJsonNode, String.class);
                        if (!condition.getOptionId().equals(selectResult)) {
                            return false;
                        }
                    }
                }
            } else {
                boolean isMet = false;
                for (LogicCondition condition : conditions) {
                    SurveyQuestion conditionQuestion = questionMap.get(condition.getQuestionKey());
                    JsonNode answerJsonNode = answerMap.get(condition.getQuestionKey());
                    if (answerJsonNode == null) {
                        continue;
                    }
                    if (Radio.TYPE.equals(conditionQuestion.getType())) {
                        RadioResult radioResult = objectMapper.treeToValue(answerJsonNode, RadioResult.class);
                        if (condition.getOptionId().equals(radioResult.getValue())) {
                            isMet = true;
                            break;
                        }
                    } else if (CheckBox.TYPE.equals(conditionQuestion.getType())) {
                        CheckBoxResult checkBoxResult = objectMapper.treeToValue(answerJsonNode, CheckBoxResult.class);
                        if (checkBoxResult.getValue().contains(condition.getOptionId())) {
                            isMet = true;
                            break;
                        }
                    } else if (Select.TYPE.equals(conditionQuestion.getType())) {
                        String selectResult = objectMapper.treeToValue(answerJsonNode, String.class);
                        if (condition.getOptionId().equals(selectResult)) {
                            isMet = true;
                            break;
                        }
                    }
                }
                if (!isMet) {
                    return false;
                }
            }
        }
        return true;
    }

    private void setUaInfo(SurveyAnswer surveyAnswer, Long surveyId) {
        String uaStr = ServletUtil.getHeaderIgnoreCase(WebUtil.getRequest(), CommonConstant.USER_AGENT);
        if (StrUtil.isBlank(uaStr)) {
            log.warn("获取user-agent失败，问卷ID: {}", surveyId);
            surveyAnswer.setUa(CommonConstant.UNKNOWN);
            surveyAnswer.setOs(CommonConstant.UNKNOWN);
            surveyAnswer.setBrowser(CommonConstant.UNKNOWN);
            surveyAnswer.setPlatform(CommonConstant.UNKNOWN);
        } else {
            UserAgent userAgent = UserAgentUtil.parse(uaStr);
            surveyAnswer.setUa(uaStr);
            surveyAnswer.setOs(userAgent.getOs().getName());
            surveyAnswer.setBrowser(userAgent.getBrowser().getName());
            surveyAnswer.setPlatform(userAgent.getPlatform().getName());
        }
    }

    private void setIpInfo(SurveyAnswer surveyAnswer, Long surveyId) {
        String ip = IpUtil.getIp(WebUtil.getRequest());
        AddrInfo addrInfo = IpUtil.getAddrInfo(ip);
        surveyAnswer.setIp(ip);
        if (addrInfo == null) {
            log.warn("获取IP地址信息失败, 问卷ID: {}", surveyId);
            surveyAnswer.setIpProvince(CommonConstant.UNKNOWN);
            surveyAnswer.setIpCity(CommonConstant.UNKNOWN);
        } else {
            surveyAnswer.setIpProvince(addrInfo.province());
            surveyAnswer.setIpCity(addrInfo.city());
        }
    }

    public SurveyAnswer getLoginUserLastAnswerBySurveyId(Long surveyId) {
        return surveyAnswerRepository.findTopBySurveyIdAndAnswererIdOrderByCreateTimeDesc(surveyId, LoginUser.me().getId());
    }
}
