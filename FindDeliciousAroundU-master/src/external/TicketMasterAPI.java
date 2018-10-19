package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;

public class TicketMasterAPI {
	/**
	 * Set Hyper-Parameter
	 */
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = "";
	private static final String API_KEY = "AWGj0GQqj6Noy9GuJFjC3L545grSkEpk";
	private static final String RADIUS = "50";
	
	private static final String EMBEDDED = "_embedded";
	private static final String EVENTS = "events";
	private static final String NAME = "name";
	private static final String ID = "id";
	private static final String URL_STR = "url";
	private static final String RATING = "rating";
	private static final String DISTANCE = "distance";
	private static final String VENUES = "venues";
	private static final String ADDRESS = "address";
	private static final String LINE1 = "line1";
	private static final String LINE2 = "line2";
	private static final String LINE3 = "line3";
	private static final String CITY = "city";
	private static final String STATE = "state";
	private static final String COUNTRY = "country";
	private static final String IMAGES = "images";
	private static final String CLASSIFICATIONS = "classifications";
	private static final String SEGMENT = "segment";

	
	public List<Item> search(double lat, double lon, String keyword) {
		// Encode keyword in url since it may contain special characters
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		
		try {
			keyword = java.net.URLEncoder.encode(keyword, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// convert lat and lon into geo hash
		String geoHash = GeoHash.encodeGeohash(lat, lon, 8);
		
		// build query String
		// "apikey=12345&geoPoint=abcd&keyword=music&radius=50"
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s", API_KEY, geoHash, keyword, RADIUS);
		
		// 1.open a HTTP connection between your java application and TicketMaster based on url
		// 2.set request method to GET
		// 3.send request to TicketMaster and get response, response code 
		//   request body is saved in InputStream of the connection
		// 4.read response body to get event data
		try {
			// 1
			HttpURLConnection connection = (HttpURLConnection) new URL(URL + "?" + query).openConnection();
			// 2
			connection.setRequestMethod("GET");
			// 3
			int responseCode = connection.getResponseCode();
			
			// for check the response code
			System.out.println("\n Sending 'GET' request to URL: " + URL + "?" + query);
			System.out.println("Response Code: " + responseCode);
			
			// 4
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			// remember to close stream
			in.close();
			
			// convert response to JSONObject
			JSONObject obj = new JSONObject(response.toString());
			if (obj.isNull(EMBEDDED)) {
				return new ArrayList<>();
			}
			JSONObject embedded = obj.getJSONObject(EMBEDDED);
			JSONArray events = embedded.getJSONArray(EVENTS);
			
			return getItemList(events);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	/**
	 * get item list
	 * 1. events are from search method
	 * @throws JSONException 
	 */
	private List<Item> getItemList(JSONArray events) throws JSONException {
		List<Item> itemList = new ArrayList<>();
		
		for (int i = 0; i < events.length(); i++) {
			JSONObject obj = events.getJSONObject(i);
			
			Item.ItemBuilder builder = new Item.ItemBuilder();
			
			if (!obj.isNull(NAME)) {
				builder.setName(obj.getString(NAME));
			}
			
			if (!obj.isNull(ID)) {
				builder.setItemId(obj.getString(ID));
			}
			
			if (!obj.isNull(URL_STR)) {
				builder.setUrl(obj.getString(URL_STR));
			}
			
			if (!obj.isNull(RATING)) {
				builder.setRating(obj.getDouble(RATING));
			}
			
			if (!obj.isNull(DISTANCE)) {
				builder.setDistance(obj.getDouble(DISTANCE));
			}
			
			builder.setAddress(getAddress(obj));
			builder.setCategories(getCategories(obj));
			builder.setImageUrl(getImageUrl(obj));
			
			itemList.add(builder.build());
		}
		return itemList;
	}
	/**
	 * get address
	 * @throws JSONException 
	 */
	private String getAddress(JSONObject event) throws JSONException {
		if (!event.isNull(EMBEDDED)) {
			JSONObject embedded = event.getJSONObject(EMBEDDED);
			
			if (!embedded.isNull(VENUES)) {
				JSONArray venues = embedded.getJSONArray(VENUES);
				
				for (int i = 0; i < venues.length(); i++) {
					JSONObject venue = venues.getJSONObject(i);
					
					StringBuilder sb = new StringBuilder();
					
					if (!venue.isNull(ADDRESS)) {
						JSONObject address = venue.getJSONObject(ADDRESS);
						
						if (!address.isNull(LINE1)) {
							sb.append(address.getString(LINE1));
							sb.append("\n");
						}
						
						if (!address.isNull(LINE2)) {
							sb.append(address.getString(LINE2));
							sb.append("\n");
						}
						
						if (!address.isNull(LINE3)) {
							sb.append(address.getString(LINE3));
							sb.append("\n");
						}
					}
					
					if (!venue.isNull(CITY)) {
						JSONObject city = venue.getJSONObject(CITY);
						
						if (!city.isNull(NAME)) {
							sb.append(city.getString(NAME));
							sb.append("\n");
						}
					}
					
					if (!venue.isNull(STATE)) {
						JSONObject state = venue.getJSONObject(STATE);
						
						if (!state.isNull(NAME)) {
							sb.append(state.getString(NAME));
							sb.append("\n");
						}
					}
					
					if (!venue.isNull(COUNTRY)) {
						JSONObject country = venue.getJSONObject(COUNTRY);
						
						if (!country.isNull(NAME)) {
							sb.append(country.getString(NAME));
						}
					}
					
					String addr = sb.toString();
					if (!addr.equals("")) {
						return addr;
					}
				}
			}
		}
		return "";
	}
	/**
	 * get image url
	 * @throws JSONException 
	 */
	private String getImageUrl(JSONObject event) throws JSONException {
		if (!event.isNull(IMAGES)) {
			JSONArray images = event.getJSONArray(IMAGES);
			
			for (int i = 0; i < images.length(); i++) {
				JSONObject image = images.getJSONObject(i);
				
				if (!image.isNull(URL_STR)) {
					return image.getString(URL_STR);
				}
			}
		}
		return "";
	}
	/**
	 * get categories
	 * @throws JSONException 
	 */
	private Set<String> getCategories(JSONObject event) throws JSONException {
		Set<String> categories = new HashSet<>();
		if (!event.isNull(CLASSIFICATIONS)) {
			JSONArray classifications = event.getJSONArray(CLASSIFICATIONS);
			
			for (int i = 0; i < classifications.length(); i++) {
				JSONObject classification = classifications.getJSONObject(i);
				
				if (!classification.isNull(SEGMENT)) {
					JSONObject segment = classification.getJSONObject(SEGMENT);
					
					if (!segment.isNull(NAME)) {
						categories.add(segment.getString(NAME));
					}
				}
			}
		}
		return categories;
	}
	/**
	 * Helper Method
	 * @param lat
	 * @param lon
	 */
	private void queryAPI(double lat, double lon) {
		List<Item> events = search(lat, lon, null);
		try {
			for (Item item : events) {
				JSONObject event = item.toJSONObject();
				System.out.println(event);
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * test result
	 */
	public static void main(String[] args) {
		TicketMasterAPI tmApi = new TicketMasterAPI();
		// Mountain View, CA
		tmApi.queryAPI(37.38, -122.08);
		// London, UK
		// tmApi.queryAPI(51.503364, -0.12);
		// Houston, TX
		//tmApi.queryAPI(29.682684, -95.295410);
	}
}
