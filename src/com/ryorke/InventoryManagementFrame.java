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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.regex.PatternSyntaxException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;

import com.ryorke.database.AccessoryEntityManager;
import com.ryorke.database.ConsoleEntityManager;
import com.ryorke.database.GameEntityManager;
import com.ryorke.entity.Accessory;
import com.ryorke.entity.Console;
import com.ryorke.entity.Game;
import com.ryorke.entity.Item;
import com.ryorke.entity.User;
import com.ryorke.entity.exception.InvalidUserAttributeException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.JLabel;

/**
 * Main application view for displaying inventory and filtering the inventory. 
 * Displays the main menu for managing users and for perform database 
 * maintenance.   
 * 
 * @author Russell Yorke
 */
@SuppressWarnings("serial")
public class InventoryManagementFrame extends JFrame {
	public final static String WINDOW_TITLE = "Inventory Manager";
	private User authenticatedUser = null;
	private boolean promptOnClose = true; 
	private InventoryTableModel inventoryTableModel = null; 

	private JTextField filterInventoryQuery;
	private JButton filter;
	private JButton filterClear;
	private JButton addInventoryItem;
	private JButton editInventoryItem;
	private JButton deleteInventoryItem;
	private JTable inventoryTable; 
	
	/**
	 * Creates a new inventory management window with a default
	 * title
	 * @param authenticatedUser A reference to the user that logged into the system
	 */
	public InventoryManagementFrame(User authenticatedUser) {
		this(InventoryManagementFrame.WINDOW_TITLE, authenticatedUser);
	}
	
	/**
	 * Creates a new inventory management window with the specified
	 * title
	 * @param title Window title
	 * @param authenticatedUser A reference to the user that logged into the system
	 */
	public InventoryManagementFrame(String title, User authenticatedUser) {
		this.authenticatedUser = authenticatedUser;
		
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		
		JMenuBar mainMenu = createMainMenu(); 
		setJMenuBar(mainMenu);		
			    
	    JScrollPane inventoryView = createInventoryTable();
	    contentPane.add(inventoryView, BorderLayout.CENTER);
	    
	    // Requires this ordering as this creates an event listener
	    // based on data from the inventoryView
	    JPanel filterControls = createFilterControls();
	    contentPane.add(filterControls, BorderLayout.NORTH);
	    
	    JPanel inventoryButtons = createInventoryButtons();
	    contentPane.add(inventoryButtons, BorderLayout.SOUTH);
	    
		setSize(new Dimension(800, 600));
		setLocationRelativeTo(null);
		setTitle(String.format("%s (Logged in as %s)", title, authenticatedUser.getUsername()));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setVisible(true);		
		
		// Event handlers
		addWindowListener(new WindowListener() {
			/**
			 * Manages the window being closed (either by closing window or by menu items)
			 * and prompts user (when necessary) if they want to logout or exit the application. 
			 * If exit/logout is not desired this event will be cancelled.
			 * 
			 *  @param e Window event information
			 */
			@Override
			public void windowClosing(WindowEvent e) {
				final int LOGOUT_ACTION = 0;
				final int EXIT_ACTION = 1;				
				final int CANCEL_ACTION = 2;
				 				
				if (promptOnClose) {
					String[] options = {"Logout", "Exit", "Cancel"};
					int response = JOptionPane.showOptionDialog(InventoryManagementFrame.this, 
							"Select an action:", "Application exiting", JOptionPane.YES_NO_CANCEL_OPTION, 
							JOptionPane.QUESTION_MESSAGE, null, options, options[CANCEL_ACTION]);
					
					if (response == LOGOUT_ACTION) {
						logout();
					} else if (response == EXIT_ACTION) {
						closeWindow(true);
					}
				} else {
					// Only exit if we don't set the depose option which indicates we 
					// want to return to the login window
					if (InventoryManagementFrame.this.getDefaultCloseOperation() != JFrame.DISPOSE_ON_CLOSE) {
						System.exit(0);
					}
				}
			}
			
			/**
			 * Not implemented
			 * 
			 * @param e Window event information
			 */
			@Override
			public void windowOpened(WindowEvent e) {}
			
			/**
			 * Not implemented
			 * 
			 * @param e Window event information
			 */
			@Override
			public void windowIconified(WindowEvent e) {}
			
			/**
			 * Not implemented
			 * 
			 * @param e Window event information
			 */
			@Override
			public void windowDeiconified(WindowEvent e) {}
			
			/**
			 * Not implemented
			 * 
			 * @param e Window event information
			 */
			@Override
			public void windowDeactivated(WindowEvent e) {}
			
			/**
			 * Not implemented
			 * 
			 * @param e Window event information
			 */
			@Override
			public void windowClosed(WindowEvent e) {}
			
			/**
			 * Not implemented
			 * 
			 * @param e Window event information
			 */
			@Override
			public void windowActivated(WindowEvent e) {}
		});
	}
	
