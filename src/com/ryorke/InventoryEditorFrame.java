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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.ryorke.entity.Accessory;
import com.ryorke.entity.Console;
import com.ryorke.entity.Game;
import com.ryorke.entity.Item;

/**
 * Inventory management frame will create a new view frame
 * for add/editing inventory items based on the type of item 
 * received. 
 * 
 * @author Russell Yorke
 */
@SuppressWarnings("serial")
public class InventoryEditorFrame extends JFrame {
	private final static String WINDOW_TITLE = "Item Editor";
	
	// Window controls
	private JPanel genericItemPanel;
	private JPanel itemSpecificPanel;
	private JButton deleteItem;
	private JButton updateItem;
	private JButton cancel;
	
	// Item information
	private Item item; 
	
	/**
	 * Creates a new item editor without an item
	 * to be used when creating a new item
	 */
	public InventoryEditorFrame() {
		this(null);
	}
	
	/**
	 * Create a new item editor based on the given item
	 * 
	 * @param item An item to edit. Pass null to create a new item
	 */
	public InventoryEditorFrame(Item item) {
		this.item = item;
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
		deleteItem = new JButton("Delete item");
		deleteItem.setMnemonic(KeyEvent.VK_I);
		updateItem = new JButton("Update item");
		deleteItem.setMnemonic(KeyEvent.VK_T);
		cancel = new JButton("Cancel");
		cancel.setMnemonic(KeyEvent.VK_A);
		cancel.addActionListener(new ActionListener() {
			/**
			 * Dismiss editor without saving any changes
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				dispatchEvent(new WindowEvent(InventoryEditorFrame.this, WindowEvent.WINDOW_CLOSING));				
			}
		});
		buttonPanel.add(updateItem);
		buttonPanel.add(deleteItem);
		buttonPanel.add(cancel);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		
		setMinimumSize(new Dimension(875, 360));
		setTitle(InventoryEditorFrame.WINDOW_TITLE + ((item == null) ? " - New Item" : " - " + item.getProductName()));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
}
