package uk.ac.gla.compositecontextualsuggester.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.ac.gla.compositecontextualsuggester.util.Bundle;
import uk.ac.gla.compositecontextualsuggester.util.Category;
import uk.ac.gla.compositecontextualsuggester.util.User;
import uk.ac.gla.compositecontextualsuggester.util.Venue;

/**
 * This class is used to build the bundles of venues. An instance of this class
 * is designed to be used as described thereafter.
 * After the creation of the instance, the rated venues (i.e. the venues that 
 * have been rated by the users), the local venues (i.e. the venues that have
 * been pre-filtered based on a context), the (FourSquare) categories and the
 * user (defined from his profile) are set using their respective setter 
 * methods.
 * Then, some overall statistics are computed about the venues (e.g. the 
 * maximum number of "likes" for a venue) by calling the method 
 * computeVenueStats.
 * The next step consists in generating a definite amount of bundles. This is
 * achieved by executing bobo (bundles one-by-one).
 * Finally, a limited amount of bundles (the best ones) are chosen from the
 * ones generated with bobo by calling the method chooseBundles. It creates a
 * list chosenBundles of venue bundles ordered from the bundle with the best
 * score to the bundle with the worst score.
 * 
 * @author Thibaut Thonet
 *
 */
public class ContextualBundleBuilder {
	
	// C_OPOP, C_TCOH and C_EAPP are constants used to define the respective
	// weight of overall popularity (opop), topical coherence (tcoh) and
	// estimated appreciation (eapp) in the computation of the bundle score.
	private static double C_OPOP = 1.0;
	private static double C_TCOH = 1.0;
	private static double C_EAPP = 10.0;
	
	// maxLikeNb corresponds to the maximum number of "likes" a FourSquare
	// venue has been given by users, for all the venues contained in
	// localVenues.
	private int maxLikeNb;
	
	// user is the user for whom the bundles of venues are to be suggested.
	private User user;
	
	// ratedVenues contains the venues that the user has rated.
	private Map<String, Venue> ratedVenues;
	// localVenues ccontains the venues located in a given context (i.e. city).
	private Map<String, Venue> localVenues;
	// categories contains the FourSquare categories.
	private Map<String, Category> categories;
	
	// candidateBundles contains the bundles generated after bobo.
	private Collection<Bundle<Venue>> candidateBundles;
	// candidateBundles contains the bundles chosen in chooseBundles, and that
	// are ordered from the bundle with the best score to the bundle with the
	// worst score.
	private List<Bundle<Venue>> chosenBundles;
	
	public ContextualBundleBuilder() {
		ratedVenues = new HashMap<String, Venue>();
		localVenues = new HashMap<String, Venue>();
		categories = new HashMap<String, Category>();
	}
	
	public void setUser(User user) {
		this.user = user;
	}

	public void setRatedVenues(Map<String, Venue> ratedVenues) {
		this.ratedVenues = ratedVenues;
	}
	
	public void setLocalVenues(Map<String, Venue> localVenues) {
		// Copy of localVenues to prevent the modification of the parameter.
		this.localVenues = new HashMap<String, Venue>(localVenues);
	}
	
	public void setCategories(Map<String, Category> categories) {
		this.categories = categories;
	}
	
	/**
	 * This method computes some overall statistics about the venues (e.g. the
	 * maximum number of "likes" for a venue). These statistics are used to
	 * compute the different criteria (e.g. opop).
	 */
	public void computeVenueStats() {
		// Set to 1 in order to prevent division by 0.
		maxLikeNb = 1;
		
		for (Venue venue : localVenues.values()) {
			int likeNb = venue.getLikes();
			if (likeNb > maxLikeNb) {
				maxLikeNb = likeNb;
			}
		}
	}
	
	/**
	 * This method computes the overall popularity (opop) of a venue. It is
	 * based on the number of "likes" the FourSquare venue has been given by
	 * users.
	 * 
	 * @param venue a venue
	 * 
	 * @return the overall popularity (opop) of the venue
	 */
	public double opop(Venue venue) {
		return (double) venue.getLikes()/maxLikeNb;
	}
	
	/**
	 * This method computes the overall popularity (opop) of a bundle of
	 * venues. It is the average opop of the venues contained in the bundle.
	 * 
	 * @param bundle a bundle of venues
	 * 
	 * @return the overall popularity (opop) of the bundle
	 */
	public double opop(Bundle<Venue> bundle) {
		double opop = 0;
		
		List<Venue> bundleVenues = bundle.getItems();
		for (Venue bundleVenue : bundleVenues) {
			opop += opop(bundleVenue);
		}
		
		opop = (double) opop/bundle.getItemNb();
		
		return opop;
	}
	
