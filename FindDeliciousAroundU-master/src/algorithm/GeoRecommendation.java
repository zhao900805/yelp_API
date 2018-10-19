package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

public class GeoRecommendation {
	
	public List<Item> recommendItems(String userId, double lat, double lon) {
		List<Item> recommendedItems = new ArrayList<>();
		DBConnection conn = DBConnectionFactory.getConnection();
		
		try {
			/**
			 * 1. Get all favorite item ids (since the category table need item ids)
			 * 2. Get all categories of favorite items, sort by count
			 * 3. Do search based on category, filter out favorite events, sort by distance
			 */
			// Step 1:
			Set<String> favoriteItemIds = conn.getFavoriteItemIds(userId);
			
			// Step 2:
			Map<String, Integer> allCategories = new HashMap<>();
			for (String itemId : favoriteItemIds) {
				Set<String> categories = conn.getCategories(itemId);
				for (String category : categories) {
/*					int count = 0;
					if (allCategories.containsKey(category)) {
						count = allCategories.get(category);
					}
					allCategories.put(category, count + 1);*/
					
					allCategories.put(category, allCategories.getOrDefault(category, 0) + 1);
				}
			}
			// sort by count
			List<Entry<String, Integer>> categoryList = new ArrayList<>(allCategories.entrySet());
			Collections.sort(categoryList, new Comparator<Entry<String, Integer>> () {

				@Override
				public int compare(Entry<String, Integer> one, Entry<String, Integer> two) {
//					if (one.getValue().equals(two.getValue())) {
//						return 0;
//					}
//					return one.getValue() < two.getValue() ? 1 : -1;
					return Integer.compare(two.getValue(), one.getValue());
				}
				
			});
			
			// Step 3
			Set<Item> visitedItems = new HashSet<>(); // search ¹ýµÄitems£¬ it should be searched items
			
			for (Entry<String, Integer> category : allCategories.entrySet()) {
				List<Item> items = conn.searchItems(lat, lon, category.getKey());
				List<Item> filteredItems = new ArrayList<>();
				
				for (Item item : items) {
					if(!favoriteItemIds.contains(item.getItemId()) 
							&& !visitedItems.contains(item)) {
						filteredItems.add(item);
					}
				}
				
				// sort by distance
				Collections.sort(filteredItems, new Comparator<Item>() {

					@Override
					public int compare(Item o1, Item o2) {
						// TODO Auto-generated method stub
						return Double.compare(o1.getDistance(), o2.getDistance());
					}
					
				});
				
				visitedItems.addAll(items);
				
				recommendedItems.addAll(filteredItems);
			}
		} finally {
			conn.close();
		}
		return recommendedItems;
	}
}
