package uk.ac.gla.compositecontextualsuggester.util;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is an implementation of a user for TREC CS. The user is
 * represented by a map matching each FourSquare venue id with a rating given
 * by the user to this venue.
 * 
 * @author Thibaut Thonet
 *
 */
public class User {
	
	private String id;
	
	// venueRatings is a map in which the key is a venue foursquare id, the
	// value is the user's rating for this venue.
	private Map<String, Double> venueRatings; 

	public User(String id) {
		this.id = id;
		this.venueRatings = new HashMap<String, Double>();
	}

	public Map<String, Double> getVenueRatings() {
		return venueRatings;
	}

	public void setVenueRatings(Map<String, Double> venueRatings) {
		this.venueRatings = venueRatings;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
