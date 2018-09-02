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
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.ryorke.database.AccessoryEntityManager;
import com.ryorke.database.ConsoleEntityManager;
import com.ryorke.database.GameEntityManager;
import com.ryorke.entity.Accessory;
import com.ryorke.entity.Console;
import com.ryorke.entity.Game;
import com.ryorke.entity.Item;
import com.ryorke.entity.User;

/**
 * Inventory management frame will create a new view frame
 * for add/editing inventory items based on the type of item 
 * received. 
 * 
 * @author Russell Yorke
 */
@SuppressWarnings("serial")
public class ItemEditorDialog extends JDialog {
	private final static String WINDOW_TITLE = "Item Editor";
	
	// Window controls
	private JPanel genericItemPanel;
	private JPanel itemSpecificPanel;
	private JButton deleteItem;
	private JButton saveItem;
	private JButton cancel;
	
	private Item item;	
	private User activeUser;
	private boolean saved = false;	// Flag to indicate that user saved the item
	
	// TODO: Load manufacture list (in ItemPanel)
	
	/**
	 * Provides confirmation if the user saved the item being edited or 
	 * cancelled. 
	 * 
	 * @return true if item saved to the database, false otherwise. 
	 */
	public boolean wasSaved() {
		return saved;
	}
	
	/**
	 * Create a new item editor based on the given item
	 * 
	 * @param item An item to edit. Pass null to create a new item
	 */
	public ItemEditorDialog(Frame owner, Item item, User activeUser) {
		super(owner, WINDOW_TITLE, true);	// Modal dialog always
		this.item = item;	// TODO: Perform clone operation so we can modify this in place
		this.activeUser = activeUser;
		saved = false;
		initializeView();
	}
	
	/**
	 * Initializes view with a generic item editor 
	 * and a item specific editor based on the type
	 * of item set
	 */
	private void initializeView() {
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());		
		
		JPanel editorPanel = new JPanel(new GridLayout(1,2));
		genericItemPanel = new ItemPanel(item);		
		editorPanel.add(genericItemPanel);
		itemSpecificPanel = null;
		if (item instanceof Game) {
			itemSpecificPanel = new GamePanel((Game)item);			
		} else if (item instanceof Accessory) {
			itemSpecificPanel = new AccessoryPanel((Accessory)item);			
		} else if (item instanceof Console) {
			itemSpecificPanel = new ConsolePanel((Console)item);			
		}
		if (itemSpecificPanel != null) {
			editorPanel.add(itemSpecificPanel);
		}
		contentPane.add(editorPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new GridLayout(1,3));		
		
		saveItem = new JButton("Save");
		saveItem.setMnemonic(KeyEvent.VK_V);
		saveItem.addActionListener(new ActionListener() {
			/**
			 * Executes the save operation when save button 
			 * clicked. 
			 * 
			 * @param e Source of event
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				performSave();				
			}
		});
		buttonPanel.add(saveItem);
		
		
		if (activeUser.isAdministrator()) {
			deleteItem = new JButton("Delete");
			deleteItem.setMnemonic(KeyEvent.VK_I);		
			buttonPanel.add(deleteItem);
		}		
		
		
		
		cancel = new JButton("Cancel");
		cancel.setMnemonic(KeyEvent.VK_A);
		cancel.addActionListener(new ActionListener() {
			/**
			 * Dismiss editor without saving any changes
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				dispatchEvent(new WindowEvent(ItemEditorDialog.this, WindowEvent.WINDOW_CLOSING));				
			}
		});
		buttonPanel.add(cancel);
		
		contentPane.add(buttonPanel, BorderLayout.SOUTH);		
		setMinimumSize(new Dimension(875, 360));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	/**
	 * Attempts to save the item and if no errors occur the dialog is closed. 
	 * If an error occurs, the user will be notified. 
	 */
	public void performSave() {
		String error = "Unable to save %s.\nReason:\n%s";
		String title = "Problem saving";
		int windowOptions = JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE;
		saved = false;	// Always reset just in case dialog is shown multiple times in future
		
		if (item instanceof Accessory) {			
			try {
				AccessoryEntityManager entityManager = AccessoryEntityManager.getManager();
				entityManager.addAccessory((Accessory)item);
				saved = true;
			} catch (SQLException | IOException exception) {
				JOptionPane.showMessageDialog(this, String.format(error, "accessory", exception.getMessage()), 
						title, windowOptions);
			}
		} else if (item instanceof Console) {
			try {
				ConsoleEntityManager entityManager = ConsoleEntityManager.getManager();
				entityManager.addConsole((Console)item);
				saved = true;
			} catch (SQLException | IOException exception) {
				JOptionPane.showMessageDialog(this, String.format(error, "console", exception.getMessage()), 
						title, windowOptions);
			}
		} else if (item instanceof Game) {
			try {
				GameEntityManager entityManager = GameEntityManager.getManager();
				entityManager.addGame((Game)item);
				saved = true;
			} catch (SQLException | IOException exception) {
				JOptionPane.showMessageDialog(this, String.format(error, "game", exception.getMessage()), 
						title, windowOptions);
			}
		} else {
			// Should not be possible to reach this code unless a new item type has been
			// created and not handled here. 
			assert(false): "performSave unable to complete due to unknown item type.";			
		}
		
		if (wasSaved()) {
			this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
	}
	
}
