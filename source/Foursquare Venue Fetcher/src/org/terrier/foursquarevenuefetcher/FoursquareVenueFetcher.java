package org.terrier.foursquarevenuefetcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * This class is used to fetch the JSON files for the Foursquare venues which
 * Foursquare IDs are contained in a given file. It contains a method main that
 * generates the JSON files.
 * 
 * The program is designed to be executed with the following options:
 * -clid <foursquare-client-id>                 Foursquare ID of the client.
 * -clsecret <foursquare-secret-id>             Foursquare secret of the
 *                                              client.
 * -output <output-directory-path>              Path to the output
 *                                              directory.
 * -venidfile <foursquare-venue-id-file-path>   Path to the file containing
 *                                              the ids of the Foursquare
 *                                              venues.
 * -verbose                                     Print information about the
 *                                              execution (optional).
 * 
 * @author Thibaut Thonet
 *
 */
public class FoursquareVenueFetcher {
	
	/**
	 * This method parses the Foursquare venue ID file and creates a collection of
	 * strings containing the IDs.
	 * 
	 * @param foursquareIdFilePath the path to file containing the Foursquare
	 * venue IDs
	 * @return the collection of Foursquare venue IDs
	 */
	public static Collection<String> getFoursquareVenueIds(String foursquareIdFilePath) throws IOException {
		File foursquareIdFile = new File(foursquareIdFilePath);
		
		Collection<String> foursquareIds = FileUtils.readLines(foursquareIdFile);
		
		return foursquareIds;
	}
	
	/**
	 * This method is adapted from Dyaa Albakour's and Romain Deveaud's code.
	 * It gets the JSON string containing all the information about a venue,
	 * given its ID.
	 * 
	 * @param venueId the Foursquare ID of a venue
	 * @param clientId the Foursquare ID of the client
	 * @param clientSecret the Foursquare secret of the client
	 * @return the JSON string of the venue
	 */
	public static String getFoursquareVenueById(String venueId, String clientId, String clientSecret) throws Exception {
		HttpsURLConnection connection = null;
		InputStream inputStream = null;
		int responseCode;
		
		String url = "";
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String vParam = format.format(calendar.getTime());
		
		try {
			url = "https://api.foursquare.com/v2/venues/" + URLEncoder.encode(venueId,"UTF-8") +
					"?client_id=" + clientId +
					"&client_secret=" + clientSecret +
					"&v=" + vParam;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		final StringBuilder out = new StringBuilder();
		
		try {
			connection = (HttpsURLConnection) (new URL(url)).openConnection();

			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.setReadTimeout(20000);	            

			connection.connect();

			// Getting the response code will open the connection,
			// send the request, and read the HTTP response headers.
			// The headers are stored until requested.
			responseCode = connection.getResponseCode();
			
			if (responseCode != HttpsURLConnection.HTTP_OK) {
				throw new Exception(Integer.toString(responseCode));
			}

			inputStream = connection.getInputStream();

			final char[] buffer = new char[2048];

			final Reader in = new InputStreamReader(inputStream, "UTF-8");
			for (;;) {
				int readSize = in.read(buffer, 0, buffer.length);
				if (readSize < 0)
					break;
				out.append(buffer, 0, readSize);
			}
			
			JsonElement parsedLine;
			
			try {
				parsedLine = new JsonParser().parse(out.toString());
			} catch (JsonSyntaxException e) {
				System.out.println("Skipped malformed line in the Foursquare crawl.");
				throw e;
			}

			return parsedLine.getAsJsonObject().get("response").getAsJsonObject().get("venue").toString();
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Not an HTTP URL");
		} catch (MalformedURLException e) {
			throw e;
		} catch (IOException e) {	
			throw e;
		}
	}

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		// options contains the different options that can be added as
		// arguments to the program.
		Options options = new Options();
		options.addOption(OptionBuilder.hasArg().isRequired().withArgName("foursquare-venue-id-file-path").withDescription("Path to the file containing the ids of the Foursquare venues.").create("venidfile"));
		options.addOption(OptionBuilder.hasArg().isRequired().withArgName("foursquare-client-id").withDescription("Foursquare ID of the client.").create("clid"));
		options.addOption(OptionBuilder.hasArg().isRequired().withArgName("foursquare-secret-id").withDescription("Foursquare secret of the client.").create("clsecret"));
		options.addOption(OptionBuilder.hasArg().isRequired().withArgName("output-directory-path").withDescription("Path to the output directory.").create("output"));
		options.addOption("verbose", false, "Print information about the execution (optional).");
		
		try {
            CommandLineParser parser = new GnuParser();
            // Parsing of the program arguments.
        	CommandLine commandLine = parser.parse(options, args);
            
            String venueIdFilePath = commandLine.getOptionValue("venidfile");
            String clientId = commandLine.getOptionValue("clid");
            String clientSecret = commandLine.getOptionValue("clsecret");
            String outputDirectoryPath = commandLine.getOptionValue("output");
		
            boolean verbose = commandLine.hasOption("verbose");
            
            // Beginning of the execution.
            long beginTime = System.currentTimeMillis();
            
            Collection<String> foursquareVenueIds = getFoursquareVenueIds(venueIdFilePath);
            
            for (String foursquareVenueId : foursquareVenueIds) {
            	File foursquareVenueFile = new File(outputDirectoryPath + File.separator + foursquareVenueId);
            	if (!foursquareVenueFile.exists()) {
            		String jsonString = getFoursquareVenueById(foursquareVenueId, clientId, clientSecret);
            		FileUtils.write(foursquareVenueFile, jsonString);
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
			formatter.printHelp("java -jar foursquare-venue-fetcher.jar", options);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
