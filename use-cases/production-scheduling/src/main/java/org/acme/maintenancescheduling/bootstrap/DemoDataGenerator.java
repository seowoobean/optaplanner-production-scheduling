package org.acme.maintenancescheduling.bootstrap;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.acme.maintenancescheduling.domain.Crew;
import org.acme.maintenancescheduling.domain.Job;
import org.acme.maintenancescheduling.domain.Timeslot; // NEW
import org.acme.maintenancescheduling.domain.WorkCalendar;
import org.acme.maintenancescheduling.persistence.CrewRepository;
import org.acme.maintenancescheduling.persistence.JobRepository;
import org.acme.maintenancescheduling.persistence.TimeslotRepository; // NEW
import org.acme.maintenancescheduling.persistence.WorkCalendarRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class DemoDataGenerator {

    @ConfigProperty(name = "schedule.demoData", defaultValue = "SMALL")
    public DemoData demoData;

    public enum DemoData { NONE, SMALL, LARGE }

    @Inject WorkCalendarRepository workCalendarRepository;
    @Inject CrewRepository crewRepository;
    @Inject JobRepository jobRepository;
    @Inject TimeslotRepository timeslotRepository; // NEW

    @Transactional
    public void generateDemoData(@Observes StartupEvent startupEvent) {
        if (demoData == DemoData.NONE) {
            return;
        }

        List<Crew> crewList = new ArrayList<>();
        crewList.add(new Crew("충전 1"));
        crewList.add(new Crew("카토너 1"));
        crewList.add(new Crew("충전 2"));
        crewList.add(new Crew("카토너 2"));
        crewList.add(new Crew("충전 3"));
        crewList.add(new Crew("카토너 3"));
        // ... (crew 추가) ...
        crewRepository.persist(crewList);

        LocalDateTime fromDateTime = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atStartOfDay();
        int weekListSize = (demoData == DemoData.LARGE) ? 16 : 8;
        LocalDateTime toDateTime = fromDateTime.plusWeeks(weekListSize);
        workCalendarRepository.persist(new WorkCalendar(fromDateTime.toLocalDate(), toDateTime.toLocalDate()));
        
        // NEW: 가능한 모든 Timeslot 객체 생성 및 저장
        List<Timeslot> timeslotList = new ArrayList<>();
        LocalDateTime currentDateTime = fromDateTime;
        while (currentDateTime.isBefore(toDateTime)) {
            timeslotList.add(new Timeslot(currentDateTime));
            currentDateTime = currentDateTime.plusHours(1); // 1시간 단위로 Timeslot 생성
        }
        timeslotRepository.persist(timeslotList);

        // --- NEW: Job 생성 로직 (롬앤 틴트 제품 예시) ---
        List<Job> jobList = new ArrayList<>();
        Random random = new Random(17); // 일관된 데이터 생성을 위해 Random 시드 고정

        // 롬앤 쥬시 래스팅 틴트 #20 다크 코코넛
        // readyDateTime, dueDateTime, idealEndDateTime은 fromDateTime을 기준으로 설정
        LocalDateTime readyDateTime1 = fromDateTime.plusHours(8 * 1); // 첫째 날 오전 8시
        LocalDateTime dueDateTime1 = readyDateTime1.plusDays(3); // 3일 후 종료
        LocalDateTime idealEndDateTime1 = readyDateTime1.plusDays(2); // 2일 후면 좋음
        jobList.add(new Job("롬앤THE쥬시래스팅틴트#20다크코코넛", 12, // 12시간 작업
                readyDateTime1, dueDateTime1, idealEndDateTime1, Set.of("틴트", "다크")));

        // 롬앤 쥬시 래스팅 틴트 #13 잇도토리
        LocalDateTime readyDateTime2 = fromDateTime.plusHours(8 * 2 + 4); // 둘째 날 오후 12시
        LocalDateTime dueDateTime2 = readyDateTime2.plusDays(2).plusHours(12); // 2일 12시간 후 종료
        LocalDateTime idealEndDateTime2 = readyDateTime2.plusDays(1).plusHours(20); // 1일 20시간 후면 좋음
        jobList.add(new Job("롬앤THE쥬시래스팅틴트#13잇도토리", 8, // 8시간 작업
                readyDateTime2, dueDateTime2, idealEndDateTime2, Set.of("틴트", "코랄")));

        // 롬앤 쥬시 래스팅 틴트 #19 썸머 센트로
        LocalDateTime readyDateTime3 = fromDateTime.plusHours(8 * 4); // 넷째 날 오전 8시
        LocalDateTime dueDateTime3 = readyDateTime3.plusDays(4); // 4일 후 종료
        LocalDateTime idealEndDateTime3 = readyDateTime3.plusDays(3).plusHours(12); // 3일 12시간 후면 좋음
        jobList.add(new Job("롬앤THE쥬시래스팅틴트#19썸머센트", 20, // 20시간 작업
                readyDateTime3, dueDateTime3, idealEndDateTime3, Set.of("틴트", "레드")));

        jobRepository.persist(jobList);
    }
}