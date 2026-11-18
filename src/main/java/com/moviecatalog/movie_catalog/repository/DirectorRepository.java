package com.moviecatalog.movie_catalog.repository;

import com.moviecatalog.movie_catalog.dto.DirectorDTO;
import com.moviecatalog.movie_catalog.entity.Director;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DirectorRepository extends JpaRepository<Director, Long> {
    
    @Query("""
            SELECT NEW com.moviecatalog.movie_catalog.dto.DirectorDTO (d.id,d.firstName,d.lastName,d.birthDate,d.country)
            FROM Director d
            """)
    List<DirectorDTO> findAllDirectors();

    @Query("""
            SELECT NEW com.moviecatalog.movie_catalog.dto.DirectorDTO (d.id,d.firstName,d.lastName,d.birthDate,d.country)
            FROM Director d
            WHERE d.id = :id
            """)
    Optional<DirectorDTO> findDirectorById(Long id);
}
