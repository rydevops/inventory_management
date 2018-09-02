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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.ryorke.entity.Item;
import com.ryorke.entity.Manufacture;
import com.ryorke.entity.PackageDimension;

/**
 * Provides utility operations for pushing/getting Item objects
 * within a database
 * 
 * @author Russell Yorke
 */
public class ItemEntityManager implements EntityManager {
	private static ItemEntityManager entityManager = null;
	private SQLiteDBManager databaseManager = null;
	private ManufactureEntityManager manufactureManager = null; 
	
	/** 
	 * Provides access to the singleton item entity manager
	 * that is responsible for interacting with the database
	 * 
	 * @return A reference to a singleton console entity manager
	 * @throws IOException if unable to access database file
	 * @throws SQLException if an error occurs while accessing the database
	 */
	public static ItemEntityManager getManager() throws IOException, SQLException {
		if (entityManager == null)
			entityManager = new ItemEntityManager();
		
		return entityManager;
	}
	
	/**
	 * Creates a new instance of the ItemEntityManager registering
	 * itself with the SQLiteDBManager
	 * 
	 * @throws IOException if unable to access database file
	 * @throws SQLException if an error occurs while accessing the database
	 */
	private ItemEntityManager() throws IOException, SQLException {
		databaseManager = SQLiteDBManager.getManager();
		databaseManager.registerEntityManager(this);	
		manufactureManager = ManufactureEntityManager.getManager();
	}	
	
	/**
	 * Idempotent method for creating a database table schema if it
	 * doesn't already exists. 
	 * 
	 * @throws SQLException If a database error occurs while processing the request. 
	 */
	@Override
	public void createTable() throws SQLException {
		final String createItemTable = "CREATE TABLE IF NOT EXISTS item "
				+ "(itemNumber INTEGER PRIMARY KEY, "
				+ "name TEXT UNIQUE NOT NULL, "
				+ "description TEXT NOT NULL, "
				+ "manufactureId INTEGER NOT NULL, " //FOREIGN KEY
				+ "releaseDate TEXT NOT NULL, " // Converted to text string
				+ "unitCost REAL DEFAULT 0.00 NOT NULL, "
				+ "unitsInStock INTEGER DEFAULT 0 NOT NULL, "
				+ "width REAL DEFAULT 0.000 NOT NULL, "
				+ "height REAL DEFAULT 0.000 NOT NULL, "
				+ "depth REAL DEFAULT 0.000 NOT NULL, "
				+ "weight REAL DEFAULT 0.000 NOT NULL)";
		
		if (!databaseManager.tableExists("item")) {
			try (Connection dbConnection = databaseManager.getConnection();
					Statement sqlStatement = dbConnection.createStatement();) {
				sqlStatement.executeUpdate(createItemTable);
			}
		}
	}
	
	/**
	 * Queries the databases for item information (based on the itemId) and loads
	 * the data into the item provided
	 * 
	 * @param item An instance of an item with the itemId preloaded.
	 * @return true if the item was found and loaded, false otherwise.  
	 * @throws SQLException If a database error occurs
	 * @throws ParseException  If database has a text string invalidly stored
	 */	
	public boolean loadItem(Item item) throws SQLException, ParseException {
		final String findItemQuery = "SELECT * FROM item WHERE itemId = ?";
		boolean itemFound = false;
		
		if (item != null) {
			try (Connection dbConnection = databaseManager.getConnection();
					PreparedStatement statement = dbConnection.prepareStatement(findItemQuery)) {
				statement.setInt(1, item.getItemNumber());
				ResultSet queryResult = statement.executeQuery();
				
				itemFound = queryResult.next();
				
				String name = queryResult.getString("name");
				String description = queryResult.getString("description");
				double unitCost = queryResult.getDouble("unitCost");
				int unitsInStock = queryResult.getInt("unitsInStock");
				float height = (float) queryResult.getDouble("height");
				float width = (float) queryResult.getDouble("width");
				float depth = (float) queryResult.getDouble("depth");
				float weight = (float) queryResult.getDouble("weight");
				
				int manufactureId = queryResult.getInt("manufactureId");
				Manufacture manufacture = manufactureManager.findManufacture(manufactureId);
				String manufactureName = (manufacture != null) ? manufacture.getName() : "";
				
				String dateFormat = "yyyy/MM/dd";				
				SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
				String dateText = queryResult.getString("releaseDate");				
				Date releaseDate = dateFormatter.parse(dateText);
				
				item.setManufacture(manufactureName);				
				item.setPackageDimension(new PackageDimension(height, width, depth, weight));
				item.setUnitCost(unitCost);
				item.setProductDescription(description);
				item.setProductName(name);
				item.setReleaseDate(releaseDate);
				item.setUnitsInStock(unitsInStock);
			}
		}
		
		
		return itemFound;
	}

