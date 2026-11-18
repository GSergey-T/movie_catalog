package com.moviecatalog.movie_catalog.repository;

import com.moviecatalog.movie_catalog.entity.Director;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectorRepository extends JpaRepository<Director, Long> {
}
