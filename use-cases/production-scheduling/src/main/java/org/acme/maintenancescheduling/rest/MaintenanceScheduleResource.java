package org.acme.maintenancescheduling.rest;

import java.util.List;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.acme.maintenancescheduling.domain.Crew;
import org.acme.maintenancescheduling.domain.Job;
import org.acme.maintenancescheduling.domain.MaintenanceSchedule;
import org.acme.maintenancescheduling.domain.Timeslot; // NEW: Timeslot import
import org.acme.maintenancescheduling.domain.WorkCalendar;
import org.acme.maintenancescheduling.persistence.CrewRepository;
import org.acme.maintenancescheduling.persistence.JobRepository;
import org.acme.maintenancescheduling.persistence.TimeslotRepository; // NEW: TimeslotRepository import
import org.acme.maintenancescheduling.persistence.WorkCalendarRepository;

@Path("/schedule")
public class MaintenanceScheduleResource {

    @Inject
    CrewRepository crewRepository;
    @Inject
    JobRepository jobRepository;
    @Inject
    WorkCalendarRepository workCalendarRepository;
    @Inject
    TimeslotRepository timeslotRepository; // NEW: TimeslotRepository 주입

    @GET
    public MaintenanceSchedule getSchedule() {
        List<Crew> crewList = crewRepository.listAll();
        List<Job> jobList = jobRepository.listAll();
        WorkCalendar workCalendar = workCalendarRepository.listAll().get(0);
        List<Timeslot> timeslotList = timeslotRepository.listAll(); // NEW: Timeslot 리스트 조회

        // CHANGED: 생성자에 timeslotList를 추가하여 호출합니다.
        MaintenanceSchedule schedule = new MaintenanceSchedule(workCalendar, crewList, timeslotList, jobList);
        
        return schedule;
    }
}