	/**
	 * This method computes the estimated appreciation (eapp) of a venue. It is
	 * based on the venue ratings given by the user weighted with the topical
	 * similarity of the parameter venue with the rated venues: it is assumed
	 * that if two venues are topically similar and the user appreciates one of
	 * them, then he is likely to appreciate as well the other venue.
	 * 
	 * @param venue a venue
	 * 
	 * @return the estimated appreciation (eapp) of the venue
	 */
	public double eapp(Venue venue) { // estimated appreciation of a venue given a user's relevance judgments.
		double eapp = 0;
		
		// totalTsim is the sum of the topical similarity between venue and all
		// the rated venues. It is computed to normalize eapp, in which topical
		// similarity is used as weight.
		double totalTsim = 0;
		
		// venueRatings maps each rated venue Id with the rating user has
		// given to this venue (the ratings have been rescaled from -1 to 4
		// into -0.25 to 1.0).
		Map<String, Double> venueRatings = user.getVenueRatings();
		for (String ratedVenueId : venueRatings.keySet()) {
			if (venueRatings.get(ratedVenueId) >= 0) {
				// The user was able to give a relevance judgment on this 
				// venue.
				eapp += venueRatings.get(ratedVenueId)*tsim(venue, ratedVenues.get(ratedVenueId));
				totalTsim += tsim(venue, ratedVenues.get(ratedVenueId));
			}
		}
		
		eapp /= totalTsim;
		
		return eapp;
	}
	
	/**
	 * This method computes the estimated appreciation (eapp) of a bundle of
	 * venues. It is the average eapp of the venues contained in the bundle.
	 * 
	 * @param bundle a bundle of venues
	 * 
	 * @return the estimated appreciation (eapp) of the bundle
	 */
	public double eapp(Bundle<Venue> bundle) {
		double eapp = 0;
		
		List<Venue> bundleVenues = bundle.getItems();
		for (Venue bundleVenue : bundleVenues) {
			eapp += eapp(bundleVenue);
		}
		
		eapp = (double) eapp/bundle.getItemNb();
		
		return eapp;
	}
	
	/**
	 * This method computes the topical similarity (tsim) between two venues.
	 * It is based on the computation of the distance between the closest (i.e.
	 * most similar) category of each venue in the FourSquare category tree.
	 * 
	 * @param venue1 a venue
	 * @param venue2 another venue
	 * 
	 * @return the topical similarity between venue1 and venue2
	 */
	public double tsim(Venue venue1, Venue venue2) {
		// categories1 contains the categories of venue1.
		Collection<Category> categories1 = new ArrayList<Category>();
		for (String categoryId : venue1.getCategories().keySet()) {
			categories1.add(categories.get(categoryId));
		}
		
		// categories2 contains the categories of venue2.
		Collection<Category> categories2 = new ArrayList<Category>();
		for (String categoryId : venue2.getCategories().keySet()) {
			categories2.add(categories.get(categoryId));
		}
		
		// maxSimilarity is the maximum similarity obtained for all pairs of
		// categories containing a category from venue1 and a category from
		// venue2.
		double maxSimilarity = 0;

		// Computation of maxSimilarity by iterating on the categories of
		// venue1 and the categories of venue2.
		for (Category category1 : categories1) {
			for (Category category2 : categories2) {
				// The similarity between two categories is defined as 
				// 1/(1 + distance) where distance is the number of edges
				// separating these two categories in the category tree.
				double similarity = (double) 1/(1 + Category.distance(category1, category2));
				if (similarity > maxSimilarity) {
					maxSimilarity = similarity;
				}
			}
		}
		
		return maxSimilarity;
	}
	
	/**
	 * This method computes the topical coherence of a bundle. It is based on
	 * the average topical similarity of each pairs of venues that can be
	 * extracted from the bundle.
	 * 
	 * @param bundle a bundle of venues
	 * 
	 * @return the topical cohesion (tcoh) of the bundle
	 */
	public double tcoh(Bundle<Venue> bundle) {
		double tcoh = 0;
		
		List<Venue> bundleVenues = bundle.getItems();
		for (Venue bundleVenue1 : bundleVenues) {
			for (Venue bundleVenue2 : bundleVenues) {
				tcoh += tsim(bundleVenue1, bundleVenue2);
			}
		}
		
		tcoh = (double) tcoh/(bundle.getItemNb()*bundle.getItemNb());
		
		return tcoh;
	}
	
