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
public class MovieActorDTO {

    Long movieId;
    Long actorId;
    String firstName;
    String lastName;
    String characterName;

    public MovieActorDTO(Long movieId, Long actorId, String firstName, String lastName, String characterName) {
        this.movieId = movieId;
        this.actorId = actorId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.characterName = characterName;
    }
}
