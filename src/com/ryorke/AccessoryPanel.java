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
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.ryorke.database.ConsoleEntityManager;
import com.ryorke.entity.Accessory;
import com.ryorke.entity.Item;

/**
 * Main application view for displaying inventory and filtering the inventory. 
 * Displays the main menu for managing users and for perform database 
 * maintenance.   
 * 
 * @author Russell Yorke
 */
@SuppressWarnings("serial")
public class AccessoryPanel extends JPanel implements ItemEditor {
	public final static Color INVALID_INPUT = new Color(255, 200, 200);
	
	private Accessory item;
	private JTextField color;
	private JTextField modelNumber;
	private JTextField platformId;
	private ConsoleEntityManager consoleManager; 
	
	/**
	 * Creates a accessory editor panel and loads the data
	 * into the fields based on the item. 
	 * 
	 * @param item The accessory item details
	 * @throws NullPointerException If item is null
	 * @throws SQLException If a database error occurs while loading the panel
	 * @throws IOException If the system is unable to access to database file. 
	 */
	public AccessoryPanel(Accessory item) throws NullPointerException, IOException, SQLException {
		if (item == null)
			throw new NullPointerException("Accessory item cannot be null");
		setLayout(new BorderLayout());		
		setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));		
		add(createControls(), BorderLayout.CENTER);
		
		this.item = item;
		refreshFields();
		
		consoleManager = ConsoleEntityManager.getManager();
	}
	
	/**
	 * Helper to quickly add components to the panel using the GridBagLayout manager
	 * 
	 * @param panel Panel to add controls to
	 * @param layout Layout manager using GridBagLayout configurations
	 * @param constraint  Constraints (rules) for configuring each component
	 * @param component Component to add to the panel
	 */
	private void addComponent(JPanel panel, GridBagLayout layout, GridBagConstraints constraint, JComponent component) {
		layout.setConstraints(component, constraint);
		panel.add(component);				
	}
	
	/**
	 * Creates a label with a top-vertical alignment
	 * 
	 * @param text Label value
	 * @param horizontalAlignment Sets text alignment (see SwingConstants)
	 * @param mnemonic Keyboard mnemonic to use
	 * @param labelFor Associates label with a component to set focus to when mnemonic is activated
	 * @return A label
	 */
	private JLabel createJLabel(String text, int horizontalAlignment, int mnemonic, JComponent labelFor) {
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(horizontalAlignment);
		label.setVerticalAlignment(SwingConstants.TOP);		
		label.setDisplayedMnemonic(mnemonic);
		label.setLabelFor(labelFor);
		
		return label;
	}
	
	/**
	 * Initializes a new panel with all the required controls setup.
	 * 
	 * @return A new panel 
	 */
	private JPanel createControls() {
		JPanel controls = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraint = new GridBagConstraints();
		controls.setLayout(layout);
		TitledBorder border = BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK, 1),
				"Accessory Information");
		controls.setBorder(border);
			
		// Initialize constriants
		constraint.weightx = 0;
		constraint.weighty = 0;
		constraint.gridwidth = 1;
		constraint.gridheight = 1;
		constraint.insets = new Insets(2,2,2,2);
		constraint.fill = GridBagConstraints.BOTH;
		
		color = new JTextField();
		color.addFocusListener(new FocusListener() {
			/**
			 * Performs field validation
			 * @param e event details
			 */
			@Override
			public void focusLost(FocusEvent e) {
				checkColor();
			}
			
			/**
			 * Not implemented
			 */
			@Override
			public void focusGained(FocusEvent e) {}
		});
		JLabel colorLabel = createJLabel("Color:", SwingConstants.RIGHT, KeyEvent.VK_O, color);
		addComponent(controls, layout, constraint, colorLabel);
		constraint.weightx = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, color);
		
		modelNumber = new JTextField();
		modelNumber.addFocusListener(new FocusListener() {
			/**
			 * Performs field validation
			 * @param e event details
			 */
			@Override
			public void focusLost(FocusEvent e) {
				checkModelNumber();
			}
			
			/**
			 * Not implemented
			 */
			@Override
			public void focusGained(FocusEvent e) {}
		});
		JLabel modelNumberLabel = createJLabel("Model number:", SwingConstants.RIGHT, KeyEvent.VK_E, modelNumber);
		constraint.weightx = 0;
		constraint.gridwidth = 1;
		addComponent(controls, layout, constraint, modelNumberLabel);
		constraint.weightx = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, modelNumber);
		
		platformId = new JTextField();
		platformId.addFocusListener(new FocusListener() {
			/**
			 * Performs field validation
			 * @param e event details
			 */
			@Override
			public void focusLost(FocusEvent e) {
				checkPlatformId();
			}
			
			/**
			 * Not implemented
			 */
			@Override
			public void focusGained(FocusEvent e) {}
		});
		JLabel platformIdLabel = createJLabel("Platform ID:", SwingConstants.RIGHT, KeyEvent.VK_F, platformId);
		constraint.weightx = 0;
		constraint.gridwidth = 1;
		addComponent(controls, layout, constraint, platformIdLabel);
		constraint.weightx = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, platformId);
		
		// FILLER
		JLabel filler = new JLabel();
		constraint.weightx = 1;
		constraint.weighty = 1;	
		addComponent(controls, layout, constraint, filler);
		return controls;
	}
	
	/**
	 * Updates the item reference within this view. 
	 * 
	 * @param item A new accessory item to replace the existing item
	 * 
	 * @throws NullPointerException when item is null
	 * @throws ClassCastException when item is not a accessory type
	 */
	@Override
	public void setItem(Item item) {
		if (item == null) {
			throw new NullPointerException("Accessory item cannot be null");
		}
		
		if (!(item instanceof Accessory)) {
			throw new ClassCastException("Unable to cast Item to Accessory Item");
		}
		
		this.item = (Accessory) item;		
		refreshFields();
		
	}

	/**
	 * Refreshes the data fields with the item stored 
	 * within this view. 
	 * 
	 * Warning: This will replace all fields with the saved content
	 *          without any prompts. As such make sure you have saved
	 *          your item details to the internal item before calling
	 *          this method. 
	 */
	@Override
	public void refreshFields() {
		color.setText(item.getColor());
		modelNumber.setText(item.getModelNumber());
		platformId.setText(Integer.toString(item.getPlatformId()));
	}

	/**
	 * Performs validation on all fields
	 * @return
	 */
	public boolean checkAllFields() {
		return checkColor() & checkModelNumber() & checkPlatformId();
	}
	
	/**
	 * Checks all fields for valid data and updates the item if no fields
	 * contain errors. 
	 * 
	 * @return true if item was updated false otherwise. 
	 */
	@Override
	public boolean updateItem() {
		boolean updateSuccessful = false; 
		
		updateSuccessful = checkColor() && checkModelNumber() && checkPlatformId();
		
		if (updateSuccessful) {
			item.setColor(color.getText());			
			item.setModelNumber(modelNumber.getText());
			try {
				item.setPlatformId(Integer.parseInt(platformId.getText()));
			} catch (NumberFormatException nfe) {
				// Do nothing, should not be possible to reach this
				assert(false): "Failed to convert platformId in updateItem";
			}
		}
		
		return updateSuccessful;
	}
	
	/**
	 * Validates the model number has been provided. 
	 * 
	 * @return true if valid, false otherwise. 
	 */
	public boolean checkModelNumber() {
		boolean isValid = false;
		
		String modelNumber = this.modelNumber.getText();		
		if (modelNumber.length() == 0) {
			setFieldStyle(this.modelNumber, "A model number must be provided", INVALID_INPUT);
		} else {
			setFieldStyle(this.modelNumber, null, Color.WHITE);
			isValid = true;
		}
		
		return isValid; 
	}
	
	/**
	 * Validates the color has been provided. 
	 * 
	 * @return true if valid, false otherwise. 
	 */
	public boolean checkColor() {
		boolean isValid = false;
		
		String color = this.color.getText();		
		if (color.length() == 0) {
			setFieldStyle(this.color, "A color must be provided", INVALID_INPUT);
		} else {
			setFieldStyle(this.color, null, Color.WHITE);
			isValid = true;
		}
		
		return isValid;
	}
	
	/**
	 * Checks the platformID to ensure it's a valid console ID (alias itemNumber)
	 * 
	 * @return true if valid, false otherwise. 
	 */
	public boolean checkPlatformId() {
		boolean isValid = true;
		try {
			Integer platformId = Integer.parseInt(this.platformId.getText());
			if (platformId > 0 && consoleManager.isConsoleId(platformId)) {
				// TODO: Perform check that platformId exists				
				item.setPlatformId(platformId);
				
				// Clear the error (if set)
				setFieldStyle(this.platformId, null, Color.WHITE);
			} else {
				isValid = false;
			}
		} catch (NumberFormatException nfe) {
			isValid = false;
		} catch (SQLException sqlexception) {
			isValid = false;			
		}
		
		if (!isValid) {
			setFieldStyle(this.platformId, "Platform ID must be a valid console item number.", INVALID_INPUT);
		}
		
		return isValid;
	}
	
	/**
	 * Configures the component to display a tooltip and change the background color
	 * 
	 * @param component A component to modify
	 * @param tooltip The tooltip to set (set to null to disable tooltips)
	 * @param bgColor A color to change the background to
	 */
	private void setFieldStyle(JComponent component, String tooltip, Color bgColor) {
		component.setBackground(bgColor);
		component.setToolTipText(tooltip);
	}
	

}
