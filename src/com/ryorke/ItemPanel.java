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
		constraint.weightx = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, itemNumber);		
		
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
		description.setRows(5); // Ensure minimum of 5 rows for editor
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
		
		manufacture = new JComboBox<String>();
		// FocusListener has to be on the inner JTextField otherwise the event won't 
		// fire when changed. 
		manufacture.getEditor().getEditorComponent().addFocusListener(this);
		JLabel manufactureLabel = createJLabel("Manufacture:", SwingConstants.RIGHT, KeyEvent.VK_M, manufacture);
		manufacture.setEditable(true);	// Allow users to enter new manufactures to be added automatically
		manufacture.setSelectedIndex(-1);
		constraint.weightx = 0;
		constraint.weighty = 0;
		constraint.gridwidth = 1; 
		addComponent(controls, layout, constraint, manufactureLabel);
		constraint.weightx = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, manufacture);
		
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
		constraint.gridwidth = 1;
		constraint.gridheight = 1;
		constraint.weighty = 0;
		constraint.weightx = 0;
		addComponent(controls, layout, constraint, releaseDateLabel);
		constraint.weightx = 1;		
		addComponent(controls, layout, constraint, releaseDate);
		constraint.weightx = 1; 
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, new JLabel("")); // FILLER

		
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
		constraint.weightx = 1;
		constraint.weighty = 0;
		constraint.gridwidth = GridBagConstraints.REMAINDER; 
		addComponent(controls, layout, constraint, packageDimensionsLabel);
		constraint.weightx = 1;
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
	 * Creates a new Inventory Item editor with fields populated
	 * based on the item
	 * 
	 * @param item A item to populate the fields with (if null no
	 *             fields will be populated)
	 *             
	 * @throws NullPointerException if item is null
	 */
	public ItemPanel(Item item) throws NullPointerException {
		if (item == null)
			throw new NullPointerException("Item cannot be null");
		
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
	public void setItem(Item item) throws NullPointerException {
		if (item == null)
			throw new NullPointerException("Item cannot be null");
		
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
	 * If field is a textbox type fields or combobox
	 * it will automatically select everything in the field. 
	 * 
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void focusGained(FocusEvent e) {
		Object sourceElement = e.getSource();
		if (sourceElement instanceof JTextField) {
			JTextField textbox = (JTextField) sourceElement;
			textbox.selectAll();
		} else if (sourceElement instanceof JTextArea) {
			JTextArea textarea = (JTextArea) sourceElement;
			textarea.selectAll();			
		} else if (sourceElement instanceof JComboBox) {
			JComboBox combobox = (JComboBox) sourceElement;
			((JTextField)combobox.getEditor().getEditorComponent()).selectAll();
		}
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
	 * Validated the component has a valid value. If valid, the corresponding
	 * item is updated with the value and the field is cleared of the invalid state
	 * coloring. If the field contains invalid data it will highlight the field. 
	 * 
	 * @return true if field contains valid date, false otherwise. 
	 */
	public boolean checkProductName() {		
		boolean isValid = false;
		
		String productName = this.productName.getText();
		if (productName.length() > 0) {
			setFieldStyle(this.productName, null, Color.WHITE);
			isValid = true;
		} else {
			setFieldStyle(this.productName, "Product name must be provided.", INVALID_INPUT);
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
		boolean isValid = false;
		
		String description = this.description.getText().trim();		
		if (description.length() > 0) {
			setFieldStyle(this.description, null, Color.WHITE);
			isValid = true;
		} else {				
			setFieldStyle(this.description, "Product description must be provided.", INVALID_INPUT);
		}
		
		return isValid;
	}
	
	/**
	 * Validates manufacture field isn't blank.  
	 * 
	 * @return true if field contains valid date, false otherwise. 
	 */
	public boolean checkManufacture() {
		boolean isValid = false;
		
		JTextField manufactureField = (JTextField) this.manufacture.getEditor().getEditorComponent();
		String manufacture = (String) manufactureField.getText().trim();
		
		if (manufacture.length() > 0) {
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
		
		Integer unitsInStock = parseInteger(this.unitsInStock.getText());
		if(unitsInStock == null) {
			setFieldStyle(this.unitsInStock, "Units in stock must be 0 or more.", INVALID_INPUT);
		} else {
			setFieldStyle(this.unitsInStock, null, Color.WHITE);
			isValid = true;
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
		
		Double unitCost = parseDouble(this.unitCost.getText(), true);
		if (unitCost == null) {
			setFieldStyle(this.unitCost, "Unit cost must be $0.00 or more.", INVALID_INPUT);
		} else {
			setFieldStyle(this.unitCost, null, Color.WHITE);
			isValid = true;
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
				
		Date value = parseDate(releaseDate.getText());
		if (value == null) {
			setFieldStyle(releaseDate, "Invalid date provided. Date must be "
					+ "in format of 'yyyy/MM/dd' and set to a valid date."
					+ " (e.g. 2018/08/26).", INVALID_INPUT);			
		} else {
			setFieldStyle(releaseDate, null, Color.WHITE);		
			isValid = true;
		}
		
		return isValid;
	}
	
	/**
	 * Helper method for parsing dates. The method allows forward and backslashes
	 * in the value. 
	 * 
	 * @param rawValue A date string in the format of 'yyyy/MM/dd' (foward or backword slashes allowed)
	 * @return A new Date object based on rawValue or null if an error occurs. 
	 */
	private Date parseDate(String rawValue) {
		Date date = null; 
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		formatter.setLenient(false); // Disable lenient date parsing of invalid dates
		
		try {
			rawValue = rawValue.replaceAll("\\\\", "/");
			date = formatter.parse(rawValue);
		} catch (ParseException pe) {
			// Catch but do nothing
		}
		
		return date; 		
	}
	
	/**
	 * Helper method for parsing float values. The conversion ignores commas if present in string
	 * 
	 * @param rawValue A string containing a float value (with/without commas). 
	 * @return A new Float instance or null (if an error occurs during parsing). 
	 */
	public Float parseFloat(String rawValue) {
		Float value = null; 
		
		try {
			rawValue = rawValue.replaceAll(",", "");
			value = new Float(rawValue);
		} catch (NumberFormatException nfe) {
			// Catch and do nothing
		}
		
		return value; 
	}
	
	/**
	 * Helper method for parsing double values. The conversion ignores commas if present in string
	 * 
	 * @param rawValue A string containing a double value (with/without commas).
	 * @param isCurrency If true, parsing assumes there may be a $ character in rawValue.  
	 * @return A new Double instance or null (if an error occurs during parsing). 
	 */
	public Double parseDouble(String rawValue, boolean isCurrency) {
		Double value = null; 
		String numberPattern = ",";
		String currencyPattern = "\\$|,";
		
		try {
			rawValue = rawValue.replaceAll((isCurrency) ? currencyPattern : numberPattern, "");
			value = new Double(rawValue);
		} catch (NumberFormatException nfe) {
			// Catch and do nothing
		}
		
		return value; 
	}
	
	/**
	 * Helper method for parsing integer values. The conversion ignores commas if present in string
	 * 
	 * @param rawValue A string containing a integer value (with/without commas).  
	 * @return A new integer instance or null (if an error occurs during parsing). 
	 */
	public Integer parseInteger(String rawValue) {
		Integer value = null; 
		String numberPattern = ",";
		
		try {
			rawValue = rawValue.replaceAll(numberPattern, "");
			value = new Integer(rawValue);
		} catch (NumberFormatException nfe) {
			// Catch and do nothing
		}
		
		return value; 
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
		
		Float tempFloat = parseFloat(width.getText());
		
		if (tempFloat == null) {
			setFieldStyle(width, "Invalid width. Value must be 0.00 or more.", INVALID_INPUT);
		} else {
			setFieldStyle(width, null, Color.WHITE);
			isValid = true;
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
		
		Float tempFloat = parseFloat(height.getText());
		
		if (tempFloat == null) {
			setFieldStyle(height, "Invalid height. Value must be 0.00 or more.", INVALID_INPUT);
		} else {
			setFieldStyle(height, null, Color.WHITE);
			isValid = true;
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
		
		Float tempFloat = parseFloat(weight.getText());
		
		if (tempFloat == null) {
			setFieldStyle(weight, "Invalid weight. Value must be 0.00 or more.", INVALID_INPUT);
		} else {
			setFieldStyle(weight, null, Color.WHITE);
			isValid = true;
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
		
		Float tempFloat = parseFloat(depth.getText());
		
		if (tempFloat == null) {
			setFieldStyle(depth, "Invalid depth. Value must be 0.00 or more.", INVALID_INPUT);
		} else {
			setFieldStyle(depth, null, Color.WHITE);
			isValid = true;
		}
		
		return isValid;
	}

	/**
	 * Performs validation on all fields
	 * @return
	 */
	public boolean checkAllFields() {
		return checkProductName() & checkProductDescription()
		& checkManufacture() & checkUnitsInStock() & checkUnitCost()
		& checkReleaseDate() & checkWidth() & checkHeight() 
		& checkDepth() & checkWeight();
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
		
		updateSuccessful = checkProductName() && checkProductDescription()
				&& checkManufacture() && checkUnitsInStock() && checkUnitCost()
				&& checkReleaseDate() && checkWidth() && checkHeight() 
				&& checkDepth() && checkWeight();
		
		
		if (updateSuccessful) { 
			item.setProductName(productName.getText());
			item.setProductDescription(description.getText());
			JTextField manufactureField = (JTextField)manufacture.getEditor().getEditorComponent();
			item.setManufacture(manufactureField.getText().trim());
			String unitsInStock = this.unitsInStock.getText();			
			item.setUnitsInStock(parseInteger(unitsInStock));
			item.setUnitCost(parseDouble(unitCost.getText(), true));
			item.setReleaseDate(parseDate(releaseDate.getText()));
			item.getPackageDimensions().setWidth(parseFloat(width.getText()));
			item.getPackageDimensions().setHeight(parseFloat(height.getText()));
			item.getPackageDimensions().setDepth(parseFloat(depth.getText()));
			item.getPackageDimensions().setWeight(parseFloat(weight.getText()));
		}
		
		return updateSuccessful;
	}
	
}

