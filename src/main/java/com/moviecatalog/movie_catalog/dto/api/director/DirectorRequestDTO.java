package com.moviecatalog.movie_catalog.dto.api.director;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class DirectorRequestDTO {

    String firstName;
    String lastName;
    LocalDate birthDate;
    String country;

    public DirectorRequestDTO(String firstName, String lastName, LocalDate birthDate, String country) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.country = country;
    }
}
