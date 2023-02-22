package com.github.czsurvey.common.valication.validator;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.github.czsurvey.common.valication.annonation.IdCard;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 身份证号校验器
 * @author YanYu
 */
public class IdCardValidator implements ConstraintValidator<IdCard, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StrUtil.isEmpty(value)) {
            return true;
        }
        return Validator.isCitizenId(value);
    }
}
