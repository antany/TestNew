package ca.antany.common.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection {
	
	Logger logger = Logger.getLogger(DBConnection.class.getName());

	private Connection dbConnection = null;
	private String connectionClass;
	private String connectionURL;

	public DBConnection(String connectionClass, ConnectionURL connectionURL) {
		this.connectionClass = connectionClass;
		this.connectionURL = connectionURL.toString();
	}

	public DBConnection(String connectionClass, String connectionURL) {
		this.connectionClass = connectionClass;
		this.connectionURL = connectionURL;
	}

	public Connection getConnection() {
		try {
			Class.forName(connectionClass);
			dbConnection = DriverManager.getConnection(connectionURL);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error when creating database connection", e);
		}
		return dbConnection;
	}

}
