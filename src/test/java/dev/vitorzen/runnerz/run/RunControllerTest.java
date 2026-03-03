package dev.vitorzen.runnerz.run;

import dev.vitorzen.runnerz.user.UserRestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RunController.class)
class RunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RunRepository runRepository;

    @MockBean
    private UserRestClient userRestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnAllRuns() throws Exception {
        // Arrange
        List<Run> runs = List.of(
                new Run(1, "Morning Run", LocalDateTime.now(), LocalDateTime.now().plus(1, ChronoUnit.HOURS), 10, Location.INDOOR)
        );

        when(runRepository.findAll()).thenReturn(runs);

        // Act && Assert
        mockMvc.perform(get("/api/runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldReturnById() throws Exception{
        // Arrange
        Run run = new Run(1, "Morning Run", LocalDateTime.now(), LocalDateTime.now().plus(1, ChronoUnit.HOURS), 10, Location.INDOOR);

        when(runRepository.findById(1)).thenReturn(Optional.of(run));

        // Act && Assert
        mockMvc.perform(get("/api/runs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturnNotFoundWhenRunDoesNotExist() throws Exception {
        // Arrange
        when(runRepository.findById(999)).thenReturn(Optional.empty());

        // Act && Assert
        mockMvc.perform(get("/api/runs/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewRun() throws Exception {
        // Arrange
        Run run = new Run(1, "Morning Run", LocalDateTime.now(), LocalDateTime.now().plus(1, ChronoUnit.HOURS), 10, Location.INDOOR);

        // Act && Assert
        mockMvc.perform(post("/api/runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(run)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldUpdateRun() throws Exception {
        // Arrange
        Run updatedRun = new Run(1, "Night Run", LocalDateTime.now(), LocalDateTime.now().plus(1, ChronoUnit.HOURS), 10, Location.INDOOR);

        // Act && Assert
        mockMvc.perform(put("/api/runs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedRun)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldDeleteRun() throws Exception {
        // Arrange
        Run updatedRun = new Run(1, "Night Run", LocalDateTime.now(), LocalDateTime.now().plus(1, ChronoUnit.HOURS), 10, Location.INDOOR);

        when(runRepository.findById(1)).thenReturn(Optional.of(updatedRun));

        // Act && Assert
        mockMvc.perform(delete("/api/runs/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldFindByLocation() throws Exception {
        // Arrange
        Run run = new Run(1, "Beach Run", LocalDateTime.now(), LocalDateTime.now().plus(1, ChronoUnit.HOURS), 10, Location.OUTDOOR);

        when(runRepository.findAllByLocation("OUTDOOR")).thenReturn(List.of(run));

        // Act && Assert
        mockMvc.perform(get("/api/runs/location/OUTDOOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("[0].location").value("OUTDOOR"));
    }
}