package com.moviecatalog.movie_catalog.service;

import com.moviecatalog.movie_catalog.dto.MovieDTO;
import com.moviecatalog.movie_catalog.dto.MovieActorDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieResponseDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.DirectorSummaryDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieActorRequestDTO;
import com.moviecatalog.movie_catalog.dto.api.movie.MovieActorResponseDTO;
import com.moviecatalog.movie_catalog.dto.MovieDirectorDTO;
import com.moviecatalog.movie_catalog.entity.Actor;
import com.moviecatalog.movie_catalog.entity.Director;
import com.moviecatalog.movie_catalog.entity.Movie;
import com.moviecatalog.movie_catalog.entity.MovieActor;
import com.moviecatalog.movie_catalog.mapper.DirectorMapper;
import com.moviecatalog.movie_catalog.mapper.MovieMapper;
import com.moviecatalog.movie_catalog.repository.ActorRepository;
import com.moviecatalog.movie_catalog.repository.DirectorRepository;
import com.moviecatalog.movie_catalog.repository.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovieService {

    MovieMapper movieMapper;
    DirectorMapper directorMapper;
    MovieRepository movieRepository;
    DirectorRepository directorRepository;
    ActorRepository actorRepository;

    public MovieService(MovieMapper movieMapper,
                        DirectorMapper directorMapper,
                        MovieRepository movieRepository,
                        DirectorRepository directorRepository,
                        ActorRepository actorRepository) {
        this.movieMapper = movieMapper;
        this.directorMapper = directorMapper;
        this.movieRepository = movieRepository;
        this.directorRepository = directorRepository;
        this.actorRepository = actorRepository;
    }

    @Transactional(readOnly = true)
    public MovieResponseDTO getMovieById(Long id) {
        MovieDTO movieDTO = movieRepository.findMovieById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movie not found with id: " + id));

        MovieResponseDTO response = movieMapper.toMovieResponseDTO(movieDTO);

        List<MovieDirectorDTO> directorList = movieRepository.findDirectorsByMovieId(id);
        List<DirectorSummaryDTO> directorSummaryDTOList = directorList.stream()
                .map(directorMapper::toDirectorSummaryDTO)
                .toList();

        List<MovieActorDTO> actorList = movieRepository.findActorsByMovieId(id);
        List<MovieActorResponseDTO> movieActorResponseDTOList = actorList.stream()
                .map(movieMapper::toMovieActorResponseDTO)
                .toList();

        response.setDirectors(directorSummaryDTOList);
        response.setActors(movieActorResponseDTOList);

        return response;
    }

    @Transactional(readOnly = true)
    public List<MovieResponseDTO> getAllMovies() {
        List<MovieResponseDTO> responses = movieRepository.findAllMovies()
                .stream()
                .map(movieMapper::toMovieResponseDTO)
                .collect(Collectors.toCollection(ArrayList::new));

        if (responses.isEmpty())
            return responses;

        List<Long> movieIds = responses.stream()
                .map(MovieResponseDTO::getId)
                .distinct()
                .toList();

        Map<Long, List<DirectorSummaryDTO>> movieIdDirectorsMap = movieRepository.findDirectorsByMovieIds(movieIds)
                .stream()
                .collect(Collectors.groupingBy(
                        MovieDirectorDTO::getMovieId,
                        LinkedHashMap::new,
                        Collectors.mapping(directorMapper::toDirectorSummaryDTO, Collectors.toList())
                ));

        Map<Long, List<MovieActorResponseDTO>> movieIdActorsMap = movieRepository
                .findActorsByMovieIds(movieIds)
                .stream()
                .collect(Collectors.groupingBy(
                        MovieActorDTO::getMovieId,
                        LinkedHashMap::new,
                        Collectors.mapping(movieMapper::toMovieActorResponseDTO, Collectors.toList())
                ));

        responses.forEach(el -> el.setDirectors(
                movieIdDirectorsMap.getOrDefault(el.getId(), List.of())
        ));
        responses.forEach(el -> el.setActors(
                movieIdActorsMap.getOrDefault(el.getId(), List.of())
        ));

        return responses;
    }

    @Transactional
    public MovieResponseDTO createMovie(MovieRequestDTO movieRequestDTO) {
        Movie newMovie = movieMapper.toMovie(movieRequestDTO);
        newMovie.setDirectorList(findDirectors(movieRequestDTO.getDirectorIds()));
        newMovie.setActorList(findActors(newMovie, movieRequestDTO.getActors()));
        Movie savedMovie = movieRepository.save(newMovie);
        return movieMapper.toMovieResponseDTO(savedMovie);
    }

    @Transactional
    public MovieResponseDTO updateMovie(Long id, MovieRequestDTO movieRequestDTO) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Movie not found with id: " + id
                ));

        movieMapper.updateMovie(movieRequestDTO, movie);

        movie.setDirectorList(findDirectors(movieRequestDTO.getDirectorIds()));
        List<MovieActor> movieActors = findActors(movie, movieRequestDTO.getActors());
        
        movie.getActorList().clear();
        movie.getActorList().addAll(movieActors);

        return movieMapper.toMovieResponseDTO(movie);
    }

    @Transactional
    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Movie not found with id: " + id
                ));

        movieRepository.delete(movie);
    }

    private List<Director> findDirectors(List<Long> directorIds) {
        if (directorIds == null || directorIds.isEmpty())
            return new ArrayList<>();

        List<Long> distinctIds = directorIds.stream()
                .distinct()
                .toList();

        Map<Long, Director> directorsById = directorRepository.findAllById(distinctIds)
                .stream()
                .collect(Collectors.toMap(Director::getId, Function.identity()));

        List<Long> missingIds = distinctIds.stream()
                .filter(directorId -> !directorsById.containsKey(directorId))
                .toList();

        if (!missingIds.isEmpty())
            throw new EntityNotFoundException("Director(s) not found with id(s): " + missingIds);

        return distinctIds.stream()
                .map(directorsById::get)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<MovieActor> findActors(Movie movie, List<MovieActorRequestDTO> actorRequests) {
        if (actorRequests == null || actorRequests.isEmpty())
            return new ArrayList<>();

        List<Long> actorIds = actorRequests.stream()
                .map(MovieActorRequestDTO::getActorId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Actor> actorsById = actorRepository.findAllById(actorIds)
                .stream()
                .collect(Collectors.toMap(Actor::getId, Function.identity()));

        List<Long> missingIds = actorRequests.stream()
                .map(MovieActorRequestDTO::getActorId)
                .filter(actorId -> actorId == null || !actorsById.containsKey(actorId))
                .distinct()
                .toList();

        if (!missingIds.isEmpty())
            throw new EntityNotFoundException("Actor(s) not found with id(s): " + missingIds);

        return actorRequests.stream()
                .map(actorRequest -> {
                    MovieActor movieActor = movieMapper.toMovieActor(actorRequest);
                    movieActor.setMovie(movie);
                    movieActor.setActor(actorsById.get(actorRequest.getActorId()));
                    return movieActor;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
