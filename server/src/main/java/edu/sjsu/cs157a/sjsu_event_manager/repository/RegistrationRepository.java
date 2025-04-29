package edu.sjsu.cs157a.sjsu_event_manager.repository;

import edu.sjsu.cs157a.sjsu_event_manager.model.Event;
import edu.sjsu.cs157a.sjsu_event_manager.model.Registration;
import edu.sjsu.cs157a.sjsu_event_manager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Integer> {

    Optional<Registration> findByUserAndEvent(User user, Event event);

    List<Registration> findByUser(User user);

    List<Registration> findByEvent(Event event);

    boolean existsByUserAndEvent(User user, Event event);

    long countByEvent(Event event);
} 