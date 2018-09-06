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

@SuppressWarnings("serial")
public class GameSelectionDialog extends JDialog {	
	private GameTableModel tableModel;
	private boolean wasSaved = false; 
	
	public GameSelectionDialog(JDialog owner, int consoleId, int[] existingGameIdsIncluded) throws SQLException, IOException, ParseException {
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
	
	public boolean wasSaved() {
		return this.wasSaved;
	}
	
	public ArrayList<Game> getIncludedGames() {
		return tableModel.getIncludedGames();
	}

	public int getGameCount() {
		return tableModel.getRowCount();
	}
	
	private class GameTableModel extends AbstractTableModel {
		public static final int FIELD_GAME_INCLUDED = 0;
		public static final int FIELD_GAME_ID = 1;
		public static final int FIELD_GAME_NAME = 2;
		
		private GameEntityManager gameManager; 
		private String[] header = { "Included", "Game ID", "Name" };
		private ArrayList<Game> gameList;
		private boolean[] gameIncluded; 
		
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
		
		@Override
		public int getRowCount() {
			int rowCount = 0;
			if (gameList != null)
				rowCount = gameList.size();
			return rowCount;
		}

		@Override
		public int getColumnCount() {
			return header.length;
		}

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
		
		@Override
		public String getColumnName(int columnIndex) {
			return header[columnIndex];
		}
		
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
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			boolean isEditable = false; 
			
			if (columnIndex == FIELD_GAME_INCLUDED)
				isEditable = true;
			
			return isEditable;
		}
		
		@Override
		public void setValueAt(Object newValue, int rowIndex, int columnIndex) {
			if (columnIndex == FIELD_GAME_INCLUDED) {
				gameIncluded[rowIndex] = (Boolean) newValue;
			}
		}
		
	}
}
