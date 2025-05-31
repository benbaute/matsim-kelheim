package org.matsim.analysis;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.vehicles.Vehicle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MyEventsHandler implements LinkLeaveEventHandler, PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler {
	private final Set<Id<Link>> modifiedLinkIds;
	private final Set<Id<Person>> affectedPersons = new HashSet<>();
	private final Map<Id<Vehicle>, Set<Id<Person>>> vehicleToPersons = new HashMap<>();

	public MyEventsHandler(Set<Id<Link>> modifiedLinkIds) {
		this.modifiedLinkIds = modifiedLinkIds;
	}

	public Set<Id<Person>> getAffectedPersons() {
		return affectedPersons;
	}

	@Override
	public void handleEvent(LinkLeaveEvent linkLeaveEvent) {
		if (modifiedLinkIds.contains(linkLeaveEvent.getLinkId())) {
			Set<Id<Person>> persons = vehicleToPersons.get(linkLeaveEvent.getVehicleId());
			if (persons != null) {
				affectedPersons.addAll(persons);
			}
		}
	}

	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
		vehicleToPersons
			.computeIfAbsent(event.getVehicleId(), v -> new HashSet<>())
			.add(event.getPersonId());
	}

	@Override
	public void handleEvent(PersonLeavesVehicleEvent event) {
		Set<Id<Person>> persons = vehicleToPersons.get(event.getVehicleId());
		if (persons != null) {
			persons.remove(event.getPersonId());
			if (persons.isEmpty()) {
				vehicleToPersons.remove(event.getVehicleId());
			}
		}
	}
}
