package com.moviecatalog.movie_catalog.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
@NoArgsConstructor
public class MovieDirectorDTO {

    Long movieId;
    Long directorId;
    String firstName;
    String lastName;

    public MovieDirectorDTO(Long movieId, Long directorId, String firstName, String lastName) {
        this.movieId = movieId;
        this.directorId = directorId;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
