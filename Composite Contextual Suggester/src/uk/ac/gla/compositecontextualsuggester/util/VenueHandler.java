package uk.ac.gla.compositecontextualsuggester.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * This class is used to parse the FourSquare venues. It creates a map matching
 * each FourSquare venue id with a venue created from the class Venue. It uses
 * a list of FourSquare venue files in Json format that have been crawled
 * because their locations are in the list of TREC CS contexts.
 * 
 * @author Thibaut Thonet
 *
 */
public class VenueHandler {
	
	private Map<String, Venue> venues; 
	
	/**
	 * This method parses the venues located in the FourSquare venue directory.
	 * It uses a list of FourSquare venue files in Json format. It builds the
	 * map matching each FourSquare venue id with a venue created from the
	 * class Venue. It contains a parameter that enables the filtering of the
	 * venues based on whether their categories belong to the black list
	 * contained in CategoryHandler.
	 * 
	 * @param venueInfoDirectoryPath the path to the directory containing the
	 * venues crawled from FourSquare
	 * @param useCategoryFilter a boolean indicating whether the venues need to
	 * be filtered based on their categories; the venues are filtered if and
	 * only if this parameter is true
	 */
	public void parseVenues(String venueInfoDirectoryPath, boolean useCategoryFilter) {
		// venues is reinitialized.
		venues = new HashMap<String, Venue>();
		
		// The files contained at venueInfoDirectoryPath are fetched.
		File venueInfoDirectory = new File(venueInfoDirectoryPath);
		File[] venueInfoFiles = venueInfoDirectory.listFiles();
		
		// Construction of venues.
		for (File venueInfoFile : venueInfoFiles) {
			if (venueInfoFile.isFile() && !FilenameUtils.getBaseName(venueInfoFile.getName()).equals("")) {
				// The current file is indeed a venue info file.
				try {
					// Creation of a venue from the current venue info file.
					Venue venue = new Venue(FileUtils.readFileToString(venueInfoFile));
					String venueId = venue.getId();	
					
					// Checking whether the venue is blacklisted (i.e. the
					// venue category is not relevant).
					if (!useCategoryFilter || !CategoryHandler.isBlackListedVenueStrict(venue)) {
						// The current venue has at least one relevant category
						// and is added to venues.
						venues.put(venueId, venue);
					}
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * This method parses the venues located in the FourSquare venue directory.
	 * It uses a list of FourSquare venue files in Json format. It builds the
	 * map matching each FourSquare venue id with a venue created from the
	 * class Venue. It contains a parameter that enables the filtering of the
	 * venues based on whether their categories belong to the black list
	 * contained in CategoryHandler. Only the venues which ids are contained in
	 * the parameter venueIds are parsed.
	 * 
	 * @param venueInfoDirectoryPath the path to the directory containing the
	 * venues crawled from FourSquare
	 * @param useCategoryFilter a boolean indicating whether the venues need to
	 * be filtered based on their categories; the venues are filtered if and
	 * only if this parameter is true
	 * @param venueIds the list of venue ids to parse
	 */
	public void parseVenues(String venueDirectoryPath, boolean useCategoryFilter, Collection<String> venueIds) {
		// venues is reinitialized.
		venues = new HashMap<String, Venue>();
		
		// The files contained at venueInfoDirectoryPath are fetched and
		// matching the ids of venueIds are fetched.
		Collection<File> venueInfoFiles = new ArrayList<File>();
		for (String venueId : venueIds) {
			venueInfoFiles.add(new File(venueDirectoryPath + File.separator + venueId));
		}
		
		// Construction of venues.
		for (File venueInfoFile : venueInfoFiles) {
			if (venueInfoFile.isFile() && !FilenameUtils.getBaseName(venueInfoFile.getName()).equals("")) {
				// The current file is indeed a venue info file.
				try {
					// Creation of a venue from the current venue info file.
					Venue venue = new Venue(FileUtils.readFileToString(venueInfoFile));
					String venueId = venue.getId();	
					
					// Checking whether the venue is blacklisted (i.e. the
					// venue category is not relevant).
					if (!useCategoryFilter || !CategoryHandler.isBlackListedVenueStrict(venue)) {
						// The current venue has at least one relevant category
						// and is added to venues.
						venues.put(venueId, venue);
					}
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
		}
	}

	public Map<String, Venue> getVenues() {
		return venues;
	}

}
