package dev.vitorzen.runnerz.run;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Repository
public class RunRepository {
    private static final Logger log = LoggerFactory.getLogger(RunRepository.class);
    private final JdbcClient jdbcClient;

    public RunRepository(JdbcClient jdbcClient){
        this.jdbcClient = jdbcClient;
    }

    public List<Run> findAll(){
        return jdbcClient.sql("SELECT * FROM RUN")
                .query(Run.class)
                .list();
    }

    public Optional<Run> findById(Integer id){
        return jdbcClient.sql("SELECT ID, TITLE, STARTED_ON, COMPLETED_ON, MILES, LOCATION FROM RUN WHERE ID = :ID")
                .param("id", id)
                .query(Run.class)
                .optional();
    }

    public void create(Run run){
        var update = jdbcClient.sql("INSERT INTO RUN(id, title, started_on, completed_on, miles, location) " +
                        "VALUES (:id, :title, :started_on, :completed_on, :miles, :location)")
                .param("id", run.id())
                .param("title", run.title())
                .param("started_on", run.startedOn())
                .param("completed_on", run.completedOn())
                .param("miles", run.miles())
                .param("location", run.location().name())
                .update();

        Assert.state(update == 1, "Failed to create Run " + run.title());
    }

    public void update(Run run, Integer id){
        var update = jdbcClient.sql("UPDATE RUN SET TITLE = :title, STARTED_ON = :started_on, COMPLETED_ON = :completed_on, MILES = :miles, LOCATION = :location WHERE ID = :id")
                .param("title", run.title())
                .param("started_on", run.startedOn())
                .param("completed_on", run.completedOn())
                .param("miles", run.miles())
                .param("location", run.location().name())
                .param("id", id)
                .update();

        Assert.state(update == 1, "Failed to update Run " + run.title());
    }

    public void delete(Integer id){
        var update = jdbcClient.sql("DELETE FROM RUN WHERE ID = :id")
                .param("id", id)
                .update();

        Assert.state(update == 1, "Failed to delete Run " + id);
    }

    public int count(){
        return jdbcClient.sql("SELECT * FROM RUN")
                .query()
                .listOfRows()
                .size();
    }

    public void saveAll(List<Run> runs){
        runs.stream()
                .forEach(this::create);
    }

    public List<Run> findByLocation(String location){
        return jdbcClient.sql("SELECT * FROM RUN WHERE LOCATION = :location")
                .param("location", location)
                .query(Run.class)
                .list();
    }
}
