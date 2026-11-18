package com.moviecatalog.movie_catalog.dto.api.actor;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class ActorResponseDTO {

    Long id;
    String firstName;
    String lastName;
    LocalDate birthDate;
    String country;
}
