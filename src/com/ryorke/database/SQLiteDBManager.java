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
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Properties;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData; 

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
	 * Generates a list of INSERT statement to import
	 * based on data in the table. 
	 * 
	 * @param tableName The table name to query
	 * @return A list of INSERT statements or null if no records found or an error occurs
	 * @throws SQLException if an error occurs while reading records from database
	 */
	public ArrayList<String> exportRecords(String tableName) throws SQLException {
		ArrayList<String> exportSQLResults = null;
		
		try (Connection dbConnection = databaseManager.getConnection(true);
			 Statement queryAllRecords = dbConnection.createStatement()) {		
			
			// Extract each column and value into an INSERT statement
			ResultSet records = queryAllRecords.executeQuery("SELECT * FROM " + tableName);
			ResultSetMetaData recordMetadata = records.getMetaData();			
			while (records.next()) {
				// Only create the record list if at least one record
				// exists. 
				if (exportSQLResults == null)
					exportSQLResults = new ArrayList<String>();
				
				ArrayList<String> columnNames = new ArrayList<String>();
				ArrayList<String> columnValues = new ArrayList<String>();
				
				for (int columnIndex = 1; columnIndex <= recordMetadata.getColumnCount(); columnIndex++) {
					columnNames.add(recordMetadata.getColumnName(columnIndex));
					
					switch (recordMetadata.getColumnType(columnIndex)) {
					case Types.INTEGER:
					case Types.FLOAT:
					case Types.DOUBLE:
					case Types.REAL:
						Object objectRecord = records.getObject(columnIndex);
						if (objectRecord == null) {
							columnValues.add("null");
						} else {						
							// Convert data type to Java Class (implicitly)
							// and convert to string
							columnValues.add(objectRecord.toString());
						}
						break;
					case Types.VARCHAR:
					case Types.NVARCHAR:
						String stringRecord = records.getString(columnIndex);
						if (stringRecord == null) {
							columnValues.add("null");
						} else {
							// Escape single quotes (if present)
							columnValues.add(String.format("'%s'", stringRecord.replaceAll("'", "''")));
						}
						break;
					default:
						// Throw an error as we should never hit this spot
						throw new SQLException("Unknown data type found during export");
					}														
				}
				
				StringBuilder insertStatement = new StringBuilder();
				insertStatement.append("INSERT INTO " + tableName + " (");
				for (int index = 0; index < columnNames.size(); index++) {					
					insertStatement.append(columnNames.get(index));
					if (index != columnNames.size() - 1)
						insertStatement.append(",");
				}
				insertStatement.append(") VALUES (");
				for (int index = 0; index < columnValues.size(); index++) {					
					insertStatement.append(columnValues.get(index));
					if (index != columnValues.size() - 1)
						insertStatement.append(",");
				}
				insertStatement.append(")");
				exportSQLResults.add(insertStatement.toString());
			}			
		}
		
		return exportSQLResults;
	}
	
	/**
	 * Establishes a new connections to the database
	 * 
	 * @return A new database Connection
	 * @throws SQLException if unable to access the database
	 */
	public Connection getConnection(Boolean enforceForceKeys) throws SQLException {
		Properties connectionProperties = new Properties();
		connectionProperties.setProperty("foreign_keys", enforceForceKeys.toString()); // Enables foreign key support
		connectionProperties.setProperty("synchronous", "OFF");  // Increases write performance at the risk of database corruption if power lost
		Connection databaseConnection = DriverManager.getConnection(connectionURL, connectionProperties);
		return databaseConnection;
	}
	
	/**
	 * Checks if a table exists within the schema
	 * 
	 * @param tableNamePattern A pattern to search for the table name (using SQL syntax)
	 * @return True if tableNamePattern is found, false otherwise
	 * 
	 * @throws SQLException if unable to access the database
	 */
	public boolean tableExists(String tableNamePattern) throws SQLException {
		boolean tableFound = false;
		try (Connection connection = getConnection(true)) {
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
		ArrayList<String> sqlStatements = new ArrayList<String>();
		
		for (EntityManager manager : registeredEntityManagers) {
			ArrayList<String> exportData = manager.exportTable();
			if (exportData != null) {
				sqlStatements.addAll(exportData);
			}
		}
		
		return sqlStatements;
	}
	
	/**
	 * Imports an SQL database file into the database.
	 * 
	 *  Note 1: This file purposefully disables foreign key constraints during import
	 *          to avoid import ordering errors.        
	 * 
	 *  Note 2:  No error checking is performed on statements.
	 * 
	 * @param sqlStatements A list of statements to execute. 
	 * @throws SQLException When a database error occurs. 
	 */
	public void importDatabase(ArrayList<String> sqlStatements) throws SQLException, IOException {
		try (Connection dbConnection = databaseManager.getConnection(false);
			 Statement sqlStatement = dbConnection.createStatement()) {
			for (String statement : sqlStatements) {
				sqlStatement.execute(statement);
			}
		}
	}
	
	/**
	 * Drops all tables within the database
	 * 
	 * @throws SQLException If an error occurs while accessing the database
	 */
	public void dropAllTables() throws SQLException {
		ArrayList<String> tableNames = new ArrayList<String>();
		try (Connection dbConnection = databaseManager.getConnection(false)) {
			// Start a new transaction
			// NOTE: Without this the import of a large dataset will 
			//       take an extremely long time. 
			dbConnection.setAutoCommit(false);
			
			// Create a list of tables and then close the results
			DatabaseMetaData dbMetadata = dbConnection.getMetaData();
			String[] types = {"TABLE"};
			ResultSet tables = dbMetadata.getTables(null, null, "%", types);			
			while (tables.next()) {
				String tableName = tables.getString("TABLE_NAME");
				tableNames.add(tableName);
			}			
			tables.close();
			
			// Drop the tables, database metadata must be closed as it 
			// creates a lock on the tables and prevents them from being 
			// dropped. 
			for (String tableName : tableNames) {
				Statement dropTable = dbConnection.createStatement();
				dropTable.execute("DROP TABLE " + tableName);
				dropTable.close();
			}
			
			dbConnection.commit();
		}
		
		
	}
}

