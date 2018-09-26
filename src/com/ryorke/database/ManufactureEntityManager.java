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

import com.ryorke.entity.Manufacture;

/**
 * Provides utility operations for pushing/getting Manufacture objects
 * within a database
 * 
 * @author Russell Yorke
 */
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
				+ "(manufactureId INTEGER PRIMARY KEY, " 
				+ "name TEXT UNIQUE NOT NULL COLLATE NOCASE)";		
				
		if (!databaseManager.tableExists("manufacture")) {
			try (Connection dbConnection = databaseManager.getConnection(true);
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
		
		try (Connection dbConnection = databaseManager.getConnection(true);
				PreparedStatement statement = dbConnection.prepareStatement(findManufactureQuery)) {
			statement.setInt(1, manufactureId);
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				manufacture = new Manufacture(manufactureId, result.getString("name"));
			}
		}
		
		return manufacture;
	}
	
	/**
	 * Performs a case-insensitive search for manufacture name. 
	 * 
	 * @param name Name to search for
	 * @return A manufacture instance if name found otherwise null
	 * @throws SQLException If a database error occurs
	 */
	public Manufacture findManufacture(String name) throws SQLException {
		final String findManufactureQuery = "SELECT * FROM manufacture WHERE name = ?";
		Manufacture manufacture = null;
		
		try (Connection dbConnection = databaseManager.getConnection(true);
				PreparedStatement statement = dbConnection.prepareStatement(findManufactureQuery)) {
			statement.setString(1, name);
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				String manufactureName = result.getString("name");
				int manufactureId = result.getInt("manufactureId"); 
				manufacture = new Manufacture(manufactureId, manufactureName);
			}
		}
		
		return manufacture;
	}
	
	/**
	 * Adds a new manufacture to the manufacture database if it doesn't already exists. 
	 * 
	 * @param name The manufacture name (case-insensitive)
	 * @return A manufacture object for the new manufacture or existing manufacture if found
	 * @throws SQLException If a database error occurs
	 */
	public Manufacture addManufacture(String name) throws SQLException {
		final String addManufactureQuery = "INSERT INTO manufacture (name) VALUES (?)";
		final String getManufactureIdQuery = "SELECT last_insert_rowid() AS manufactureId";

		Manufacture manufacture = findManufacture(name);
		
		if (manufacture == null) {
			try (Connection dbConnection = databaseManager.getConnection(true);
					PreparedStatement statement = dbConnection.prepareStatement(addManufactureQuery)) {
				statement.setString(1, name);
				statement.executeUpdate();
				
				Statement lastRowStatement = dbConnection.createStatement();
				ResultSet results = lastRowStatement.executeQuery(getManufactureIdQuery);
				if (results.next()) {
					manufacture = new Manufacture(results.getInt("manufactureId"), name);
				}
				
				
			}
		}
		
		return manufacture;
	}
	
	/**
	 * Retrieves a list of manufactures from the database
	 * 
	 * @return A list of manufactures
	 * @throws SQLException If a database error occurs
	 */
	public ArrayList<Manufacture> getManufactures() throws SQLException {
		final String getAllManufacturesQuery = "SELECT * FROM manufacture";
		ArrayList<Manufacture> manufactures = null; 
		
		try (Connection dbConnection = databaseManager.getConnection(true);
				Statement statement = dbConnection.createStatement();
				ResultSet results = statement.executeQuery(getAllManufacturesQuery)) {
			while (results.next()) {
				int manufactureId = results.getInt("manufactureId");
				String name = results.getString("name");
				
				if (manufactures == null)
					manufactures = new ArrayList<Manufacture>();
				
				manufactures.add(new Manufacture(manufactureId, name));
			}
		}
		
		return manufactures; 
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
