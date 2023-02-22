package com.github.czsurvey.common.valication.annonation;

import com.github.czsurvey.common.valication.validator.FlagValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 标识位校验
 * 校验字段的值是否在指定的值中
 *
 * @author YanYu
 */
@Constraint(validatedBy = {FlagValidator.StringValidator.class, FlagValidator.CollectionValidator.class})
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface Flag {

    String[] value() default {};

    /* 获取枚举类中所有属性为Value的值 */
    Class<? extends Enum<?>> enumClass() default DefaultEnum.class;

    /* 枚举类中标识字段属性的字段名 */
    String enumField() default "value";

    String message() default "不存在的标志位";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    enum DefaultEnum { }
}
