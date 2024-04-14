package com.ka.hospitalsos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class KNearestNeighbors {

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula to calculate distance between two points
        final double R = 6371; // Radius of the Earth in kilometers
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public List<Location> findNearestLocations(Location queryLocation, List<Location> locations, int k) {
        // Calculate distances to all locations
        for (Location location : locations) {
            double distance = calculateDistance(queryLocation.getLatitude(), queryLocation.getLongitude(),
                    location.getLatitude(), location.getLongitude());
            location.setDistance(distance);
        }

        // Sort locations based on distance
        Collections.sort(locations, new Comparator<Location>() {
            @Override
            public int compare(Location loc1, Location loc2) {
                return Double.compare(loc1.getDistance(), loc2.getDistance());
            }
        });

        // Return k nearest neighbors
        return locations.subList(0, Math.min(k, locations.size()));
    }
}
