package uk.ac.gla.compositecontextualsuggester.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

/**
 * This class is used to parse the profiles (i.e. the users) of TREC CS. It
 * creates a map matching each profile id with a user. It uses the profile
 * file of TREC CS containing the ratings of a sample of venues and the file 
 * matching each venue id from the example file of TREC CS with its 
 * corresponding Foursquare id. The ratings are rescaled from -1 to 4 into
 * -0.25 to 1.0).
 * 
 * @author Thibaut Thonet
 *
 */
public class UserHandler {

	Map<String, User> users;

	/**
	 * This method parses the profile file of TREC CS containing the ratings of
	 * a sample of venues. It uses as well the file matching each venue id from
	 * the example file of TREC CS with its corresponding Foursquare id. It
	 * builds the map users matching each profile id with a user that is 
	 * created from the ratings in the profile file. The ratings are rescaled
	 * from -1 to 4 into -0.25 to 1.0).
	 * 
	 * @param profileFilePath the path to the profile file of TREC CS
	 * @param exampleToFoursquareFilePath the path to the file matching each
	 * venue id from the example file of TREC CS with its corresponding 
	 * Foursquare id
	 */
	public void parseUsers(String profileFilePath, String exampleToFoursquareFilePath) {
		try {
			// users is initialized.
			users = new HashMap<String, User>();
			
			// exampleToFoursquareIds maps venue ids from the example file to
			// Foursquare venue ids.
			Map<String, String> exampleToFoursquareIds = new HashMap<String, String>();
			
			// Construction of exampleToFoursquareIds.
			File exampleToFoursquareFile = new File(exampleToFoursquareFilePath);
			List<String> lines = FileUtils.readLines(exampleToFoursquareFile);
			for (String line : lines) {
				// values[0] is the id in the example file and values[1] is the
				// corresponding Foursquare id.
				String[] values = line.split(",");
				exampleToFoursquareIds.put(values[0], values[1]);
			}
			
			// Construction of users.
			File profileFile = new File(profileFilePath);
			lines = FileUtils.readLines(profileFile);
			lines.remove(0); // The first line contains the header.
			for (String line : lines) {
				// values[0] is the id of the user, values[1] is the id of a
				// venue in the example file and values[3] is the user's rating
				// for this venue's website.
				
				// Split values if comma is outside quotes.
				String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
				String userId = values[0];
				String venueFoursquareId = exampleToFoursquareIds.get(values[1]);
				double venueWebSiteRating = Integer.parseInt(values[3]);				
				double venueRating = venueWebSiteRating;
				
				// venueRating is rescaled between -0.25 and +1.0.
				venueRating /= 4.0; 
				
				// Adding the rating of the venue for the appropriate user.
				if (venueFoursquareId != null) {
					// The Foursquare venue corresponding to the example venue
					// has been found.
					if (users.get(userId) == null) {
						// First time encountering this user.
						Map<String, Double> venueRatings = new HashMap<String, Double>();
						venueRatings.put(venueFoursquareId, venueRating);

						User user = new User(userId);
						user.setVenueRatings(venueRatings);
						users.put(userId, user);
					} else {
						// This user has already been encountered.
						User user = users.get(userId);

						Map<String, Double> venueRatings = user.getVenueRatings();
						venueRatings.put(venueFoursquareId, venueRating);
					}
				}
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
	
	public Map<String, User> getUsers() {
		return users;
	}
}
