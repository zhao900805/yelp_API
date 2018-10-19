package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.RestaurantItem;
import entity.RestaurantItem.RestaurantItemBuilder;

public class YelpAPI {

	/**
	 * Set Hyper Parameters
	 */
	
	private static final String URL = "https://api.yelp.com/v3/businesses/search";
	private static final String ACCESS_TOKEN = "Authorization";
	private static final String TOKEN_VALUE = "Bearer 5EMbW8vdKaIqBQH0ShxcMWxdP04hnszSjvYQniL44tst14lRaIFjqPuKlDkewG4KVUgcNZ5E2D7dMiPL9YoFqiyQ2DWcCCXm6zPGiiASEFvVEMcUDNsN-4w9_0SQW3Yx";
	private static final String DEFAULT_LOCATION = "11551OhioAve,LosAngeles,CA";
	
	private static final String BUSINESSES = "businesses";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String RATING = "rating";
	private static final String CATEGORIES = "categories";
	private static final String TITLE = "title";
	private static final String LOCATION = "location";
	private static final String DISPLAY_ADDRESS = "display_address";
	private static final String IMAGE_URL = "image_url";
	private static final String ITEM_URL = "url";
	private static final String DISTANCE = "distance";
	private static final String PHONE = "displayPhone";
	
	public List<RestaurantItem> search(String loc, Double lat, Double lon, String categories) {
		String query = "";
		if (loc == null && lat == null && lon == null) {
			query = String.format("location=%s", DEFAULT_LOCATION);
		} else if (lat == null && lon == null) {
			query = String.format("location=%s", loc);
		} else {
			query = String.format("latitude=%s&longitude=%s", lat, lon);
		}
		
		if (categories != null) {
			query += String.format("&categories=%s", categories);
		}
		
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(URL + "?" + query).openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty(ACCESS_TOKEN, TOKEN_VALUE);
			
			// for debug
			int responseCode = connection.getResponseCode();
			System.out.println("\n Sending 'GET' request to URL: " + URL + "?" + query);
			System.out.println("Response Code: " + responseCode);
			
			// read response
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			// convert response to JSONObject
			JSONObject obj = new JSONObject(response.toString());
			if (obj.isNull(BUSINESSES)) {
				return new ArrayList<>();
			}
			JSONArray businesses = obj.getJSONArray(BUSINESSES);
			
			return getRestaurantItemList(businesses);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	private List<RestaurantItem> getRestaurantItemList(JSONArray businesses) throws JSONException {
		List<RestaurantItem> itemList = new ArrayList<>();
		
		for (int i = 0; i < businesses.length(); i++) {
			JSONObject business = businesses.getJSONObject(i);
			
			RestaurantItemBuilder builder = new RestaurantItemBuilder();
			
			if (!business.isNull(ID)) {
				builder.setItemId(business.getString(ID));
			}
			
			if (!business.isNull(NAME)) {
				builder.setName(business.getString(NAME));
			}
			
			if (!business.isNull(RATING)) {
				builder.setRating(business.getDouble(RATING));
			}
			
			if (!business.isNull(IMAGE_URL)) {
				builder.setImageUrl(business.getString(IMAGE_URL));
			}
			
			if (!business.isNull(ITEM_URL)) {
				builder.setUrl(business.getString(ITEM_URL));
			}
			
			if (!business.isNull(DISTANCE)) {
				builder.setDistance(business.getDouble(DISTANCE));
			}
			
			if (!business.isNull(PHONE)) {
				builder.setDisplayPhone(business.getString(PHONE));
			}
			
			if (!business.isNull(LOCATION)) {
				builder.setLocation(getDisplayAddress(business));
			}
			
			if (!business.isNull(CATEGORIES)) {
				builder.setCategories(getCategories(business));
			}
			
			itemList.add(builder.build());
		}
		return itemList;
	}

	private Set<String> getCategories(JSONObject business) throws JSONException {
		Set<String> set = new HashSet<>();
		JSONArray categories = business.getJSONArray(CATEGORIES);
		
		for (int i = 0; i < categories.length(); i++) {
			JSONObject category = categories.getJSONObject(i);
			if (!category.isNull(TITLE)) {
				set.add(category.getString(TITLE));
			}
		}
		return set;
	}

	private String getDisplayAddress(JSONObject business) throws JSONException {
		JSONObject locationDetails = business.getJSONObject(LOCATION);
		if (!locationDetails.isNull(DISPLAY_ADDRESS)) {
			JSONArray array =  locationDetails.getJSONArray(DISPLAY_ADDRESS);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < array.length(); i++) {
				String obj = array.getString(i);
				sb.append(obj + "\n");
			}
			return sb.toString();
		}
		return "";
	}

	public void check(String query) {
		try {
			//String query = String.format("location=%s", DEFAULT_LOCATION);
			HttpURLConnection connection = (HttpURLConnection) new URL(URL + "?" + query).openConnection();
			
			connection.setRequestMethod("GET");
			connection.setRequestProperty(ACCESS_TOKEN, TOKEN_VALUE);
			
			
			int responseCode = connection.getResponseCode();
			System.out.println("\n Sending 'GET' request to URL: " + URL + "?" + DEFAULT_LOCATION);
			System.out.println("Response Code: " + responseCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void queryAPI(String loc, Double lat, Double lon, String cat) {
		List<RestaurantItem> items = search(loc, lat, lon, cat);
		
		try {
			for (RestaurantItem item : items) {
				JSONObject obj = item.toJSONObject();
				System.out.println(obj);
			} 
		} catch (Exception e) {
			e.printStackTrace();
			
		}
	}
	
	public static void main(String[] args) {
		YelpAPI yelpApi= new YelpAPI();
		//yelpApi.check();
		yelpApi.queryAPI(null, 37.38, -122.08, "desserts");
		
	}
}
