package org.acme.maintenancescheduling.domain;

import java.time.LocalDateTime;
import java.util.Set;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
@Entity
public class Job {

    @PlanningId
    @Id
    @GeneratedValue
    private Long id;

    // --- 문제 사실 (Problem Facts) ---
    // 이 값들은 솔버가 변경하지 않습니다.
    private String name;
    private int durationInHours;
    private LocalDateTime readyDateTime;
    private LocalDateTime dueDateTime;
    private LocalDateTime idealEndDateTime;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> tagSet;


    // --- 계획 변수 (Planning Variables) ---
    // 이 값들은 솔버가 최적의 해를 찾기 위해 변경합니다.
    @PlanningVariable(valueRangeProviderRefs = {"crewRange"})
    @ManyToOne
    private Crew crew;

    @PlanningVariable(valueRangeProviderRefs = {"timeslotRange"})
    @ManyToOne
    private Timeslot timeslot;


    // No-arg constructor required for Hibernate and OptaPlanner
    public Job() {
    }

    public Job(String name, int durationInHours, LocalDateTime readyDateTime, LocalDateTime dueDateTime,
            LocalDateTime idealEndDateTime, Set<String> tagSet) {
        this.name = name;
        this.durationInHours = durationInHours;
        this.readyDateTime = readyDateTime;
        this.dueDateTime = dueDateTime;
        this.idealEndDateTime = idealEndDateTime;
        this.tagSet = tagSet;
    }

    @Override
    public String toString() {
        return name + "(" + id + ")";
    }

    // --- 편의 메소드 (Convenience Methods) ---

    /**
     * 할당된 Timeslot으로부터 실제 시작 시간을 가져옵니다.
     * UI나 제약조건에서 사용됩니다.
     */
    @Transient
    public LocalDateTime getStartDateTime() {
        if (timeslot == null) {
            return null;
        }
        return timeslot.getStartDateTime();
    }

    /**
     * 시작 시간과 작업 기간을 바탕으로 실제 종료 시간을 계산합니다.
     * UI나 제약조건에서 사용됩니다.
     */
    @Transient
    public LocalDateTime getEndDateTime() {
        if (timeslot == null) {
            return null;
        }
        return timeslot.getStartDateTime().plusHours(durationInHours);
    }


    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDurationInHours() {
        return durationInHours;
    }

    public LocalDateTime getReadyDateTime() {
        return readyDateTime;
    }

    public LocalDateTime getDueDateTime() {
        return dueDateTime;
    }

    public LocalDateTime getIdealEndDateTime() {
        return idealEndDateTime;
    }

    public Set<String> getTagSet() {
        return tagSet;
    }

    public Crew getCrew() {
        return crew;
    }

    public void setCrew(Crew crew) {
        this.crew = crew;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }
}