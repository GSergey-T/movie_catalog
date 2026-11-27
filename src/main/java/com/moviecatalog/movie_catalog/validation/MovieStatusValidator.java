package com.moviecatalog.movie_catalog.validation;

import com.moviecatalog.movie_catalog.enums.MovieStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class MovieStatusValidator implements ConstraintValidator<ValidMovieStatus, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return Arrays.stream(MovieStatus.values())
                .anyMatch(status -> status.name().equals(value));
    }
}
