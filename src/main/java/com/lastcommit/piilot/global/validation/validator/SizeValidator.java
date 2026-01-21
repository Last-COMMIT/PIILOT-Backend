package com.lastcommit.piilot.global.validation.validator;

import com.lastcommit.piilot.global.validation.annotation.ValidSize;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SizeValidator implements ConstraintValidator<ValidSize, Integer> {

    private int min;
    private int max;

    @Override
    public void initialize(ValidSize constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value >= min && value <= max;
    }
}
