package com.moviecatalog.movie_catalog.entity;

import com.moviecatalog.movie_catalog.enums.MovieStatus;
import jakarta.persistence.*;
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
@NoArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "movie_seq_gen")
    @SequenceGenerator(
            name = "movie_seq_gen",
            sequenceName = "movie_id_seq",
            allocationSize = 1
    )
    Long id;

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

    @OneToMany(mappedBy = "movie",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    List<MovieActor> actorList = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Movie(Long id, String title, String description, Integer releaseYear, Integer durationMinutes, String ageRating, MovieStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.durationMinutes = durationMinutes;
        this.ageRating = ageRating;
        this.status = status;
    }

    public Movie(Long id, String title, String description, Integer releaseYear, Integer durationMinutes, String ageRating, MovieStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.durationMinutes = durationMinutes;
        this.ageRating = ageRating;
        this.status = status;
        this.createdAt = createdAt;
    }
}
