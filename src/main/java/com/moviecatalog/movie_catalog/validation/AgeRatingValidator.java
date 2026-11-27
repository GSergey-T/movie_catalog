package com.moviecatalog.movie_catalog.validation;

import com.moviecatalog.movie_catalog.enums.AgeRating;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class AgeRatingValidator implements ConstraintValidator<ValidAgeRating, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return Arrays.stream(AgeRating.values())
                .anyMatch(rating -> rating.getValue().equals(value));
    }
}
