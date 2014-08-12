package org.terrier.compositecontextualsuggester.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.terrier.compositecontextualsuggester.util.Bundle;
import org.terrier.compositecontextualsuggester.util.Category;
import org.terrier.compositecontextualsuggester.util.CategoryHandler;
import org.terrier.compositecontextualsuggester.util.LocatedIdHandler;
import org.terrier.compositecontextualsuggester.util.Location;
import org.terrier.compositecontextualsuggester.util.LocationHandler;
import org.terrier.compositecontextualsuggester.util.User;
import org.terrier.compositecontextualsuggester.util.UserHandler;
import org.terrier.compositecontextualsuggester.util.Venue;
import org.terrier.compositecontextualsuggester.util.VenueHandler;

/**
 * This class is used to wrap the processing related to the contextual
 * suggestion of bundles of venues. It contains a method main that generates a
 * log file containing the bundles recommended for each pair of the users and
 * contexts that are specified in the arguments.
 * 
 * The program is designed to be executed with the following options:
 * -append                                        Append the result to the
 *                                                output file (optional).
 * -categories <foursquare-category-file-path>    Path to the Foursquare
 *                                                category file.
 * -contextids <context-id1> ... <context-idn>    Context ID(s) to process,
 *                                                separated by spaces;
 *                                                default: all contexts
 *                                                (optional).
 * -contexts <CS-contexts-file-path>              Path to the CS context
 *                                                file.
 * -extfs <example_venue2foursquare-file-path>    Path to the file
 *                                                example_venue2foursquare.
 * -nbuncreate <number-of-bundles-to-create>      Number of bundles to
 *                                                create and choose from,
 *                                                superior or equal to
 *                                                nbunret; default:
 *                                                10*nbunret (optional).
 * -nbunret <number-of-bundles-to-return>         Number of bundles to
 *                                                return; default: 10
 *                                                (optional).
 * -nvenpbun <number-of-venues-per-bundle>        Number of venues per
 *                                                bundle; default: 5
 *                                                (optional).
 * -output <output-file-path>                     Path to the output file.
 * -profiles <CS-profile-file-path>               Path to the CS profile
 *                                                file.
 * -profileids <profile-id1> ... <profile-idm>    Profile ID(s) to process,
 *                                                separated by spaces;
 *                                                default: all profiles
 *                                                (optional).
 * -venpcity <venue-per-city-id-directory-path>   Path to the venue per city
 *                                                id directory.
 * -venues <foursquare-venue-directory-path>      Path to the Foursquare
 *                                                venue directory.
 * -verbose                                       Print information about
 *                                                the execution (optional).
 * 
 * Each line of the generated log file is built according to the following 
 * format: 
 * "userId_contextId bundleRank.venueRank venueId bundleScore
 * similarRelevantVenueId1#...#similarRelevantVenueIdN", where userId is the ID
 * of the user, contextId is the ID of the context, bundleRank is the rank of
 * the bundle for this user-context pair, venueRank is the rank of the venue
 * in this bundle, venueId is the Foursquare ID of the venue, bundleScore is
 * the score of the bundle (between 0 and 1), similarRelevantVenueId1, ..., 
 * similarRelevantVenueIdN are venues rated by the user that are topically
 * similar to the current venue and got a good rating by this user.
 * 
 * @author Thibaut Thonet
 *
 */
