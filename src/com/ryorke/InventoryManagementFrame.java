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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import com.ryorke.entity.Accessory;
import com.ryorke.entity.Console;
import com.ryorke.entity.Game;
import com.ryorke.entity.Item;
import com.ryorke.entity.PackageDimension;

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
	
	private boolean promptOnClose = true; 
	
	/**
	 * Controls
	 */
	private JTextField filterInventoryQuery;
	private JButton filter;
	private JButton addInventoryItem;
	private JButton editInventoryItem;
	private JButton deleteInventoryItem;
	private JTable inventoryTable; 
	
	/**
	 * Inventory data
	 * TODO:
	 * 		Connect data models into database
	 */
	ArrayList<Item> inventory = new ArrayList<Item>();
	
	/**
	 * Creates a new inventory management window with a default
	 * title
	 */
	public InventoryManagementFrame() {
		this(InventoryManagementFrame.WINDOW_TITLE);
	}
	
	/**
	 * Creates a new inventory management window with the specified
	 * title
	 * @param title Window title
	 */
	public InventoryManagementFrame(String title) {
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		
		JMenuBar mainMenu = createMainMenu(); 
		setJMenuBar(mainMenu);		
		
	    JPanel filterControls = createFilterControls();
	    contentPane.add(filterControls, BorderLayout.NORTH);
	    
	    JScrollPane inventoryView = createInventoryTable();
	    contentPane.add(inventoryView, BorderLayout.CENTER);
	    
	    JPanel inventoryButtons = createInventoryButtons();
	    contentPane.add(inventoryButtons, BorderLayout.SOUTH);
	    
		setSize(new Dimension(800, 600));
		setLocationRelativeTo(null);
		setTitle(title);
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
							"Select an action:", "Closing", JOptionPane.YES_NO_CANCEL_OPTION, 
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
	 * Prompts user for item type and creates new item based
	 * on selection
	 * 
	 * @return A new item unless user cancelled.
	 */
	private Item createNewItem() {		
		String[] availableTypes = { "Video Game", "Accessory", "Console" };
		String message = "What type of item?";
		String title = "Item Selection";		
		
		String response = (String) JOptionPane.showInputDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE, null, availableTypes, availableTypes[0]);
		
		Item item = null;
		if (response == availableTypes[0]) {
			item = new Game();
		} else if (response == availableTypes[1]) {
			item = new Accessory();
		} else if (response == availableTypes[2]) {
			item = new Console();
		}
		
		return item;
	}
	
	/**
	 * Creates a scrollable inventory table
	 * 
	 * @return Configured inventory table
	 */
	private JScrollPane createInventoryTable() {
		// TODO: Remove sample data
		// ============= SAMPLE DATA TO BE REMOVED ====================
		inventory.add(new Game(1, "God of War", "A brand new game with remastered content",
				250, 79.99, "Santa Monica Studios", new GregorianCalendar(2018, 12, 31).getTime(), new PackageDimension(4, 5, 6, 1), 
				4, 1, 1, "M - Mature 17+"));
		inventory.add(new Accessory(2, "Wireless Beats", "Simply wireless headphones", 200, 49.95, "Apple", new GregorianCalendar(2019, 07, 15).getTime(), 
				new PackageDimension(3, 4, 6, 5.7F),  "White", "MF-A1234", 1));
		inventory.add(new Console(3, "Playstation 4", "Next generation console", 100, 499.99, "Sony", new GregorianCalendar(2013, 12, 15).getTime(), 
				new PackageDimension(6, 17, 15, 25),  "Black", "1TB", "SPCH-1000", null, 2));

		String [] header = {"Item Number",
				"Product Name",
				"Product Description", 
				"Item Type",
				"Units in Stock", 
				"Unit Cost", 
				"Manufacture",
				"Release Date"};
		
		Object[][] tableData = new Object[inventory.size()][header.length];
		
		for (int inventoryItem = 0; inventoryItem < inventory.size(); inventoryItem++) {
			Item item = inventory.get(inventoryItem);
			String itemType = "";
			if (item instanceof Game) {
				itemType = "Video Game";
			} else if (item instanceof Accessory) {
				itemType = "Accessory";
			} else if (item instanceof Console) {
				itemType = "Console";
			}
			
			SimpleDateFormat dateFormatter = (SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
			dateFormatter.applyPattern("YYYY/MM/dd");		
			DecimalFormat currencyFormat = new DecimalFormat("#,###0.00");
			
			int headerCounter = 0;
			tableData[inventoryItem][headerCounter++] = item.getItemNumber();
			tableData[inventoryItem][headerCounter++] = item.getProductName();			
			tableData[inventoryItem][headerCounter++] = item.getProductDescription();			
			tableData[inventoryItem][headerCounter++] = itemType;
			tableData[inventoryItem][headerCounter++] = item.getUnitsInStock();
			tableData[inventoryItem][headerCounter++] = currencyFormat.format(item.getUnitCost());
			tableData[inventoryItem][headerCounter++] = item.getManufacture();
			tableData[inventoryItem][headerCounter++] = dateFormatter.format(item.getReleaseDate());
	
		}
		// ============= END OF SAMPLE DATA ===========================
	
		JScrollPane tableScroller = new JScrollPane();		
		inventoryTable = new JTable(tableData, header);
		tableScroller.setViewportView(inventoryTable);
		inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		inventoryTable.setRowSelectionInterval(0, 0);
		inventoryTable.setCellSelectionEnabled(false);
		inventoryTable.setRowSelectionAllowed(true);
		
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
		editManageDB.addActionListener(new ActionListener() {
			/** 
			 * Display database management window
			 * 
			 *  @param e event information 
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				new InventoryDBManagementFrame();
			}
			
		});
		editManageUsers.setMnemonic(KeyEvent.VK_U);
		editManageUsers.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
		editManageUsers.addActionListener(new ActionListener() {
			/** 
			 * Display user management window
			 * 
			 * @param e event information
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				new UserManagementFrame();
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
						"Inventory Management Database\nAuthor: Russell Yorke\nVersion: 0.3", 
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
		filter = new JButton("Filter");
		
		filterLabel.setLabelFor(filterInventoryQuery);
		filterLabel.setDisplayedMnemonic(KeyEvent.VK_I);
		filter.setMnemonic(KeyEvent.VK_T);
		
		filterPanel.add(filterLabel, BorderLayout.WEST);
		filterPanel.add(filterInventoryQuery, BorderLayout.CENTER);
		filterPanel.add(filter, BorderLayout.EAST);
		
		return filterPanel;		
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
		editInventoryItem = new JButton("Edit Item");
		deleteInventoryItem = new JButton("Delete Item");
		
		addInventoryItem.setMnemonic(KeyEvent.VK_A);
		editInventoryItem.setMnemonic(KeyEvent.VK_D);
		deleteInventoryItem.setMnemonic(KeyEvent.VK_L);
		
		buttonPanel.add(addInventoryItem);
		buttonPanel.add(editInventoryItem);
		buttonPanel.add(deleteInventoryItem);
		
		
		addInventoryItem.addActionListener(new ActionListener() {
			
			/**
			 * Creates a new blank editor to create a 
			 * new item
			 * 
			 * @param e action event details
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				Item item = createNewItem();
				if (item != null) {
					new InventoryEditorFrame(item);
				}				
			}
		});
		editInventoryItem.addActionListener(new ActionListener() {
			
			/**
			 * Creates a new editor with the selected item
			 * 
			 * @param e action event details
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedRow = inventoryTable.getSelectedRow();								
				new InventoryEditorFrame(inventory.get(selectedRow));				
			}
		});
		
		deleteInventoryItem.addActionListener(new ActionListener() {
			/**
			 * Deletes the selected item
			 * 
			 * @param e action event details
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(InventoryManagementFrame.this, "Delete Item action not yet implemented", "Deleting Item", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		return buttonPanel;
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
}
