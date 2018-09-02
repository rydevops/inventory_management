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

import com.ryorke.entity.Console;

/**
 * Provides utility operations for pushing/getting Console objects
 * within a database
 * 
 * @author Russell Yorke
 */
public class ConsoleEntityManager implements EntityManager {
	private static ConsoleEntityManager entityManager = null;
	private static ItemEntityManager itemEntityManager = null;
	private SQLiteDBManager databaseManager = null;
	
	/** 
	 * Provides access to the singleton console entity manager
	 * that is responsible for interacting with the database
	 * 
	 * @return A reference to a singleton console entity manager
	 * @throws IOException if unable to access database file
	 * @throws SQLException if an error occurs while accessing the database
	 */
	public static ConsoleEntityManager getManager() throws IOException, SQLException {
		if (entityManager == null)
			entityManager = new ConsoleEntityManager();
		
		if (itemEntityManager == null)
			itemEntityManager = ItemEntityManager.getManager();
		
		return entityManager;
	}
	
	/**
	 * Creates a new instance of the ConsoleEntityManager registering
	 * itself with the SQLiteDBManager
	 * 
	 * @throws IOException if unable to access database file
	 * @throws SQLException if an error occurs while accessing the database
	 */
	private ConsoleEntityManager() throws IOException, SQLException {
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
		final String createTableQuery = "CREATE TABLE IF NOT EXISTS console "
				+ "(consoleId INTEGER UNIQUE NOT NULL, "	// consoleId = itemId or foreign key
				+ "color TEXT NOT NULL, "
				+ "controllersIncluded INTEGER, "
				+ "diskSpace TEXT NOT NULL, "
				+ "includedGameIds TEXT DEFAULT '', "  // comma-separated int with foreign key of games
				+ "modelNumber TEXT NOT NULL)";
		
		if (!databaseManager.tableExists("console")) {
			try (Connection dbConnection = databaseManager.getConnection();
					Statement sqlStatement = dbConnection.createStatement();) {
				sqlStatement.executeUpdate(createTableQuery);
			}
		}
	}
	
	/**
	 * Retrieves a list of consoles from the database
	 * 
	 * @return A list of consoles
	 * @throws SQLException If a database error occurs
	 * @throws ParseException If a release date was incorrectly stored within the database. 
	 */
	public ArrayList<Console> getConsoles() throws SQLException, ParseException {
		final String getAllConsolesQuery = "SELECT * FROM console";
		ArrayList<Console> consoles = null;
		
		try (Connection dbConnection = databaseManager.getConnection();
				Statement statement = dbConnection.createStatement();
				ResultSet consoleResults = statement.executeQuery(getAllConsolesQuery)) {
			while (consoleResults.next()) {
				int consoleId = consoleResults.getInt("consoleId");
				String color = consoleResults.getString("color");
				int controllersIncluded = consoleResults.getInt("controllersIncluded");
				String diskSpace = consoleResults.getString("diskSpace");				
				String modelNumber = consoleResults.getString("modelNumber");
				
				String gameIdsCSV = consoleResults.getString("includedGameIds");
				int[] gameIds = null;
				if (gameIdsCSV != null) {
					String[] splitGameIds = gameIdsCSV.split(",");
					gameIds = new int[splitGameIds.length];
					for (int index = 0; index < splitGameIds.length; index++) {
						gameIds[index] = Integer.parseInt(splitGameIds[index]);
					}				
				}
				
				Console console = new Console();
				console.setItemNumber(consoleId);
				console.setColor(color);
				console.setControllersIncluded(controllersIncluded);
				console.setIncludedGameId(gameIds);
				console.setDiskSpace(diskSpace);
				console.setModelNumber(modelNumber);
				itemEntityManager.loadItem(console);
				
				if (consoles == null)
					consoles = new ArrayList<Console>();
				
				consoles.add(console);
			}
		}
		
		return null; 
	}

	/**
	 * Creates a new console record (and associated item and manufacture records) within database. 
	 * Once created, the consoleId will be updated to reflect the consoleId within the database
	 * 
	 * @param console A new console entity
	 * @throws SQLException 
	 */
	public void addConsole(Console console) throws SQLException {
		final String insertConsoleQuery = "INSERT INTO console (consoleId, color, controllersIncluded, "
				+ "diskSpace, includedGameIds, modelNumber) VALUES (?, ?, ?, ?, ?, ?)";
		
		// Create the associated item record first to acquire a new itemId
		itemEntityManager.addItem(console);
		
		try (Connection dbConnection = databaseManager.getConnection();
				PreparedStatement insertStatement = dbConnection.prepareStatement(insertConsoleQuery)) {
			insertStatement.setInt(1, console.getItemNumber());
			insertStatement.setString(2, console.getColor());
			insertStatement.setInt(3, console.getControllersIncluded());
			insertStatement.setString(4, console.getDiskSpace());
			insertStatement.setString(6, console.getModelNumber());
			
			String includedGameIds = null;
			for (int gameId : console.getIncludedGameId()) {
				if (includedGameIds == null) {
					includedGameIds = Integer.toString(gameId);
				} else {
					includedGameIds += "," + Integer.toString(gameId);
				}
			}
			insertStatement.setString(5, includedGameIds);
			
			insertStatement.executeUpdate();
		}
	}
	
	/**
	 * Updates the console entry (and associated item and manufacture entries) within the database
	 * 
	 * @param console Console details to be saved to the database
	 * @throws SQLException If a database error occurs
	 */
	public void updateConsole(Console console) throws SQLException {
		final String updateConsoleQuery = "UPDATE console SET color = ?, controllersIncluded = ?, "
				+ "diskSpace = ?, includedGameIds = ?, modelNumber = ? WHERE consoleId = ?";
		
		// Update the item component of this console
		itemEntityManager.updateItem(console);
		
		try (Connection dbConnection = databaseManager.getConnection();
				PreparedStatement updateStatement = dbConnection.prepareStatement(updateConsoleQuery)) {
			updateStatement.setInt(6, console.getItemNumber());
			updateStatement.setString(1, console.getColor());
			updateStatement.setInt(2, console.getControllersIncluded());
			updateStatement.setString(3, console.getDiskSpace());
			updateStatement.setString(5, console.getModelNumber());
			
			String includedGameIds = null;
			for (int gameId : console.getIncludedGameId()) {
				if (includedGameIds == null) {
					includedGameIds = Integer.toString(gameId);
				} else {
					includedGameIds += "," + Integer.toString(gameId);
				}
			}
			updateStatement.setString(4, includedGameIds);
			
			updateStatement.executeUpdate();
		}
	}
	
	/**
	 * Deletes the console entry (and associated item entry) from the database
	 * @param console A console to be deleted
	 * @throws SQLException If a database error occurs
	 */
	public void deleteConsole(Console console) throws SQLException {
		final String deleteConsoleQuery = "DELETE FROM console WHERE consoleId = ?";
		
		itemEntityManager.deleteItem(console);
		
		try (Connection dbConnection = databaseManager.getConnection();
				PreparedStatement deleteStatement = dbConnection.prepareStatement(deleteConsoleQuery)) {
			deleteStatement.setInt(1, console.getItemNumber());			
			deleteStatement.executeUpdate();
		}
	}
	
	@Override
	public ArrayList<String> exportTable() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
