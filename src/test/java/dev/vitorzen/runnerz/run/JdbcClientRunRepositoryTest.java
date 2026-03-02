package dev.vitorzen.runnerz.run;

import dev.vitorzen.runnerz.user.UserRestClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Testcontainers // This annotation is util because i'm working with docker container and i need it
@JdbcTest // This carry only the necessary to communicate with SQL
@Transactional // This made rollback of database
@Import(JdbcClientRunRepository.class) // without it the Spring Boot will complain that cannot found this bean
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// This made the compiler cannot search for H2 Database. I'm working with real postgreSQL database

public class JdbcClientRunRepositoryTest {

    @Container // Say to JUnit "Before to run something, guarantee this container is up"
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    JdbcClientRunRepository repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @MockBean
    UserRestClient userRestClient;

    int firstId = Math.abs(UUID.randomUUID().hashCode());
    int secondId = Math.abs(UUID.randomUUID().hashCode());

    // Truncated to seconds to prevent precision mismatch during comparison
    LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    @Test
    void shouldFindAllRuns() {
        // Arrange
        Run run1 = new Run(firstId, "Morning Run", LocalDateTime.now(), LocalDateTime.now().plus(1, ChronoUnit.HOURS), 10, Location.INDOOR);
        Run run2 = new Run(secondId, "Night Run", LocalDateTime.now(), LocalDateTime.now().plus(3, ChronoUnit.HOURS), 20, Location.OUTDOOR);

        repository.saveAll(List.of(run1, run2));

        // Act
        List<Run> runs = repository.findAll();

        // Assert
        assertEquals(2, runs.size());
    }

    @Test
    void shouldFindRunById() {
        // Arrange
        Run run = new Run(firstId, "Afternoon Run", LocalDateTime.now(), LocalDateTime.now().plus(1, ChronoUnit.HOURS), 20, Location.OUTDOOR);
        repository.create(run);

        // Act
        Optional<Run> result = repository.findById(firstId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(firstId, result.get().id());
    }

    @Test
    void shouldCreate() {
        // Arrange
        Run run = new Run(firstId, "Test Create Running", now, now.plus(4, ChronoUnit.HOURS), 50, Location.OUTDOOR);

        // Act
        repository.create(run);
        Run savedRun = repository.findById(firstId).get();

        // Asset
        assertTrue(repository.findById(firstId).isPresent());
        assertEquals(run, savedRun);
    }

    @Test
    void shouldUpdate() {
        // Arrange
        Run run = new Run(firstId, "Afternoon Run", now, now.plus(1, ChronoUnit.HOURS), 20, Location.OUTDOOR);
        repository.create(run);

        // Act
        Run updatedRun = new Run(firstId, "Night Run late gym", now, now.plus(2, ChronoUnit.HOURS), 23, Location.OUTDOOR);
        repository.update(updatedRun, firstId);

        // Assert
        Run result = repository.findById(firstId).get();
        assertEquals(updatedRun, result);
    }

    @Test
    void shouldDelete() {
        // Arrange
        Run run = new Run(firstId, "Morning Run", now, now.plus(1, ChronoUnit.HOURS), 20, Location.OUTDOOR);
        repository.create(run);

        // Act
        repository.delete(firstId);

        // Assert
        assertTrue(repository.findById(firstId).isEmpty());
    }

    @Test
    void shouldCount() {
        // Arrange
        Run run1 = new Run(firstId, "Morning Run", LocalDateTime.now(), LocalDateTime.now().plus(1, ChronoUnit.HOURS), 10, Location.INDOOR);
        Run run2 = new Run(secondId, "Night Run", LocalDateTime.now(), LocalDateTime.now().plus(3, ChronoUnit.HOURS), 20, Location.OUTDOOR);

        repository.saveAll(List.of(run1, run2));

        // Act
        int count = repository.count();

        // Assert
        assertEquals(2, count);
    }

    @Test
    void shouldSaveAll() {
        // Arrange
        Run run1 = new Run(firstId, "Morning Run", LocalDateTime.now(), LocalDateTime.now().plus(1, ChronoUnit.HOURS), 10, Location.INDOOR);
        Run run2 = new Run(secondId, "Night Run", LocalDateTime.now(), LocalDateTime.now().plus(3, ChronoUnit.HOURS), 20, Location.OUTDOOR);
        List<Run> runs = List.of(run1, run2);

        // Act
        repository.saveAll(runs);

        // Assert
        List<Run> savedRuns = repository.findAll();
        assertEquals(2, savedRuns.size());
    }

    @Test
    void shouldFindByLocation() {
        // Arrange
        Run run1 = new Run(firstId, "Morning Run", LocalDateTime.now(), LocalDateTime.now().plus(1, ChronoUnit.HOURS), 10, Location.INDOOR);
        repository.create(run1);

        // Act
        List<Run> runsByLocation = repository.findByLocation(String.valueOf(Location.INDOOR));

        // Assert
        assertEquals(1, runsByLocation.size());
    }
}