package com.moviecatalog.movie_catalog.service;

import com.moviecatalog.movie_catalog.dto.ActorDTO;
import com.moviecatalog.movie_catalog.dto.api.actor.ActorRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.actor.ActorResponseDTO;
import com.moviecatalog.movie_catalog.entity.Actor;
import com.moviecatalog.movie_catalog.mapper.ActorMapper;
import com.moviecatalog.movie_catalog.repository.ActorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ActorService {

    private final ActorMapper actorMapper;
    private final ActorRepository actorRepository;

    public ActorService(ActorMapper actorMapper, ActorRepository actorRepository) {
        this.actorMapper = actorMapper;
        this.actorRepository = actorRepository;
    }

    @Transactional(readOnly = true)
    public ActorResponseDTO getActorById(Long id) {
        return actorRepository.findActorById(id)
                .map(actorMapper::toActorResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Actor not found with id: " + id
                ));
    }

    @Transactional(readOnly = true)
    public List<ActorResponseDTO> getAllActors() {
        List<ActorDTO> actorDTOList = actorRepository.findAllActors();

        return actorDTOList.stream()
                .map(actorMapper::toActorResponseDTO)
                .toList();
    }

    @Transactional
    public ActorResponseDTO createActor(ActorRequestDTO actorRequestDTO) {
        Actor newActor = actorMapper.toActor(actorRequestDTO);
        Actor savingActor = actorRepository.save(newActor);
        return actorMapper.toActorResponseDTO(savingActor);
    }

    @Transactional
    public ActorResponseDTO updateActor(Long id, ActorRequestDTO updActorDTO) {
        Actor actor = actorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Actor not found with id: " + id
                ));
        
        actorMapper.updateActor(updActorDTO, actor);
        
        return actorMapper.toActorResponseDTO(actor);
    }

    @Transactional
    public void deleteActor(Long id) {
        Actor actor = actorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Actor not found with id: " + id
                ));
        
        actorRepository.delete(actor);
    }
}
