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
				+ "unitCost REAL DEFAULT = 0.00 NOT NULL, "
				+ "unitsInStock INTEGER DEFAULT = 0 NOT NULL, "
				+ "width REAL DEFAULT = 0.000 NOT NULL, "
				+ "height REAL DEFAULT = 0.000 NOT NULL, "
				+ "depth REAL DEFAULT = 0.000 NOT NULL, "
				+ "weight REAL DEFAULT = 0.000 NOT NULL)";
		
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

	public int addItem(Item item) {
		final String insertDefaultUserQuery = "INSERT INTO user (username, password, firstName, lastName, administrator) "
				+ "VALUES ('admin', 'admin', 'Administrative', 'User', 1)";		
		final String insertUserQuery = "INSERT INTO item "
				+ "(name, description, manufactureId, releaseDate, unitCost, "
				+ "unitsInStock, width, height, depth, weight) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		// TODO: Get manufactureId based on name, if it doesn't exist create it
		
		return 0;	// TODO: return new itemId
	}
	
	public void updateItem(Item item) {
		
	}
	
	public void deleteItem(Item item) {
		
	}
	
	@Override
	public ArrayList<String> exportTable() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
