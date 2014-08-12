package org.terrier.compositecontextualsuggester.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

/**
 * This class is used to parse the file that contains the contexts of TREC CS
 * (i.e. locations). It creates a map matching each context id with a location.
 * 
 * @author Thibaut Thonet
 *
 */
public class LocationHandler {
	
	Map<String, Location> locations;
	
	/**
	 * This method parses the context file of TREC CS and builds the map 
	 * locations matching each context id with a location.
	 * 
	 * @param contextFilePath the path to the context file of TREC CS
	 */
	public void parseLocations(String contextFilePath) {
		try {
			// locations is initialized.
			locations = new HashMap<String, Location>();

			// Construction of locations.
			File contextFile = new File(contextFilePath);
			List<String> lines = FileUtils.readLines(contextFile);
			// The first line contains the header.
			lines.remove(0); 
			for (String line : lines) {
				// values[0] is the id of the location, values[1] is the name
				// of the location (city), values[3] is the latitude of the
				// location and values[4] is the longitude of the location.
				
				// Split if comma is outside quotes.
				String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
				String locationId = values[0];
				String locationName = values[1];
				double locationLatitude = Double.parseDouble(values[3]);
				double locationLongitude = Double.parseDouble(values[4]);

				// The current location is created and added to locations.
				Location location = new Location(locationLatitude, locationLongitude, locationName);
				locations.put(locationId, location);
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	public Map<String, Location> getLocations() {
		return locations;
	}

}
