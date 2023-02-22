package com.github.czsurvey.common.valication.validator;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.github.czsurvey.common.valication.annonation.Mobile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


/**
 * 移动电话校验器
 * @author YanYu
 */
public class MobileValidator implements ConstraintValidator<Mobile, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StrUtil.isEmpty(value)) {
            return true;
        }
        return Validator.isMobile(value);
    }
}
