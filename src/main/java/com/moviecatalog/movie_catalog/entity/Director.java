package com.moviecatalog.movie_catalog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class Director {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "director_seq_gen")
    @SequenceGenerator(
            name = "director_seq_gen",
            sequenceName = "director_id_seq",
            allocationSize = 1
    )
    Long id;
    String firstName;
    String lastName;
    LocalDate birthDate;
    String country;
    
    @ManyToMany(mappedBy = "directorList")
    List<Movie> movieList = new ArrayList<>();
}
