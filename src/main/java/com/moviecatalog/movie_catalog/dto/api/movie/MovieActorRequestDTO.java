package com.moviecatalog.movie_catalog.dto.api.movie;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class MovieActorRequestDTO {

    @NotNull(message = "ID актёра обязателен")
    @Positive(message = "ID актёра должен быть положительным числом")
    Long actorId;
    
    @NotBlank(message = "Имя персонажа обязательно и не должно состоять из пробелов")
    @Size(max = 255, message = "Имя персонажа не должно быть длиннее 255 символов")
    String characterName;
}
