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

import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.DriverManager;
import java.sql.ResultSet; 

/**
 * Responsible for managing the overall 
 * SQLite database system providing access to 
 * connections and created the necessary database files. 
 * 
 * @author Russell Yorke
 */
public class SQLiteDBManager {
	public static final String DB_FILENAME = "inventory.db";
	private File dbFile;
	private String connectionURL;
	private ArrayList<EntityManager> registeredEntityManagers = new ArrayList<EntityManager>();
	private static SQLiteDBManager databaseManager = null;
	
	/**
	 * Provides access to the singleton SQLiteDBManager. If the manager hasn't yet
	 * been created it will be initialized/creates automatically. 
	 * 
	 * @return A singleton database manager instance
	 * @throws IOException
	 */
	public static SQLiteDBManager getManager() throws IOException {
		if (databaseManager == null)
			databaseManager = new SQLiteDBManager(DB_FILENAME);
		
		return databaseManager;
	}
	
	/**
	 * Initializes the database manager using the database file. 
	 * 
	 * @param dbFilename Filename (and optional pathname) to the database file
	 * @throws IOException if filename is a directory or cannot be read/written. 
	 */
	private SQLiteDBManager(String dbFilename) throws IOException {
		dbFile = new File(dbFilename);
		
		if (dbFile.exists() && dbFile.isDirectory()) {
			throw new IOException(String.format("%s is a directory and cannot be used as a database file", 
					dbFile.getAbsolutePath()));
		} else if (dbFile.exists() && (!dbFile.canRead() | !dbFile.canWrite())) {
			throw new IOException(String.format("Unable to read/write to %s.", dbFile.getAbsolutePath()));
		}
		
		connectionURL = String.format("jdbc:sqlite:%s", dbFile.getAbsolutePath());
	}
	
	/**
	 * Establishes a new connections to the database
	 * 
	 * @return A new database Connection
	 * @throws SQLException if unable to access the database
	 */
	public Connection getConnection() throws SQLException {
		Connection databaseConnection = DriverManager.getConnection(connectionURL);
		return databaseConnection;
	}
	
	/**
	 * Checks if a table exists within the schema
	 * 
	 * @param tableNamePattern A patttern to search for the table name (using SQL syntax)
	 * @return True if tableNamePattern is found, false otherwise
	 * 
	 * @throws SQLException if unable to access the database
	 */
	public boolean tableExists(String tableNamePattern) throws SQLException {
		boolean tableFound = false;
		try (Connection connection = getConnection()) {
			DatabaseMetaData metadata = connection.getMetaData();
			ResultSet results = metadata.getTables(null, null, tableNamePattern, null);
			tableFound = results.next();
			results.close();
		}
		return tableFound;
	}
	
	/**
	 * Registers a new entity class with the database manager and initializes
	 * the tables (calling createTable() on all registered entity managers)
	 * 
	 * @param entity A new entity to register with the database manager
	 * 
	 * @throws NullPointerException if entityManager is null
	 * @throws SQLException When a database error occurs. 
	 */
	public void registerEntityManager(EntityManager entityManager) throws NullPointerException, SQLException {
		if (entityManager == null)
			throw new NullPointerException("EntityManager cannot be null");
		
		registeredEntityManagers.add(entityManager);
		createEntityManagerTables();		
	}
	
	/**
	 * Calls createTable() for all registered entity managers ensuring
	 * their database tables have been created within the database. createTable()
	 * must be idempotent as this method will call the createTable() method every
	 * time a new entity is registered.  
	 * 
	 * @throws SQLException When a database error occurs. 
	 */
	private void createEntityManagerTables() throws SQLException {
		for (EntityManager entity : registeredEntityManagers) {
			entity.createTable();
		}
	}
	
	/**
	 * Executes the exportData method for all registered entity classes for purposes of
	 * backing up the database
	 * 
	 * @return A list of SQL commands from each entity that can be used to restore the database
	 * 
	 * @throws SQLException When a database error occurs. 
	 */
	public ArrayList<String> exportDatabase() throws SQLException {
		// TODO: 
		// Add implementation details
		return null;
	}
	
	/**
	 * Imports an SQL database file into the database. 
	 * 
	 * @throws SQLException When a database error occurs. 
	 */
	public void importDatabase() throws SQLException {
		// TODO: 
		// Add implementation details
	}
}

