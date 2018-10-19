package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MySQLTableCreation { // 创建SQL的所有table， 通过jdbc 操作数据库
	/**
	 * 1. connect to MySQL
	 */
	// Run this as Java Application to reset db schema
	public static void main(String[] args) { // 创建数据库和设置初始值
		try {
			// 1. connect to MySQL
			System.out.println("Connection to " + MySQLDBUtil.URL);
			// reflection:
			// 以运行中的值作为输入来创建一个类
			//Class.forName("com.mysql.jdb.Driver").getConstructor().newInstance();
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			// 获得操作数据库的connection
			Connection conn = DriverManager.getConnection(MySQLDBUtil.URL);
			
			if (conn == null) {
				return;
			}
			
			// 2. drop existed tables
			// Syntax for DROP:
			// DROP TABLE IF EXISTS table_name
//			Statement stmt = conn.createStatement(); // 用来指向SQL语句
//			String sql = "DROP TABLE IF EXISTS categories";
//			stmt.executeUpdate(sql);
//			
//			sql = "DROP TABLE IF EXISTS items";
//			stmt.executeUpdate(sql);
//			
//			sql = "DROP TABLE IF EXISTS histories";
//			stmt.executeUpdate(sql);
//			
//			sql = "DROP TABLE IF EXISTS users";
//			stmt.executeUpdate(sql);
			
			Statement stmt = conn.createStatement();
			String sql = "DROP TABLE IF EXISTS categories";
			stmt.executeUpdate(sql);
			
			sql = "DROP TABLE IF EXISTS history";
			stmt.executeUpdate(sql);
			
			sql = "DROP TABLE IF EXISTS items";
			stmt.executeUpdate(sql);
			
			sql = "DROP TABLE IF EXISTS users";
			stmt.executeUpdate(sql);
			// 3. create 4 tables: Uset, Item, Category, History
			// Syntax for CREATE
			// CREATE TABLE table_name {
			// 			column1 datatype,
			//          column2 datatype,
			//          column3 datatype,
			// 			...
			// };
//			sql = "CREATE TABLE items ("
//					+ "item_id VARCHAR(255) NOT NULL,"
//					+ "name VARCHAR(255),"
//					+ "rating FLOAT,"
//					+ "address VARCHAR(255),"
//					+ "image_url VARCHAR(255),"
//					+ "url VARCHAR(255),"
//					+ "distance FLOAT,"
//					+ "PRIMARY KEY (item_id)"
//					+ ")";
			sql = "CREATE TABLE items ("
					+ "item_id VARCHAR(255) NOT NULL,"
					+ "name VARCHAR(255),"
					+ "rating FLOAT,"
					+ "location VARCHAR(255),"
					+ "image_url VARCHAR(255),"
					+ "url VARCHAR(255),"
					+ "distance FLOAT,"
					+ "PRIMARY KEY (item_id)"
					+ ")";
			System.out.println(sql);
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE categories ("
					+ "item_id VARCHAR(255) NOT NULL,"
					+ "category VARCHAR(255) NOT NULL,"
					+ "PRIMARY KEY (item_id, category),"
					+ "FOREIGN KEY (item_id) REFERENCES items(item_id)"
					+ ")";
			System.out.println(sql);
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE users ("
					+ "user_id VARCHAR(255) NOT NULL,"
					+ "password VARCHAR(255) NOT NULL,"
					+ "first_name VARCHAR(255),"
					+ "last_name VARCHAR(255),"
					+ "PRIMARY KEY (user_id)"
					+ ")";
			System.out.println(sql);
			stmt.executeUpdate(sql);
			
//			sql = "CREATE TABLE history ("
//					+ "user_id VARCHAR(255) NOT NULL,"
//					+ "item_id VARCHAR(255) NOT NULL,"
//					+ "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
//					+ "PRIMARY KEY (user_id, item_id),"
//					+ "FOREIGN KEY (user_id) REFERENCES users(user_id),"
//					+ "FOREIGN KEY (item_id) REFEREBCES items(item_id)"
//					+ ")";
			sql = "CREATE TABLE history ("
					+ "user_id VARCHAR(255) NOT NULL,"
					+ "item_id VARCHAR(255) NOT NULL,"
					+ "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
					+ "PRIMARY KEY (user_id, item_id),"
					+ "FOREIGN KEY (item_id) REFERENCES items(item_id),"
					+ "FOREIGN KEY (user_id) REFERENCES users(user_id)"
					+ ")";
			System.out.println(sql);
			stmt.executeUpdate(sql);
			
			
			// 4. insert data into the created tables
			// Syntax for INSERT
			// INSERT INTO table_name (col1, col2, col3,...)
			// VALUES(val1, val2, val2)
			sql = "INSERT INTO users VALUES ("
					+ "'1111', '3229c1097c00d497a0fd282d586be050', 'John', 'Smith')";
			System.out.println(sql);
//			sql = "INSERT INTO users VALUES ("
//					+ "'1111', '3229c1097c00d497a0fd282d586be050', 'John', 'Smith')";
			
			System.out.println("Executing query: " + sql);
			stmt.executeUpdate(sql);
			
			sql = "INSERT INTO users VALUES ("
					+ "'fatfat', 'alwaysfatcat', 'Yugan', 'Ball')";
			System.out.println("Executing query: " + sql);
			stmt.executeUpdate(sql);
			
			System.out.println("Import is done successfully");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
