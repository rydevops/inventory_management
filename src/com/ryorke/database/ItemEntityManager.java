package com.ryorke.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class ItemEntityManager implements EntityManager {
	private static ItemEntityManager entityManager = null; 
	private SQLiteDBManager databaseManager = null;
	
	/** 
	 * Provides access to the singleton console entity manager
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
	 * Creates a new instance of the UserEntityManager registering
	 * itself with the SQLiteDBManager
	 * 
	 * @throws IOException if unable to access database file
	 * @throws SQLException if an error occurs while accessing the database
	 */
	private ItemEntityManager() throws IOException, SQLException {
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
		final String createTableQuery = "CREATE TABLE IF NOT EXISTS item "
				+ "(itemId INTEGER PRIMARY KEY, "
				+ "productDescription TEXT NOT NULL, "
				+ "productName TEXT NOT NULL, "
				+ "releaseDate TEXT NOT NULL, "
				+ "unitCost REAL NOT NULL, "
				+ "unitsInStock INTEGER NOT NULL, "
				+ "manufacture TEXT NOT NULL, "
				+ "height REAL NOT NULL, "
				+ "width REAL NOT NULL, "
				+ "depth REAL NOT NULL, "				
				+ "weight REAL NOT NULL)";
		
		if (!databaseManager.tableExists("item")) {
			try (Connection dbConnection = databaseManager.getConnection();
					Statement sqlStatement = dbConnection.createStatement();) {
				sqlStatement.executeUpdate(createTableQuery);
			}
		}
	}

	@Override
	public ArrayList<String> exportTable() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
