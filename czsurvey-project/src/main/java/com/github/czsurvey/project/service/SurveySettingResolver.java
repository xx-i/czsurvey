//package com.github.czsurvey.project.service;
//
//import cn.hutool.core.util.ClassUtil;
//import cn.hutool.extra.spring.SpringUtil;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.github.czsurvey.common.exception.BadRequestAlertException;
//import com.github.czsurvey.project.entity.Survey;
//import com.github.czsurvey.project.entity.SurveySetting;
//import com.github.czsurvey.project.repository.SurveyRepository;
//import com.github.czsurvey.project.repository.SurveySettingRepository;
//import com.github.czsurvey.project.service.setting.validator.SettingValidator;
//import lombok.Data;
//import lombok.Getter;
//import lombok.SneakyThrows;
//import org.reflections.Reflections;
//
//import java.lang.reflect.Method;
//import java.lang.reflect.ParameterizedType;
//import java.lang.reflect.Type;
//import java.util.*;
//
///**
// * @author YanYu
// */
//@Getter
//public class SurveySettingResolver {
//
//    private static final Map<String, SettingDefinition> SETTING_DEFINITION_MAP = new HashMap<>();
//
//    private static final ObjectMapper om = new ObjectMapper();
//
//    private static final SurveySettingRepository surveySettingRepository = SpringUtil.getBean(SurveySettingRepository.class);
//
//    private static final SurveyRepository surveyRepository = SpringUtil.getBean(SurveyRepository.class);
//
//    static {
//        initResolver();
//    }
//
//    private final Long surveyId;
//
//    private SurveySettingResolver(Long surveyId) {
//        this.surveyId = surveyId;
//    }
//
//    @SneakyThrows
//    public static SurveySettingResolver createResolver(Long surveyId) {
//        surveyRepository.findById(surveyId)
//            .orElseThrow(() -> new BadRequestAlertException("问卷不存在", Survey.entityName(), "id_not_found"));
//        return new SurveySettingResolver(surveyId);
//    }
//
//    @SneakyThrows
//    public void set(String key, JsonNode value) {
//        SettingDefinition definition = SETTING_DEFINITION_MAP.get(key);
//        if (definition == null) {
//            throw new IllegalArgumentException("key 不存在");
//        }
//        Object obj = om.treeToValue(value, definition.getSettingType());
//        definition.getVerificationMethod().invoke(definition.getBean(), obj, getSettingMap());
//        SurveySetting setting = surveySettingRepository.findTopBySurveyIdAndSettingKey(surveyId, key)
//            .map(s -> {
//                s.setSettingValue(value);
//                return s;
//            })
//            .orElseGet(() ->  new SurveySetting(null, surveyId, key, om.valueToTree(obj)));
//        surveySettingRepository.save(setting);
//    }
//
//    @SneakyThrows
//    @SuppressWarnings("unchecked")
//    public <T> T get(String key, Class<T> clazz) {
//        SettingDefinition definition = SETTING_DEFINITION_MAP.get(key);
//        if (definition == null) {
//            throw new IllegalArgumentException("key 不存在");
//        }
//        Optional<SurveySetting> setting = surveySettingRepository.findTopBySurveyIdAndSettingKey(surveyId, key);
//        if (setting.isPresent()) {
//            return (T) om.treeToValue(setting.get().getSettingValue(), definition.getSettingType());
//        } else {
//            return (T) definition.getDefaultValue();
//        }
//    }
//
//
//    @SneakyThrows
//    public Map<String, Object> getSettingMap() {
//        Map<String, Object> settingMap = new HashMap<>();
//        List<SurveySetting> settings = surveySettingRepository.findBySurveyId(surveyId);
//
//        for (SurveySetting setting : settings) {
//            SettingDefinition definition = SETTING_DEFINITION_MAP.get(setting.getSettingKey());
//            if (definition == null) {
//                continue;
//            }
//            settingMap.put(setting.getSettingKey(), om.treeToValue(setting.getSettingValue(), definition.getSettingType()));
//        }
//
//        SETTING_DEFINITION_MAP.forEach((key, value) -> {
//            if (!settingMap.containsKey(key)) {
//                settingMap.put(key, value.getDefaultValue());
//            }
//        });
//        return settingMap;
//    }
//
//    @SneakyThrows
//    @SuppressWarnings("all")
//    private static void initResolver() {
//        Reflections reflections = new Reflections("com.github.czsurvey.project.validator");
//        Set<Class<? extends SettingValidator>> validatorClasses = reflections.getSubTypesOf(SettingValidator.class);
//        for (Class<? extends SettingValidator> clazz : validatorClasses) {
//            SettingDefinition definition = new SettingDefinition();
//            SettingValidator instance = clazz.getDeclaredConstructor().newInstance();
//            String settingKey = instance.getKey();
//            definition.setKey(settingKey);
//            definition.setBean(instance);
//            definition.setDefaultValue(instance.getDefaultValue());
//            for (Type genericInterface : clazz.getGenericInterfaces()) {
//                if (genericInterface instanceof ParameterizedType t && t.getTypeName().startsWith(SettingValidator.class.getTypeName())) {
//                    definition.setSettingType((Class<?>) t.getActualTypeArguments()[0]);
//                }
//            }
//            definition.setVerificationMethod(ClassUtil.getDeclaredMethod(clazz, "validate", Object.class, Map.class));
//            SETTING_DEFINITION_MAP.put(definition.getKey(), definition);
//        }
//    }
//
//    @Data
//    public static class SettingDefinition {
//
//        private String key;
//
//        private Class<?> settingType;
//
//        private Method verificationMethod;
//
//        private Object bean;
//
//        private Object defaultValue;
//    }
//}
