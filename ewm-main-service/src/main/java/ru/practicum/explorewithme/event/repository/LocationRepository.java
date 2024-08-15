package ru.practicum.explorewithme.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.event.model.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
}
