package org.terrier.compositecontextualsuggester.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * This class is an implementation of a Foursquare category.
 * 
 * @author Thibaut Thonet
 *
 */
public class Category {
	
	private String id;
	private String name;
	private String pluralName;
	private String shortName;
	
	// parent is the parent category of this category in the tree of all Foursquare categories.
	private Category parent;
	
	private Collection<String> icon;
	
	// categories are the children categories of this category in the tree of all Foursquare categories.
	private Collection<Category> categories;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPluralName() {
		return pluralName;
	}

	public void setPluralName(String pluralName) {
		this.pluralName = pluralName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public Collection<String> getIcon() {
		return icon;
	}

	public void setIcon(Collection<String> icon) {
		this.icon = icon;
	}

	public Collection<Category> getCategories() {
		return categories;
	}

	public void setCategories(Collection<Category> categories) {
		this.categories = categories;
	}
	
	public Category getParent() {
		return parent;
	}

	public void setParent(Category parent) {
		this.parent = parent;
	}
	
	/**
	 * This method overrides the method toString. It creates the Json code of 
	 * the category and returns it.
	 * 
	 * @return the Json code of the category
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{"
				+ "\"id\":\"" + id + "\","
				+ "\"name\":\"" + name + "\","
				+ "\"pluralName\":\"" + pluralName + "\","
				+ "\"shortName\":\"" + shortName + "\",");
		
		if (icon != null && icon.size() == 2) {
			builder.append("\"icon\":{"
					+ "\"prefix\":\"" + icon.toArray()[0]+ "\","
					+ "\"suffix\":\"" + icon.toArray()[1]+ "\"},");
		}
		
		
		builder.append("\"categories\":[");
		
		boolean isFirstIteration = true;
		for (Category category : categories) {
			if (isFirstIteration) {
				// No comma.
				isFirstIteration = false;
			} else {
				builder.append(",");
			}
			builder.append(category.toString());
		}

		builder.append("]}");
		return builder.toString();
	}
	
	/**
	 * This method overrides the method equals. This category is equal to the
	 * object given in parameter if and only if this object is a category and
	 * the ids of both these categories are the same.
	 * 
	 * @return a boolean that indicates whether this category is equal to the 
	 * parameter object
	 */
	@Override
	public boolean equals(Object object) {
		if (object instanceof Category) {
			Category category = (Category) object;
			return category.getId().equals(this.getId());
		}
		return false;
	}
	
	/**
	 * This method computes the path from this category to the root of the
	 * category tree. This path is represented by the collection of categories
	 * that are located between this category and the root (both included).
	 * 
	 * @return the collection of categories that are located between this
	 * category and the root
	 */
	public Collection<Category> pathToRoot() {
		Collection<Category> pathToRoot = new ArrayList<Category>();
		
		Category category = this;
		// This category is added to the path to root.
		pathToRoot.add(category);
		
		// Iteration on the parents of this category.
		while (category.getParent() != null) {
			category = category.getParent();
			pathToRoot.add(category);
		}
		
		return pathToRoot;
	}
	
	/**
	 * This static method computes the distance in the category tree between
	 * the two categories given as parameters. It is based on the computation
	 * of the lowest common ancestor of these two categories and gives the
	 * number of edges separating these two categories in the tree.
	 * 
	 * @param category1 a category
	 * @param category2 another category
	 * 
	 * @return the distance in the tree between the two categories
	 */
	public static int distance(Category category1, Category category2) {
		// The path to the root is computed for category1 and category2.
		Collection<Category> pathToRoot1 = category1.pathToRoot();	
		Collection<Category> pathToRoot2 = category2.pathToRoot();
		
		int distanceToCommonAncestor1 = 0;
		int distanceToCommonAncestor2 = 0;
		
		boolean commonAncestorFound = false;
		// Iteration on the categories located on the path between category1
		// and root.
		Iterator<Category> pathToRootIterator1 = pathToRoot1.iterator();
	    while (!commonAncestorFound && pathToRootIterator1.hasNext()){
	    	Category pathToRootCategory1 = pathToRootIterator1.next();
	    	distanceToCommonAncestor2 = 0;
	    	
	    	// Iteration on the categories located on the path between
	    	// category2 and root.
	    	Iterator<Category> pathToRootIterator2 = pathToRoot2.iterator();
	    	while (!commonAncestorFound && pathToRootIterator2.hasNext()) {
	    		Category pathToRootCategory2 = pathToRootIterator2.next();
	    		
	    		if (pathToRootCategory1.equals(pathToRootCategory2)) {
	    			// The pathes to root for category1 and category2 are
	    			// intersecting: the lowest common ancestor is found.
	    			commonAncestorFound = true;
	    		} else {
	    			// The lowest common ancestor has not been found yet: the
	    			// distance is incremented and the iteration on the path
	    			// between category2 and root continues.
	    			distanceToCommonAncestor2++;
	    		}
	    	}
	    	
	    	if (!commonAncestorFound) {
	    		// The lowest common ancestor has not been found yet: the
    			// distance is incremented and the iteration on the path
    			// between category1 and root continues.
	    		distanceToCommonAncestor1++;
	    	}
	    }
		
	    // The distance between category1 and category2 is equal to the
	    // distance between category1 and their lowest common ancestor added to
	    // the distance between category2 and their common ancestor.
	    return distanceToCommonAncestor1 + distanceToCommonAncestor2;
	}

}
