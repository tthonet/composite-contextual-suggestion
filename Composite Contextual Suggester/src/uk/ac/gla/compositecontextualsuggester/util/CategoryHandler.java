package uk.ac.gla.compositecontextualsuggester.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This class is used to parse the FourSquare category file. It creates a map
 * matching each category id with a category, that preserve the tree structure
 * information because of their respective parent and children categories.
 * It contains as well a static black list of the categories that have no 
 * touristic purpose.
 * 
 * @author Thibaut Thonet
 *
 */
public class CategoryHandler {
	
	private Map<String, Category> categories = new HashMap<String, Category>();
	
	// BLACK_LIST is a static set of venue ids that have no touristic purpose.
	private static Set<String> BLACK_LIST = new HashSet<String>();
	
	static {
		BLACK_LIST.add("530e33ccbcbc57f1066bbfe4"); //			States & Municipalities
		BLACK_LIST.add("50aa9e094b90af0d42d5de0d"); // 				City
		BLACK_LIST.add("5345731ebcbc57f1066c39b2"); // 				County
		BLACK_LIST.add("530e33ccbcbc57f1066bbff7"); //				Country
//		BLACK_LIST.add("4f2a25ac4b909258e854f55f"); //				Neighborhood
		BLACK_LIST.add("530e33ccbcbc57f1066bbff8"); //				State
		BLACK_LIST.add("530e33ccbcbc57f1066bbff3"); //				Town
//		BLACK_LIST.add("530e33ccbcbc57f1066bbff9"); //				Village
//		BLACK_LIST.add("4bf58dd8d48988d130941735"); //			Building
		BLACK_LIST.add("4bf58dd8d48988d196941735"); // 				Hospital
		BLACK_LIST.add("4bf58dd8d48988d124941735"); //			Office
		BLACK_LIST.add("4c38df4de52ce0d596b336e1"); // 			Parking
		BLACK_LIST.add("4e67e38e036454776db1fb3a"); // 	Residence
		BLACK_LIST.add("5032891291d4c4b30a586d68"); // 		Assisted Living
		BLACK_LIST.add("4bf58dd8d48988d103941735"); // 		Home (private)
		BLACK_LIST.add("4f2a210c4b9023bd5841ed28"); // 		Housing Development
		BLACK_LIST.add("4d954b06a243a5684965b473"); // 		Residential Building (Apartment / Condo)
		BLACK_LIST.add("4bf58dd8d48988d1d5941735"); //		Hotel Bar
		BLACK_LIST.add("4d4b7105d754a06379d81259"); // 	Travel & Transport
		BLACK_LIST.add("4bf58dd8d48988d1ed931735"); // 		Airport
		BLACK_LIST.add("4bf58dd8d48988d1ef931735"); //   		Airport Food Court
		BLACK_LIST.add("4bf58dd8d48988d1f0931735"); //   		Airport Gate
		BLACK_LIST.add("4eb1bc533b7b2c5b1d4306cb"); //   		Airport Lounge
		BLACK_LIST.add("4bf58dd8d48988d1eb931735"); //   		Airport Terminal
		BLACK_LIST.add("4bf58dd8d48988d1ec931735"); //   		Airport Tram
		BLACK_LIST.add("4bf58dd8d48988d1f7931735"); //   		Plane
		BLACK_LIST.add("4bf58dd8d48988d12d951735"); // 		Boat or Ferry
		BLACK_LIST.add("52f2ab2ebcbc57f1066b8b4b"); // 		Border Crossing
		BLACK_LIST.add("4bf58dd8d48988d1fe931735"); // 		Bus Station
		BLACK_LIST.add("4bf58dd8d48988d12b951735"); // 		Bus Line
		BLACK_LIST.add("52f2ab2ebcbc57f1066b8b4f"); // 		Bus Stop
		BLACK_LIST.add("52f2ab2ebcbc57f1066b8b50"); // 		Cable Car
		BLACK_LIST.add("4bf58dd8d48988d1f6931735"); // 		General Travel
		BLACK_LIST.add("4bf58dd8d48988d1fa931735"); // 		Hotel
		BLACK_LIST.add("4bf58dd8d48988d1f8931735"); //			Bed & Breakfast	     
		BLACK_LIST.add("4f4530a74b9074f6e4fb0100"); //			Boarding House	     
		BLACK_LIST.add("4bf58dd8d48988d1ee931735"); // 			Hostel     
		BLACK_LIST.add("4bf58dd8d48988d132951735"); //			Hotel Pool    
		BLACK_LIST.add("4bf58dd8d48988d1fb931735"); //			Motel
		BLACK_LIST.add("4bf58dd8d48988d12f951735"); //			Resort
		BLACK_LIST.add("4bf58dd8d48988d133951735"); //			Roof Deck 
		BLACK_LIST.add("52f2ab2ebcbc57f1066b8b4c"); // 		Intersection
		BLACK_LIST.add("4bf58dd8d48988d1fc931735"); // 		Light Rail
		BLACK_LIST.add("4f2a23984b9023bd5841ed2c"); // 		Moving Target
		BLACK_LIST.add("52f2ab2ebcbc57f1066b8b53"); // 		RV Park
		BLACK_LIST.add("4bf58dd8d48988d1ef941735"); // 		Rental Car Location
		BLACK_LIST.add("4d954b16a243a5684b65b473"); // 		Rest Area
		BLACK_LIST.add("4bf58dd8d48988d1f9931735"); // 		Road
		BLACK_LIST.add("52f2ab2ebcbc57f1066b8b52"); // 		Street
		BLACK_LIST.add("4bf58dd8d48988d1fd931735"); // 		Subway
		BLACK_LIST.add("4bf58dd8d48988d130951735"); // 		Taxi
		BLACK_LIST.add("52f2ab2ebcbc57f1066b8b4d"); // 		Toll Booth
		BLACK_LIST.add("52f2ab2ebcbc57f1066b8b4e"); // 		Toll Plaza
		BLACK_LIST.add("4f4530164b9074f6e4fb00ff"); // 		Tourist Information Center
		BLACK_LIST.add("4bf58dd8d48988d129951735"); // 		Train Station
		BLACK_LIST.add("4f4531504b9074f6e4fb0102"); // 			Platform
		BLACK_LIST.add("4bf58dd8d48988d12a951735"); // 			Train
		BLACK_LIST.add("52f2ab2ebcbc57f1066b8b51"); // 		Tram
		BLACK_LIST.add("4f04b25d2fb6e1c99f3db0c0"); // 		Travel Lounge
		BLACK_LIST.add("52f2ab2ebcbc57f1066b8b4a"); // 		Tunnel	
	};
	