	/**
	 * This method computes the score of a bundle of venues. It is based on a
	 * combination of the overall popularity (opop), the topical coherence
	 * (tcoh) and the estimated appreciation (eapp). Each criterion is weighted
	 * by a constant to tune its impact on the score.
	 * 
	 * @param bundle a bundle of venues
	 * 
	 * @return the score of the bundle
	 */
	public double score(Bundle<Venue> bundle) {
		return Math.pow(
				Math.pow(opop(bundle), C_OPOP)*
				Math.pow(tcoh(bundle), C_TCOH)*
				Math.pow(eapp(bundle), C_EAPP), 
				1/(C_OPOP + C_TCOH + C_EAPP));
	}
	
	/**
	 * This method generates a definite amount of bundles of venues. It first
	 * builds a list of pivots, containing the local venues ordered by
	 * decreasing overall popularity (opop). This list is then used to create
	 * bundles around those pivots, by calling the method pickBundle. Once a
	 * pivot is used to build a bundle, this pivot and the venues of this
	 * bundle are tagged so that they will not be processed again. At the end
	 * of the processing, the collection of bundles candidateBundles is built.
	 * 
	 * @param maxVenuesPerBundleNb the maximum number of venues a bundle should
	 * contain
	 * @param bundleNb the number of bundles to generate
	 */
	public void bobo(int maxVenuesPerBundleNb, int bundleNb) {
		candidateBundles = new ArrayList<Bundle<Venue>>();

		// pivots is initialised with the local venues ordered by decreasing 
		// opop.
		List<Venue> pivots = new ArrayList<Venue>(localVenues.values());
		Collections.sort(pivots, new Comparator<Venue>() {
		    public int compare(Venue venue1, Venue venue2) {
		        return -Double.compare(opop(venue1), opop(venue2));
		    }
		});

		// processPivot indicates if a pivot (represented by its ID) has to be
		// processed or not.
		HashMap<String, Boolean> processPivot = new HashMap<String, Boolean>();
		for (Venue pivot : pivots) {
			// At the beginning, pivots are all considered for the processing.
			processPivot.put(pivot.getId(), true);
		}

		// Construction of candidateBundles by iterating on the pivots.
		Iterator<Venue> pivotIterator = pivots.iterator();
		while (pivotIterator.hasNext() && candidateBundles.size() < bundleNb) {
			Venue pivot = pivotIterator.next();
			String pivotId = pivot.getId();

			if (processPivot.get(pivotId)) {
				// The current pivot has to be processed.

				// A bundle is created around pivot using the other venues.
				localVenues.remove(pivotId);
				Bundle<Venue> bundle = pickBundle(pivot, maxVenuesPerBundleNb);
				
				// The venues chosen for the bundle are removed from venues and
				// will not be processed as future pivots.
				List<Venue> bundleVenues = bundle.getItems();
				for (Venue bundleVenue : bundleVenues) {
					localVenues.remove(bundleVenue.getId());
					processPivot.put(bundleVenue.getId(), false);
				}

				// bundle is added to the candidate bundles.
				candidateBundles.add(bundle);
			}
		}
	}
	
	/**
	 * This method builds a bundle around a pivotal venue. The venues that have
	 * the best combination of topical similarity (tsim) with the pivot and
	 * estimated appreciation (eapp) are selected to be aggregated to the
	 * pivot in order to form a cohesive bundle. Such best venues are found by
	 * calling the method findMaxTsimVenue.
	 * 
	 * @param pivot a pivotal venue
	 * @param maxVenuePerBundleNb the maximum number of venues a bundle should
	 * contain
	 * 
	 * @return a cohesive bundle built around the pivotal venue
	 */
	public Bundle<Venue> pickBundle(Venue pivot, int maxVenuePerBundleNb) {
		// At the beginning, the bundle of venues contains only the pivot.
		List<Venue> bundleVenues = new ArrayList<Venue>();
		bundleVenues.add(pivot);
		Bundle<Venue> bundle = new Bundle<Venue>(bundleVenues);
		
		// activeVenues is initialized as a copy of venues.
		Map<String, Venue> activeVenues = new HashMap<String, Venue>(localVenues);
		
		// Construction of bundle by finding the most topically similar venues
		// to the pivot.
		while (bundle.getItemNb() < maxVenuePerBundleNb && !activeVenues.isEmpty()) {
			Venue maxTsimVenue = findMaxTsimVenue(pivot, activeVenues);
			if (maxTsimVenue != null) {
				// maxTsimVenue won't be processed again.
				activeVenues.remove(maxTsimVenue.getId());
				bundleVenues.add(maxTsimVenue);
			}
		}
		
		return bundle;
	}
	
