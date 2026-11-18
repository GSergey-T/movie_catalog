package com.moviecatalog.movie_catalog.mapper;

import com.moviecatalog.movie_catalog.dto.ActorDTO;
import com.moviecatalog.movie_catalog.dto.api.actor.ActorRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.actor.ActorResponseDTO;
import com.moviecatalog.movie_catalog.entity.Actor;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ActorMapper {
    
    Actor toActor(ActorRequestDTO actorRequestDTO);
    
    ActorResponseDTO toActorResponseDTO(Actor actor);

    ActorResponseDTO toActorResponseDTO(ActorDTO actorDTO);

    void updateActor(ActorRequestDTO request, @MappingTarget Actor actor);
}
