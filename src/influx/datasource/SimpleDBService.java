package influx.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;



public class SimpleDBService{
	private Connection con = null;

	public static boolean AUTOCOMMIT = true;
	public static String DRIVER = "com.mysql.jdbc.Driver";
	public static String URL = "jdbc:mysql://localhost:3306/influx";//"jdbc:mysql://localhost:3306/heritrix";
	public static String USR = "root";
	public static String PWD = "influx";
	
	/**
	 * Create a dbservice using user-specified settings
	 * @param driver
	 * @param url
	 * @param usr
	 * @param pwd
	 * @param autoCommit
	 */
	public SimpleDBService(String driver, String url, String usr, String pwd, boolean autoCommit){
		try {
			Class.forName(driver).newInstance();
			con = DriverManager.getConnection(url, usr, pwd);
			con.setAutoCommit(autoCommit);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Create a dbservice using default settings
	 */
	public SimpleDBService(){
		try {
			Class.forName(DRIVER).newInstance();
			con = DriverManager.getConnection(URL, USR, PWD);
			con.setAutoCommit(AUTOCOMMIT);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void close() {
		if (con != null) {
			try {
				con.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				con = null;
			}
		}
	}
	
	public static void main(String [] args){
		SimpleDBService d = new SimpleDBService();
		String sql = "insert into PROJECT ";
		sql += "(AUDIENCES, DB_ENVIRONMENT, DESCRIPTION, FEATURES, HOME, " +
				"LANGUAGES, LAST_UPDATE, LICENSE, NAME, OS, REGISTERED, " +
				"RELEASE_DATE, TOPICS, TRANSLATIONS, UI) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		//String sql2 = "insert into test (fff,ss) values (?,?)";
		
		try {
			PreparedStatement pstmt = d.con.prepareStatement(sql);
			pstmt.setString(1, "s");
			pstmt.setString(2, "s");
			pstmt.setString(2, "s");
			pstmt.setString(3, "s");
			pstmt.setString(4, "s");
			pstmt.setString(5, "s");
			pstmt.setString(6, "s");
			pstmt.setString(7, "s");
			pstmt.setString(8, "s");
			pstmt.setString(9, "s");
			pstmt.setString(10, "s");
			pstmt.setString(11, "s");
			pstmt.setString(12, "s");
			pstmt.setString(13, "s");
			pstmt.setString(14, "s");
			pstmt.setString(15, "s");
			pstmt.executeUpdate();
			d.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	public Connection getCon() {
		return con;
	}
}
