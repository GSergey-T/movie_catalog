package com.moviecatalog.movie_catalog.repository;

import com.moviecatalog.movie_catalog.entity.MovieActor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieActorRepository extends JpaRepository<MovieActor, Long> {
}
