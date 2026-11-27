package com.moviecatalog.movie_catalog.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = AgeRatingValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAgeRating {
    String message() default "Age rating must be one of: G, PG, PG-13, R, NC-17, U, 12A, 15, 18";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
