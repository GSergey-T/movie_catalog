package com.moviecatalog.movie_catalog.dto.api.movie;

import com.moviecatalog.movie_catalog.enums.MovieStatus;
import com.moviecatalog.movie_catalog.validation.ValidMovieStatus;
import com.moviecatalog.movie_catalog.validation.ValidAgeRating;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class MovieRequestDTO {

    @NotBlank(message = "Название обязательно и не должно состоять из пробелов")
    @Size(max = 255, message = "Название не должно быть длиннее 255 символов")
    String title;

    @Size(max = 5000, message = "Описание не должно быть длиннее 5000 символов")
    String description;

    @NotNull(message = "Год выпуска обязателен")
    @Min(value = 1888, message = "Год выпуска не может быть раньше 1888")
    @Max(value = 2100, message = "Год выпуска не может быть позже 2100")
    Integer releaseYear;

    @NotNull(message = "Продолжительность обязательна")
    @Positive(message = "Продолжительность должна быть больше нуля")
    @Max(value = 1440, message = "Продолжительность не может превышать 1440 минут")
    Integer durationMinutes;
    
    @ValidAgeRating
    String ageRating;
    
    @ValidMovieStatus
    String status;

    @NotNull(message = "Список directorIds обязателен; можно передать пустой массив")
    @Size(max = 100, message = "Нельзя указать больше 100 режиссёров")
    List<@NotNull(message = "ID режиссёра не может быть null") @Positive(message = "ID режиссёра должен быть положительным числом") Long> directorIds;
    
    @NotNull(message = "Список actors обязателен; можно передать пустой массив")
    @Size(max = 100, message = "Нельзя указать больше 100 актёров")
    List<@Valid MovieActorRequestDTO> actors;

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
        this.status = status.name();
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
