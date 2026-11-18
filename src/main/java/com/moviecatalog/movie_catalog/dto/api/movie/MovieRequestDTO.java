package com.moviecatalog.movie_catalog.dto.api.movie;

import com.moviecatalog.movie_catalog.enums.MovieStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieRequestDTO {

    String title;
    String description;
    Integer releaseYear;
    Integer durationMinutes;
    String ageRating;
    MovieStatus status;
}
