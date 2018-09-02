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

import com.ryorke.entity.Game;

/**
 * Provides utility operations for pushing/getting Game objects
 * within a database
 * 
 * @author Russell Yorke
 */
public class GameEntityManager implements EntityManager {
	private static GameEntityManager entityManager = null;
	private static ItemEntityManager itemEntityManager = null;
	private SQLiteDBManager databaseManager = null;
	
	/** 
	 * Provides access to the singleton game entity manager
	 * that is responsible for interacting with the database
	 * 
	 * @return A reference to a singleton game entity manager
	 * @throws IOException if unable to access database file
	 * @throws SQLException if an error occurs while accessing the database
	 */
	public static GameEntityManager getManager() throws IOException, SQLException {
		if (entityManager == null)
			entityManager = new GameEntityManager();
		
		if (itemEntityManager == null)
			itemEntityManager = ItemEntityManager.getManager();
		
		return entityManager;
	}
	
	/**
	 * Creates a new instance of the GameEntityManager registering
	 * itself with the SQLiteDBManager
	 * 
	 * @throws IOException if unable to access database file
	 * @throws SQLException if an error occurs while accessing the database
	 */
	private GameEntityManager() throws IOException, SQLException {
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
		final String createTableQuery = "CREATE TABLE IF NOT EXISTS game "
				+ "(gameId INTEGER UNIQUE NOT NULL, "	// gameId = itemId or foreign key
				+ "numberOfDiscs INTEGER NOT NULL, "
				+ "numberOfPlayers INTEGER NOT NULL, "
				+ "consoleId INTEGER NOT NULL, "
				+ "esrbRating TEXT NOT NULL)";
		
		if (!databaseManager.tableExists("game")) {
			try (Connection dbConnection = databaseManager.getConnection();
					Statement sqlStatement = dbConnection.createStatement();) {
				sqlStatement.executeUpdate(createTableQuery);
			}
		}
	}
	
	/**
	 * Retrieves a list of games from the database
	 * 
	 * @return A list of games
	 * @throws SQLException If a database error occurs
	 * @throws ParseException If a release date was incorrectly stored within the database. 
	 */
	public ArrayList<Game> getGames() throws SQLException, ParseException {
		final String getAllGamesQuery = "SELECT * FROM game";
		ArrayList<Game> games = null;
		
		try (Connection dbConnection = databaseManager.getConnection();
				Statement statement = dbConnection.createStatement();
				ResultSet gamesResult = statement.executeQuery(getAllGamesQuery)) {
			while (gamesResult.next()) {
				int gameId = gamesResult.getInt("gameId");
				int numberOfDiscs = gamesResult.getInt("numberOfDiscs");
				int numberOfPlayers = gamesResult.getInt("numberOfPlayers");
				int consoleId = gamesResult.getInt("consoleId");
				String esrbRating = gamesResult.getString("esrbRating");
				
				Game game = new Game();
				game.setItemNumber(gameId);
				game.setNumberOfDiscs(numberOfDiscs);
				game.setNumberOfPlayers(numberOfPlayers);
				game.setPlatformId(consoleId);
				game.setEsrbRating(esrbRating);
				
				itemEntityManager.loadItem(game);
				
				if (games == null)
					games = new ArrayList<Game>();
				
				games.add(game);
			}
		}
		
		return null; 
	}

	/**
	 * Creates a new game record (and associated item) within database. 
	 * Once created, the gameId will be updated to reflect the gameId within the database
	 * 
	 * @param game A new game entity
	 * @throws SQLException If a database error occurs
	 */
	public void addGame(Game game) throws SQLException {
		final String insertGameQuery = "INSERT INTO game (gameId, numberOfDiscs, numberOfPlayers, "
				+ "consoleId, esrbRating) VALUES (?, ?, ?, ?, ?)";
		
		// Create the associated item record first to acquire a new itemId
		itemEntityManager.addItem(game);
		
		try (Connection dbConnection = databaseManager.getConnection();
				PreparedStatement insertStatement = dbConnection.prepareStatement(insertGameQuery)) {
			insertStatement.setInt(1, game.getItemNumber());
			insertStatement.setInt(2, game.getNumberOfDiscs());
			insertStatement.setInt(3, game.getNumberOfPlayers());
			insertStatement.setInt(4, game.getPlatformId());
			insertStatement.setString(5, game.getEsrbRating());
			
			insertStatement.executeUpdate();
		}
	}
	
	/**
	 * Updates the game entry (and associated item) within the database
	 * 
	 * @param game Game details to be saved to the database
	 * @throws SQLException If a database error occurs
	 */
	public void updateGame(Game game) throws SQLException {
		final String updateGameQuery = "UPDATE game SET numberOfDiscs = ?, "
				+ "numberOfPlayers = ?, consoleId = ?, esrbRating = ? WHERE gameId = ?";
		
		// Update the item component of this accessory
		itemEntityManager.updateItem(game);
		
		try (Connection dbConnection = databaseManager.getConnection();
				PreparedStatement updateStatement = dbConnection.prepareStatement(updateGameQuery)) {
			updateStatement.setInt(5, game.getItemNumber());
			updateStatement.setInt(1, game.getNumberOfDiscs());
			updateStatement.setInt(2, game.getNumberOfPlayers());
			updateStatement.setInt(3, game.getPlatformId());
			updateStatement.setString(4, game.getEsrbRating());
			
			updateStatement.executeUpdate();
		}
	}
	
	/**
	 * Deletes the game entry (and associated item entry) from the database
	 * @param game An game to be deleted
	 * @throws SQLException If a database error occurs
	 */
	public void deleteGame(Game game) throws SQLException {
		final String deleteGameQuery = "DELETE FROM game WHERE gameId = ?";
		
		itemEntityManager.deleteItem(game);
		
		try (Connection dbConnection = databaseManager.getConnection();
				PreparedStatement deleteStatement = dbConnection.prepareStatement(deleteGameQuery)) {
			deleteStatement.setInt(1, game.getItemNumber());			
			deleteStatement.executeUpdate();
		}
	}
	
	@Override
	public ArrayList<String> exportTable() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
