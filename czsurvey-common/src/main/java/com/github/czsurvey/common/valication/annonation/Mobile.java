package com.github.czsurvey.common.valication.annonation;

import com.github.czsurvey.common.valication.validator.MobileValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 校验手机号
 * @author YanYu
 */
@Constraint(validatedBy = {MobileValidator.class})
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface Mobile {

    String message() default "错误的电话号码";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
