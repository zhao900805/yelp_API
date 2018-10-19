package db;

import db.mysql.MySQLConnection;
import db.mysql.MySQLConnectionYelp;

public class DBConnectionFactory { // ����DBConnection instance
	// This should change based on the pipeline
	private static final String DEFAULT_DB = "mysql";
	
	public static DBConnection getConnection(String db) {
		switch (db) {
		case "mysql":
			return new MySQLConnectionYelp();
			//return null;
		case "mangodb":
			// return new MongoDBConnection();
			return null;
		default:
			throw new IllegalArgumentException("Invalid db: " + db);
		}
	}
	
	public static DBConnection getConnection() { // Ĭ�ϰ汾����Ҫoverload
		return getConnection(DEFAULT_DB);
	}
}
