package com.yby.api.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@DecimalMin("0.0")
@DecimalMax("100.0")
public @interface DecimalFactor {

    String message() default "fator deve estar entre 0 e 100";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
