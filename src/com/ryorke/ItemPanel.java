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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.ryorke.database.ManufactureEntityManager;
import com.ryorke.entity.Item;
import com.ryorke.entity.Manufacture;

/**
 * Item Inventory Panel - Provides a generic item editor 
 * 
 * @author Russell Yorke
 */
@SuppressWarnings("serial")
public class ItemPanel extends JPanel implements ItemEditor, FocusListener {
	public final static Color INVALID_INPUT = new Color(255, 200, 200); 
	
	private JTextField itemNumber;	
	private JTextField productName;
	private JTextArea description;
	private JTextField unitsInStock;
	private JTextField unitCost;
	private JComboBox<String> manufacture;
	private JTextField releaseDate;
	private JTextField height;
	private JTextField width;
	private JTextField depth;
	private JTextField weight;

	private Item item;
	
	
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
				"Item Information");
		controls.setBorder(border);
			
		// Initialize constraints
		constraint.weightx = 0;
		constraint.weighty = 0;
		constraint.gridwidth = 1;
		constraint.gridheight = 1;
		constraint.insets = new Insets(2,2,2,2);
		constraint.fill = GridBagConstraints.BOTH;
	
		itemNumber = new JTextField();
		JLabel itemNumberLabel = createJLabel("Item Number:", SwingConstants.RIGHT, 0, itemNumber);
		itemNumber.setEditable(false);
		itemNumber.setFocusable(false);	// Remove from tab order
		addComponent(controls, layout, constraint, itemNumberLabel);		
		addComponent(controls, layout, constraint, itemNumber);
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, new JLabel()); // FILLER
		
		productName = new JTextField();
		productName.addFocusListener(this);
		JLabel productNameLabel = createJLabel("Name:", SwingConstants.RIGHT, KeyEvent.VK_N, productName);
		constraint.weightx = 0;
		constraint.gridwidth = 1;
		addComponent(controls, layout, constraint, productNameLabel);
		constraint.weightx = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, productName);
		
		JScrollPane descriptionPane = new JScrollPane();
		description = new JTextArea();
		description.addFocusListener(this);
		JLabel descriptionLabel = createJLabel("Description:", SwingConstants.RIGHT, KeyEvent.VK_D, descriptionPane);
		descriptionPane.setViewportView(description);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		constraint.weightx = 0;
		constraint.gridwidth = 1;
		addComponent(controls, layout, constraint, descriptionLabel);
		constraint.weightx = 1;
		constraint.weighty = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, descriptionPane);
		
		unitsInStock = new JTextField();
		unitsInStock.addFocusListener(this);
		JLabel unitsInStockLabel = createJLabel("Units in stock:", SwingConstants.RIGHT, KeyEvent.VK_S, unitsInStock);
		unitCost = new JTextField();
		unitCost.addFocusListener(this);
		JLabel unitCostLabel = createJLabel("Unit cost:", SwingConstants.RIGHT, KeyEvent.VK_C, unitCost);
		constraint.weightx = 0;
		constraint.weighty = 0;
		constraint.gridwidth = 1; 
		addComponent(controls, layout, constraint, unitsInStockLabel);
		constraint.weightx = 1;
		addComponent(controls, layout, constraint, unitsInStock);
		constraint.weightx = 0;
		addComponent(controls, layout, constraint, unitCostLabel);
		constraint.weightx = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, unitCost);
		
		manufacture = new JComboBox<String>();
		// FocusListener has to be on the inner JTextField otherwise the event won't 
		// fire when changed. 
		manufacture.getEditor().getEditorComponent().addFocusListener(this);
		JLabel manufactureLabel = createJLabel("Manufacture:", SwingConstants.RIGHT, KeyEvent.VK_M, manufacture);
		releaseDate = new JTextField();
		releaseDate.addFocusListener(this);
		JLabel releaseDateLabel = createJLabel("Release date:", SwingConstants.RIGHT, KeyEvent.VK_R, releaseDate);
		try {
			ManufactureEntityManager manager = ManufactureEntityManager.getManager();
			ArrayList<Manufacture> manufactures = manager.getManufactures();
			if (manufactures != null) {
				for (Manufacture manufactureElement : manufactures) {
					manufacture.addItem(manufactureElement.getName());			
				}
			}
		} catch (SQLException | IOException exception) {
			JOptionPane.showMessageDialog(this, String.format("Unable to load manufactures."
					+ "\nReason:\n%s", exception.getMessage()), "Manufacture load error", 
					JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE);
		}
		manufacture.setEditable(true);	// Allow users to enter new manufactures to be added automatically
		manufacture.setSelectedIndex(-1);
		constraint.weightx = 0;
		constraint.weighty = 0;
		constraint.gridwidth = 1; 
		addComponent(controls, layout, constraint, manufactureLabel);
		constraint.weightx = 1;
		addComponent(controls, layout, constraint, manufacture);
		constraint.weightx = 0;
		addComponent(controls, layout, constraint, releaseDateLabel);
		constraint.weightx = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, releaseDate);

		
		height = new JTextField();
		height.addFocusListener(this);
		width = new JTextField();
		width.addFocusListener(this);
		depth = new JTextField();
		depth.addFocusListener(this);
		weight = new JTextField();
		weight.addFocusListener(this);
		JLabel packageDimensionsLabel = new JLabel("Package Dimensions:");
		JLabel heightLabel = createJLabel("Height", SwingConstants.LEFT, KeyEvent.VK_H, height);
		JLabel widthLabel = createJLabel("Width", SwingConstants.LEFT, KeyEvent.VK_W, width);
		JLabel depthLabel = createJLabel("Depth", SwingConstants.LEFT, KeyEvent.VK_P, depth);
		JLabel weightLabel = createJLabel("Weight", SwingConstants.LEFT, KeyEvent.VK_G, weight);
		addComponent(controls, layout, constraint, packageDimensionsLabel);
		constraint.weightx = 0;
		constraint.weighty = 0;
		constraint.gridwidth = 1; 
		addComponent(controls, layout, constraint, height);
		addComponent(controls, layout, constraint, width);
		addComponent(controls, layout, constraint, depth);
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, weight);
		constraint.gridwidth = 1; 
		addComponent(controls, layout, constraint, heightLabel);
		addComponent(controls, layout, constraint, widthLabel);
		addComponent(controls, layout, constraint, depthLabel);
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, weightLabel);
		
		return controls;		
	}
	
	/**
	 * Creates a new Inventory Item editor with no fields populated
	 */
	public ItemPanel() {
		this(null);
	}
	
	/**
	 * Creates a new Inventory Item editor with fields populated
	 * based on the item
	 * 
	 * @param item A item to populate the fields with (if null no
	 *             fields will be populated)
	 */
	public ItemPanel(Item item) {
		setLayout(new BorderLayout());		
		setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));		
		add(createControls(), BorderLayout.CENTER);
		
		this.item = item;
		refreshFields();
	}

	/**
	 * Updates the item reference within this view. 
	 * 
	 * @param item A new item to replace the existing item
	 * 
	 * @throws NullPointerException when item is null, item must be set to a valid object
	 */
	@Override
	public void updateItem(Item item) throws NullPointerException {
		if (item == null) {
			throw new NullPointerException("Item references cannot be null");
		}
		this.item = item;		
	}

	/**
	 * Refreshes the data fields with the item stored 
	 * within this view. 
	 * 
	 * Warning: This will replace all fields with the saved content
	 *          without any prompts. As such make sure you have saved
	 *          your item details to the internal item before calling
	 *          this method. 
	 * 
	 */
	@Override
	public void refreshFields() {
		SimpleDateFormat dateFormatter = (SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
		dateFormatter.applyPattern("YYYY/MM/dd");
		DecimalFormat dimensionFormat = new DecimalFormat("#,###0.0000");
		DecimalFormat currencyFormat = new DecimalFormat("#,###0.00");
		
		if (this.item != null) {
			// Don't display the item number if this is a new item not yet saved
			// to the database
			if (item.getItemNumber() >= 0)
				itemNumber.setText(Integer.toString(item.getItemNumber()));
			
			productName.setText(item.getProductName());
			description.setText(item.getProductDescription());
			unitsInStock.setText(Integer.toString(item.getUnitsInStock()));
			unitCost.setText(currencyFormat.format(item.getUnitCost()));
			manufacture.setSelectedItem(item.getManufacture());
			releaseDate.setText(dateFormatter.format(item.getReleaseDate()));
			height.setText(dimensionFormat.format(item.getPackageDimensions().getHeight()));
			width.setText(dimensionFormat.format(item.getPackageDimensions().getWidth()));
			depth.setText(dimensionFormat.format(item.getPackageDimensions().getDepth()));
			weight.setText(dimensionFormat.format(item.getPackageDimensions().getWeight()));
		}
	}


	/**
	 * Not implemented/used
	 */
	@Override
	public void focusGained(FocusEvent e) {}


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
	
	/**
	 * Performs field validation and field-to-item updates
	 */
	@Override
	public void focusLost(FocusEvent e) {
		Object source = e.getSource();
		
		if (source == productName) {
			checkProductName();
		} else if (source == description) {
			checkProductDescription();
		} else if (source == manufacture.getEditor().getEditorComponent()) {
			checkManufacture();
		} else if (source == unitsInStock) {
			checkUnitsInStock();
		} else if (source == unitCost) {
			checkUnitCost();
		} else if (source == releaseDate) {
			checkReleaseDate();
		} else if (source == height) {
			checkHeight();
		} else if (source == width) {
			checkWidth();
		} else if (source == depth) {
			checkDepth();
		} else if (source == weight) {
			checkWeight();
		} 
	}
	
	/**
	 * Perform validation and item updates on all fields within this view. 
	 * 
	 * @return true if all fields are validate and updated, false otherwise
	 */
	public boolean validateAllFields() {
		return checkProductName() && checkProductDescription() &&
				checkManufacture() && checkUnitsInStock() && checkUnitCost() &&
				checkReleaseDate() && checkHeight() && checkWeight() && checkDepth() &&
				checkWeight();
	}
	
	/**
	 * Validated the component has a valid value. If valid, the corresponding
	 * item is updated with the value and the field is cleared of the invalid state
	 * coloring. If the field contains invalid data it will highlight the field. 
	 * 
	 * @return true if field contains valid date, false otherwise. 
	 */
	public boolean checkProductName() {
		String value = productName.getText();
		boolean isValid = false;
		
		if (value.length() > 0) {
			item.setProductName(value);
			setFieldStyle(productName, null, Color.WHITE);
			isValid = true;
		} else {
			setFieldStyle(productName, "Product name must be provided.", INVALID_INPUT);
		}			
		
		return isValid;
	}
	
	/**
	 * Validated the component has a valid value. If valid, the corresponding
	 * item is updated with the value and the field is cleared of the invalid state
	 * coloring. If the field contains invalid data it will highlight the field. 
	 * 
	 * @return true if field contains valid date, false otherwise. 
	 */
	public boolean checkProductDescription() {		
		String value = description.getText().trim();
		boolean isValid = false;
		
		if (value.length() > 0) {
			item.setProductDescription(value);
			setFieldStyle(description, null, Color.WHITE);
			isValid = true;
		} else {				
			setFieldStyle(description, "Product description must be provided.", INVALID_INPUT);
		}
		
		return isValid;
	}
	
	/**
	 * Validated the component has a valid value. If valid, the corresponding
	 * item is updated with the value and the field is cleared of the invalid state
	 * coloring. If the field contains invalid data it will highlight the field. 
	 * 
	 * @return true if field contains valid date, false otherwise. 
	 */
	public boolean checkManufacture() {
		boolean isValid = false;
		
		// Extract information from the inner JTextField. This is required to 
		// allow the use of an editable field that may not exist as a selectable
		// item
		JTextField manufactureField = (JTextField) manufacture.getEditor().getEditorComponent();
		String value = (String) manufactureField.getText();
		
		if (value.length() > 0) {
			item.setManufacture(value);
			setFieldStyle(manufactureField, null, Color.WHITE);
			isValid = true;
		} else {				
			setFieldStyle(manufactureField, "A manufacture must be provided.", INVALID_INPUT);				
		}
		
		return isValid;
	}
	
	/**
	 * Validated the component has a valid value. If valid, the corresponding
	 * item is updated with the value and the field is cleared of the invalid state
	 * coloring. If the field contains invalid data it will highlight the field. 
	 * 
	 * @return true if field contains valid date, false otherwise. 
	 */
	public boolean checkUnitsInStock() {
		boolean isValid = false;
		
		try {
			int value = Integer.parseInt(unitsInStock.getText().replaceAll(",", ""));
			if (value >= 0) {
				item.setUnitsInStock(value);
				setFieldStyle(unitsInStock, null, Color.WHITE);
				isValid = true;
			}
		} catch (NumberFormatException nfe) {
			// Do nothing here
		}
		
		if (!isValid) {
			setFieldStyle(unitsInStock, "Invalid units in stock. Value must be 0 or more.", INVALID_INPUT);
		}
		
		return isValid;
	}
	
	/**
	 * Validated the component has a valid value. If valid, the corresponding
	 * item is updated with the value and the field is cleared of the invalid state
	 * coloring. If the field contains invalid data it will highlight the field. 
	 * 
	 * @return true if field contains valid date, false otherwise. 
	 */
	public boolean checkUnitCost() {
		boolean isValid = false;
		
		try {
			double value = Double.parseDouble(unitCost.getText().replaceAll("\\$|,", ""));
			if (value >= 0.00) {
				item.setUnitCost(value);
				setFieldStyle(unitCost, null, Color.WHITE);
				isValid = true;
			}
		} catch (NumberFormatException nfe) {
			// Do nothing here
		}
		
		if (!isValid) {
			setFieldStyle(unitCost, "Invalid unit cost. Value must be 0.00 or more.", INVALID_INPUT);
		}
		
		return isValid; 
	}
	
	/**
	 * Validated the component has a valid value. If valid, the corresponding
	 * item is updated with the value and the field is cleared of the invalid state
	 * coloring. If the field contains invalid data it will highlight the field. 
	 * 
	 * @return true if field contains valid date, false otherwise. 
	 */
	public boolean checkReleaseDate() {
		boolean isValid = false;
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		formatter.setLenient(false); // Disable lenient date parsing of invalid dates
		
		try {
			Date value = formatter.parse(releaseDate.getText().replaceAll("\\\\", "/"));
			item.setReleaseDate(value);
			setFieldStyle(releaseDate, null, Color.WHITE);
			isValid = true;
		} catch (ParseException pe) {
			setFieldStyle(releaseDate, "Invalid date provided. Date must be in format of 'yyyy/MM/dd' and set to a valid date."
					+ " (e.g. 2018/08/26).", INVALID_INPUT);	
		}
		
		return isValid;
	}
	
	/**
	 * Validated the component has a valid value. If valid, the corresponding
	 * item is updated with the value and the field is cleared of the invalid state
	 * coloring. If the field contains invalid data it will highlight the field. 
	 * 
	 * @return true if field contains valid date, false otherwise. 
	 */
	public boolean checkWidth() {
		boolean isValid = false;
		
		try {
			float value = Float.parseFloat(width.getText().replaceAll(",", ""));
			if (value >= 0.00) {
				item.getPackageDimensions().setWidth(value);
				setFieldStyle(width, null, Color.WHITE);
				isValid = true;
			}
		} catch (NumberFormatException nfe) {
			// Do nothing here
		}
		
		if (!isValid) {
			setFieldStyle(width, "Invalid width. Value must be 0.00 or more.", INVALID_INPUT);
		}
		
		return isValid;
	}
	
	/**
	 * Validated the component has a valid value. If valid, the corresponding
	 * item is updated with the value and the field is cleared of the invalid state
	 * coloring. If the field contains invalid data it will highlight the field. 
	 * 
	 * @return true if field contains valid date, false otherwise. 
	 */
	public boolean checkHeight() {
		boolean isValid = false;
		
		try {
			float value = Float.parseFloat(height.getText().replaceAll(",", ""));
			if (value >= 0.00) {
				item.getPackageDimensions().setHeight(value);
				setFieldStyle(height, null, Color.WHITE);
				isValid = true;
			}
		} catch (NumberFormatException nfe) {
			// Do nothing here
		}
		
		if (!isValid) {
			setFieldStyle(height, "Invalid height. Value must be 0.00 or more.", INVALID_INPUT);
		}
		
		return isValid;
	}
	
	/**
	 * Validated the component has a valid value. If valid, the corresponding
	 * item is updated with the value and the field is cleared of the invalid state
	 * coloring. If the field contains invalid data it will highlight the field. 
	 * 
	 * @return true if field contains valid date, false otherwise. 
	 */
	public boolean checkWeight() {
		boolean isValid = false;
		
		try {
			float value = Float.parseFloat(weight.getText().replaceAll(",", ""));
			if (value >= 0.00) {
				item.getPackageDimensions().setWeight(value);
				setFieldStyle(weight, null, Color.WHITE);
				isValid = true;
			}
		} catch (NumberFormatException nfe) {
			// Do nothing here
		}
		
		if (!isValid) {
			setFieldStyle(weight, "Invalid weight. Value must be 0.00 or more.", INVALID_INPUT);
		}
		
		return isValid;
	}
	
	/**
	 * Validated the component has a valid value. If valid, the corresponding
	 * item is updated with the value and the field is cleared of the invalid state
	 * coloring. If the field contains invalid data it will highlight the field. 
	 * 
	 * @return true if field contains valid date, false otherwise. 
	 */
	public boolean checkDepth() {
		boolean isValid = false;
		
		try {
			float value = Float.parseFloat(depth.getText().replaceAll(",", ""));
			if (value >= 0.00) {
				item.getPackageDimensions().setDepth(value);
				setFieldStyle(depth, null, Color.WHITE);
				isValid = true;
			}
		} catch (NumberFormatException nfe) {
			// Do nothing here
		}
		
		if (!isValid) {
			setFieldStyle(depth, "Invalid depth. Value must be 0.00 or more.", INVALID_INPUT);
		}
		
		return isValid;
	}
	
}

