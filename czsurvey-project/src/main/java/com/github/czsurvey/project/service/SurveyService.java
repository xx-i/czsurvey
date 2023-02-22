package com.github.czsurvey.project.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.czsurvey.common.exception.BadRequestAlertException;
import com.github.czsurvey.common.exception.InternalServerErrorException;
import com.github.czsurvey.common.util.IpUtil;
import com.github.czsurvey.common.util.WebUtil;
import com.github.czsurvey.extra.security.model.LoginUser;
import com.github.czsurvey.project.constant.ProjectConstant;
import com.github.czsurvey.project.constant.SettingConstant;
import com.github.czsurvey.project.constant.SurveyConstant;
import com.github.czsurvey.project.entity.*;
import com.github.czsurvey.project.entity.enums.*;
import com.github.czsurvey.project.repository.*;
import com.github.czsurvey.project.request.SurveyStatusRequest;
import com.github.czsurvey.project.request.SurveySummaryRequest;
import com.github.czsurvey.project.response.SurveyDetailResponse;
import com.github.czsurvey.project.response.SurveyPageResponse;
import com.github.czsurvey.project.response.SurveyTerseStatResponse;
import com.github.czsurvey.project.service.question.type.Radio;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author YanYu
 */
@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;

    private final ProjectRepository projectRepository;

    private final SurveyPageRepository surveyPageRepository;

    private final SurveyQuestionRepository surveyQuestionRepository;

    private final SurveySettingRepository surveySettingRepository;

    private final SurveyLogicRepository surveyLogicRepository;

    private final SurveyAnswerRepository surveyAnswerRepository;

    private final ObjectMapper objectMapper;

    public Survey getSurvey(Long surveyId) {
        return surveyRepository.findEffectiveSurveyByIdAndUserId(surveyId, LoginUser.me().getId())
            .orElseThrow(() -> new BadRequestAlertException("问卷:" + surveyId + "不存在", Project.entityName(), "survey_not_found"));
    }

    public SurveyDetailResponse getSurveyDetail(Long surveyId) {
        Supplier<BadRequestAlertException> surveyNotFound =
            () -> new BadRequestAlertException("问卷:" + surveyId + "不存在", Project.entityName(), "survey_not_found");
        // 问卷被移入回收站
        projectRepository.findEffectiveProjectBySurveyId(surveyId).orElseThrow(surveyNotFound);

        SurveySetting setting = surveySettingRepository.findBySurveyId(surveyId).orElseThrow(surveyNotFound);
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(surveyNotFound);
        List<SurveyPageResponse> pages = surveyPageRepository
            .findBySurveyIdOrderByOrderNum(surveyId)
            .stream()
            .map(page -> {
                SurveyPageResponse pageResponse = new SurveyPageResponse();
                pageResponse.setPageKey(page.getPageKey());
                pageResponse.setOrderNum(page.getOrderNum());
                pageResponse.setQuestions(new ArrayList<>());
                return pageResponse;
            })
            .toList();
        List<SurveyQuestion> questions = surveyQuestionRepository.findBySurveyIdOrderByOrderNum(surveyId);
        Map<String, Integer> pageIndexMap = new HashMap<>();
        for (int i = 0; i < pages.size(); i++) {
            pageIndexMap.put(pages.get(i).getPageKey(), i);
        }
        questions.forEach(ques -> pages.get(pageIndexMap.get(ques.getPageKey())).getQuestions().add(ques));

        List<SurveyLogic> logics = surveyLogicRepository.findBySurveyId(surveyId);

        SurveyDetailResponse surveyDetailResponse = new SurveyDetailResponse();
        surveyDetailResponse.setSurvey(survey);
        surveyDetailResponse.setPages(pages);
        surveyDetailResponse.setSettings(setting);
        surveyDetailResponse.setLogics(logics);

        return surveyDetailResponse;
    }

    /**
     * 新建一个问卷
     * @param folderId 目录ID
     * @return 问卷
     */
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    public Survey createSurvey(Long folderId) {
        if (!ProjectConstant.ROOT_PARENT_ID.equals(folderId)) {
            projectRepository
                .findById(folderId)
                .orElseThrow(() -> new BadRequestAlertException("文件夹不存在" + folderId, Project.entityName(), "id_not_found"));
        }

        // create survey
        Survey survey = new Survey();
        survey.setTitle(SurveyConstant.DEFAULT_SURVEY_TITLE);
        survey.setInstruction(objectMapper.readTree(SurveyConstant.DEFAULT_SURVEY_INSTRUCTION));
        survey.setConclusion(objectMapper.readTree(SurveyConstant.DEFAULT_SURVEY_CONCLUSION));
        survey.setStatus(ProjectStatus.CLOSE);
        survey.setUserId(LoginUser.me().getId());
        survey = surveyRepository.save(survey);

        // create page
        SurveyPage page = new SurveyPage(survey.getId(), "p_" + RandomUtil.randomString(4), 0);
        surveyPageRepository.save(page);

        //create question
        SurveyQuestion question = new SurveyQuestion();
        question.setQuestionKey("q_" + RandomUtil.randomString(4));
        question.setTitle(objectMapper.readTree(SurveyConstant.DEFAULT_SURVEY_QUESTION_TITLE));
        question.setPageKey(page.getPageKey());
        question.setSurveyId(survey.getId());
        question.setType(Radio.TYPE);
        question.setOrderNum(0);
        question.setRequired(true);
        question.setAdditionalInfo(objectMapper.readTree(SurveyConstant.DEFAULT_SURVEY_RADIO_SETTING));
        surveyQuestionRepository.save(question);

        // create setting
        SurveySetting setting = new SurveySetting();
        setting.setSurveyId(survey.getId());
        setting.setDisplayQuestionNo(true);
        setting.setAllowRollback(true);
        setting.setLoginRequired(false);
        setting.setAnswererType(AnswererType.ALL);
        setting.setEnableUserAnswerLimit(false);
        setting.setEnableIpAnswerLimit(false);
        setting.setMaxAnswers(SettingConstant.NO_LIMIT_MAX_ANSWER);
        setting.setAddToContact(false);
        setting.setEnableChange(false);
        setting.setAnonymously(false);
        setting.setBreakpointResume(false);
        setting.setSaveLastAnswer(false);
        surveySettingRepository.save(setting);

        //create project
        Project project = new Project();
        project.setName(survey.getTitle());
        project.setParentId(folderId);
        project.setOwnerType(ProjectType.SURVEY);
        project.setOwnerId(survey.getId());
        project.setDeleted(false);
        project.setUserId(LoginUser.me().getId());
        projectRepository.save(project);
        return survey;
    }

    @Transactional(rollbackFor = Exception.class)
    public Survey updateSurvey(Long surveyId, SurveySummaryRequest surveySummaryRequest) {
        Supplier<BadRequestAlertException> surveyNotFound =
            () -> new BadRequestAlertException("问卷:" + surveyId + "不存在", Project.entityName(), "survey_not_found");
        Project project = projectRepository
            .findEffectiveProjectBySurveyIdAndUserId(surveyId, LoginUser.me().getId())
            .orElseThrow(surveyNotFound);
        Survey survey = surveyRepository
            .findEffectiveSurveyByIdAndUserId(surveyId, LoginUser.me().getId())
            .orElseThrow(surveyNotFound);

        BeanUtil.copyProperties(surveySummaryRequest, survey, CopyOptions.create().ignoreNullValue());
        project.setName(survey.getTitle());
        projectRepository.save(project);
        return surveyRepository.save(survey);
    }

    public ProjectStatus updateSurveyStatus(SurveyStatusRequest surveyStatusRequest) {
        Survey survey = surveyRepository.findEffectiveSurveyByIdAndUserId(surveyStatusRequest.getSurveyId(), LoginUser.me().getId())
            .orElseThrow(() -> new BadRequestAlertException("问卷:" + surveyStatusRequest.getSurveyId() + "不存在", Project.entityName(), "survey_not_found"));
        survey.setStatus(surveyStatusRequest.getStatus());
        surveyRepository.save(survey);
        return surveyStatusRequest.getStatus();
    }

    @Transactional(rollbackFor = Exception.class)
    public void renameSurveyTitle(Long surveyId, String surveyTitle) {
        Survey survey = surveyRepository.findEffectiveSurveyByIdAndUserId(surveyId, LoginUser.me().getId())
            .orElseThrow(() -> new BadRequestAlertException("问卷:" + surveyId + "不存在", Project.entityName(), "survey_not_found"));
        survey.setTitle(surveyTitle);
        Project project = projectRepository.findByOwnerIdAndOwnerType(surveyId, ProjectType.SURVEY)
            .orElseThrow(() -> new InternalServerErrorException("找不到该问卷所属的项目"));
        project.setName(surveyTitle);
        projectRepository.save(project);
    }

    public SurveyStatus getSurveyAnswerState(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
            .orElseThrow(() -> new BadRequestAlertException("问卷:" + surveyId + "不存在", Project.entityName(), "survey_not_found"));

        /* 决定问卷是否可以填写的配置的判断放最前面，因为即使设置了允许用户修改该问卷也将是不可回答状态 */
        if (survey.getStatus().equals(ProjectStatus.CLOSE)) {
            return SurveyStatus.NOT_OPEN;
        }
        SurveySetting setting = surveySettingRepository.findById(surveyId)
            .orElseThrow(() -> new InternalServerErrorException("找不到该问卷的配置，问卷ID: " + surveyId));

        if (setting.getLoginRequired()) {
            LoginUser.me();
        }

        LocalDateTime beginTime = setting.getBeginTime();
        LocalDateTime endTime = setting.getEndTime();
        if (beginTime != null && LocalDateTime.now().isBefore(beginTime)) {
            return SurveyStatus.NOT_STATED;
        }
        if (endTime != null && LocalDateTime.now().isAfter(endTime)) {
            return SurveyStatus.FINISHED;
        }

        /* 用户回答次数限制的判断放后面，如果设置了允许用户修改，则可以修改之前的问卷 */
        if (setting.getEnableUserAnswerLimit()) {
            LoginUser user = LoginUser.me();
            LimitFreq userLimitFreq = setting.getUserLimitFreq();
            Long answeredCount;
            if (userLimitFreq.equals(LimitFreq.ONLY)) {
                answeredCount = surveyAnswerRepository.countBySurveyIdAndAnswererId(surveyId, user.getId());
            } else {
                LocalDateTime startDateTime = getStartDateTimeByLimitFreq(userLimitFreq);
                answeredCount = surveyAnswerRepository.countBySurveyIdAndAnswererIdAndCreateTimeAfter(surveyId, user.getId(), startDateTime);
            }
            if (answeredCount >= setting.getUserLimitNum()) {
                return setting.getEnableChange() ? SurveyStatus.ALREADY_ANSWERED_BUT_CAD_MODIFY : SurveyStatus.ALREADY_ANSWERED;
            }
        }
        if (setting.getEnableIpAnswerLimit()) {
            String ip = IpUtil.getIp(WebUtil.getRequest());
            LimitFreq ipLimitFreq = setting.getIpLimitFreq();
            Long answeredCount;
            if (ipLimitFreq.equals(LimitFreq.ONLY)) {
                answeredCount = surveyAnswerRepository.countBySurveyIdAndIp(surveyId, ip);
            } else {
                LocalDateTime startDateTime = getStartDateTimeByLimitFreq(ipLimitFreq);
                answeredCount = surveyAnswerRepository.countBySurveyIdAndIpAndCreateTimeAfter(surveyId, ip, startDateTime);
            }
            if (answeredCount >= setting.getIpLimitNum()) {
                return setting.getEnableChange() ? SurveyStatus.ALREADY_ANSWERED_BUT_CAD_MODIFY : SurveyStatus.ALREADY_ANSWERED;
            }
        }

        /* 最大回答次数的判断放到用户答题次数限制的后面，如果用户已经回答过则不去校验最大回答次数 */
        Integer maxAnswers = setting.getMaxAnswers();
        if (maxAnswers > 0 && maxAnswers <= surveyAnswerRepository.countBySurveyId(surveyId)) {
            return setting.getEnableChange() ? SurveyStatus.EXCEED_LIMIT_BUT_CAN_MODIFY : SurveyStatus.EXCEED_LIMIT;
        }
        return SurveyStatus.NORMAL;
    }

    private LocalDateTime getStartDateTimeByLimitFreq(LimitFreq freq) {
        LocalDate nowDate = LocalDate.now();
        LocalDateTime nowDateTime = LocalDateTime.now();
        return switch (freq) {
            case HOUR -> nowDate.atTime(nowDateTime.getHour(), 0);
            case DAY -> nowDate.atStartOfDay();
            case WEEK -> nowDate.with(ChronoField.DAY_OF_WEEK, 1).atStartOfDay();
            case WITHIN_7_DAYS -> nowDateTime.plusDays(-7);
            case MONTH -> nowDate.with(ChronoField.DAY_OF_MONTH, 1).atStartOfDay();
            case WITHIN_30_DAYS -> nowDateTime.plusDays(-30);
            default -> throw new InternalServerErrorException("不存在的时间限制");
        };
    }


    public void deleteSurveyPage(Long surveyId, String pageKey) {
        surveyRepository.findEffectiveSurveyByIdAndUserId(surveyId, LoginUser.me().getId())
            .orElseThrow(() -> new BadRequestAlertException("问卷:" + surveyId + "不存在", Survey.entityName(), "survey_not_found"));
        List<SurveyQuestion> pageQuestions = surveyQuestionRepository.findBySurveyIdAndPageKey(surveyId, pageKey);
        if (pageQuestions.size() == 0) {
            throw new BadRequestAlertException("分页:" + pageKey + "不存在", SurveyPage.entityName());
        }

        Set<String> pageQuestionKeys = pageQuestions
            .stream()
            .map(SurveyQuestion::getQuestionKey)
            .collect(Collectors.toSet());

        if (surveyPageRepository.countBySurveyId(surveyId) <= 1) {
            throw new BadRequestAlertException("一个问卷至少存在一页", SurveyPage.entityName());
        }
        List<String> questionKeys = surveyQuestionRepository.findQuestionKeyByRefQuestionKeyIn(surveyId, pageQuestionKeys);
        if (!pageQuestionKeys.containsAll(questionKeys)) {
            throw new BadRequestAlertException("页面中存在被其它页面引用的题目，不能被删除", SurveyPage.entityName());
        }

        Long conditionalRefLogicCount = surveyLogicRepository.countByConditionQuestionKeys(surveyId, pageQuestionKeys);
        Long resultRefLogicCount = surveyLogicRepository.countByResultQuestionKeys(surveyId, pageQuestionKeys);

        if (conditionalRefLogicCount > 0 || resultRefLogicCount > 0) {
            throw new BadRequestAlertException("页面中存在被逻辑引用的题目，不能被删除", SurveyPage.entityName());
        }

        surveyQuestionRepository.deleteAll(pageQuestions);
        surveyPageRepository.deleteById(new SurveyPagePrimaryKey(surveyId, pageKey));
    }

    public SurveyTerseStatResponse getTerseStat(Long surveyId) {
        surveyRepository.findEffectiveSurveyByIdAndUserId(surveyId, LoginUser.me().getId())
            .orElseThrow(() -> new BadRequestAlertException("问卷:" + surveyId + "不存在", Survey.entityName(), "survey_not_found"));
        Long todayCount = surveyAnswerRepository.countBySurveyId(surveyId);
        Long totalCount = surveyAnswerRepository.countBySurveyIdAndCreateTimeAfter(surveyId, LocalDate.now().atStartOfDay());
        Integer avgDuration = surveyAnswerRepository.getAvgDurationBySurveyId(surveyId);
        return new SurveyTerseStatResponse(todayCount, totalCount, avgDuration);
    }
}
