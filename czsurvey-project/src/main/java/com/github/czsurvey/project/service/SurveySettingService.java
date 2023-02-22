package com.github.czsurvey.project.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.github.czsurvey.common.exception.InternalServerErrorException;
import com.github.czsurvey.common.exception.InvalidParameterException;
import com.github.czsurvey.extra.security.model.LoginUser;
import com.github.czsurvey.project.entity.Survey;
import com.github.czsurvey.project.entity.SurveySetting;
import com.github.czsurvey.project.entity.enums.AnswererType;
import com.github.czsurvey.project.repository.SurveyRepository;
import com.github.czsurvey.project.repository.SurveySettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author YanYu
 */
@Service
@RequiredArgsConstructor
public class SurveySettingService {

    private final SurveyRepository surveyRepository;

    private final SurveySettingRepository surveySettingRepository;

    public SurveySetting getSurveySetting(Long surveyId) {
        return surveySettingRepository.findBySurveyId(surveyId)
            .orElseThrow(() -> new InvalidParameterException("问卷设置不存在", SurveySetting.entityName()));
    }

    public void setSurveySetting(Map<String, Object> settingRequest) {
        long surveyId = getSurveyIdFormMap(settingRequest);
        if (!surveyRepository.existsEffectiveSurveyByIdAndUserId(surveyId, LoginUser.me().getId())) {
            throw new InvalidParameterException("问卷:" + surveyId + "不存在", Survey.entityName());
        }
        SurveySetting setting = surveySettingRepository.findBySurveyId(surveyId)
            .orElseThrow(() -> new InternalServerErrorException("服务器异常，找不到该问卷的配置信息"));

        BeanUtil.fillBeanWithMap(settingRequest, setting, true, CopyOptions.create().ignoreNullValue());
        if (settingRequest.containsKey("beginTime") && settingRequest.get("beginTime") == null) {
            setting.setBeginTime(null);
        }
        if (settingRequest.containsKey("endTime") && settingRequest.get("endTime") == null) {
            setting.setEndTime(null);
        }

        if (!setting.getLoginRequired()) {
            if (setting.getAnswererType().equals(AnswererType.DESIGNATED_CONTACTS)) {
                throw new InvalidParameterException("设置指定联系人时需要先开启登录验证", SurveySetting.entityName());
            }
            if (setting.getEnableUserAnswerLimit()) {
                throw new InvalidParameterException("设置用户回答次数限制时需要先开启登录验证", SurveySetting.entityName());
            }
            if (setting.getAddToContact()) {
                throw new InvalidParameterException("设置将答题者保存为联系人时需要先开启登录验证", SurveySetting.entityName());
            }
            if (setting.getEnableChange()) {
                throw new InvalidParameterException("设置允许用户修改时需要先开启登录验证", SurveySetting.entityName());
            }
            if (setting.getAnonymously()) {
                throw new InvalidParameterException("设置匿名回收时需要先开启登录验证", SurveySetting.entityName());
            }
        }

        if (AnswererType.DESIGNATED_CONTACTS.equals(setting.getAnswererType())) {
            // todo: 指定联系人可答逻辑
        }

        if (setting.getEnableUserAnswerLimit()) {
            if (setting.getUserLimitFreq() == null) {
                throw new InvalidParameterException("回答频率不能为空", SurveySetting.entityName());
            }
            if (setting.getUserLimitNum() == null || setting.getUserLimitNum() < 1) {
                throw new InvalidParameterException("回答次数不能为空且必须大于0", SurveySetting.entityName());
            }
        } else {
            setting.setUserLimitFreq(null);
            setting.setUserLimitNum(null);
        }
        if (setting.getEnableIpAnswerLimit()) {
            if (setting.getIpLimitFreq() == null) {
                throw new InvalidParameterException("回答频率不能为空", SurveySetting.entityName());
            }
            if (setting.getIpLimitNum() == null || setting.getIpLimitNum() < 1) {
                throw new InvalidParameterException("回答次数不能为空且必须大于0", SurveySetting.entityName());
            }
        } else {
            setting.setIpLimitFreq(null);
            setting.setIpLimitNum(null);
        }

        if (
            setting.getBeginTime() != null
            && setting.getEndTime() != null
            && !setting.getEndTime().isAfter(setting.getBeginTime())
        ) {
            throw new InvalidParameterException("结束时间必须在开始时间之后", SurveySetting.entityName());
        }
        if (setting.getMaxAnswers() < 0) {
            throw new InvalidParameterException("问卷回收上限不能小于0", SurveySetting.entityName());
        }
        surveySettingRepository.save(setting);
    }

    private long getSurveyIdFormMap(Map<String, Object> map) {
        long surveyId;
        Object idObj = map.get("surveyId");
        if (idObj == null) {
            throw new InvalidParameterException("问卷ID不能为空", Survey.entityName());
        }
        if (idObj instanceof String idStr) {
            surveyId = Long.parseLong(idStr);
        } else if (idObj instanceof Long idLong) {
            surveyId = idLong;
        } else {
            throw new InvalidParameterException("问卷ID必须是一个数字", Survey.entityName());
        }
        return surveyId;
    }
}
