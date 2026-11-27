package com.moviecatalog.movie_catalog.controller;

import com.moviecatalog.movie_catalog.exception.GlobalExceptionHandler;
import com.moviecatalog.movie_catalog.service.ActorService;
import com.moviecatalog.movie_catalog.service.DirectorService;
import com.moviecatalog.movie_catalog.service.MovieService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ActorController.class, DirectorController.class, MovieController.class})
@Import(GlobalExceptionHandler.class)
class RequestValidationMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActorService actorService;

    @MockitoBean
    private DirectorService directorService;

    @MockitoBean
    private MovieService movieService;

    @Test
    void actorRequestReportsEachMissingOrBlankField() throws Exception {
        mockMvc.perform(post("/api/actors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":" ","lastName":"","country":" "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.lastName").exists())
                .andExpect(jsonPath("$.errors.birthDate").exists())
                .andExpect(jsonPath("$.errors.country").exists())
                .andExpect(jsonPath("$.errors.length()").value(4));
    }

    @Test
    void nullActorIdIsRejected() throws Exception {
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMovie("[]", """
                            [{"actorId":null,"characterName":"Neo"}]
                            """)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['actors[0].actorId']").exists());
    }

    @Test
    void durationOver1440MinutesIsRejected() throws Exception {
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"title":"A Film","releaseYear":2020,"durationMinutes":1441,"ageRating":"PG","status":"RELEASED","directorIds":[],"actors":[]}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.durationMinutes").exists());
    }

    @Test
    void actorBirthDateInFutureIsRejected() throws Exception {
        mockMvc.perform(post("/api/actors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Alex","lastName":"Smith","birthDate":"2999-01-01","country":"UK"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.birthDate").exists());

        verifyNoInteractions(actorService);
    }

    @Test
    void directorRequestReportsAllInvalidFields() throws Exception {
        mockMvc.perform(post("/api/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"","lastName":" ","country":"","birthDate":"2999-01-01"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.lastName").exists())
                .andExpect(jsonPath("$.errors.country").exists())
                .andExpect(jsonPath("$.errors.birthDate").exists());
    }

    @Test
    void invalidMovieFieldsAndNestedActorAreReportedTogether() throws Exception {
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":" ",
                                  "description":"x",
                                  "releaseYear":1800,
                                  "durationMinutes":0,
                                  "ageRating":"INVALID",
                                  "status":"UNKNOWN",
                                  "directorIds":[0],
                                  "actors":[{"actorId":-1,"characterName":" "}]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.releaseYear").exists())
                .andExpect(jsonPath("$.errors.durationMinutes").exists())
                .andExpect(jsonPath("$.errors.ageRating").exists())
                .andExpect(jsonPath("$.errors.status").exists())
                .andExpect(jsonPath("$.errors['directorIds[0]']").exists())
                .andExpect(jsonPath("$.errors['actors[0].actorId']").exists())
                .andExpect(jsonPath("$.errors['actors[0].characterName']").exists());
    }

    @Test
    void emptyDirectorAndActorListsAreAllowed() throws Exception {
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMovie("[]", "[]")))
                .andExpect(status().isCreated());
    }

    @Test
    void nullDirectorAndActorListsAreRejected() throws Exception {
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMovie("null", "null")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.directorIds").exists())
                .andExpect(jsonPath("$.errors.actors").exists());
    }

    @Test
    void nullDirectorIdIsRejected() throws Exception {
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMovie("[null]", "[]")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['directorIds[0]']").exists());
    }

    @Test
    void missingStatusAndAgeRatingAreRejected() throws Exception {
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Film","releaseYear":2020,"durationMinutes":100,"directorIds":[],"actors":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.status").exists())
                .andExpect(jsonPath("$.errors.ageRating").exists());
    }

    @Test
    void overlongMovieTitleIsRejected() throws Exception {
        String longTitle = "x".repeat(256);
        String content = """
                {"title":"%s","releaseYear":2020,"durationMinutes":100,"ageRating":"PG","status":"RELEASED","directorIds":[],"actors":[]}
                """.formatted(longTitle);

        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void invalidBirthDateFormatReturnsFieldSpecificJsonError() throws Exception {
        mockMvc.perform(post("/api/actors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Alex","lastName":"Smith","birthDate":"31-12-2000","country":"UK"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.birthDate").exists());
    }

    @Test
    void validStatusAndAgeRatingAreAccepted() throws Exception {
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMovie("[]", "[]")))
                .andExpect(status().isCreated());
    }

    @Test
    void nonStringStatusReturnsFieldSpecificJsonError() throws Exception {
        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMovie("[]", "[]").replace("\"RELEASED\"", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.status").exists());
    }

    private String validMovie(String directorIds, String actors) {
        return """
                {"title":"A Film","description":"Description","releaseYear":2020,"durationMinutes":100,"ageRating":"PG-13","status":"RELEASED","directorIds":%s,"actors":%s}
                """.formatted(directorIds, actors);
    }
}
