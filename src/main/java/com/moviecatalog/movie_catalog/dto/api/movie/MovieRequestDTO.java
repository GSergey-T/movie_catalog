package com.moviecatalog.movie_catalog.dto.api.movie;

import com.moviecatalog.movie_catalog.enums.MovieStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class MovieRequestDTO {

    String title;
    String description;
    Integer releaseYear;
    Integer durationMinutes;
    String ageRating;
    MovieStatus status;
    List<Long> directorIds;
    List<MovieActorRequestDTO> actors;

    public MovieRequestDTO(String title, 
                           String description, 
                           Integer releaseYear, 
                           Integer durationMinutes, 
                           String ageRating, 
                           MovieStatus status, 
                           List<Long> directorIds) {
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.durationMinutes = durationMinutes;
        this.ageRating = ageRating;
        this.status = status;
        this.directorIds = directorIds;
    }

    public MovieRequestDTO(String title, 
                           String description, 
                           Integer releaseYear, 
                           Integer durationMinutes,
                           String ageRating, 
                           MovieStatus status, 
                           List<Long> directorIds,
                           List<MovieActorRequestDTO> actors) {
        this(title, description, releaseYear, durationMinutes, ageRating, status, directorIds);
        this.actors = actors;
    }
}
