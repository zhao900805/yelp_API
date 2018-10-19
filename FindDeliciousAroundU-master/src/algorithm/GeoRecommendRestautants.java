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
import entity.RestaurantItem;

public class GeoRecommendRestautants {
	
	/**
	 * 1. Get all favorite restaurant-item ids (since the category table need item ids)
	 * 2. Get all categories of favorite items, sort by count
	 * 3. Do search based on category, filter out favorite events, sort by distance
	 */
	public List<RestaurantItem> recommendItems(String userId, String loc, Double lat, Double lon) {
		List<RestaurantItem> recommendItems = new ArrayList<>();
		DBConnection conn = DBConnectionFactory.getConnection();
		
		try {
			// step 1. get item ids
			Set<String> favoriteItemIds = conn.getFavoriteItemIds(userId);
			
			// step 2.
			// get categories, sort by counts 
			Map<String, Integer> sortedCategories = new HashMap<>();
			
			for (String itemId : favoriteItemIds) {
				Set<String> categories = conn.getCategories(itemId);
				for (String category : categories) {
					sortedCategories.put(category, sortedCategories.getOrDefault(category, 0) + 1);
				}
			}
			
			List<Map.Entry<String, Integer>> categoryList = new ArrayList<>(sortedCategories.entrySet());
			Collections.sort(categoryList, new Comparator<Map.Entry<String, Integer>>() {

				@Override
				public int compare(Entry<String, Integer> one, Entry<String, Integer> two) {
					return one.getValue().compareTo(two.getValue());
				}
				
			});
			
			// step 3
			Set<RestaurantItem> visitedItem = new HashSet<>();
			
			for (Map.Entry<String, Integer> entry : categoryList) {
				List<RestaurantItem> items = conn.searchRestaurant(null, lat, lon, entry.getKey());
				
				List<RestaurantItem> filteredItems = new ArrayList<>();
				
				for (RestaurantItem item : items) {
					if (!favoriteItemIds.contains(item.getItemId())
							&& !visitedItem.contains(item)) {
						filteredItems.add(item);
					}
				}
				
				// sort filtered items
				Collections.sort(filteredItems, new Comparator<RestaurantItem>() {

					@Override
					public int compare(RestaurantItem arg0, RestaurantItem arg1) {
						return Double.compare(arg0.getDistance(), arg1.getDistance());
					}
					
				});
				
				visitedItem.addAll(items);
				
				recommendItems.addAll(filteredItems);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		conn.close();
		return recommendItems;
	}

	
	
}
