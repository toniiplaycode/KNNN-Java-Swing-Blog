package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static final String DB_URL = "jdbc:mysql://localhost:3306/BlogDB";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "123";
    
    public static Connection getConnection() throws SQLException {
        try {
            // Load MySQL driver (optional in modern versions of MySQL)
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Return the connection object
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Connection failed");
            e.printStackTrace();
            throw e;
        }
        return null;
    }
}