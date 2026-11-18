package com.moviecatalog.movie_catalog.service;

import com.moviecatalog.movie_catalog.dto.api.director.DirectorRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.director.DirectorResponseDTO;
import com.moviecatalog.movie_catalog.entity.Director;
import com.moviecatalog.movie_catalog.mapper.DirectorMapper;
import com.moviecatalog.movie_catalog.repository.DirectorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DirectorService {
    
    DirectorMapper directorMapper;
    DirectorRepository directorRepository;

    public DirectorService(DirectorMapper directorMapper, DirectorRepository directorRepository) {
        this.directorMapper = directorMapper;
        this.directorRepository = directorRepository;
    }

    @Transactional(readOnly = true)
    public DirectorResponseDTO getDirectorById(Long id) {
        return directorRepository.findDirectorById(id)
                .map(directorMapper::toDirectorResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Director not found with id: " + id
                ));
    }

    @Transactional(readOnly = true)
    public List<DirectorResponseDTO> getAllDirectors() {
        return directorRepository.findAllDirectors()
                .stream()
                .map(directorMapper::toDirectorResponseDTO)
                .toList();
    }

    @Transactional
    public DirectorResponseDTO createDirector(DirectorRequestDTO directorRequestDTO) {
        Director newDirector = directorMapper.toDirector(directorRequestDTO);
        Director savedDirector = directorRepository.save(newDirector);
        return directorMapper.toDirectorResponseDTO(savedDirector);
    }
    
    @Transactional
    public DirectorResponseDTO updateDirector(Long id, DirectorRequestDTO directorRequestDTO) {
        Director director = directorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Director not found with id: " + id
                ));
        
        directorMapper.updateDirector(directorRequestDTO, director);
        
        return directorMapper.toDirectorResponseDTO(director);
    }
    
    @Transactional
    public void deleteDirector(Long id) {
        Director director = directorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Director not found with id: " + id
                ));
        
        directorRepository.delete(director);
    }
}
