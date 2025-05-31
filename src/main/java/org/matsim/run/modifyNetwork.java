package org.matsim.run;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;


public class modifyNetwork {
	private final int decimalPlaces = 2;
	private final boolean writeNetworkFile = true;

	public modifyNetwork() {
	}

	public static void main(String[] args) {
		Network network = NetworkUtils.createNetwork();
		new MatsimNetworkReader(network).readFile("input/v3.1/kelheim-v3.0-network-with-pt.xml");

		modifyNetwork modifier = new modifyNetwork();
		modifier.changeNetwork(network,100);

		if (modifier.writeNetworkFile) {
			new NetworkWriter(network).write("input/v3.1/kelheim-modified-network.xml");
		}
	}

	public void changeNetwork(Network network, double desiredMaxSpeed) {
		int changedLinks = 0;
		int carLinks = 0;
		int nonCarLinks = 0;

		Set<Id<Link>> modifiedLinks = new HashSet<>();

		Set<String> allModes = new HashSet<>();
		Map<Double, Integer> originalSpeeds = new HashMap<>();
		Map<Double, Integer> modifiedSpeeds = new HashMap<>();

		for (Link l : network.getLinks().values()) {
			Set<String> modes = l.getAllowedModes();
			allModes.addAll(modes);



			if (modes.contains(TransportMode.car)) {
				carLinks++;
				double originalSpeed = l.getFreespeed();

				// Round speed for counting
				double roundedOriginal = roundTo(originalSpeed);
				originalSpeeds.put(roundedOriginal, originalSpeeds.getOrDefault(roundedOriginal, 0) + 1);

				double desiredMaxSpeedInMs = desiredMaxSpeed / 3.6; // km/h to m/s
				if (originalSpeed > desiredMaxSpeedInMs) {
//					double newSpeed = Math.max(desiredMaxSpeedInMs, originalSpeed / 2);
//					l.setFreespeed(newSpeed);
					l.setFreespeed(desiredMaxSpeedInMs);
					modifiedLinks.add(l.getId());
					l.getAttributes().putAttribute("modified", true);
					l.getAttributes().putAttribute("originalSpeed", originalSpeed);
					changedLinks++;
				}

				// Count the (possibly modified) speed
				double roundedModified = roundTo(l.getFreespeed());
				modifiedSpeeds.put(roundedModified, modifiedSpeeds.getOrDefault(roundedModified, 0) + 1);
			} else {
				nonCarLinks++;
			}
		}

		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("output/modified_links-s" + desiredMaxSpeed + ".txt"))) {
			for (Id<Link> linkId : modifiedLinks) {
				writer.write(linkId.toString());
				writer.newLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		printSpeedDistribution("Original Speeds (before changes):", originalSpeeds);
		printSpeedDistribution("Modified Speeds (after changes):", modifiedSpeeds);
		System.out.println("Changed links: " + changedLinks);
		System.out.println("Car links: " + carLinks);
		System.out.println("Non-car links: " + nonCarLinks);
		System.out.println("All modes: " + allModes);
	}

	private double roundTo(double value) {
		return Math.round(value * Math.pow(10, decimalPlaces)) / Math.pow(10, decimalPlaces);
	}

	private void printSpeedDistribution(String title, Map<Double, Integer> speedCounts) {
		System.out.println(title);
		speedCounts.entrySet().stream()
			.sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
			.forEach(entry -> System.out.println(
				"Speed [m/s]: " + entry.getKey() +
					", Speed [km/h]: " + roundTo(entry.getKey() * 3.6) +
					" -> Count: " + entry.getValue()));
	}
}

