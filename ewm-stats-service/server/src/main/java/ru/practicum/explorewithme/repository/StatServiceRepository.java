package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.model.Stat;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatServiceRepository extends JpaRepository<Stat, Long> {

    @Query("SELECT s.app, s.uri, COUNT(DISTINCT s.ip) " +
            "FROM Stat s WHERE s.timestamp BETWEEN :start AND :end " +
            "GROUP BY s.app, s.uri")
    List<Object[]> findUniqueStatsWithoutUris(LocalDateTime start, LocalDateTime end);

    @Query("SELECT s.app, s.uri, COUNT(s.ip) " +
            "FROM Stat s WHERE s.timestamp BETWEEN :start AND :end " +
            "GROUP BY s.app, s.uri")
    List<Object[]> findStatsWithoutUris(LocalDateTime start, LocalDateTime end);

    @Query("SELECT s.app, s.uri, COUNT(DISTINCT s.ip) " +
            "FROM Stat s WHERE s.timestamp BETWEEN :start AND :end " +
            "AND s.uri IN :uris " +
            "GROUP BY s.app, s.uri")
    List<Object[]> findUniqueStats(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT s.app, s.uri, COUNT(s.ip) " +
            "FROM Stat s WHERE s.timestamp BETWEEN :start AND :end " +
            "AND s.uri IN :uris " +
            "GROUP BY s.app, s.uri")
    List<Object[]> findStats(LocalDateTime start, LocalDateTime end, List<String> uris);
}