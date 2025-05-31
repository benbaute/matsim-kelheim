package org.matsim.analysis;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class MyAnalysis {
	public static void main(String[] args) throws IOException {
		String eventsFile = "output/kelheim-v3.0-1pct.output_events.xml";
		String linkIdsFile = "output/modified_links-s100.0.txt";
		String affectedPersonsIdsFile = "output/affectedPersons-s100.0.txt";

		Set<Id<Link>> modifiedLinks = readLinkIdsFromFile(linkIdsFile);

		EventsManager manager = EventsUtils.createEventsManager();
		MyEventsHandler handler = new MyEventsHandler(modifiedLinks);
		manager.addHandler(handler);
		manager.addHandler(handler);
		EventsUtils.readEvents(manager, eventsFile);

		Set<Id<Person>> affectedPersons = handler.getAffectedPersons();
		writePersonIdsToFile(affectedPersons, affectedPersonsIdsFile);
	}

	public static Set<Id<Link>> readLinkIdsFromFile(String filename) throws IOException {
		Set<Id<Link>> linkIds = new HashSet<>();
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = br.readLine()) != null) {
				linkIds.add(Id.createLinkId(line.trim()));
			}
		}
		return linkIds;
	}

	public static void writePersonIdsToFile(Set<Id<Person>> personIds, String filename) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
			for (Id<Person> id : personIds) {
				writer.write(id.toString());
				writer.newLine();
			}
		}
	}
}
