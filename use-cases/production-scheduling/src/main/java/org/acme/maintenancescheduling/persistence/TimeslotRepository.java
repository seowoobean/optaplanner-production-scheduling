package org.acme.maintenancescheduling.persistence;

import org.acme.maintenancescheduling.domain.Timeslot;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TimeslotRepository implements PanacheRepository<Timeslot> {

}