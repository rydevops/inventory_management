package com.ryorke.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.ryorke.entity.Manufacture;

public class ManufactureEntityManager implements EntityManager {
	private static ManufactureEntityManager entityManager = null;
	private SQLiteDBManager databaseManager = null;
	
	/** 
	 * Provides access to the singleton manufacture entity manager
	 * that is responsible for interacting with the database
	 * 
	 * @return A reference to a singleton console entity manager
	 * @throws IOException if unable to access database file
	 * @throws SQLException if an error occurs while accessing the database
	 */
	public static ManufactureEntityManager getManager() throws IOException, SQLException {
		if (entityManager == null)
			entityManager = new ManufactureEntityManager();
		
		return entityManager;
	}
	
	/**
	 * Creates a new instance which will self-register
	 * itself with the SQLiteDBManager
	 * 
	 * @throws IOException if unable to access database file
	 * @throws SQLException if an error occurs while accessing the database
	 */
	private ManufactureEntityManager() throws IOException, SQLException {
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
		final String createManufactureTable = "CREATE TABLE IF NOT EXISTS manufacture"
				+ "(manufactureId INTEGER PRIMARY KEY" 
				+ "name TEXT UNIQUE NOT NULL)";		
				
		if (!databaseManager.tableExists("manufacture")) {
			try (Connection dbConnection = databaseManager.getConnection();
					Statement sqlStatement = dbConnection.createStatement()) {
				sqlStatement.executeUpdate(createManufactureTable);
			}
		}				
	}

	/**
	 * Finds a manufacture by id within a database
	 * 
	 * @param manufactureId The ID of the manufacture being searched for
	 * @return A manufacture instance or null if manufacture was not found
	 * 
	 * @throws SQLException If a database error occurs
	 */
	public Manufacture findManufacture(int manufactureId) throws SQLException {
		final String findManufactureQuery = "SELECT name FROM manufacture WHERE manufactureId = ?";
		Manufacture manufacture = null;
		
		try (Connection dbConnection = databaseManager.getConnection();
				PreparedStatement statement = dbConnection.prepareStatement(findManufactureQuery)) {
			statement.setInt(1, manufactureId);
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				manufacture = new Manufacture(manufactureId, result.getString("name"));
			}
		}
		
		return manufacture;
	}
	
	public void addManufacture(String name) {
		// TODO: 
		// Check if manufacture name already exists (in uppercase). If
		// it exists, return the manufactureId. If it doesn't exists, create it
		// and return the manufactureId.
	}
	
	public ArrayList<String> getManufactureNames() {
		// TODO: Implement
		return null; 
	}
	
	@Override
	public ArrayList<String> exportTable() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
