package com.github.czsurvey.project.service.question;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.czsurvey.common.exception.InternalServerErrorException;
import com.github.czsurvey.project.entity.SurveyQuestion;
import com.github.czsurvey.project.service.question.type.CheckBox;
import com.github.czsurvey.project.service.question.type.Radio;
import com.github.czsurvey.project.service.question.type.Select;
import com.github.czsurvey.project.service.question.type.setting.ReferenceSetting;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

//@Service
public class QuestionTypeUtil {

    private static final ObjectMapper OM = new ObjectMapper();

    private static final Map<String, QuestionTypeDefinition> QUESTION_TYPE_DEFINITION_MAP = new HashMap<>();

    private static final Set<String> REFERENCE_QUESTION_TYPE = new HashSet<>();

    private static final Set<String> DISPLAY_MODE_QUESTION_TYPE_SET = new HashSet<>();

    private static final Set<String> INPUT_MODE_QUESTION_TYPE_SET = new HashSet<>();

    static {
        // 忽略json中多余的属性
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        loadQuestionTypeDefinition();
    }

    /**
     * 获取所有的问题类型
     * @return 问题类型
     */
    public static Set<String> getQuestionTypes() {
        return QUESTION_TYPE_DEFINITION_MAP.keySet();
    }

    /**
     * 获取含有输入结果的问题类型
     * @return 问题类型
     */
    public static Set<String> getInputModeQuestionTypes() {
        return QUESTION_TYPE_DEFINITION_MAP.values()
            .stream()
            .filter(QuestionTypeDefinition::isInputModeQuestion)
            .map(QuestionTypeDefinition::getQuestionType)
            .collect(Collectors.toSet());
    }

    public static QuestionType<Object> getQuestionTypeBean(String type) {
        return getQuestionDefinition(type).getBean();
    }

    public static QuestionTypeDefinition getQuestionDefinition(String type) {
        QuestionTypeDefinition definition = QUESTION_TYPE_DEFINITION_MAP.get(type);
        if (definition == null) {
            throw new IllegalArgumentException("问题类型: " + type + " 不存在");
        }
        return definition;
    }

    /**
     * 校验
     * @param type 问题类型
     * @param setting 问题设置
     * @return 去除多余属性后的问题设置
     */
    public static JsonNode validateAndConvertQuestionSetting(String type, JsonNode setting) {
        QuestionType<Object> questionTypeBean = getQuestionTypeBean(type);
        Object settingValue = settingTreeToValue(type, setting);
        questionTypeBean.validateSetting(settingValue);
        return OM.valueToTree(settingValue);
    }

    /**
     * 将treeNode转为对应的setting类型
     * @param questionType 问题类型
     * @param treeNode treeNode
     * @return setting
     */
    @SneakyThrows
    public static Object settingTreeToValue(String questionType, JsonNode treeNode) {
        return OM.treeToValue(treeNode, getQuestionDefinition(questionType).getSettingType());
    }

    /**
     *
     * @param question
     * @param value
     * @param questionMap
     * @param answerMap
     * @return
     */
    @SneakyThrows
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static JsonNode validateAndConvertQuestionResult(
        SurveyQuestion question,
        Map<String, SurveyQuestion> questionMap,
        Map<String, JsonNode> answerMap
    ) {
        Object setting = settingTreeToValue(question.getType(), question.getAdditionalInfo());
        QuestionTypeDefinition questionDefinition = getQuestionDefinition(question.getType());
        if (!questionDefinition.isInputModeQuestion()) {
            return null;
        }
        JsonNode value = answerMap.get(question.getQuestionKey());
        InputModeQuestionType questionTypeBean = (InputModeQuestionType) questionDefinition.getBean();
        Object questionValue = OM.treeToValue(value, questionDefinition.getValueType());
        questionTypeBean.validateResult(
            setting,
            OM.treeToValue(value, questionDefinition.getValueType()),
            questionMap,
            answerMap
        );
        return OM.valueToTree(questionValue);
    }

    public static Set<String> getReferenceQuestionType() {
        return REFERENCE_QUESTION_TYPE;
    }

    public static boolean isReferenceQuestionType(String questionType) {
        return REFERENCE_QUESTION_TYPE.contains(questionType);
    }

