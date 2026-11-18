package com.moviecatalog.movie_catalog.controller;


import com.moviecatalog.movie_catalog.dto.api.director.DirectorRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.director.DirectorResponseDTO;
import com.moviecatalog.movie_catalog.service.DirectorService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/directors")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DirectorController {
    
    DirectorService directorService;

    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }
    
    @PostMapping
    public ResponseEntity<DirectorResponseDTO> create(@RequestBody DirectorRequestDTO request) {
        DirectorResponseDTO result = directorService.createDirector(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DirectorResponseDTO> getById(@PathVariable Long id) {
        DirectorResponseDTO result = directorService.getDirectorById(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<DirectorResponseDTO>> getAll() {
        List<DirectorResponseDTO> result = directorService.getAllDirectors();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DirectorResponseDTO> update(@PathVariable Long id, @RequestBody DirectorRequestDTO request) {
        DirectorResponseDTO result = directorService.updateDirector(id, request);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        directorService.deleteDirector(id);
        return ResponseEntity.noContent().build();
    }
}
