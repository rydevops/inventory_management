/**
 * Copyright 2018 Russell Yorke
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ryorke.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.ryorke.entity.User;
import com.ryorke.entity.exception.InvalidUserAttributeException;

/**
 * User database manager providing functionality to interact 
 * between the user table(s) and objects. This is a singleton class 
 * that is initialized upon first use. 
 * 
 * @author Russell Yorke
 */
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
	 * Retrieves a list of users from the database
	 * 
	 * @return A list of users within the database or null if no users found
	 * @throws SQLException If an error occurs while processing the database request
	 * @throws InvalidUserAttributeException Thrown if database information has been modified with invalid data
	 */
	public ArrayList<User> getUsers() throws SQLException, InvalidUserAttributeException {
		ArrayList<User> userList = null;
		final String query = "SELECT * FROM user";
		
		try (Connection connection = databaseManager.getConnection();
				Statement statement = connection.createStatement();
				ResultSet results = statement.executeQuery(query)) {
			while (results.next()) {
				int userId = results.getInt("userId");
				String username = results.getString("username");
				String password = results.getString("password");
				String firstName = results.getString("firstName");
				String lastName = results.getString("lastName");
				boolean isAdministrator = (results.getInt("administrator") == 1) ? true : false;
				
				User user = new User(userId, username, password, firstName, lastName, isAdministrator);
				
				if (userList == null) {
					userList = new ArrayList<User>();
				}
				
				userList.add(user);
			}
		}
		
		return userList; 
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
				+ "VALUES ('admin', 'admin', 'Administrative', 'User', 1)";
		
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
	 * @return A user object if authentication passed, null otherwise
	 * 
	 * @throws SQLException if a database error occurs while processing the request.
	 * @throws InvalidUserAttributeException If attributes returned from the database are incorrectly formatted for an authenticated User
	 */
	public User authenticateUser(String username, String password) throws SQLException, InvalidUserAttributeException {
		User authenticatedUser = null;
		String userQuery = "SELECT * FROM user WHERE username = ?";
		
		try (Connection dbConnection = databaseManager.getConnection();
				PreparedStatement sqlStatement = dbConnection.prepareStatement(userQuery)){
			sqlStatement.setString(1, username);
			ResultSet results = sqlStatement.executeQuery();
			
			if (results.next()) {
				if (results.getString("password").equals(password)) {
					int userId = results.getInt("password");					
					String firstName = results.getString("firstName");
					String lastName = results.getString("lastName");
					boolean isAdministrator = (results.getInt("administrator") == 1) ? true : false;
					
					authenticatedUser = new User(userId, username, password, firstName, lastName, isAdministrator);					
				}
			}
			results.close();			
		}
		
		return authenticatedUser; 
	}
	
	/**
	 * Inserts a new user into the database and updates the userId within the entity
	 * 
	 * Note: This application does not encrypt the passwords
	 * 
	 * @param user A new user to insert
	 * @throws SQLException If an SQL/Database error occurs
	 */
	public void createUser(User user) throws SQLException {
		String insertUserQuery = "INSERT INTO user (username, password, firstName, lastName, administrator) VALUES (?, ?, ?, ?, ?)";
		String userIdQuery = "SELECT last_insert_rowid() AS userId";
		
		try (Connection dbConnection = databaseManager.getConnection();
				PreparedStatement insertUserStatement = dbConnection.prepareStatement(insertUserQuery);
				Statement lastRowInsertStatement = dbConnection.createStatement()) {
			insertUserStatement.setString(1, user.getUsername());
			insertUserStatement.setString(2,  user.getPassword());
			insertUserStatement.setString(3,  user.getFirstName());
			insertUserStatement.setString(4,  user.getLastName());
			insertUserStatement.setInt(5, (user.isAdministrator()) ? 1 : 0);
			int rowsInserted = insertUserStatement.executeUpdate();
			assert(rowsInserted == 1): "Creation of user returns an invalid number of rows. This should always be 1.";
			
			ResultSet result = lastRowInsertStatement.executeQuery(userIdQuery);
			result.next();
			user.setUserId(result.getInt(1));				
		}
	}
	
	/**
	 * Updates an existing user based on the userId
	 * 
	 * @param user An existing user to update
	 * @throws SQLException If an SQL/Database error occurs
	 */
	public void updateUser(User user) throws SQLException {
		String updateUserQuery = "UPDATE user SET username = ?, password = ?, firstName = ?, lastName = ?, administrator = ? WHERE userId = ?";
		
		try (Connection dbConnection = databaseManager.getConnection();
				PreparedStatement updateUserStatement = dbConnection.prepareStatement(updateUserQuery)) {
			updateUserStatement.setString(1, user.getUsername());
			updateUserStatement.setString(2, user.getPassword());
			updateUserStatement.setString(3, user.getFirstName());
			updateUserStatement.setString(4, user.getLastName());
			updateUserStatement.setInt(5, (user.isAdministrator()) ? 1 : 0);
			updateUserStatement.setInt(6, user.getUserId());
			
			int rowsUpdated = updateUserStatement.executeUpdate();
			
			assert(rowsUpdated == 1): String.format("Only one user should ever be updated. %d users were updated instead.", rowsUpdated);			
		}
	}
	
	/**
	 * Deletes a user based on userID
	 * @param user An existing user to delete
	 * @throws SQLException If an SQL/Database error occurs
	 */
	public void deleteUser(User user) throws SQLException {
		String deleteUserQuery = "DELETE FROM user WHERE userId = ?";
		
		try (Connection dbConnection = databaseManager.getConnection();
				PreparedStatement deleteUserStatement = dbConnection.prepareStatement(deleteUserQuery)) {
			deleteUserStatement.setInt(1, user.getUserId());
			
			int rowsDeleted = deleteUserStatement.executeUpdate();
			
			assert(rowsDeleted == 1): String.format("More than one row (%d) was deleted.", rowsDeleted);			
		}
	}
}
