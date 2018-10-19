package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;
import entity.RestaurantItem;
import external.TicketMasterAPI;
import external.YelpAPI;

public class MySQLConnectionYelp implements DBConnection {
	// 与数据库的链接
		// 创建这个connection后，之后所有与数据库的链接都经过这个connection完成
		private Connection conn;
		
		private PreparedStatement saveItemStmt = null;
		
		// singleton for prepared statement
		private PreparedStatement getSaveItemstmt() {
			try {
				if (saveItemStmt == null) {
					if (conn == null) {
						System.err.println("DB connection failed!");
						return null;
					}
					String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
					saveItemStmt = conn.prepareStatement(sql);

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return saveItemStmt;
		}
		
		public MySQLConnectionYelp() {
			try {
				// reflection:
				// 以运行中的值作为输入来创建一个类
//				Class.forName("com.mysql.jdbc.Driver").getConstructor().newInstance();
				Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
				// 获得操作数据库的connection
				conn = DriverManager.getConnection(MySQLDBUtil.URL);
				// 需要判断 conn 是否有效
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void close() { // 关闭connection
			// TODO Auto-generated method stub
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void setFavoriteItems(String userId, List<String> itemIds) {
			if (conn == null) {
				System.err.println("DB connection failed!");
				return;
			}
			
			try {
				String sql = "INSERT IGNORE INTO history (user_id, item_id) VALUES (?, ?)";
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setString(1, userId);
				for (String itemId : itemIds) {
				stmt.setString(2, itemId);
				stmt.executeUpdate();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void unsetFavoriteItems(String userId, List<String> itemIds) {
			if (conn == null) {
				System.err.println("DB connection failed!");
				return;
			}
			
			try {
				String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setString(1, userId);
				for (String itemId : itemIds) {
				stmt.setString(2, itemId);
				stmt.executeUpdate();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public Set<String> getFavoriteItemIds(String userId) {
			if (conn == null) {
				return new HashSet<>();
			}
			
			Set<String> favoriteItemIds = new HashSet<>();
			
			try {
				String sql = "SELECT item_id FROM history WHERE user_id = ?";
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setString(1, userId);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					String itemId = rs.getString("item_id");
					favoriteItemIds.add(itemId);
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return favoriteItemIds;
		}

		@Override
		// from history table get item_ids based on userId
		// deal with items table
		public Set<Item> getFavoriteItems(String userId) {
			if (conn == null) {
				System.err.println("DB connection failed");
				return new HashSet<>();
			}
			
			Set<Item> favoriteItems = new HashSet<>();
			Set<String> itemIds = getFavoriteItemIds(userId);
			
			try {
				String sql = "SELECT * FROM items WHERE item_id = ?";
				PreparedStatement stmt = conn.prepareStatement(sql);
				
				for (String itemId : itemIds) {
					stmt.setString(1, itemId);
					ResultSet rs = stmt.executeQuery();
					
					Item.ItemBuilder builder = new Item.ItemBuilder();
					
					// iterator all the result set
					while (rs.next()) { 
						
						builder.setItemId(rs.getString("item_id"));
						builder.setName(rs.getString("name"));
						builder.setAddress(rs.getString("address"));
						builder.setImageUrl(rs.getString("image_url"));
						builder.setUrl(rs.getString("url"));
						builder.setCategories(getCategories(itemId));
						builder.setRating(rs.getDouble("rating"));
						builder.setDistance(rs.getDouble("distance"));
						
						favoriteItems.add(builder.build());
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return favoriteItems;
		}
		
		
		@Override
		public Set<RestaurantItem> getFavoriteRestaurantItems(String userId) {
			if (conn == null) {
				System.err.println("DB connection failed");
				return new HashSet<>();
			}
			
			Set<RestaurantItem> favoriteItems = new HashSet<>();
			Set<String> itemIds = getFavoriteItemIds(userId);
			
			try {
				String sql = "SELECT * FROM items WHERE item_id = ?";
				PreparedStatement stmt = conn.prepareStatement(sql);
				
				for (String itemId : itemIds) {
					stmt.setString(1, itemId);
					ResultSet rs = stmt.executeQuery();
					
					RestaurantItem.RestaurantItemBuilder builder = new RestaurantItem.RestaurantItemBuilder();
					
					// iterator all the result set
					while (rs.next()) { 
						
						builder.setItemId(rs.getString("item_id"));
						builder.setName(rs.getString("name"));
						builder.setLocation(rs.getString("location"));
						builder.setImageUrl(rs.getString("image_url"));
						builder.setUrl(rs.getString("url"));
						builder.setCategories(getCategories(itemId));
						builder.setRating(rs.getDouble("rating"));
						builder.setDistance(rs.getDouble("distance"));
						
						favoriteItems.add(builder.build());
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return favoriteItems;
		}

		@Override
		public Set<String> getCategories(String itemId) {
			if (conn == null) {
				System.err.println("DB connection failed");
				return new HashSet<>();
			}
			
			Set<String> categories = new HashSet<>();
			
			try {
				String sql = "SELECT category FROM categories WHERE item_id = ?";
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setString(1, itemId);
				ResultSet rs = stmt.executeQuery();
				
				while (rs.next()) {
					categories.add(rs.getString("category"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return categories;
		}

		@Override
		public List<Item> searchItems(double lat, double lon, String term) {
			TicketMasterAPI tmAPI = new TicketMasterAPI();
			List<Item> items = tmAPI.search(lat, lon, term);
			
			for (Item item : items) {
				saveItem(item);
			}
			return items;
		}
		@Override
		public List<RestaurantItem> searchRestaurant(String loc, Double lat, Double lon, String cat) {
			YelpAPI yelpAPI = new YelpAPI();
			List<RestaurantItem> items = yelpAPI.search(loc, lat, lon, cat);
			
			for (RestaurantItem item : items) {
				saveRestaurantItem(item);
			}
			return items;
		}

		@Override
		public void saveItem(Item item) { // deal with db
			// 在使用connection的时候都要判断其是否有效
			if (conn == null) {
				System.err.println("DB connection failed!");
				return;
			}
			
			try {
				// SQL Injection
				// 复用的时候效率高
				String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
				PreparedStatement stmt = conn.prepareStatement(sql);
				// saveItemStmt = getSaveItemstmt();
				// 自行检查输入的string从而解决SQL Injection
				stmt.setString(1, item.getItemId());
				stmt.setString(2, item.getName());
				stmt.setDouble(3, item.getRating());
				stmt.setString(4, item.getAddress());
				stmt.setString(5, item.getImageUrl());
				stmt.setString(6, item.getUrl());
				stmt.setDouble(7, item.getDistance());
				stmt.executeUpdate();
				
				sql = "INSERT IGNORE INTO categories VALUES (?, ?)";
				stmt = conn.prepareStatement(sql);
				stmt.setString(1, item.getItemId());
				for (String category : item.getCategories()) {
					stmt.setString(2, category);
					stmt.executeUpdate();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		public void saveRestaurantItem(RestaurantItem item) {
			// 在使用connection的时候都要判断其是否有效
			if (conn == null) {
				System.err.println("DB connection failed!");
				return;
			}
			
			try {
				// SQL Injection
				// 复用的时候效率高
				String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
				PreparedStatement stmt = conn.prepareStatement(sql);
				// saveItemStmt = getSaveItemstmt();
				// 自行检查输入的string从而解决SQL Injection
				stmt.setString(1, item.getItemId());
				stmt.setString(2, item.getName());
				stmt.setDouble(3, item.getRating());
				stmt.setString(4, item.getLocation());
				stmt.setString(5, item.getImageUrl());
				stmt.setString(6, item.getUrl());
				stmt.setDouble(7, item.getDistance());
				stmt.executeUpdate();
				
				sql = "INSERT IGNORE INTO categories VALUES (?, ?)";
				stmt = conn.prepareStatement(sql);
				stmt.setString(1, item.getItemId());
				for (String category : item.getCategories()) {
					stmt.setString(2, category);
					stmt.executeUpdate();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public String getFullname(String userId) {
			if (conn == null) {
				System.err.println("DB connection failed!");
				return "";
			}
			
			StringBuilder fullName = new StringBuilder();
			
			try {
				String sql = "SELECT * FROM users WHERE user_id = ?";
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setString(1, userId);
				ResultSet rs = stmt.executeQuery();
				
				while (rs.next()) {
					fullName.append(rs.getString("first_name"));
					fullName.append(' ');
					fullName.append(rs.getString("last_name"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return fullName.toString();
		}

		@Override
		public boolean verifyLogin(String userId, String password) {
			if (conn == null) {
				System.err.println("DB connection failed!");
				return false;
			}

			try {
				String sql = "SELECT user_id FROM users WHERE user_id = ? and password = ?";
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setString(1, userId);
				stmt.setString(2, password);
				ResultSet rs = stmt.executeQuery();
				
				if (rs.next()) {
					return true;
				}
				
			} catch (Exception e) {
				
				e.printStackTrace();
				
			}
			
			return false;
			
		}

		@Override
		public boolean registerClient(String userId, String password, String firstName, String lastName) {
			if (conn == null) {
				System.err.println("DB connection failed!");
				return false;
			}
			
			try {
				String sql = "SELECT * FROM users WHERE user_id = ?";
				
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setString(1, userId);

				ResultSet rs = stmt.executeQuery();
				
				// de-duplicate registered names
				if (rs.next()) {
					return false;
				}
				
				sql = "INSERT INTO users VALUES (?, ?, ?, ?)";
				stmt = conn.prepareStatement(sql);
				stmt.setString(1, userId);
				stmt.setString(2, password);
				stmt.setString(3, firstName);
				stmt.setString(4, lastName);
				stmt.executeUpdate();
			} catch (Exception e) {
				
				e.printStackTrace();
				
			}
			
			return true;
		}
		
//		public static void main(String[] args) {
//			DBConnection connection = DBConnectionFactory.getConnection();
//			boolean res = connection.registerClient("222", "1234", "YH", "Long");
//		}
}
