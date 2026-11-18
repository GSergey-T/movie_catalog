package com.moviecatalog.movie_catalog.mapper;

import com.moviecatalog.movie_catalog.dto.DirectorDTO;
import com.moviecatalog.movie_catalog.dto.api.director.DirectorRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.director.DirectorResponseDTO;
import com.moviecatalog.movie_catalog.entity.Director;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DirectorMapper {
    
    Director toDirector(DirectorRequestDTO directorRequestDTO);
    
    DirectorResponseDTO toDirectorResponseDTO (Director director);

    DirectorResponseDTO toDirectorResponseDTO (DirectorDTO directorDTO);
    
    void updateDirector(DirectorRequestDTO directorRequestDTO, @MappingTarget Director director);
}