	/**
	 * Displays new item dialog. If user saves the item the inventory table
	 * will be updated with the new item. 
	 */
	private void showNewItemDialog() {
		Item newItem = selectItemType();
		
		if (newItem != null) {
			ItemEditorDialog editor = new ItemEditorDialog(this, newItem);
			editor.setVisible(true);
			
			if (editor.wasSaved()) {
				inventoryTableModel.addRow(newItem);
			}
		}
	}
	
	/**
	 * Shows the edit item dialog based on the selected item. 
	 */
	private void showEditItemDialog() {
		// Get the model data row (convert the view row to the model)
		int selectedRow = inventoryTable.getSelectedRow();		
		selectedRow = inventoryTable.convertRowIndexToModel(selectedRow);
		if (selectedRow > -1) {
			ItemEditorDialog editor = new ItemEditorDialog(InventoryManagementFrame.this, inventoryTableModel.getRow(selectedRow));
			editor.setVisible(true);
			
			if (editor.wasSaved()) {
				inventoryTableModel.fireTableRowsUpdated(selectedRow, selectedRow);
			}
		} else {
			JOptionPane.showMessageDialog(this, "No item selected to be edited. Select an item and try again.", 
					"No item selected", JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Promotes user for an item type to be created and generates a new default item of that type. 
	 * 
	 * @return A empty item or null if cancelled
	 */
	private Item selectItemType() {		
		String[] availableTypes = { "Accessory", "Console", "Video Game" };
		String message = "What type of item?";
		String title = "Create new item";		
		
		String response = (String) JOptionPane.showInputDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE, null, availableTypes, availableTypes[0]);
		
		Item item = null;
		if (response == availableTypes[0]) {
			item = new Accessory();			
		} else if (response == availableTypes[1]) {
			item = new Console();
		} else if (response == availableTypes[2]) {			
			item = new Game();
		}
		return item;
	}
	
	/**
	 * Creates a scrollable inventory table. If an error occurs while attempting
	 * to load the inventory into the table, the application will display an error 
	 * and exit. 
	 * 
	 * @return Configured inventory table
	 */
	private JScrollPane createInventoryTable() {
		try {
			inventoryTableModel = new InventoryTableModel();
		} catch (SQLException | IOException | ParseException exception) {
			String errorMessage = String.format("An error occured while attempting to load "
					+ "the inventory from the database.\nThe application will now close.\nContact your system "
					+ "administrator if the problem persists.\nReason:\n%s", exception.getMessage());
			String errorTitle = "Unable to load inventory";
			int windowOptions = JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE;
			JOptionPane.showMessageDialog(this, errorMessage, errorTitle, windowOptions);
			exception.printStackTrace();
			System.exit(1);
		}
		
		inventoryTable = new JTable(inventoryTableModel);		
		inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		inventoryTable.setRowSorter(inventoryTableModel.getTableSorter());
		inventoryTable.addMouseListener(new MouseListener() {

			/**
			 * Begins editing an item when double clicked
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && !e.isConsumed()) {
					showEditItemDialog();
				}				
			}
			
			/*
			 * (non-Javadoc)
			 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseReleased(MouseEvent e) {}
			
			/*
			 * (non-Javadoc)
			 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
			 */
			@Override
			public void mousePressed(MouseEvent e) {}
			
			/*
			 * (non-Javadoc)
			 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseExited(MouseEvent e) {}
			
			/*
			 * (non-Javadoc)
			 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseEntered(MouseEvent e) {}
			
		});
		
		
		JScrollPane tableScroller = new JScrollPane(inventoryTable);
		
		return tableScroller;
	}

	/**
	 * Creates the applications main menu bar
	 * @return Configure menu bar
	 */
	private JMenuBar createMainMenu() {
		JMenuBar mainMenu = new JMenuBar();
		
		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		mainMenu.add(file);
		
		JMenuItem fileLogout = new JMenuItem("Logout");
		fileLogout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		fileLogout.setMnemonic(KeyEvent.VK_L);
		file.add(fileLogout);
		fileLogout.addActionListener(new ActionListener() {
			/**
			 * Logs user out of application and returns them
			 * to the login screen
			 * @param e event information
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				logout();
			}
		});
		
		JMenuItem fileExit = new JMenuItem("Exit");
		fileExit.setMnemonic(KeyEvent.VK_X);
		fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		file.add(fileExit);
		fileExit.addActionListener(new ActionListener() {
			/**
			 * Exits the application without prompts
			 * @param e event information
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				closeWindow(true);
			}
		});
		
		JMenu edit = new JMenu("Edit");
		JMenuItem editManageDB = new JMenuItem("Manage Database");
		JMenuItem editManageUsers = new JMenuItem("Manage Users");
		edit.setMnemonic(KeyEvent.VK_E);
		editManageDB.setMnemonic(KeyEvent.VK_D);
		editManageDB.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		editManageDB.setEnabled(authenticatedUser.isAdministrator());
		editManageDB.addActionListener(new ActionListener() {
			/** 
			 * Display database management window
			 * 
			 *  @param e event information 
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				new InventoryDBManagementFrame(InventoryManagementFrame.this).setVisible(true);
			}
			
		});
		editManageUsers.setMnemonic(KeyEvent.VK_U);
		editManageUsers.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
		editManageUsers.setEnabled(authenticatedUser.isAdministrator());
		editManageUsers.addActionListener(new ActionListener() {
			/** 
			 * Display user management window
			 * 
			 * @param e event information
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					UserManagementDialog userManagement = new UserManagementDialog(InventoryManagementFrame.this);
					userManagement.setVisible(true);
				}   catch (SQLException | IOException exception) {
					String errorMessage = String.format("Unable to retrieve users from the database.\n\nReason:\n%s", exception.getMessage());
					int displayOptions = JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE;
					JOptionPane.showMessageDialog(null, errorMessage, "Failed to retrieve users", displayOptions);
				} catch (InvalidUserAttributeException invalidUserAttributeException) {
					// This catch should only occur if database modifications are performed outside of this
					// application. 
					String errorMessage = String.format("User databse table corrupted. Unable to retrieve users. Contact system administrator for further assistance.\n\n"
							+ "Response:\n%s", invalidUserAttributeException.getMessage());
					int displayOptions = JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE;
					JOptionPane.showMessageDialog(null, errorMessage, "Failed to retrieve users", displayOptions);
					invalidUserAttributeException.printStackTrace();
				}
			}
		});
		edit.add(editManageDB);
		edit.add(editManageUsers);
		mainMenu.add(edit);	
		
		JMenu help = new JMenu("Help");
		JMenuItem helpAbout = new JMenuItem("About");
		help.setMnemonic(KeyEvent.VK_H);
		helpAbout.setMnemonic(KeyEvent.VK_A);
		helpAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		help.add(helpAbout);
		mainMenu.add(help);
		helpAbout.addActionListener(new ActionListener() {
			
			/**
			 * Displays about dialog
			 * 
			 * @param e Event information
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(InventoryManagementFrame.this, 
						"Inventory Management Database\nAuthor: Russell Yorke\nVersion: 0.5", 
						"About", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		
		return mainMenu;
	}
	
	/**
	 * Creates a new panel containing the inventory table filter
	 * controls. 
	 * 
	 * @return A panel containing the filter controls
	 */
	private JPanel createFilterControls() {
		BorderLayout layoutManager = new BorderLayout();
		layoutManager.setHgap(5);
		layoutManager.setVgap(5);		
		JPanel filterPanel = new JPanel(layoutManager);
		filterPanel.setBorder(BorderFactory.createLineBorder(filterPanel.getBackground(), 5));
		
		
		JLabel filterLabel = new JLabel("Filter Query:");
		filterInventoryQuery = new JTextField();
		filterInventoryQuery.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				applyFilter();				
			}
		});
		filter = new JButton("Filter");
		filter.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				applyFilter();
			}
		});
		filterClear = new JButton("Clear Filter");
		filterClear.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				inventoryTableModel.getTableSorter().setRowFilter(null);				
			}
		});
		
