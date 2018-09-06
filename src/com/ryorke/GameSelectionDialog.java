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
package com.ryorke;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import com.ryorke.database.GameEntityManager;
import com.ryorke.entity.Game;

/**
 * Displays a new modal dialog allowing users to select one or more games to included. 
 * 
 * @author Russell Yorke
 */
@SuppressWarnings("serial")
public class GameSelectionDialog extends JDialog {	
	private GameTableModel tableModel;
	private boolean wasSaved = false; 	// Flag to provide feedback if the "Add selected gamed" button was clicked. 
	
	/**
	 * Creates a new dialog populating the available games and checking any games provided. 
	 * 
	 * @param owner The window to attach this modal dialog to
	 * @param consoleId Filters game list to games for the desired console
	 * @param existingGameIdsIncluded A list of game IDs to check when displayed
	 * @throws SQLException If a database error occurs
	 * @throws IOException If system is unable to access the database files. 
	 * @throws ParseException If an error occurs in parsing dates within the database tables
	 */
	public GameSelectionDialog(JDialog owner, int consoleId, int[] existingGameIdsIncluded) 
			throws SQLException, IOException, ParseException {
		super(owner, true);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
 
		JLabel instructions = new JLabel("Select all games to include:");
		contentPane.add(instructions, BorderLayout.NORTH);
 	
		tableModel = new GameTableModel(consoleId, existingGameIdsIncluded);
		JTable gameList = new JTable(tableModel);
		gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		gameList.setCellSelectionEnabled(false);
		gameList.setRowSelectionAllowed(false);
		gameList.setColumnSelectionAllowed(false);
		JScrollPane tableScrollView = new JScrollPane(gameList);
		contentPane.add(tableScrollView, BorderLayout.CENTER);
 
		JButton addGames = new JButton("Add selected games");
		addGames.addActionListener(new ActionListener() {
			/**
			* Closes the dialog saving the changes. 
			* @param e Action event details
			*/
			@Override
			public void actionPerformed(ActionEvent e) {
				wasSaved = true;
				GameSelectionDialog.this.dispatchEvent(
						new WindowEvent(GameSelectionDialog.this, WindowEvent.WINDOW_CLOSING));
				}
			});
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			/**
			* Closes the dialog without saving the changes. 
			* @param e Action event details
			*/
			@Override
			public void actionPerformed(ActionEvent e) {
				GameSelectionDialog.this.dispatchEvent(
						new WindowEvent(GameSelectionDialog.this, WindowEvent.WINDOW_CLOSING));
				}
			});
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
		buttonPanel.add(addGames);
		buttonPanel.add(cancel);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);				
				
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("Game selection");
		setLocationRelativeTo(owner);
		pack();
	}
	
	/**
	 * Confirms that the user saved the requested changes
	 * @return True if user clicks "add games" false otherwise. 
	 */
	public boolean wasSaved() {
		return this.wasSaved;
	}
	
	/**
	 * Retrieves a list of games that were selected. 
	 * 
	 * @return A list of games or null if no games selected. 
	 */
	public ArrayList<Game> getIncludedGames() {
		return tableModel.getIncludedGames();
	}

	/**
	 * Retrieves the total number of games available for the selected console
	 * @return The number of games available
	 */
	public int getGameCount() {
		return tableModel.getRowCount();
	}
	
	/**
	 * A model for displaying the game list including a checkbox to select one or more games. 
	 * 
	 * @author Russell Yorke
	 *
	 */
	private class GameTableModel extends AbstractTableModel {
		public static final int FIELD_GAME_INCLUDED = 0;
		public static final int FIELD_GAME_ID = 1;
		public static final int FIELD_GAME_NAME = 2;
		
		private GameEntityManager gameManager; 
		private String[] header = { "Included", "Game ID", "Name" };
		private ArrayList<Game> gameList;
		private boolean[] gameIncluded; 
		
		/**
		 * Creates a new model filtering the game list to the desired console and places a checkmark 
		 * in the selected game list if including. 
		 * 
		 * @param consoleId Filters the game list based on console
		 * @param existingGameIdsIncluded A list of existing game IDs to be checked
		 * @throws SQLException If a database error occurs
		 * @throws IOException If the system is unable to access the database file
		 * @throws ParseException If a date parsing error occurs within the database 
		 */
		public GameTableModel(int consoleId, int[] existingGameIdsIncluded) throws SQLException, IOException, ParseException {
			gameManager = GameEntityManager.getManager();
			gameList = gameManager.getGames(consoleId);
			if (gameList != null && gameList.size() > 0)
				gameIncluded = new boolean[gameList.size()];
			
			if (existingGameIdsIncluded != null) {
				for (int gameIndex = 0; gameIndex < gameList.size(); gameIndex++) {
					for (int includedGameIndex = 0; includedGameIndex < existingGameIdsIncluded.length; includedGameIndex++) {
						if (gameList.get(gameIndex).getItemNumber() == existingGameIdsIncluded[includedGameIndex]) {
							gameIncluded[gameIndex] = true; // Toggle the checkbox on
							break;	// Skip the rest of the existing game list and continue
						}
					}
				}
			}
		}
		
		/**
		 * Retrieves a list of games selected (checked)
		 * 
		 * @return A list of games (checked) or null if no games selected. 
		 */
		public ArrayList<Game> getIncludedGames() {
			ArrayList<Game> includedGames = null;
			
			for (int index = 0; index < gameIncluded.length; index++) {
				if (gameIncluded[index] == true) {
					if (includedGames == null)
						includedGames = new ArrayList<Game>();
					
					includedGames.add(gameList.get(index));
				}
			}
			
			return includedGames;
		}
		
		/**
		 * Retrieves total number of rows in the table list
		 * @return Rows in model
		 */
		@Override
		public int getRowCount() {
			int rowCount = 0;
			if (gameList != null)
				rowCount = gameList.size();
			return rowCount;
		}

		/**
		 * Gets number of available columns
		 * @return count of columns
		 */
		@Override
		public int getColumnCount() {
			return header.length;
		}

		/**
		 * Retrieves a cell's value
		 * @param rowIndex Row to look at
		 * @param columnIndex Column to look at
		 * @return A cell value
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object result = null; 
			
			if (columnIndex == FIELD_GAME_INCLUDED) {
				result = new Boolean(gameIncluded[rowIndex]);
			} else {
				Game selectedGame = gameList.get(rowIndex);
				switch (columnIndex) {
				case FIELD_GAME_ID:
					result = selectedGame.getItemNumber();
					break;
				case FIELD_GAME_NAME:
					result = selectedGame.getProductName();
					break;
				}
			}
			return result;
		}
		
		/**
		 * Gets a column name
		 * @param columnIndex The column to get the name for
		 * @return A column name
		 */
		@Override
		public String getColumnName(int columnIndex) {
			return header[columnIndex];
		}
		
		/**
		 * Retrieves the type of class used for each column
		 * @param columnIndex The column to get the name for
		 * @return The type of data returned in this column 
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> type = null; 
			
			switch (columnIndex) {
			case FIELD_GAME_INCLUDED:
				type = Boolean.class;
				break;
			case FIELD_GAME_ID:
				type = Integer.class;
				break;
			case FIELD_GAME_NAME:
				type = String.class;
				break;
			}
			
			return type; 
		}
		
		/**
		 * Checks if the column can be editted. Only the checkbox (selection method) 
		 * can be modified in this model. 
		 * 
		 * @param rowIndex The row to check for editing rights
		 * @param columnIndex The column to check for editing rights
		 * @return true if field can be editted, false otherwise. 
		 */
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			boolean isEditable = false; 
			
			if (columnIndex == FIELD_GAME_INCLUDED)
				isEditable = true;
			
			return isEditable;
		}
		
		/**
		 * Changes the cell value. only allows game included checkbox column to 
		 * be changed. 
		 * @param newValue The value to set the cell to
		 * @param rowIndex The row to edit
		 * @param columnIndex the column to edit
		 */
		@Override
		public void setValueAt(Object newValue, int rowIndex, int columnIndex) {
			if (columnIndex == FIELD_GAME_INCLUDED) {
				gameIncluded[rowIndex] = (Boolean) newValue;
			}
		}
		
	}
}
