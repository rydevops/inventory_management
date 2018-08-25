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
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.ryorke.entity.Game;
import com.ryorke.entity.Item;

/**
 * Game Inventory Panel - Provides a game specific editor 
 * 
 * @author Russell Yorke
 */
@SuppressWarnings("serial")
public class GamePanel extends JPanel implements ItemEditor {
	private JSpinner numberOfDiscs;
	private JSpinner numberOfPlayers;
	private JTextField platformId;
	private JComboBox<String> esrbRating; 
	
	private Game item;
	
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
	 * Defines the ESRB Rating defined at https://www.esrb.org/ratings/
	 * 
	 * @return A list containing all the ratings
	 */
	private String[] getESRBRatings() {
		String[] esrbRating = {"E - Everyone", "E - Everyone 10+", "Teen", "M - Mature 17+", "A - Adult only"};
		return esrbRating;
	}
	
	/**
	 * Initializes a new panel with all the required controls setup.
	 * 
	 * @return A new panel 
	 */
	private JPanel createControls() {
		final int INITIAL_START = 1;
		final int STEP_COUNT = 1;
		final int MIN_DISCS = 1;
		final int MAX_DISCS = 20;
		final int MIN_PLAYERS = 1;
		final int MAX_PLAYERS = 4;
		
		JPanel controls = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraint = new GridBagConstraints();
		controls.setLayout(layout);
		TitledBorder border = BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK, 1),
				"Game Information");
		controls.setBorder(border);
			
		// Initialize constriants
		constraint.weightx = 0;
		constraint.weighty = 0;
		constraint.gridwidth = 1;
		constraint.gridheight = 1;
		constraint.insets = new Insets(2,2,2,2);
		constraint.fill = GridBagConstraints.BOTH;
		
		numberOfDiscs = new JSpinner(new SpinnerNumberModel(INITIAL_START, MIN_DISCS, MAX_DISCS, STEP_COUNT));		
		JLabel numberOfDiscsLabel = createJLabel("Number of discs:", SwingConstants.RIGHT, KeyEvent.VK_U, numberOfDiscs);
		for (int discCount = 1; discCount < 20; discCount = 20)
		numberOfPlayers = new JSpinner(new SpinnerNumberModel(INITIAL_START, MIN_PLAYERS, MAX_PLAYERS, STEP_COUNT));
		JLabel numberOfPlayersLabel = createJLabel("Number of Players:", SwingConstants.RIGHT, KeyEvent.VK_L, numberOfPlayers);
		addComponent(controls, layout, constraint, numberOfDiscsLabel);
		constraint.weightx = 1;
		addComponent(controls, layout, constraint, numberOfDiscs);
		constraint.weightx = 0;
		addComponent(controls, layout, constraint, numberOfPlayersLabel);
		constraint.weightx = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, numberOfPlayers);
		
		platformId = new JTextField();
		JLabel platformIdLabel = createJLabel("Platform ID:", SwingConstants.RIGHT, KeyEvent.VK_F, platformId);
		esrbRating = new JComboBox<String>();
		for (String rating : getESRBRatings()) {
			esrbRating.addItem(rating);
		}
		JLabel esrbRatingLabel = createJLabel("ESRB:", SwingConstants.RIGHT, KeyEvent.VK_E, esrbRating);
		constraint.weightx = 0;
		constraint.gridwidth = 1;
		addComponent(controls, layout, constraint, platformIdLabel);
		constraint.weightx = 1;
		addComponent(controls, layout, constraint, platformId);
		constraint.weightx = 0;
		addComponent(controls, layout, constraint, esrbRatingLabel);
		constraint.weightx = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, esrbRating);
		
		// Space filler
		JLabel filler = new JLabel();
		constraint.weightx = 1;
		constraint.weighty = 1;
		constraint.gridwidth = GridBagConstraints.REMAINDER;
		addComponent(controls, layout, constraint, filler);
		
		return controls;
	}
	
	/**
	 * Creates a new game item editor with no fields populated
	 */
	public GamePanel() {
		this(null);
	}
	
	/**
	 * Creates a new game item editor with fields populated
	 * based on the item
	 * 
	 * @param item A Game item to populate the fields with (if null no
	 *             fields will be populated)
	 */
	public GamePanel(Game item) throws ClassCastException {		
		setLayout(new BorderLayout());		
		setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));		
		add(createControls(), BorderLayout.CENTER);
		
		this.item = item;
		refreshFields();
	}
	
	/**
	 * Updates the item reference within this view. 
	 * 
	 * @param item A new game item to replace the existing item
	 * 
	 * @throws NullPointerException when item is null, item must be set to a valid object
	 */
	@Override
	public void updateItem(Item item) {
		if (item == null) {
			throw new NullPointerException("Item references cannot be null");
		}
		
		if (!(item instanceof Game)) {
			throw new ClassCastException("Unable to cast Item to Game Item");
		}
		this.item = (Game) item;		
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
			numberOfDiscs.setValue(item.getNumberOfDiscs());
			numberOfPlayers.setValue(item.getNumberOfPlayers());
			platformId.setText(Integer.toString((item.getPlatformId())));
			esrbRating.setSelectedItem(item.getEsrbRating());
		}
	}
}
