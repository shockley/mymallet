package edu.nudt.influx.Utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtils {

	public Connection connect(String driver, String url, String user,
			String pswd, boolean autocommit) {
		Connection db_connection = null;
		try {
			Class.forName(driver).newInstance();
			db_connection = DriverManager.getConnection(url, user, pswd);
			db_connection.setAutoCommit(autocommit);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return db_connection;
	}

	public void close(Connection dbconn) {
		if (dbconn != null) {
			try {
				dbconn.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				dbconn = null;
			}
		}
	}

	public ResultSet executeQuery(Connection db_connection, String sql) {
		ResultSet result_set = null;
		try {
			Statement db_statement = db_connection.createStatement();
			result_set = db_statement.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result_set;
	}

	public void executeUpdate(Connection db_connection, String sql) {
		try {
			Statement db_statement = db_connection.createStatement();
			db_statement.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