	/**
	 * This method indicates if a venue is black-listed. A venue is
	 * black-listed if and only if all its categories have their ids in the
	 * black-list.
	 * 
	 * @param venue a venue
	 * 
	 * @return true if all the venue's categories are in the black-list, false
	 * otherwise.
	 */
	public static boolean isBlackListedVenueStrict(Venue venue) {
		boolean isBlackListed = true;
		
		for(String id : venue.getCategories().keySet()){
			isBlackListed &= BLACK_LIST.contains(id);
		}
		
		return isBlackListed;
	}

	/**
	 * This method parses the Foursquare category file and creates the map
	 * categories matching each category id with a category. These categories
	 * conserve the tree structure information because of their respective
	 * parent and children categories.
	 * 
	 * @param venueCategoryFilePath the path to the FourSquare category file
	 */
	public void parseCategories(String venueCategoryFilePath) {
		try {
			// json contains the json code of the category file
			String json = FileUtils.readFileToString(new File(venueCategoryFilePath));
	
			JsonParser parser = new JsonParser();
			JsonArray array = parser.parse(json).getAsJsonArray();
			
			// Creation of root, which is an artificial category.
			Category root = new Category();
			root.setId("root");
			root.setName("root");
			root.setPluralName("root");
			root.setShortName("root");
			Collection<String> rootIcon = new ArrayList<String>();
			rootIcon.add("root");
			rootIcon.add(".png");
			root.setIcon(rootIcon);
			root.setCategories(buildCategories(array, root));
			root.setParent(null);
			
			// categories is initialized.
			categories = new HashMap<String, Category>();
			addCategories(root);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * This method builds a category from the JsonObject given in parameter
	 * and its parent is defined as the parent given in parameter. If the
	 * category has descendants, the descendant categories will be built 
	 * recursively as well by calling buildCategories.
	 * 
	 * @param object a JsonObject corresponding to a category
	 * @param parent the parent of the category to create
	 * 
	 * @return the category with all his descendants, built from the JsonObject
	 * and the parent category
	 */
	public Category buildCategory(JsonObject object, Category parent) {
		// Creation of category.
		Category category = new Category();
		category.setParent(parent);
		category.setId(object.get("id").getAsString());
		category.setName(object.get("name").getAsString());
		category.setPluralName(object.get("pluralName").getAsString());
		category.setShortName(object.get("shortName").getAsString());
		
		// Creation of the icon of the category.
		JsonObject iconObject = object.get("icon").getAsJsonObject();
		Collection<String> iconCollection = new ArrayList<String>();
		iconCollection.add(iconObject.get("prefix").getAsString());
		iconCollection.add(iconObject.get("suffix").getAsString());
		category.setIcon(iconCollection);
		
		// If the category has children, the method buildCategories will call
		// recursively on each child the method buildCategory.
		if (object.get("categories") != null) {
			category.setCategories(buildCategories(object.get("categories").getAsJsonArray(), category));
		} else {
			category.setCategories(new ArrayList<Category>());
		}
		
		return category;
	}

	/**
	 * This method builds a collection of categories from the JsonArray and 
	 * calls buildCategory for each JsonObject contained in the JsonArray. If
	 * the categories have descendants, the descendant categories will be
	 * built recursively as well by calling buildCategory.
	 * 
	 * @param array a JsonArray containing JsonObjects that correspond to 
	 * categories
	 * @param parent the parent of the categories to build
	 * 
	 * @return the collection of categories with all their descendants, built
	 * from the JsonArray and the parent category
	 */
	public Collection<Category> buildCategories(JsonArray array, Category parent) {
		Collection<Category> categories = new ArrayList<Category>();
		
		// Creation of a category for each JsonElement contained in the 
		// JsonArray.
		for (JsonElement element : array) {
			Category category =  buildCategory(element.getAsJsonObject(), parent);
			categories.add(category);
		}
		
		return categories;
	}
	
	/**
	 * This method recursively adds all the descendant categories of the root
	 * category to categories.
	 * 
	 * @param root the root category
	 */
	public void addCategories(Category root) {
		for (Category category : root.getCategories()) {
			categories.put(category.getId(), category);
			addCategories(category);
		}
	}
	
	public Map<String, Category> getCategories() {
		return categories;
	}

}
