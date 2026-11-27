package com.moviecatalog.movie_catalog.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = MovieStatusValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMovieStatus {
    String message() default "Status must be one of: PLANNED, FILMING, RELEASED";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
