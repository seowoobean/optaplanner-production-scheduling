package org.acme.maintenancescheduling.solver;

import java.time.temporal.ChronoUnit;

import org.acme.maintenancescheduling.domain.Job;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

public class MaintenanceScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                crewConflict(constraintFactory),
                readyTime(constraintFactory),
                dueTime(constraintFactory),
                idealTime(constraintFactory)
        };
    }

    // A crew can do at most one job at the same time.
    private Constraint crewConflict(ConstraintFactory constraintFactory) {
        // CHANGED: Use getStartDateTime() and getEndDateTime() with overlapping joiner
        return constraintFactory.forEachUniquePair(Job.class,
                Joiners.equal(Job::getCrew),
                Joiners.overlapping(Job::getStartDateTime, Job::getEndDateTime))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Crew conflict");
    }

    // A job must not start before its ready time.
    private Constraint readyTime(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Job.class)
                .filter(job -> job.getStartDateTime() != null && job.getStartDateTime().isBefore(job.getReadyDateTime()))
                .penalize(HardSoftScore.ONE_HARD,
                        job -> (int) ChronoUnit.HOURS.between(job.getStartDateTime(), job.getReadyDateTime()))
                .asConstraint("Ready time");
    }

    // A job must finish before its due time.
    private Constraint dueTime(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Job.class)
                .filter(job -> job.getEndDateTime() != null && job.getEndDateTime().isAfter(job.getDueDateTime()))
                .penalize(HardSoftScore.ONE_HARD,
                        job -> (int) ChronoUnit.HOURS.between(job.getDueDateTime(), job.getEndDateTime()))
                .asConstraint("Due time");
    }
    
    // A job should preferably finish before its ideal end time.
    private Constraint idealTime(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Job.class)
                .filter(job -> job.getEndDateTime() != null && job.getEndDateTime().isAfter(job.getIdealEndDateTime()))
                .penalize(HardSoftScore.ONE_SOFT,
                        job -> (int) ChronoUnit.HOURS.between(job.getIdealEndDateTime(), job.getEndDateTime()))
                .asConstraint("Ideal end time");
    }
}