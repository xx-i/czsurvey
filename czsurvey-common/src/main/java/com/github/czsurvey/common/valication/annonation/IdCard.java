package com.github.czsurvey.common.valication.annonation;


import com.github.czsurvey.common.valication.validator.IdCardValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 校验是否是身份证号
 * @author YanYu
 */
@Constraint(validatedBy = {IdCardValidator.class})
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface IdCard {

    String message() default "错误的身份证号";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}