package com.moviecatalog.movie_catalog.dto.api.director;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Getter
@Setter
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class DirectorRequestDTO {

    @NotBlank(message = "Имя обязательно и не должно состоять из пробелов")
    @Size(max = 100, message = "Имя не должно быть длиннее 100 символов")
    String firstName;
    
    @NotBlank(message = "Фамилия обязательна и не должна состоять из пробелов")
    @Size(max = 100, message = "Фамилия не должна быть длиннее 100 символов")
    String lastName;
    
    @NotNull(message = "Дата рождения обязательна")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    LocalDate birthDate;
    
    @NotBlank(message = "Страна обязательна и не должна состоять из пробелов")
    @Size(max = 100, message = "Название страны не должно быть длиннее 100 символов")
    String country;

    public DirectorRequestDTO(String firstName, String lastName, LocalDate birthDate, String country) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.country = country;
    }
}