	/**
	 * Inserts a new row into the item table and set the itemId on the item provided.
	 * 
	 * This method will also create a new manufacture if it doesn't already exist within
	 * the database. 
	 * 
	 * @param item An item to insert which will also be modified to reflect the new itemId
	 * @throws SQLException If a database error occurs
	 */
	public void addItem(Item item) throws SQLException {
		final String insertItemQuery = "INSERT INTO item "
				+ "(name, description, manufactureId, releaseDate, unitCost, "
				+ "unitsInStock, width, height, depth, weight) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		final String getItemIdQuery = "SELECT last_insert_rowid() AS itemId";
		final String dateFormat = "yyyy/MM/dd";
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		
		// Create the manufacture entry (if it doesn't already exist) and retrieve details about
		// that manufacture 
		Manufacture manufacture = manufactureManager.addManufacture(item.getManufacture());
		

		try (Connection dbConnection = databaseManager.getConnection();
				PreparedStatement insertItemStatement = dbConnection.prepareStatement(insertItemQuery)) {
			insertItemStatement.setString(1, item.getProductName());
			insertItemStatement.setString(2, item.getProductDescription());
			insertItemStatement.setInt(3, manufacture.getManufactureId());
			insertItemStatement.setString(4,  dateFormatter.format(item.getReleaseDate()));
			insertItemStatement.setDouble(5, item.getUnitCost());
			insertItemStatement.setInt(6, item.getUnitsInStock());
			insertItemStatement.setFloat(7, item.getPackageDimensions().getWidth());
			insertItemStatement.setFloat(8, item.getPackageDimensions().getHeight());
			insertItemStatement.setFloat(9, item.getPackageDimensions().getDepth());
			insertItemStatement.setFloat(10, item.getPackageDimensions().getWeight());
			insertItemStatement.executeUpdate();
			
			// Retrieve the itemId based on the last row inserted
			Statement lastRowIdStatement = dbConnection.createStatement();
			ResultSet result = lastRowIdStatement.executeQuery(getItemIdQuery);
			if (result.next()) {
				item.setItemNumber(result.getInt("itemId"));
			}
		}
	}
	
	/**
	 * Updates the database with the information provided in the item. If a new manufacture
	 * needs to be created during this operation it will automatically be created. 
	 * 
	 * @param item Item that needs to be saved to the database
	 * @throws SQLException If a database error occurs
	 */
	public void updateItem(Item item) throws SQLException {
		final String updateItemQuery = "UPDATE item "
				+ "SET name = ?, description = ?, manufactureId = ?, releaseDate = ?, unitCost = ?, "
				+ "unitsInStock = ?, width = ?, height = ?, depth = ?, weight = ? "
				+ "WHERE itemId = ?";
		final String dateFormat = "yyyy/MM/dd";
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		
		// Create the manufacture entry (if it doesn't already exist) and retrieve details about
		// that manufacture 
		Manufacture manufacture = manufactureManager.addManufacture(item.getManufacture());
		
		try (Connection dbConnection = databaseManager.getConnection();
				PreparedStatement updateItemStatement = dbConnection.prepareStatement(updateItemQuery)) {
			updateItemStatement.setString(1, item.getProductName());
			updateItemStatement.setString(2, item.getProductDescription());
			updateItemStatement.setInt(3, manufacture.getManufactureId());
			updateItemStatement.setString(4,  dateFormatter.format(item.getReleaseDate()));
			updateItemStatement.setDouble(5, item.getUnitCost());
			updateItemStatement.setInt(6, item.getUnitsInStock());
			updateItemStatement.setFloat(7, item.getPackageDimensions().getWidth());
			updateItemStatement.setFloat(8, item.getPackageDimensions().getHeight());
			updateItemStatement.setFloat(9, item.getPackageDimensions().getDepth());
			updateItemStatement.setFloat(10, item.getPackageDimensions().getWeight());
			updateItemStatement.executeUpdate();
		}
	}
	
	/**
	 * Deletes an item from the database
	 * 
	 * @param item The item to be deleted
	 * @throws SQLException If an error occurs
	 */
	public void deleteItem(Item item) throws SQLException {
		final String deleteItemQuery = "DELETE FROM item WHERE itemId = ?";
		
		try (Connection dbConnection = databaseManager.getConnection();
				PreparedStatement deleteStatement = dbConnection.prepareStatement(deleteItemQuery)) {
			deleteStatement.setInt(1, item.getItemNumber());			
			deleteStatement.executeUpdate();
		}
	}
	
	@Override
	public ArrayList<String> exportTable() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
