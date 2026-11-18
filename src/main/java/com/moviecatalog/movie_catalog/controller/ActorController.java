package com.moviecatalog.movie_catalog.controller;

import com.moviecatalog.movie_catalog.dto.api.actor.ActorRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.actor.ActorResponseDTO;
import com.moviecatalog.movie_catalog.service.ActorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/actors")
public class ActorController {
    
    private final ActorService actorService;

    public ActorController(ActorService actorService) {
        this.actorService = actorService;
    }

    @PostMapping
    public ResponseEntity<ActorResponseDTO> create(@RequestBody ActorRequestDTO requestDTO) {
        ActorResponseDTO result = actorService.createActor(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ActorResponseDTO> getById(@PathVariable Long id) {
        ActorResponseDTO result = actorService.getActorById(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<ActorResponseDTO>> getAll() {
        List<ActorResponseDTO> result = actorService.getAllActors();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActorResponseDTO> update(@PathVariable Long id, @RequestBody ActorRequestDTO updActorDTO) {
        ActorResponseDTO result = actorService.updateActor(id, updActorDTO);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        actorService.deleteActor(id);
        return ResponseEntity.noContent().build();
    }
}
