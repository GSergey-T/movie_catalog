package com.moviecatalog.movie_catalog.repository;

import com.moviecatalog.movie_catalog.dto.MovieDTO;
import com.moviecatalog.movie_catalog.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query("""
            SELECT NEW com.moviecatalog.movie_catalog.dto.MovieDTO (m.id, m.title, m.description, m.releaseYear, m.durationMinutes, m.ageRating, m.status)
            FROM Movie m
            """)
    List<MovieDTO> findAllMovies();

    @Query("""
            SELECT NEW com.moviecatalog.movie_catalog.dto.MovieDTO (m.id, m.title, m.description, m.releaseYear, m.durationMinutes, m.ageRating, m.status)
            FROM Movie m
            WHERE m.id = :id
            """)
    Optional<MovieDTO> findMovieById(Long id);
}
