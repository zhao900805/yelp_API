package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import entity.RestaurantItem;
import external.TicketMasterAPI;
import external.YelpAPI;

// This is a Singleton pattern
public class MySQLConnection implements DBConnection {
	// �����ݿ������
	// �������connection��֮�����������ݿ�����Ӷ��������connection���
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
	
	public MySQLConnection() {
		try {
			// reflection:
			// �������е�ֵ��Ϊ����������һ����
			//Class.forName("com.mysql.jdb.Driver").getConstructor().newInstance();
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			// ��ò������ݿ��connection
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
			// ��Ҫ�ж� conn �Ƿ���Ч
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void close() { // �ر�connection
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
/*		if (conn == null) {
			System.err.println("DB connection fail!");
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
		
		return favoriteItemIds;*/
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
				while (rs.next()) { // primary key: exist and unique
/*					builder.setName(rs.getString("item_id"));
					builder.setItemId(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setCategories(getCategories(itemId));
					builder.setRating(rs.getDouble("rating"));
					builder.setDistance(rs.getDouble("distance"));
					
					favoriteItems.add(builder.build());*/
					
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
	
	public List<RestaurantItem> searchRestaurant(String loc, Double lat, Double lon, String cat) {
		YelpAPI yelpAPI = new YelpAPI();
		List<RestaurantItem> items = yelpAPI.search(loc, lat, lon, cat);
		
		for (RestaurantItem item : items) {
//			saveItem(item);
		}
		return items;
	}

	@Override
	public void saveItem(Item item) { // deal with db
		// ��ʹ��connection��ʱ��Ҫ�ж����Ƿ���Ч
		if (conn == null) {
			System.err.println("DB connection failed!");
			return;
		}
		
		try {
			// SQL Injection
/*			
			String sql = String.format("INSERT IGNORE INTO items (%s, %s, %s, %s, %s, %s, %s)", 
					item.getItemId(),
					item.getName(),
					item.getRating(),
					item.getAddress(),
					item.getImageUrl(),
					item.getUrl(),
					item.getDistance());*/
			// ? is prepare statement
			// ���õ�ʱ��Ч�ʸ�
			String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			// saveItemStmt = getSaveItemstmt();
			// ���м�������string�Ӷ����SQL Injection
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
	public Set<RestaurantItem> getFavoriteRestaurantItems(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean registerClient(String userId, String password, String firstName, String lastName) {
		// TODO Auto-generated method stub
		return false;
	}


}
