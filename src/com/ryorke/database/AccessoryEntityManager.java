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
import java.text.ParseException;
import java.util.ArrayList;

import com.ryorke.entity.Accessory;

/**
 * Provides utility operations for pushing/getting Accessory objects
 * within a database
 * 
 * @author Russell Yorke
 */
public class AccessoryEntityManager implements EntityManager {
	private static AccessoryEntityManager entityManager = null;
	private static ItemEntityManager itemEntityManager = null;
	private SQLiteDBManager databaseManager = null;
	
	/** 
	 * Provides access to the singleton accessory entity manager
	 * that is responsible for interacting with the database
	 * 
	 * @return A reference to a singleton accessory entity manager
	 * @throws IOException if unable to access database file
	 * @throws SQLException if an error occurs while accessing the database
	 */
	public static AccessoryEntityManager getManager() throws IOException, SQLException {
		if (entityManager == null)
			entityManager = new AccessoryEntityManager();
		
		if (itemEntityManager == null)
			itemEntityManager = ItemEntityManager.getManager();
		
		return entityManager;
	}
	
	/**
	 * Creates a new instance of the AccessoryEntityManager registering
	 * itself with the SQLiteDBManager
	 * 
	 * @throws IOException if unable to access database file
	 * @throws SQLException if an error occurs while accessing the database
	 */
	private AccessoryEntityManager() throws IOException, SQLException {
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
		final String createTableQuery = "CREATE TABLE IF NOT EXISTS accessory "
				+ "(accessoryId INTEGER UNIQUE NOT NULL, "	// accessoryId = itemId or foreign key
				+ "color TEXT NOT NULL, "
				+ "consoleId INTEGER NOT NULL, "	// An id to an existing console
				+ "modelNumber TEXT NOT NULL,"
				+ "FOREIGN KEY(accessoryId) REFERENCES item(itemId) ON DELETE RESTRICT,"
				+ "FOREIGN KEY(consoleId) REFERENCES console(consoleId) ON DELETE RESTRICT)";
		
		if (!databaseManager.tableExists("accessory")) {
			try (Connection dbConnection = databaseManager.getConnection();
					Statement sqlStatement = dbConnection.createStatement();) {
				sqlStatement.executeUpdate(createTableQuery);
			}
		}
	}
	
	/**
	 * Retrieves a list of accessories from the database
	 * 
	 * @return A list of accessories
	 * @throws SQLException If a database error occurs
	 * @throws ParseException If a release date was incorrectly stored within the database. 
	 */
	public ArrayList<Accessory> getAccessories() throws SQLException, ParseException {
		final String getAllAccessoriesQuery = "SELECT * FROM accessory";
		ArrayList<Accessory> accessories = null;
		
		try (Connection dbConnection = databaseManager.getConnection();
				Statement statement = dbConnection.createStatement();
				ResultSet accessoriesResult = statement.executeQuery(getAllAccessoriesQuery)) {
			while (accessoriesResult.next()) {
				int accessoryId = accessoriesResult.getInt("accessoryId");
				String color = accessoriesResult.getString("color");
				int consoleId = accessoriesResult.getInt("consoleId");
				String modelNumber = accessoriesResult.getString("modelNumber");
				
				Accessory accessory = new Accessory();
				accessory.setItemNumber(accessoryId);
				accessory.setColor(color);
				accessory.setPlatformId(consoleId);
				accessory.setModelNumber(modelNumber);
				itemEntityManager.loadItem(accessory);
				
				if (accessories == null)
					accessories = new ArrayList<Accessory>();
				
				accessories.add(accessory);
			}
		}
		
		return accessories; 
	}

	/**
	 * Creates a new accessory record (and associated item) within database. 
	 * Once created, the accessoryId will be updated to reflect the accessoryId within the database
	 * 
	 * @param accessory A new accessory entity
	 * @throws SQLException If a database error occurs
	 */
	public void addAccessory(Accessory accessory) throws SQLException {
		final String insertAccessoryQuery = "INSERT INTO accessory (accessoryId, color, consoleId, "
				+ "modelNumber) VALUES (?, ?, ?, ?)";
		
		// Create the associated item record first to acquire a new itemId
		itemEntityManager.addItem(accessory);
		
		try (Connection dbConnection = databaseManager.getConnection();
				PreparedStatement insertStatement = dbConnection.prepareStatement(insertAccessoryQuery)) {
			insertStatement.setInt(1, accessory.getItemNumber());
			insertStatement.setString(2, accessory.getColor());
			insertStatement.setInt(3, accessory.getPlatformId());
			insertStatement.setString(4, accessory.getModelNumber());
			
			insertStatement.executeUpdate();
		}
	}
	
	/**
	 * Updates the accessory entry (and associated item) within the database
	 * 
	 * @param accessory Accessory details to be saved to the database
	 * @throws SQLException If a database error occurs
	 */
	public void updateAccessory(Accessory accessory) throws SQLException {
		final String updateAccessoryQuery = "UPDATE accessory SET color = ?, "
				+ "consoleId = ?, modelNumber = ? WHERE accessoryId = ?";
		
		// Update the item component of this accessory
		itemEntityManager.updateItem(accessory);
		
		try (Connection dbConnection = databaseManager.getConnection();
				PreparedStatement updateStatement = dbConnection.prepareStatement(updateAccessoryQuery)) {
			updateStatement.setInt(4, accessory.getItemNumber());
			updateStatement.setString(1, accessory.getColor());
			updateStatement.setInt(2, accessory.getPlatformId());
			updateStatement.setString(3, accessory.getModelNumber());
			
			updateStatement.executeUpdate();
		}
	}
	
	/**
	 * Deletes the accessory entry (and associated item entry) from the database
	 * @param accessory An accessory to be deleted
	 * @throws SQLException If a database error occurs
	 */
	public void deleteAccessory(Accessory accessory) throws SQLException {
		final String deleteAccessoryQuery = "DELETE FROM accessory WHERE accessoryId = ?";
		
		
		
		try (Connection dbConnection = databaseManager.getConnection();
				PreparedStatement deleteStatement = dbConnection.prepareStatement(deleteAccessoryQuery)) {
			deleteStatement.setInt(1, accessory.getItemNumber());			
			deleteStatement.executeUpdate();
			
			// Must delete item after deleting the accessory due to foreign constraints
			itemEntityManager.deleteItem(accessory);
		}
	}
	
	/**
	 * Creates a list of SQL statements necessary to recreate the
	 * database table and data within the table. 
	 * 
	 * @return A list of SQL commands
	 * @throws SQLException If a database error occurs.
	 */
	@Override
	public ArrayList<String> exportTable() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
