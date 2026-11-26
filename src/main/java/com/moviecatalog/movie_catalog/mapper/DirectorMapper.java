package com.moviecatalog.movie_catalog.mapper;

import com.moviecatalog.movie_catalog.dto.DirectorDTO;
import com.moviecatalog.movie_catalog.dto.MovieDirectorDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieDirectorResponseDTO;
import com.moviecatalog.movie_catalog.dto.api.director.DirectorRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.director.DirectorResponseDTO;
import com.moviecatalog.movie_catalog.entity.Director;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DirectorMapper {
    
    Director toDirector(DirectorRequestDTO directorRequestDTO);
    
    DirectorResponseDTO toDirectorResponseDTO (Director director);

    DirectorResponseDTO toDirectorResponseDTO (DirectorDTO directorDTO);

    @Named("toMovieDirector")
    MovieDirectorResponseDTO toMovieDirectorDTO(Director director);

    @Mapping(source = "directorId", target = "id")
    MovieDirectorResponseDTO toMovieDirectorDTO(MovieDirectorDTO movieDirectorDTO);
    
    void updateDirector(DirectorRequestDTO directorRequestDTO, @MappingTarget Director director);
}
