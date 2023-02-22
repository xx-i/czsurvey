package com.github.czsurvey.common.valication.validator;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.github.czsurvey.common.valication.annonation.PlateNumber;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 车牌号校验器
 * @author YanYu
 */
public class PlateNumberValidator implements ConstraintValidator<PlateNumber, String>{

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StrUtil.isEmpty(value)) {
            return true;
        }
        return Validator.isPlateNumber(value);
    }
}
