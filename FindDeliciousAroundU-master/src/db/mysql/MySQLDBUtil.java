package db.mysql;

public class MySQLDBUtil {
	
	private static final String HOSTNAME = "localhost";
//	private static final String PORT_NUM = "8889"; // change it to your local mysql port number
	private static final String PORT_NUM = "3306"; // Amazon EC2 port
//	public static final String DB_NAME = "laiproject";
	public static final String DB_NAME = "yelp";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "root";
	public static final String URL = "jdbc:mysql://"
			+ HOSTNAME + ":" + PORT_NUM + "/" + DB_NAME
			+ "?user=" + USERNAME + "&password=" + PASSWORD
			+ "&autoReconnect=true&serverTimezone=UTC";
}
