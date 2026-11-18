package com.moviecatalog.movie_catalog.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class DirectorDTO {

    Long id;
    String firstName;
    String lastName;
    LocalDate birthDate;
    String country;

    public DirectorDTO(Long id, String firstName, String lastName, LocalDate birthDate, String country) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.country = country;
    }
}
