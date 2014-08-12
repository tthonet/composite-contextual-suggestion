package org.terrier.compositecontextualsuggester.util;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a generic implementation of a bundle.
 * 
 * @author Thibaut Thonet
 *
 * @param <I> the type of the items contained in the bundle.
 * 
 */
public class Bundle<I> {
	
	private List<I> items;
	
	public Bundle() {
		items = new ArrayList<I>();
	}
	
	public Bundle(List<I> items) {
		this.items = items;
	}

	public int getItemNb() {
		return items.size();
	}

	public List<I> getItems() {
		return items;
	}

	public void setItems(List<I> items) {
		this.items = items;
	}

}
