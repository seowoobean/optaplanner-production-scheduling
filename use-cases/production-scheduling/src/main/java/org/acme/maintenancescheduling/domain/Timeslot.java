package org.acme.maintenancescheduling.domain;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.optaplanner.core.api.domain.lookup.PlanningId;

@Entity
public class Timeslot {

    @PlanningId
    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime startDateTime;
    // 필요하다면 종료 시간도 추가할 수 있습니다.
    // private LocalDateTime endDateTime;

    public Timeslot() {
    }

    public Timeslot(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    @Override
    public String toString() {
        return startDateTime.toString();
    }

    // --- Getters and setters ---
    public Long getId() {
        return id;
    }
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }
}