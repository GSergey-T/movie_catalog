package com.moviecatalog.movie_catalog.entity;

import jakarta.persistence.*;
import lombok.*;
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
@NoArgsConstructor
public class Actor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actor_seq_gen")
    @SequenceGenerator(
            name = "actor_seq_gen",
            sequenceName = "actor_id_seq",
            allocationSize = 1
    )
    Long id;
    String firstName;
    String lastName;
    LocalDate birthDate;
    String country;

    @OneToMany(mappedBy = "actor")
    List<MovieActor> movieList = new ArrayList<>();

    public Actor(String firstName, String lastName, LocalDate birthDate, String country) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.country = country;
    }

    public Actor(Long id, String firstName, String lastName, LocalDate birthDate, String country) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.country = country;
    }
}
