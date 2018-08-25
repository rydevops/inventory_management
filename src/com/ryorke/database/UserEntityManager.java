package com.ryorke.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class UserEntityManager implements EntityManager {
	private static UserEntityManager entityManager = null; 
	private SQLiteDBManager databaseManager = null;
	/** 
	 * Provides access to the singleton user entity manager
	 * that is responsible for interacting with the database
	 * 
	 * @return A reference to a singleton user entity manager
	 * @throws IOException if unable to access database file
	 * @throws SQLException if an error occurs while accessing the database
	 */
	public static UserEntityManager getManager() throws IOException, SQLException {
		if (entityManager == null)
			entityManager = new UserEntityManager();
		
		return entityManager;
	}

	/**
	 * Creates a new instance of the UserEntityManager registering
	 * itself with the SQLiteDBManager
	 * 
	 * @throws IOException if unable to access database file
	 * @throws SQLException if an error occurs while accessing the database
	 */
	private UserEntityManager() throws IOException, SQLException {
		databaseManager = SQLiteDBManager.getManager();
		databaseManager.registerEntityManager(this);		
	}
	
	/**
	 * Idempotent method for creating a database table schema if it
	 * doesn't already exists. 
	 * 
	 * @throws SQLException If a database error occurs while processing the request. 
	 */
	@Override
	public void createTable() throws SQLException {
		final String createTableQuery = "CREATE TABLE IF NOT EXISTS user "
				+ "(userId INTEGER PRIMARY KEY, "
				+ "username TEXT NOT NULL UNIQUE, "
				+ "password TEXT NOT NULL, "
				+ "firstName TEXT NOT NULL, lastName TEXT NOT NULL, "
				+ "administrator INTEGER DEFAULT 0)";
		final String insertDefaultUserQuery = "INSERT INTO user (username, password, firstName, lastName, administrator) "
				+ "VALUES ('admin', 'admin', 'Administrator', 'Administrator', 1)";
		
		if (!databaseManager.tableExists("user")) {
			try (Connection dbConnection = databaseManager.getConnection();
					Statement sqlStatement = dbConnection.createStatement();) {
				sqlStatement.executeUpdate(createTableQuery);

				int result = sqlStatement.executeUpdate(insertDefaultUserQuery);
				
				// User insert should always yield 1 row updated
				assert (result == 1): "Inserting default administrative user failed";
			}
		}

	}

	/**
	 * Provides a list of SQL commands used to restore a database table(s)
	 * for this entity.
	 * 
	 * @return A list of SQL commands
	 * @throws SQLException if a database error occurs while processing the request.
	 */
	@Override
	public ArrayList<String> exportTable() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Performs user credential validation
	 * 
	 * @param username A username to validate
	 * @param password A password belonging to the username
	 * @return True if credentials are validate, false otherwise
	 * 
	 * @throws SQLException if a database error occurs while processing the request.
	 */
	public boolean authenticateUser(String username, String password) throws SQLException {
		boolean validCredentials = false;
		String userQuery = "SELECT password FROM user WHERE username = ?";
		
		try (Connection dbConnection = databaseManager.getConnection();
				PreparedStatement sqlStatement = dbConnection.prepareStatement(userQuery)){
			sqlStatement.setString(1, username);
			ResultSet results = sqlStatement.executeQuery();
			
			if (results.next()) {
				if (results.getString(1).equals(password)) {
					validCredentials = true;
				}
			}
			results.close();			
		}
		
		return validCredentials; 
	}
}
