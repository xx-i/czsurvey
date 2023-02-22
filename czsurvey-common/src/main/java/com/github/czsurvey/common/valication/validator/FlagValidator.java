package com.github.czsurvey.common.valication.validator;

import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.czsurvey.common.valication.annonation.Flag;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标识位校验器
 * @author YanYu
 */
public class FlagValidator {

    private static List<String> getFlagList(Flag constraintAnnotation) {
        Class<? extends Enum<?>> enumClass = constraintAnnotation.enumClass();
        /* 优先判断注解中value的值 */
        if (ObjectUtil.isNotEmpty(constraintAnnotation.value())) {
            return Arrays.asList(constraintAnnotation.value());
            /* 如果value没有值，则判断枚举类。枚举类必须有 value 字段 */
        } else if (!Flag.DefaultEnum.class.equals(enumClass)) {
            List<Object> list = EnumUtil.getFieldValues(enumClass, constraintAnnotation.enumField());
            return list.stream().map(Object::toString).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    /**
     * 校验String类型
     */
    public static class StringValidator implements ConstraintValidator<Flag, String> {

        private List<String> values;

        @Override
        public void initialize(Flag constraintAnnotation) {
            this.values = getFlagList(constraintAnnotation);
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            /* 放过空值校验 */
            if (StrUtil.isEmpty(value)) {
                return true;
            }
            /* 如果从注解中获取的集合的值为空的话，可能是代码写错了，这时候应该检查一下代码 */
            if (ObjectUtil.isEmpty(values)) {
                return false;
            }
            return values.contains(value);
        }
    }

    /**
     * 集合类型校验器
     */
    public static class CollectionValidator implements ConstraintValidator<Flag, Collection<String>> {

        private List<String> values;

        @Override
        public void initialize(Flag constraintAnnotation) {
            this.values = getFlagList(constraintAnnotation);
        }

        @Override
        public boolean isValid(Collection<String> value, ConstraintValidatorContext context) {
            if (ObjectUtil.isEmpty(value)) {
                return true;
            }
            for (String next : value) {
                if (!values.contains(next)) {
                    return false;
                }
            }
            return true;
        }
    }
}