    /**
     * 判断题目类型是否为选择题
     * @param type 题目类型
     * @return 是否是选择题
     */
    public static boolean isChoiceQuestion(String type) {
        return Set.of(Radio.TYPE, CheckBox.TYPE, Select.TYPE).contains(type);
    }

    /**
     * 获取选择题所有选项的IDSet
     * @param jsonNode 题目配置
     * @return optionIdSet
     */
    public static Set<String> getChoiceQuestionOptionIdSet(JsonNode jsonNode) {
        JsonNode optionsJsonNode = jsonNode.get("options");
        if (optionsJsonNode == null) {
            throw new InternalServerErrorException("解析选择题类型的配置时出错, 找不到options属性");
        }
        if (optionsJsonNode.isArray()) {
            Set<String> optionIdSet = new HashSet<>();
            for (JsonNode option : optionsJsonNode) {
                optionIdSet.add(option.get("id").asText());
            }
            return optionIdSet;
        }
        throw new InternalServerErrorException("解析选择题类型的配置时出错, options属性不是一个数组");
    }

    public static boolean getIsInputModeQuestion(String type) {
        QuestionTypeDefinition definition = QUESTION_TYPE_DEFINITION_MAP.get(type);
        if (definition == null) {
            throw new InternalServerErrorException("问题类型不存在");
        }
        return definition.isInputModeQuestion;
    }

    public static Set<String> getDisplayModeQuestionTypes() {
        return DISPLAY_MODE_QUESTION_TYPE_SET;
    }

    @SneakyThrows
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void loadQuestionTypeDefinition() {
        ConfigurableListableBeanFactory beanFactory = (ConfigurableListableBeanFactory) SpringUtil.getBeanFactory();
        Map<String, QuestionType> questionTypeMap = beanFactory.getBeansOfType(QuestionType.class);
        for (Map.Entry<String, QuestionType> entry : questionTypeMap.entrySet()) {
            QuestionTypeDefinition questionTypeDefinition = new QuestionTypeDefinition();
            questionTypeDefinition.setQuestionType(entry.getValue().getQuestionType());
            questionTypeDefinition.setBean(entry.getValue());

            String beanClassName = beanFactory.getBeanDefinition(entry.getKey()).getBeanClassName();
            Type[] types = Class.forName(beanClassName).getGenericInterfaces();

            for (Type type : types) {
                if (type instanceof ParameterizedType parameterizedType) {
                    Class<?> questionMode = (Class<?>) parameterizedType.getRawType();
                    if (InputModeQuestionType.class.isAssignableFrom(questionMode)) {
                        questionTypeDefinition.setInputModeQuestion(true);
                        JavaType settingType = OM.constructType(parameterizedType.getActualTypeArguments()[0]);
                        if (settingType.isTypeOrSubTypeOf(ReferenceSetting.class)) {
                            REFERENCE_QUESTION_TYPE.add(entry.getValue().getQuestionType());
                        }
                        questionTypeDefinition.setSettingType(settingType);
                        questionTypeDefinition.setValueType(OM.constructType(parameterizedType.getActualTypeArguments()[1]));
                        break;
                    } else if (QuestionType.class.isAssignableFrom(questionMode)) {
                        questionTypeDefinition.setInputModeQuestion(false);
                        questionTypeDefinition.setSettingType(OM.constructType(parameterizedType.getActualTypeArguments()[0]));
                        break;
                    }
                }
            }

            if (QUESTION_TYPE_DEFINITION_MAP.containsKey(questionTypeDefinition.getQuestionType())) {
                throw new BeanCreationException("问题类型重复，类型：" + questionTypeDefinition.getQuestionType());
            }
            QUESTION_TYPE_DEFINITION_MAP.put(questionTypeDefinition.getQuestionType(), questionTypeDefinition);
            if (questionTypeDefinition.isInputModeQuestion()) {
                INPUT_MODE_QUESTION_TYPE_SET.add(questionTypeDefinition.getQuestionType());
            } else {
                DISPLAY_MODE_QUESTION_TYPE_SET.add(questionTypeDefinition.getQuestionType());
            }
        }
    }

    @Data
    public static class QuestionTypeDefinition {

        private String questionType;

        private JavaType settingType;

        private boolean isInputModeQuestion;

        private JavaType valueType;

        private QuestionType<Object> bean;
    }
}
