package com.moviecatalog.movie_catalog.mapper;

import com.moviecatalog.movie_catalog.dto.MovieDTO;
import com.moviecatalog.movie_catalog.dto.MovieActorDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieActorRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieActorResponseDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieResponseDTO;
import com.moviecatalog.movie_catalog.entity.Movie;
import com.moviecatalog.movie_catalog.entity.MovieActor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = DirectorMapper.class)
public interface MovieMapper {

    @Mapping(target = "actorList", ignore = true)
    Movie toMovie(MovieRequestDTO movieRequestDTO);

    @Mapping(
            target = "directors",
            source = "directorList",
            qualifiedByName = "toDirectorSummary"
    )
    @Mapping(target = "actors", source = "actorList")
    MovieResponseDTO toMovieResponseDTO(Movie movie);

    MovieResponseDTO toMovieResponseDTO(MovieDTO movieDTO);

    @Mapping(target = "directorList", ignore = true)
    @Mapping(target = "actorList", ignore = true)
    void updateMovie(MovieRequestDTO movieRequestDTO, @MappingTarget Movie movie);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actor", ignore = true)
    @Mapping(target = "movie", ignore = true)
    MovieActor toMovieActor(MovieActorRequestDTO movieActorRequestDTO);

    @Mapping(target = "id", source = "actor.id")
    @Mapping(target = "firstName", source = "actor.firstName")
    @Mapping(target = "lastName", source = "actor.lastName")
    MovieActorResponseDTO toMovieActorResponseDTO(MovieActor movieActor);

    @Mapping(target = "id", source = "actorId")
    MovieActorResponseDTO toMovieActorResponseDTO(MovieActorDTO movieActorDTO);
}