	/**
	 * This method finds the venue that has the best combination of topical
	 * similarity (tsim) with the pivot and estimated appreciation (eapp) in
	 * a given collection of venues.
	 * 
	 * @param pivot a pivotal venue
	 * @param activeVenues the venues in which the best venue has to be found
	 * 
	 * @return the venue that has the best combination of topical similarity
	 * (tsim) with the pivot and estimated appreciation (eapp) in activeVenues
	 */
	public Venue findMaxTsimVenue(Venue pivot, Map<String, Venue> activeVenues) {
		double maxTsim = -1;
		Venue maxTsimVenue = null;
		
		for (Venue venue : activeVenues.values()) {
			double tsim = (2*tsim(pivot, venue) + eapp(venue))/3;
			
			if (tsim > maxTsim) {
				maxTsimVenue = venue;
				maxTsim = tsim;
			}
		}

		return maxTsimVenue;
	}
	
	/**
	 * This method is used to choose the best bundles from the ones that have
	 * been generated in candidateBundles. The best bundles are the ones
	 * maximizing their score. Such best bundles are found by calling the
	 * method findMaxScoreBundle.
	 * 
	 * @param bundleNb the number of bundles to return to the user
	 */
	public void chooseBundles(int bundleNb) {
		// activeBundles is initialized as a copy of candidateBundles.
		Collection<Bundle<Venue>> activeBundles = new ArrayList<Bundle<Venue>>(candidateBundles);
		
		chosenBundles = new ArrayList<Bundle<Venue>>();
		
		// Selection of the bundles.
		while (chosenBundles.size() < bundleNb && !activeBundles.isEmpty()) {
			Bundle<Venue> maxScoreBundle = findMaxScoreBundle(activeBundles);
			if (maxScoreBundle != null) {
				activeBundles.remove(maxScoreBundle);
				chosenBundles.add(maxScoreBundle);
			}
		}
	}

	/**
	 * This method finds the bundle that has the best score in a given
	 * collection of bundles.
	 * 
	 * @param activeBundles the bundles in which the best bundle has to be
	 * found
	 * 
	 * @return the bundle that has the best score in activeBundles
	 */
	public Bundle<Venue> findMaxScoreBundle(Collection<Bundle<Venue>> activeBundles) {
		double maxScore = -1;
		Bundle<Venue> maxScoreBundle = null;
		
		for (Bundle<Venue> bundle : activeBundles) {
			double score = score(bundle);
			if (score > maxScore) {
				maxScoreBundle = bundle;
				maxScore = score;
			}
		}
		
		return maxScoreBundle;
	}

	public Collection<Bundle<Venue>> getChosenBundles() {
		return chosenBundles;
	}
	
	/**
	 * This method finds within the venues rated by the user which venues are
	 * both similar to a given venue and have been given good ratings (i.e. 3
	 * or 4 out of 4) by the user. It is used to inform the user that the
	 * venues that are recommed to him are similar to other venues he liked 
	 * when he rated the sample venues.
	 * 
	 * @param venue a venue
	 * 
	 * @return a list of the venues that have been rated by the users, and that
	 * both got good ratings and are topically similar to venue; the list is
	 * ordered by decreasing estimated appreciation (eapp)
	 */
	public List<Venue> findSimilarRelevantVenues(Venue venue) {
		List<Venue> similarRelevantVenues = new ArrayList<Venue>();
		
		// venueRatings maps each rated venue Id with the rating user has
		// given to this venue (the ratings have been rescaled from -1 to 4
		// into -0.25 to 1.0).
		Map<String, Double> venueRatings = user.getVenueRatings();
		for (String ratedVenueId : venueRatings.keySet()) {
			Venue ratedVenue = ratedVenues.get(ratedVenueId);
			if (tsim(venue, ratedVenue) == 1.0 && 
					(venueRatings.get(ratedVenueId) == 0.75 || venueRatings.get(ratedVenueId) == 1.0)) {
				// ratedVenue is both topically similar to venue (tsim = 1) and
				// got a good rating (0.75/1.0 or 1.0/1.0). It is thus added to
				// similarRelevantVenues.
				similarRelevantVenues.add(ratedVenue);
			}
		}
		
		// similarRelevantVenues is ordered by decreasing estimated
		// appreciation (eapp). Estimated appreciation is chosen over the
		// rating of the venue in order to smooth the rating and take into
		// account the complete profile of the user.
		Collections.sort(similarRelevantVenues, new Comparator<Venue>() {
		    public int compare(Venue venue1, Venue venue2) {
		    	return -Double.compare(eapp(venue1), eapp(venue2));
		    }
		});
		
		return similarRelevantVenues;
	}

}
