package uk.ac.gla.compositecontextualsuggester.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

/**
 * This class is used to parse a file that contains a list of venue ids into a
 * collection of strings. This processing enables the filtering of the venues
 * for a given context, each context being associated with a file containing
 * the list of the FourSquare venue ids located in it.
 * 
 * @author Thibaut Thonet
 *
 */
public class LocatedIdHandler {
	
	private Collection<String> locatedIds;
	
	/**
	 * This method parses the file containing the FourSquare venue ids and
	 * builds the collection of ids locatedIds.
	 * 
	 * @param locatedIdFilePath the path to the file containing the foursquare
	 * ids of a context
	 */
	public void parseLocatedIds(String locatedIdFilePath) {
		try {
			// locatedIds is initialized.
			locatedIds = new ArrayList<String>();

			// Construction of locatedIds.
			File contextFile = new File(locatedIdFilePath);
			locatedIds.addAll(FileUtils.readLines(contextFile));
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
	
	public Collection<String> getLocatedIds() {
		return locatedIds;
	}

}
