package com.moviecatalog.movie_catalog.mapper;

import com.moviecatalog.movie_catalog.dto.api.movie.MovieRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieResponseDTO;
import com.moviecatalog.movie_catalog.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MovieMapper {
    
    Movie toMovie(MovieRequestDTO movieRequestDTO);
    
    MovieResponseDTO toMovieResponseDTO(Movie movie);
}
