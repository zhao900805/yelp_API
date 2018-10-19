package entity;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class RestaurantItem {

	private String itemId;
	private String name;
	private double rating;
	private Set<String> categories;
	private String location;
	private String imageUrl;
	private String url;
	private double distance;
	private String displayPhone;
	
	public String getItemId() {
		return itemId;
	}
	public String getName() {
		return name;
	}
	public double getRating() {
		return rating;
	}
	public Set<String> getCategories() {
		return categories;
	}
	public String getLocation() {
		return location;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public String getUrl() {
		return url;
	}
	public double getDistance() {
		return distance;
	}
	public String getDisplayPhone() {
		return displayPhone;
	}
	
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		
		try {
			obj.put("item_id", itemId);
			obj.put("name", name);
			obj.put("raring", rating);
			obj.put("categories", new JSONArray(categories));
			obj.put("location", location);
			obj.put("image_url", imageUrl);
			obj.put("url", url);
			obj.put("distance", distance);
			obj.put("display_phone", displayPhone);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	private RestaurantItem(RestaurantItemBuilder builder) {
		this.itemId = builder.itemId;
		this.name = builder.name;
		this.rating = builder.rating;
		this.categories = builder.categories;
		this.location = builder.location;
		this.imageUrl = builder.imageUrl;
		this.url = builder.url;
		this.distance = builder.distance;
		this.displayPhone = builder.displayPhone;
	}
	
	public static class RestaurantItemBuilder {
		private String itemId;
		private String name;
		private double rating;
		private Set<String> categories;
		private String location;
		private String imageUrl;
		private String url;
		private double distance;
		private String displayPhone;
		
		
		
		public void setItemId(String itemId) {
			this.itemId = itemId;
		}



		public void setName(String name) {
			this.name = name;
		}



		public void setRating(double rating) {
			this.rating = rating;
		}



		public void setCategories(Set<String> categories) {
			this.categories = categories;
		}



		public void setLocation(String location) {
			this.location = location;
		}



		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}



		public void setUrl(String url) {
			this.url = url;
		}



		public void setDistance(double distance) {
			this.distance = distance;
		}



		public void setDisplayPhone(String displayPhone) {
			this.displayPhone = displayPhone;
		}



		public RestaurantItem build() {
			return new RestaurantItem(this);
		}
	}

	/**
	 * IMPORTANT! While we use set of items
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((itemId == null) ? 0 : itemId.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RestaurantItem other = (RestaurantItem) obj;
		if (itemId == null) {
			if (other.itemId != null)
				return false;
		} else if (!itemId.equals(other.itemId))
			return false;
		return true;
	}
	

	
}
