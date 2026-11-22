package com.moviecatalog.movie_catalog.mapper;

import com.moviecatalog.movie_catalog.dto.MovieDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieResponseDTO;
import com.moviecatalog.movie_catalog.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = DirectorMapper.class)
public interface MovieMapper {
    
    Movie toMovie(MovieRequestDTO movieRequestDTO);

    @Mapping(
            target = "directors",
            source = "directorList",
            qualifiedByName = "toDirectorSummary"
    )
    MovieResponseDTO toMovieResponseDTO(Movie movie);

    MovieResponseDTO toMovieResponseDTO(MovieDTO movieDTO);

    @Mapping(target = "directorList", ignore = true)
    void updateMovie(MovieRequestDTO movieRequestDTO, @MappingTarget Movie movie);
}
