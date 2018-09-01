package com.ryorke.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;

import com.ryorke.entity.Console;

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
				+ "(consoleId INTEGER UNIQUE, "	// consoleId = itemId or foreign key
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

	public void addConsole(Console console) {
		final String insertConsoleQuery = "INSERT INTO console (consoleId, color, controllersIncluded, "
				+ "diskSpace, includedGameIds, modelNumber) VALUES (?, ?, ?, ?, ?, ?)";
		
		int newConsoleId = itemEntityManager.addItem(console);
	}
	
	public void updateConsole(Console console) {
		final String updateConsoleQuery = "UPDATE console SET color = ?, controllersIncluded = ?, "
				+ "diskSpace = ?, includedGameIds = ?, modelNumber = ? WHERE consoleId = ?";
		
		itemEntityManager.updateItem(console);
	}
	
	public void deleteConsole(Console console) {
		final String deleteConsoleQuery = "DELETE FROM console WHERE consoleId = ?";
		
		itemEntityManager.deleteItem(console);
	}
	
	@Override
	public ArrayList<String> exportTable() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
