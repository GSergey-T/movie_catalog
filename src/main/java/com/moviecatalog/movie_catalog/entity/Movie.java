package com.moviecatalog.movie_catalog.entity;

import com.moviecatalog.movie_catalog.enums.MovieStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "movie_seq_gen")
    @SequenceGenerator(
            name = "movie_seq_gen",
            sequenceName = "movie_id_seq", 
            allocationSize = 1             
    )
    Long id;

    @NotBlank
    @Column(nullable = false)
    String title;
    
    String description;
    Integer releaseYear;
    
    @Positive 
    Integer durationMinutes;
    
    String ageRating;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    MovieStatus status;

    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "movies_directors",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "director_id")
    )
    List<Director> directorList = new ArrayList<>();
    
    @OneToMany(mappedBy = "movie")
    List<MovieActor> actorList = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
