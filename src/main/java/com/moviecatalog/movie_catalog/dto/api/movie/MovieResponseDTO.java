package com.moviecatalog.movie_catalog.dto.api.movie;

import com.moviecatalog.movie_catalog.enums.MovieStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieResponseDTO {

    Long id;
    String title;
    String description;
    Integer releaseYear;
    Integer durationMinutes;
    String ageRating;
    MovieStatus status;
    LocalDateTime createdAt;

    public MovieResponseDTO(Long id, String title, String description, Integer releaseYear, Integer durationMinutes, String ageRating, MovieStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.durationMinutes = durationMinutes;
        this.ageRating = ageRating;
        this.status = status;
    }
}
