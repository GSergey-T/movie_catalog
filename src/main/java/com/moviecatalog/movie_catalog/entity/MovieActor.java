package com.moviecatalog.movie_catalog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class MovieActor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "movie_actor_seq_gen")
    @SequenceGenerator(
            name = "movie_actor_seq_gen",
            sequenceName = "movie_actor_id_seq",
            allocationSize = 1
    )
    Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    Actor actor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    Movie movie;
    
    String characterName;
}