public class ContextualBundleSuggester {
	
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException, InterruptedException {
		
		// options contains the different options that can be added as
		// arguments to the program.
		Options options = new Options();
		options.addOption(OptionBuilder.hasArg().isRequired().withArgName("foursquare-venue-directory-path").withDescription("Path to the Foursquare venue directory.").create("venues"));
		options.addOption(OptionBuilder.hasArg().isRequired().withArgName("foursquare-category-file-path").withDescription("Path to the Foursquare category file.").create("categories"));
		options.addOption(OptionBuilder.hasArg().isRequired().withArgName("CS-profile-file-path").withDescription("Path to the CS profile file.").create("profiles"));
		options.addOption(OptionBuilder.hasArg().isRequired().withArgName("example_venue2foursquare-file-path").withDescription("Path to the file example_venue2foursquare.").create("extfs"));
        options.addOption(OptionBuilder.hasArg().isRequired().withArgName("CS-contexts-file-path").withDescription("Path to the CS context file.").create("contexts"));
        options.addOption(OptionBuilder.hasArg().isRequired().withArgName("venue-per-city-id-directory-path").withDescription("Path to the venue per city id directory.").create("venpcity"));
        options.addOption(OptionBuilder.hasArg().isRequired().withArgName("output-file-path").withDescription("Path to the output file.").create("output"));
        options.addOption("append", false, "Append the result to the output file (optional).");
        options.addOption(OptionBuilder.hasArg().withArgName("number-of-bundles-to-return").withDescription("Number of bundles to return; default: 10 (optional).").create("nbunret"));
        options.addOption(OptionBuilder.hasArg().withArgName("number-of-venues-per-bundle").withDescription("Number of venues per bundle; default: 5 (optional).").create("nvenpbun"));
        options.addOption(OptionBuilder.hasArg().withArgName("number-of-bundles-to-create").withDescription("Number of bundles to create and choose from, superior or equal to nbunret; default: 10*nbunret (optional).").create("nbuncreate"));
        options.addOption(OptionBuilder.hasArgs(Option.UNLIMITED_VALUES).withArgName("profile-id1> ... <profile-idm").withDescription("Profile ID(s) to process, separated by spaces; default: all profiles (optional).").create("profileids"));
        options.addOption(OptionBuilder.hasArgs(Option.UNLIMITED_VALUES).withArgName("context-id1> ... <context-idn").withDescription("Context ID(s) to process, separated by spaces; default: all contexts (optional).").create("contextids"));
        options.addOption("verbose", false, "Print information about the execution (optional).");
        
        try {
            CommandLineParser parser = new GnuParser();
            // Parsing of the program arguments.
        	CommandLine commandLine = parser.parse(options, args);
            
            String venueDirectoryPath = commandLine.getOptionValue("venues");
			String venueCategoryFilePath = commandLine.getOptionValue("categories");
			String profileFilePath = commandLine.getOptionValue("profiles");
			String exampleToFoursquareFilePath = commandLine.getOptionValue("extfs");
			String contextFilePath = commandLine.getOptionValue("contexts");
			String venuePerCityDirectoryPath = commandLine.getOptionValue("venpcity");
			String outputFilePath = commandLine.getOptionValue("output");
			boolean appendToOutputFile = commandLine.hasOption("append");
			int bundleToReturnNb = commandLine.hasOption("nbunret") ? Integer.parseInt(commandLine.getOptionValue("nbunret")) : 10;			
			int venuesPerBundleNb = commandLine.hasOption("nvenpbun") ? Integer.parseInt(commandLine.getOptionValue("nvenpbun")) : 5;
			int bundleToCreateNb = commandLine.hasOption("nbuncreate") ? Integer.parseInt(commandLine.getOptionValue("nbuncreate")) : 10*bundleToReturnNb;
            boolean verbose = commandLine.hasOption("verbose");
            
            // Beginning of the execution.
    		long beginTime = System.currentTimeMillis();
			
    		// Fetching the Foursquare categories.
			CategoryHandler categoryHandler = new CategoryHandler();
			categoryHandler.parseCategories(venueCategoryFilePath);
			Map<String, Category> categories = categoryHandler.getCategories();
			
			// Fetching the users (i.e. profiles).
			UserHandler userHandler = new UserHandler();
			userHandler.parseUsers(profileFilePath, exampleToFoursquareFilePath);
			Map<String, User> users = userHandler.getUsers();
			if (commandLine.hasOption("profileids")) {
				// The profiles of the program specified a list of profiles
				// using the option -profileids. Only these profiles will be
				// processed.
				String[] userIds = commandLine.getOptionValues("userids");
				
				// Users (i.e. profiles) are filtered.
				Map<String, User> filteredUsers = new HashMap<String, User>();
				for (String userId : userIds) {
					filteredUsers.put(userId, users.get(userId));
				}	
				users = filteredUsers;
			}	
			
			// Fetching the locations (i.e. contexts).
			LocationHandler locationHandler = new LocationHandler();
			locationHandler.parseLocations(contextFilePath);
			Map<String, Location> locations = locationHandler.getLocations();
			if (commandLine.hasOption("contextids")) {
				// The contexts of the program specified a list of contexts
				// using the option -contextids. Only these contexts will be
				// processed.
				String[] locationIds = commandLine.getOptionValues("contextids");
				
				// Locations (i.e. contexts) are filtered.
				Map<String, Location> filteredLocations = new HashMap<String, Location>();
				for (String locationId : locationIds) {
					filteredLocations.put(locationId, locations.get(locationId));
				}	
				locations = filteredLocations;
			}
			
			// iterationCount counts the number of user-location pairs have
			// been processed.
			int iterationCount = 1;
			
			// Iteration on locations.
			for (String locationId : locations.keySet()) {
				Location location = locations.get(locationId);
				
				// Fetching the ID of the venues that are located in the
				// current location.
				LocatedIdHandler locatedIdHandler = new LocatedIdHandler();
				locatedIdHandler.parseLocatedIds(venuePerCityDirectoryPath + File.separator + location.getName() + ".ids.filtered");

				// Fetching the venues associated with these IDs.
				VenueHandler venueHandler = new VenueHandler();
				venueHandler.parseVenues(venueDirectoryPath, true, locatedIdHandler.getLocatedIds()); // Filtering of venues with blacklisted categories.
				Map<String, Venue> localVenues = venueHandler.getVenues();
				
				// Iteration on users.
				for (String userId : users.keySet()) {
					User user = users.get(userId);

					// Fetching the venues rated by te current user.
					venueHandler.parseVenues(venueDirectoryPath, false, user.getVenueRatings().keySet()); // No filtering of venues.
					Map<String, Venue> ratedVenues = venueHandler.getVenues();

					if (verbose) {
						// Displaying the progress of the execution.
						System.out.println("[" + iterationCount + "/" + locations.size()*users.size() + "] userId: " + userId + ", contextId: " + locationId);
					}

					// Construction of the bundles.
					ContextualBundleBuilder contextualBundleBuilder = new ContextualBundleBuilder();
					contextualBundleBuilder.setRatedVenues(ratedVenues);
					contextualBundleBuilder.setLocalVenues(localVenues);
					contextualBundleBuilder.setCategories(categories);
					contextualBundleBuilder.setUser(user);
					contextualBundleBuilder.computeVenueStats();
					contextualBundleBuilder.bobo(venuesPerBundleNb, bundleToCreateNb);
					contextualBundleBuilder.chooseBundles(bundleToReturnNb);
					Collection<Bundle<Venue>> bundles = contextualBundleBuilder.getChosenBundles();
					
					// In the first iteration, the boolean appendToOutputFile
					// determines whether the output file is overwritten (in
					// the case it already exists). In the other iterations,
					// the result is always appended to the output file.
					FileWriter fileWriter = new FileWriter(outputFilePath, iterationCount == 1 ? appendToOutputFile : true);
					PrintWriter printWriter = new PrintWriter(fileWriter, true);

					int bundleRank = 1;
					for (Bundle<Venue> bundle : bundles) {
						List<Venue> bundleVenues = bundle.getItems();

						int venueRank = 1;
						for (Venue venue : bundleVenues) {
							StringBuilder stringBuilder = new StringBuilder();
							stringBuilder.append(userId + "_" + locationId + " " + bundleRank + "." + venueRank + " " + venue.getId() + " " + contextualBundleBuilder.score(bundle));

							// Computation of the venues rated by the user that
							// are topically similar to the current venue and
							// got a good rating by this user.
							Collection<Venue> similarRelevantVenues = contextualBundleBuilder.findSimilarRelevantVenues(venue);
							if (!similarRelevantVenues.isEmpty()) {
								boolean firstIteration = true;
								for (Venue similarRelevantVenue : similarRelevantVenues) {
									if (firstIteration) {
										// No "#" to append in the first
										// iteration.
										stringBuilder.append(" " + similarRelevantVenue.getId());
										firstIteration = false;
									} else {
										stringBuilder.append("#" + similarRelevantVenue.getId());
									}
								}
							}

							printWriter.println(stringBuilder.toString());

							venueRank++;
						}

						bundleRank++;
					}

					printWriter.close();
					
					iterationCount++;
				}
			}
			
			// End of the execution.
			long endTime = System.currentTimeMillis();

			// Total execution time.
			float totalTime = endTime - beginTime;
			DecimalFormat decimalFormat = new DecimalFormat("0.000");
			if (verbose) {
				System.out.println("Execution time: " + decimalFormat.format(totalTime/1000) + " second(s)");
			}
        } catch(ParseException exception) {
            System.out.print("Parsing error: ");
            System.out.println(exception.getMessage());
            
            // Displaying the usage.
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -Xmx512m -jar composite-contextual-suggester.jar", options);
        }
	}

}