		filterLabel.setLabelFor(filterInventoryQuery);
		filterLabel.setDisplayedMnemonic(KeyEvent.VK_I);
		filter.setMnemonic(KeyEvent.VK_T);
		
		filterPanel.add(filterLabel, BorderLayout.WEST);
		filterPanel.add(filterInventoryQuery, BorderLayout.CENTER);
		JPanel buttonContainer = new JPanel(new GridLayout(1, 2));
		buttonContainer.add(filter);
		buttonContainer.add(filterClear);
		filterPanel.add(buttonContainer, BorderLayout.EAST);
		
		return filterPanel;		
	}
	
	/**
	 * Filters the table by applying applying the regex against
	 * the item name and/or description columns. 
	 */
	private void applyFilter() {
		String filterExpression = filterInventoryQuery.getText();
		
		if (filterExpression.length() > 0) {
			TableRowSorter<InventoryTableModel> sorter = inventoryTableModel.getTableSorter();
			 
			
			try {
				RowFilter<InventoryTableModel, Integer> appliedFilter = RowFilter.regexFilter(filterExpression, 
					InventoryTableModel.ITEM_DESCRIPTION, InventoryTableModel.ITEM_NAME);
				sorter.setRowFilter(appliedFilter);
			} catch (PatternSyntaxException syntaxError) {
				JOptionPane.showMessageDialog(InventoryManagementFrame.this, "Invalid filter provided", "Filter error", 
						JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(InventoryManagementFrame.this, "No filter provide.", "Invalid filter",
					JOptionPane.OK_OPTION|JOptionPane.INFORMATION_MESSAGE);
		}
	}
	/**
	 * Creates a set of buttons for modifying a selected
	 * inventory item
	 * @return A panel containing the buttons
	 */
	private JPanel createInventoryButtons() {
		GridLayout layoutManager = new GridLayout(1,  3);
		JPanel buttonPanel = new JPanel(layoutManager);
		
		addInventoryItem = new JButton("Add Item");
		addInventoryItem.setMnemonic(KeyEvent.VK_A);
		addInventoryItem.addActionListener(new ActionListener() {
			
			/**
			 * Creates a new blank editor to create a 
			 * new item
			 * 
			 * @param e action event details
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				showNewItemDialog();				
			}
		});
		buttonPanel.add(addInventoryItem);
		
		editInventoryItem = new JButton("Edit Item");
		editInventoryItem.setMnemonic(KeyEvent.VK_D);
		editInventoryItem.addActionListener(new ActionListener() {
			
			/**
			 * Creates a new editor with the selected item
			 * 
			 * @param e action event details
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				showEditItemDialog();				
			}
		});
		buttonPanel.add(editInventoryItem);
		
		if (authenticatedUser.isAdministrator()) {
			deleteInventoryItem = new JButton("Delete Item");		
			deleteInventoryItem.setMnemonic(KeyEvent.VK_L);
			deleteInventoryItem.addActionListener(new ActionListener() {
				/**
				 * Deletes the selected item
				 * 
				 * @param e action event details
				 */
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteSelectedItem();
				}
			});
			buttonPanel.add(deleteInventoryItem);
		}
		
		return buttonPanel;
	}
	
	/**
	 * Deletes the selected item from the table and database
	 */
	public void deleteSelectedItem() {
		int selectedItemIndex = inventoryTable.getSelectedRow();
		selectedItemIndex = inventoryTable.convertRowIndexToModel(selectedItemIndex);
		String message;
		String title;
		int options;
		Item selectedItem; 
		
		if (selectedItemIndex >= 0) {
			selectedItem = inventoryTableModel.getRow(selectedItemIndex);
			message = String.format("About to delete \"%s\" item. Are you sure?", selectedItem.getProductName());
			title = "Delete item?";
			options = JOptionPane.YES_NO_OPTION;
			
			int response = JOptionPane.showConfirmDialog(this, message, title, options);
			
			if (response == JOptionPane.YES_OPTION) {
				try {					
					boolean performDelete = true;
					
					if (selectedItem instanceof Game && gameIncludedWithConsole((Game)selectedItem)) {
						performDelete = false;
						JOptionPane.showMessageDialog(this, 
								"Unable to delete selected game. This game is included with a console and must be excluded before continuing.", 
								"Deletion aborted", JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE);
					}
					
					if (performDelete) {
						inventoryTableModel.deleteRow(selectedItemIndex);
					}
				} catch (SQLException | IOException | ParseException exception) {
					String errorMessage = String.format("Unable to delete selected item. Ensure that no items refer to this item before attempting to delete it.\n\nReason:\n%s", exception.getMessage());
					JOptionPane.showMessageDialog(this, errorMessage, "Item deletion failed", 
							JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE);
				}
			}
		} else {
			message = "An item must be selected before performing a delete operation.";
			title = "Delete failed";
			options = JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE;
			
			JOptionPane.showMessageDialog(this, message, title, options);			
		}
		
	}
	
	
	/**
	 * Performs check to determine if the Game is a game included with a console
	 * 
	 * @param game An existing game object
	 * @return True if game is included as part of a console, false otherwise
	 * @throws SQLException If a database error occurs
	 * @throws IOException If database cannot access the database file
	 * @throws ParseException If an error occurs in parsing data from the database
	 */
	private boolean gameIncludedWithConsole(Game game) throws SQLException, IOException, ParseException {
		boolean gameReferencedByConsole = false;

		ConsoleEntityManager consoleManager = ConsoleEntityManager.getManager();
		ArrayList<Console> consoles = consoleManager.getConsoles();
		for (Console console : consoles) {
			if (console.getIncludedGameId() != null) {
				for (int includedGameId : console.getIncludedGameId()) {
					if (includedGameId == game.getItemNumber()) {
						gameReferencedByConsole = true;
						break;
					}
				}
			}
				
			if (gameReferencedByConsole)
				break;
		}
		
		return gameReferencedByConsole; 
	}
	
	/**
	 * Invalidates user session returning to the authentication screen
	 */
	private void logout() {
		new AuthenticationFrame();
		
		closeWindow(false);
	}
	
	/**
	 * Closing the window allowing the application to exit if this
	 * is the last open window. Prompting is disabled.
	 */
	private void closeWindow(boolean exitApplication) {
		if (!exitApplication)
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		promptOnClose = false;
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
	
	/**
	 * Manages the inventory dispalyed in a JTable including pulling in the data
	 * from a database, added to a database, and deletes items. 
	 * 
	 * @author Russell Yorke
	 *
	 */
	class InventoryTableModel extends AbstractTableModel {
		/**
		 * Field identifiers for columns within the table
		 */
		private final static int ITEM_ID = 0;
		private final static int ITEM_NAME = 1;
		private final static int ITEM_DESCRIPTION = 2;
		private final static int ITEM_TYPE = 3;
		private final static int ITEM_UNITS_IN_STOCK = 4;
		private final static int ITEM_UNIT_COST = 5;
		private final static int ITEM_MANUFACTURE = 6;
		private final static int ITEM_RELEASSE_DATE = 7;
		
		private String[] header = { "Item Number", "Name", "Description", "Type", "Units in Stock", "Unit Cost", "Manufacture", "Release Date" };
		private ArrayList<Item> inventoriedItems = new ArrayList<Item>();
		
		private ConsoleEntityManager consoleManager;
		private AccessoryEntityManager accessoryManager;
		private GameEntityManager gameManager;
		
		private TableRowSorter<InventoryTableModel> tableSorter;
		
		/**
		 * Populates the table inventory from the database
		 * 
		 * @throws IOException If database file cannot be accessed
		 * @throws SQLException If a database error occurred
		 * @throws ParseException If the database contains an invalid date format item
		 */
		public InventoryTableModel() throws IOException, SQLException, ParseException {
			consoleManager = ConsoleEntityManager.getManager();
			accessoryManager = AccessoryEntityManager.getManager();
			gameManager = GameEntityManager.getManager();
			
			ArrayList<Console> consoles = consoleManager.getConsoles();
			if (consoles != null)
				inventoriedItems.addAll(consoles);
			
			ArrayList<Accessory> accessories = accessoryManager.getAccessories();
			if (accessories != null)
				inventoriedItems.addAll(accessories);
			
			ArrayList<Game> games = gameManager.getGames();
			if (games != null)
				inventoriedItems.addAll(games);
			
			// Attach table sorter
			setupTableSorter();
		}
		
		/**
		 * Creates a default TableRowSorter and then overrides
		 * a select handful of columns that do not have comparator
		 * implementations available. This will provide the custom
		 * comparator actions to ensure sort order is valid.
		 * 
		 * NOTE: Due to the formatters displaying text custom implementations
		 * are required to convert the data back into its raw format. 
		 */
		private void setupTableSorter() {
			tableSorter = new TableRowSorter<>(this);
			
			// Converts units in to a number for sorting
			tableSorter.setComparator(ITEM_UNITS_IN_STOCK, new Comparator<String>() {
				@Override
				public int compare(String unitsInStockString1, String unitsInStockString2) {
					String removeCharacters = "[$,]";
					Integer unitsInStock1 = new Integer(unitsInStockString1.replaceAll(removeCharacters, ""));
					Integer unitsInStock2 = new Integer(unitsInStockString2.replaceAll(removeCharacters, ""));

					return unitsInStock1.compareTo(unitsInStock2);
				}				
			});
			
			
			// Sorts unit costs. 
			tableSorter.setComparator(ITEM_UNIT_COST, new Comparator<String>() {
				@Override
				public int compare(String unitsCostString1, String unitsCostString2) {
					String removeCharacters = "[$,]";
					Double unitsCost1 = new Double(unitsCostString1.replaceAll(removeCharacters, ""));
					Double unitsCost2 = new Double(unitsCostString2.replaceAll(removeCharacters, ""));

					return unitsCost1.compareTo(unitsCost2);
				}				
			});
			
		}
		
		/**
		 * Returns the table sorter being used on this model
		 * 
		 * @return A table sorter
		 */
		public TableRowSorter<InventoryTableModel> getTableSorter() {
			return tableSorter;
		}
		
		/**
		 * Inserts a new record and updates the table
		 * 
		 * @param item New item to insert
		 */
		public void addRow(Item item) {
			inventoriedItems.add(item);
			int rowInsertedAt = inventoriedItems.size() - 1;
			fireTableRowsInserted(rowInsertedAt, rowInsertedAt);
		}
		
		/**
		 * Deletes an item from the inventory model and database
		 * 
		 * @param rowIndex The row to delete
		 * @throws IndexOutOfBoundsException If rowIndex is invalid
		 * @throws SQLException If a database error occurs.
		 */
		public void deleteRow(int rowIndex) throws IndexOutOfBoundsException, SQLException {
			Item selectedItem = inventoriedItems.get(rowIndex);
						
			if (selectedItem instanceof Accessory) {
				accessoryManager.deleteAccessory((Accessory)selectedItem);				
			} else if (selectedItem instanceof Console) {
				consoleManager.deleteConsole((Console)selectedItem);
			} else if (selectedItem instanceof Game) {
				gameManager.deleteGame((Game)selectedItem);
			}
			
			inventoriedItems.remove(selectedItem);
			fireTableRowsDeleted(rowIndex, rowIndex);			
		}
		
		
		/**
		 * Gets the Item object at the requested row
		 * @param rowIndex The item index to retrieve
		 * @return an item
		 * @throws IndexOutOfBoundsException if rowIndex is invalid
		 */
		public Item getRow(int rowIndex) throws IndexOutOfBoundsException {
			Item selectedItem = inventoriedItems.get(rowIndex);
			return selectedItem;
		}
		
		/**
		 * Counts the number of columns available
		 * @return Column count
		 */
		@Override
		public int getColumnCount() {
			return header.length;
		}

		/**
		 * Counts the total number of rows of data within the inventory
		 * 
		 * @return Total number of inventory rows
		 */
		@Override
		public int getRowCount() {
			return inventoriedItems.size();
		}

		/**
		 * Retrieve a cells values
		 * @param rowIndex The specific row to get data from
		 * @param columnIndex the specific column to get data from
		 * @return An cells value
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Item item = null; 
			Object value = null; 
			
			if (inventoriedItems.size() > rowIndex) {
				item = inventoriedItems.get(rowIndex);
				
				switch (columnIndex) {
				case ITEM_ID:
					value = item.getItemNumber();
					break;
				case ITEM_NAME:
					value = item.getProductName();
					break;
				case ITEM_DESCRIPTION:
					value = item.getProductDescription();
					break;
				case ITEM_TYPE:					
					if (item instanceof Accessory)
						value = "Accessory";
					else if (item instanceof Console)
						value = "Console";
					else if (item instanceof Game)
						value = "Game";
					else
						// Shouldn't be possible to reach this unless a new type has been created and not
						// accounted for within this model
						assert(item instanceof Accessory || item instanceof Console || item instanceof Game): "Unknown or unexpected item type found in ItemTableModel";
					
					break;
				case ITEM_UNITS_IN_STOCK:
					DecimalFormat decimalFormatter = new DecimalFormat("#,##0");
					decimalFormatter.setMaximumFractionDigits(0);
					value = decimalFormatter.format(item.getUnitsInStock());
					break;
				case ITEM_UNIT_COST:
					NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
					value = currencyFormatter.format(item.getUnitCost());
					break;
				case ITEM_MANUFACTURE:
					value = item.getManufacture();
					break;
				case ITEM_RELEASSE_DATE:
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
					Date releaseDate = item.getReleaseDate();
					value = formatter.format(releaseDate);
					break;
				}
			}
			
			return value;
		}
		
		/**
		 * Provides the name of a column
		 * @param columnIndex The column index to get the column name from
		 * @return A column name
		 */
		@Override
		public String getColumnName(int columnIndex) {			
			return header[columnIndex];
		}
		
		/**
		 * Returns the class type for each column to enable sorting
		 * of columns within the table. 
		 * 
		 * @param columnIndex The column index being sorted
		 * @return A class type to be used in sorting
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> result = Object.class; 
			if (!inventoriedItems.isEmpty()) {			
				result = getValueAt(0, columnIndex).getClass();
			}
			
			return result;
		}
		
	}
}
