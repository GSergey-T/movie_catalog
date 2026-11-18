package com.moviecatalog.movie_catalog.repository;

import com.moviecatalog.movie_catalog.dto.ActorDTO;
import com.moviecatalog.movie_catalog.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ActorRepository extends JpaRepository<Actor, Long> {

    @Query("""
        SELECT NEW com.moviecatalog.movie_catalog.dto.ActorDTO( a.id,a.firstName,a.lastName,a.birthDate,a.country)
        FROM Actor a
        """)
    List<ActorDTO> findAllActors();

    @Query("""
        SELECT NEW com.moviecatalog.movie_catalog.dto.ActorDTO (a.id,a.firstName,a.lastName,a.birthDate,a.country)
        FROM Actor a
        WHERE a.id = :id
        """)
    Optional<ActorDTO> findActorById(Long id);
}
