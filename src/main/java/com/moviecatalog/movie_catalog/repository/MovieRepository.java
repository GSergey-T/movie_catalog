package com.moviecatalog.movie_catalog.repository;

import com.moviecatalog.movie_catalog.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}
