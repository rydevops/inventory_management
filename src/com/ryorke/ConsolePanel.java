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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.ryorke.database.GameEntityManager;
import com.ryorke.entity.Console;
import com.ryorke.entity.Game;
import com.ryorke.entity.Item;



/**
 * Console Inventory Panel - Provides a console specific editor 
 * 
 * @author Russell Yorke
 */
@SuppressWarnings("serial")
public class ConsolePanel extends JPanel implements ItemEditor {
	public final static Color INVALID_INPUT = new Color(255, 200, 200);
	
	private Console item;
	private JTextField color;
	private JTextField diskSpace;
	private JTextField modelNumber;
	private JList<Game> includedGamesList;
	private JSpinner controllersIncluded;
	private JButton addGame;
	private JButton removeGame;
	private JDialog parent; 
	
	/**
	 * Creates a console editor panel and loads the data
	 * into the fields based on the item. 
	 * 
	 * @param item The console item details
	 * @throws NullPointerException If item is null
	 */
	public ConsolePanel(JDialog parent, Console item) throws NullPointerException {
		if (item == null)
			throw new NullPointerException("Console item cannot be null");
		
		this.parent = parent;
		this.item = item;
		
		setLayout(new BorderLayout());		
		setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));		
		add(createControls(), BorderLayout.CENTER);
		
		
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
			 * Not used
			 */
			@Override
			public void focusGained(FocusEvent e) {
				
			}
		});
		JLabel colorLabel = createJLabel("Color:", SwingConstants.RIGHT, KeyEvent.VK_O, color);
		addComponent(controls, layout, constraint, colorLabel);
		constraint.weightx = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, color);		
		
		diskSpace = new JTextField();
		diskSpace.addFocusListener(new FocusListener() {
			
			/**
			 * Performs field validation
			 * @param e event details
			 */
			@Override
			public void focusLost(FocusEvent e) {
				checkDiskSpace();
			}
			
			/**
			 * Not used
			 */
			@Override
			public void focusGained(FocusEvent e) {
				
			}
		});
		JLabel diskSpaceLabel = createJLabel("Disk space:", SwingConstants.RIGHT, KeyEvent.VK_K, diskSpace);		
		constraint.gridwidth = 1;
		constraint.weightx = 0;
		addComponent(controls, layout, constraint, diskSpaceLabel);
		constraint.weightx = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, diskSpace);		
		
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
			 * Not used
			 */
			@Override
			public void focusGained(FocusEvent e) {
				
			}
		});
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

		
		DefaultListModel<Game> gameListModel = new DefaultListModel<Game>();
		int[] gameList = item.getIncludedGameId();
		if (gameList != null) {
			try {
				GameEntityManager manager = GameEntityManager.getManager();
				for (int gameId : gameList) {
					gameListModel.addElement(manager.getGame(gameId));
				}
			} catch (Exception exception) {
				JOptionPane.showMessageDialog(parent, String.format("Unable to load included games list.\nReason:\n%s", 
						exception.getMessage()), "Game list failed to load", 
						JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE);
				this.dispatchEvent(new WindowEvent(parent, WindowEvent.WINDOW_CLOSING));
			}
			
		}
		includedGamesList = new JList<Game>(gameListModel);	
		JLabel includedGameIdLabel = createJLabel("Included games:", SwingConstants.RIGHT, KeyEvent.VK_A, includedGamesList);
		JScrollPane includedGameIdScrollView = new JScrollPane(includedGamesList);
		constraint.gridwidth = 1;
		constraint.weightx = 0;
		addComponent(controls, layout, constraint, includedGameIdLabel);
		constraint.weightx = 1;
		constraint.weighty = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, includedGameIdScrollView);	
		
		addGame = new JButton("Add game");
		addGame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addIncludedGame();				
			}
		});
		constraint.weighty = 0;	
		addComponent(controls, layout, constraint, addGame);
		removeGame = new JButton("Remove game");
		removeGame.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				removeSelectedGame();				
			}
		});
		addComponent(controls, layout, constraint, removeGame);

		return controls;
	}
	
	private void removeSelectedGame() {
		DefaultListModel<Game> model = (DefaultListModel<Game>) includedGamesList.getModel();
		int[] selectedGameIndices = includedGamesList.getSelectedIndices();
		if (selectedGameIndices.length > 0)
			for (int selectedGameIndex = selectedGameIndices.length -1; selectedGameIndex >= 0; selectedGameIndex--) {
				model.removeElementAt(selectedGameIndices[selectedGameIndex]);
			}
		else
			JOptionPane.showMessageDialog(parent, "Select a game to be delete.", 
					"No game selected", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
		
	}
	
	private void addIncludedGame() {
		try {
			DefaultListModel<Game> model = (DefaultListModel<Game>) includedGamesList.getModel();
			int[] existingGameIdsIncluded = null;
			if (model.getSize() > 0) {
				existingGameIdsIncluded = new int[model.getSize()];
				for (int index = 0; index < model.getSize(); index++) {
					existingGameIdsIncluded[index] = model.getElementAt(index).getItemNumber();
				}
			}
			
			GameSelectionDialog gameSelectionDialog = new GameSelectionDialog(parent, item.getItemNumber(), existingGameIdsIncluded);
			if (gameSelectionDialog.getGameCount() > 0) {
				gameSelectionDialog.setVisible(true);
				if (gameSelectionDialog.wasSaved()) {
					ArrayList<Game> selectedGames = gameSelectionDialog.getIncludedGames(); 
					model.clear();
					if (selectedGames != null) {
						for (Game game : selectedGames ) {
							model.addElement(game);				
						}
					}
				}
			} else {
				JOptionPane.showMessageDialog(this, "No games found for this console", 
						"No games found", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
				
			}
		} catch (Exception exception) {
			JOptionPane.showMessageDialog(this, "Unable to load game list", 
					"Error loading game list", JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Updates the item reference within this view. 
	 * 
	 * @param item A new console item to replace the existing item
	 * 
	 * @throws NullPointerException when item is null, item must be set to a valid object
	 * @throws ClassCastException when item is not a console type
	 */
	@Override
	public void setItem(Item item) throws NullPointerException, ClassCastException {
		if (item == null) {
			throw new NullPointerException("Console item cannot be null");
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
		color.setText(item.getColor());
		controllersIncluded.setValue(item.getControllersIncluded());
		diskSpace.setText(item.getDiskSpace());
		modelNumber.setText(item.getModelNumber());
	}

	/**
	 * Performs validation on all fields
	 * @return
	 */
	public boolean checkAllFields() {
		return checkColor() && checkDiskSpace() && checkModelNumber();
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
		
		updateSuccessful = checkColor() && checkDiskSpace() && checkModelNumber();
		
		if (updateSuccessful) {
			item.setColor(color.getText());
			item.setControllersIncluded((Integer)controllersIncluded.getValue());
			item.setDiskSpace(diskSpace.getText());			
			item.setModelNumber(modelNumber.getText());
			
			// Create a list of game ids to be included
			ListModel<Game> model = includedGamesList.getModel();
			int[] gameIds = null;
			if (model.getSize() > 0) {
				gameIds = new int[model.getSize()];
				for (int index = 0; index < model.getSize(); index++) {
					gameIds[index] = model.getElementAt(index).getItemNumber();
				}
			}
			item.setIncludedGameId(gameIds);
		}
		
		return updateSuccessful;
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
	 * Validates the disk space has been provided. 
	 * 
	 * @return true if valid, false otherwise. 
	 */
	public boolean checkDiskSpace() {
		boolean isValid = false;
		
		String diskSpace = this.diskSpace.getText();		
		if (diskSpace.length() == 0) {
			setFieldStyle(this.diskSpace, "Available disk space must be provided", INVALID_INPUT);
		} else {
			setFieldStyle(this.diskSpace, null, Color.WHITE);
			isValid = true;
		}
		
		return isValid; 
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
}
