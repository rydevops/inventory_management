package com.ryorke;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.ryorke.entity.Console;
import com.ryorke.entity.Item;

@SuppressWarnings("serial")
public class ConsolePanel extends JPanel implements ItemEditor {
	private Console item;
	private JTextField color;
	private JTextField diskSpace;
	private JTextField modelNumber;
	private JList<String> includedGameId;
	private JSpinner controllersIncluded;
	
	/**
	 * Creates a blank console editor panel
	 */
	public ConsolePanel() {
		this(null);
	}
	
	/**
	 * Creates a console editor panel and loads the data
	 * into the fields based on the item. 
	 * 
	 * @param item The console item details
	 */
	public ConsolePanel(Console item) {
		setLayout(new BorderLayout());		
		setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));		
		add(createControls(), BorderLayout.CENTER);
		
		this.item = item;
		refreshFields();
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
		label.setVerticalAlignment(SwingConstants.TOP);	// TODO: Make this configurable	
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
				"Console Information");
		controls.setBorder(border);
			
		// Initialize constriants
		constraint.weightx = 0;
		constraint.weighty = 0;
		constraint.gridwidth = 1;
		constraint.gridheight = 1;
		constraint.insets = new Insets(2,2,2,2);
		constraint.fill = GridBagConstraints.BOTH;
		
		color = new JTextField();
		JLabel colorLabel = createJLabel("Color:", SwingConstants.RIGHT, KeyEvent.VK_O, color);
		addComponent(controls, layout, constraint, colorLabel);
		constraint.weightx = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, color);		
		
		diskSpace = new JTextField();
		JLabel diskSpaceLabel = createJLabel("Disk space:", SwingConstants.RIGHT, KeyEvent.VK_K, diskSpace);		
		constraint.gridwidth = 1;
		constraint.weightx = 0;
		addComponent(controls, layout, constraint, diskSpaceLabel);
		constraint.weightx = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, diskSpace);		
		
		modelNumber = new JTextField();
		JLabel modelNumberLabel = createJLabel("Model number:", SwingConstants.RIGHT, KeyEvent.VK_B, modelNumber);
		constraint.gridwidth = 1;
		constraint.weightx = 0;
		addComponent(controls, layout, constraint, modelNumberLabel);
		constraint.weightx = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, modelNumber);
		
		final int MIN_CONTROLLERS = 1;
		final int MAX_CONTROLLERS = 4;
		final int STEP_COUNT = 1;
		final int DEFAULT_CONTROLLERS = MIN_CONTROLLERS;
		controllersIncluded = new JSpinner(new SpinnerNumberModel(DEFAULT_CONTROLLERS, MIN_CONTROLLERS, MAX_CONTROLLERS,STEP_COUNT));
		JLabel controllersIncludedLabel = createJLabel("Controllers:", SwingConstants.RIGHT, KeyEvent.VK_L, controllersIncluded);
		constraint.gridwidth = 1;
		constraint.weightx = 0;
		addComponent(controls, layout, constraint, controllersIncludedLabel);
		constraint.weightx = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, controllersIncluded);

		includedGameId = new JList<String>(new DefaultListModel<String>());
		JLabel includedGameIdLabel = createJLabel("Included games:", SwingConstants.RIGHT, KeyEvent.VK_A, includedGameId);
		JScrollPane includedGameIdScrollView = new JScrollPane(includedGameId);
		// *** SAMPLE DATA ***
		// TODO: Remove and replace with database data
		// TODO: Add method for adding game IDs to this list
		// TODO: Cleanup/Remove this commented block
//		DefaultListModel<String> lm = (DefaultListModel<String>) includedGameId.getModel();
//		lm.addElement("God of War");
		// *** END OF SAMPLE DATA
		constraint.gridwidth = 1;
		constraint.weightx = 0;
		addComponent(controls, layout, constraint, includedGameIdLabel);
		constraint.weightx = 1;
		constraint.weighty = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, includedGameIdScrollView);		
		
		return controls;
	}
	
	/**
	 * Updates the item reference within this view. 
	 * 
	 * @param item A new console item to replace the existing item
	 * 
	 * @throws NullPointerException when item is null, item must be set to a valid object
	 */
	@Override
	public void updateItem(Item item) {
		if (item == null) {
			throw new NullPointerException("Item references cannot be null");
		}
		
		if (!(item instanceof Console)) {
			throw new ClassCastException("Unable to cast Item to Console Item");
		}
		this.item = (Console) item;		
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
	 * 
	 */
	@Override
	public void refreshFields() {
		if (item != null) {
			color.setText(item.getColor());
			controllersIncluded.setValue(item.getControlersIncluded());
			diskSpace.setText(item.getDiskSpace());
			modelNumber.setText(item.getModelNumber());
		}
	}
}
