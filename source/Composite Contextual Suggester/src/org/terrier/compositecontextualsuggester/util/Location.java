package org.terrier.compositecontextualsuggester.util;

/**
 * This class is an implementation a geographic location described by its
 * latitude and longitude, and possibly a name (e.g. a city name).
 * 
 * @author Thibaut Thonet
 *
 */
public class Location {
	
	private double latitude;
	private double longitude;
	private String name;
	
	public Location(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public Location(double latitude, double longitude, String name) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.name = name;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * This method computes the Euclidean distance between two locations.
	 * 
	 * @param location1 a location
	 * @param location2 another location
	 * 
	 * @return the Euclidean distance between these locations. 
	 */
	public static double distance(Location location1, Location location2) {
		return Math.sqrt((location2.latitude - location1.latitude)*(location2.latitude - location1.latitude) + 
				(location2.longitude - location1.longitude)*(location2.longitude - location1.longitude));
	}
	
}
