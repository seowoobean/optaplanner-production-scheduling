package org.acme.maintenancescheduling.domain;

import java.util.List;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.solution.ProblemFactProperty; // NEW: import 추가
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.SolverStatus;


@PlanningSolution
public class MaintenanceSchedule {

    // CHANGED: @ProblemFactCollectionProperty -> @ProblemFactProperty
    @ProblemFactProperty
    private WorkCalendar workCalendar;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "crewRange")
    private List<Crew> crewList;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "timeslotRange")
    private List<Timeslot> timeslotList;

    @PlanningEntityCollectionProperty
    private List<Job> jobList;

    @PlanningScore
    private HardSoftScore score;

    private SolverStatus solverStatus;

    public MaintenanceSchedule() {
    }

    public MaintenanceSchedule(WorkCalendar workCalendar, List<Crew> crewList, List<Timeslot> timeslotList, List<Job> jobList) {
        this.workCalendar = workCalendar;
        this.crewList = crewList;
        this.timeslotList = timeslotList;
        this.jobList = jobList;
    }
    
    // --- Getters and setters ---
    public WorkCalendar getWorkCalendar() { return workCalendar; }
    public List<Crew> getCrewList() { return crewList; }
    public List<Timeslot> getTimeslotList() { return timeslotList; }
    public List<Job> getJobList() { return jobList; }
    public HardSoftScore getScore() { return score; }
    public void setScore(HardSoftScore score) { this.score = score; }
    public SolverStatus getSolverStatus() { return solverStatus; }
    public void setSolverStatus(SolverStatus solverStatus) { this.solverStatus = solverStatus; }
}