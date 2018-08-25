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

import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import java.io.File;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;


/**
 * Provides database management options for backup and restore/import of 
 * database data
 *  
 * @author Russell Yorke
 *
 */
@SuppressWarnings("serial")
public class InventoryDBManagementFrame extends JFrame {
	public final static String WINDOW_TITLE = "Database Management";
	private static File lastDirectoryBrowed = null;
	
	private JTextField fileLocation; 
	private JProgressBar progressBar;
	private JButton fileBrowser;
	private JRadioButton exportDatabase;
	private JRadioButton importDatabase;
	private JCheckBox overwriteDatabase;
	private JButton executeAction;
	private JButton close;
	
	/**
	 * TODO LIST: 
	 * 1. the executeAction button doesn't update when the radio buttons are selected
	 *    and this needs to be fixed
	 */
	/**
	 * Creates a new inventory management window with a default
	 * title and no owner
	 */
	public InventoryDBManagementFrame() {
		this(InventoryDBManagementFrame.WINDOW_TITLE);
	}

	
	/**
	 * Create a new inventory DB management window with the specified
	 * title as a modal dialog
	 * 
	 * @param title Window title
	 */
	public InventoryDBManagementFrame(String title) {
		Container contentPane = getContentPane();
		((JPanel)contentPane).setBorder(BorderFactory.createLineBorder(contentPane.getBackground(), 5));
		
		JPanel fileLocationPane = createFileLocationPane();
		contentPane.add(fileLocationPane, BorderLayout.NORTH);
		
		JPanel buttonsPane = createButtons();
		contentPane.add(buttonsPane, BorderLayout.CENTER);
		
		JPanel progressBarPane = createProgressBar();
		contentPane.add(progressBarPane, BorderLayout.SOUTH);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		setTitle(title);
		setResizable(false);
		setVisible(true);		
	}
	
	private JPanel createProgressBar() {
		BorderLayout layoutManager = new BorderLayout(); 
		JPanel panel = new JPanel(layoutManager);
		
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setVisible(false);	
		
		panel.add(progressBar, BorderLayout.CENTER);
		
		return panel;		
	}
	
	private JPanel createButtons() {
		BorderLayout layoutManager = new BorderLayout();
		layoutManager.setHgap(5);
		layoutManager.setVgap(5);
		
		JPanel panel = new JPanel(layoutManager);
		JPanel optionsPane = new JPanel(new GridLayout(2, 2));
		JPanel buttonPane = new JPanel(new GridLayout(1, 2));
				
		ButtonGroup buttonGroup = new ButtonGroup();
		exportDatabase = new JRadioButton("Export to file");
		importDatabase = new JRadioButton("Import from file");
		overwriteDatabase = new JCheckBox("Overwrite/Truncate database on import");
		executeAction = new JButton("Export");
		close = new JButton("Close");
				
		exportDatabase.setMnemonic(KeyEvent.VK_E);
		importDatabase.setMnemonic(KeyEvent.VK_I);
		optionsPane.setBorder(BorderFactory.createTitledBorder("Options"));
		overwriteDatabase.setEnabled(false);
		exportDatabase.setSelected(true);
		executeAction.setMnemonic(KeyEvent.VK_X);
		close.setMnemonic(KeyEvent.VK_C);
		
		
		panel.setBorder(BorderFactory.createLineBorder(panel.getBackground(), 5));
		((GridLayout)buttonPane.getLayout()).setHgap(5);
		((GridLayout)buttonPane.getLayout()).setVgap(5);
		
		buttonGroup.add(exportDatabase);
		buttonGroup.add(importDatabase);
		
		optionsPane.add(exportDatabase);
		optionsPane.add(importDatabase);
		optionsPane.add(overwriteDatabase);
		
		buttonPane.add(executeAction);
		buttonPane.add(close);
		
		panel.add(optionsPane, BorderLayout.CENTER);
		panel.add(buttonPane, BorderLayout.SOUTH);		
		
		importDatabase.addChangeListener(new ChangeListener() {
			/**
			 * Enables/Disables the overwrite database option based
			 * on the state of this RadioButton
			 * @param e event information
			 */
			@Override
			public void stateChanged(ChangeEvent e) {
				overwriteDatabase.setEnabled(importDatabase.isSelected());				
			}
		});
		
		close.addActionListener(new ActionListener() {
			/**
			 * Closes the database management window
			 * 
			 * @param e event information
			 */
			@Override
			public void actionPerformed(ActionEvent e) {			
				InventoryDBManagementFrame.this.dispatchEvent(
						new WindowEvent(InventoryDBManagementFrame.this, 
								WindowEvent.WINDOW_CLOSING));
				
			}
		});
		
		executeAction.addActionListener(new ActionListener() {
			/**
			 * Performs import/export based on selected file and operations
			 * @param e Action event information
			 */
			@Override
			public void actionPerformed(ActionEvent e) {														
				// =========== SAMPLE PROGRESS BAR VALUES ===========					
				File selectedFile = new File(fileLocation.getText());
				
				if (selectedFile.getPath().length() > 0 && 
						(exportDatabase.isSelected() || selectedFile.exists())) {
					progressBar.setValue(0);
					progressBar.setVisible(true);
					progressBar.setStringPainted(true);	
					setEnableControls(false);
					setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					Timer progressRunner = new Timer();		
					progressRunner.scheduleAtFixedRate(new TimerTask() {
						/** 
						 * Updates the progress bar (visually and textually) until
						 * the progress bar is completed based on the type of operation
						 * being performed.
						 */
						@Override
						public void run() {
							int currentValue = progressBar.getValue();													
							currentValue++;
							String statusString = "";
							if (exportDatabase.isSelected()) {
								statusString = new Integer(currentValue).toString() + "% Exported";
							} else {
								statusString = new Integer(currentValue).toString() + "% Imported";
							}
							progressBar.setString(statusString);
							progressBar.setValue(currentValue);
							
							if (currentValue == 100) {
								setEnableControls(true);
								setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
								if (exportDatabase.isSelected()) {
									statusString = "Export completed successfully!";
								} else {
									statusString = "Import completed successfully!";
								}
								progressBar.setString(statusString);
								cancel();
							}
						}
					}, 50, 50);
				} else {
					if (selectedFile.getPath().length() == 0) {
						JOptionPane.showMessageDialog(null, 
								"File location not set.\nSet file location and try again.", 
								"File not set", JOptionPane.ERROR_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(null, 
								"Selected file for import does not exist.", 
								"File not found", JOptionPane.ERROR_MESSAGE);
					}
				}
				// ============ END OF SAMPLE TIMER ===========
				
			}
		});
		return panel;
	}
	
	
	/** 
	 * Enables/Disables input to controls to ensure users cannot
	 * change inputs after an import/export operation is running
	 * 
	 * @param enabled Flag to indicate state of controls
	 */
	private void setEnableControls(boolean enabled) {
		fileLocation.setEnabled(enabled); 		
		fileBrowser.setEnabled(enabled);
		exportDatabase.setEnabled(enabled);
		importDatabase.setEnabled(enabled);
		if (enabled && importDatabase.isSelected()) {
			overwriteDatabase.setEnabled(enabled);
		} else {
			overwriteDatabase.setEnabled(enabled);
		}
		executeAction.setEnabled(enabled);
		close.setEnabled(enabled);
	}
	
	/**
	 * Configures the file location fields
	 * 
	 * @return A new panel with controls initialized
	 */
	private JPanel createFileLocationPane() {
		BorderLayout layoutManager = new BorderLayout();
		layoutManager.setHgap(5);
		layoutManager.setVgap(5);
		
		JPanel panel = new JPanel(layoutManager);
		JLabel fileLocationLabel = new JLabel("File Location:");
		fileLocation = new JTextField(25);
		fileBrowser = new JButton("Browse");
		
		fileLocationLabel.setDisplayedMnemonic(KeyEvent.VK_F);
		fileLocationLabel.setLabelFor(fileLocation);
		fileBrowser.setMnemonic(KeyEvent.VK_B);
		
		panel.add(fileLocationLabel, BorderLayout.WEST);
		panel.add(fileLocation, BorderLayout.CENTER);
		panel.add(fileBrowser, BorderLayout.EAST);
		
		fileBrowser.addActionListener(new ActionListener() {
			/**
			 * Sets the file to import or export 
			 * 
			 * @param e Action information
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				File defaultLocation = new File(System.getProperty("user.home"));
				FileSelector fileSelector = new FileSelector();
				FileNameExtensionFilter sqlFilter = new FileNameExtensionFilter("SQL Database Files", "sql");
				
				fileSelector.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileSelector.addChoosableFileFilter(sqlFilter);
				fileSelector.setFileFilter(sqlFilter);
				fileSelector.setMultiSelectionEnabled(false);
				fileSelector.setCurrentDirectory( (lastDirectoryBrowed != null) ? lastDirectoryBrowed : defaultLocation );
				
				int optionSelected = JOptionPane.ERROR_MESSAGE;
				if (exportDatabase.isSelected()) {
					fileSelector.setDialogTitle("Export database");
					optionSelected = fileSelector.showSaveDialog(InventoryDBManagementFrame.this);				
				} else {
					fileSelector.setDialogTitle("Import database");
					fileSelector.setAcceptAllFileFilterUsed(false);
					optionSelected = fileSelector.showOpenDialog(InventoryDBManagementFrame.this);						
				}
				 
				if (optionSelected == JFileChooser.APPROVE_OPTION) {
					fileLocation.setText(fileSelector.getSelectedFile().getPath());
					lastDirectoryBrowed = fileSelector.getSelectedFile().getParentFile();
				}	
			}
		});
		
		return panel;
		
	}
	
	/**
	 * A custom FileChooser that provides additional functionality including:
	 * 
	 * - Prompt to overwrite if file exists in SaveDialog
	 * - Automatic file extension addition (if not present) in SaveDialog
	 * - File must exists in OpenDialog 
	 * 
	 * Note: This class only performs these validations if the following
	 *       conditions are true:
	 *       
	 *       FileSelectionMode is FILES_ONLY
	 *       MultiSelectionEnabled is false
	 *       JFileChooser executed showSaveDialog or showOpenDialog
	 *       
	 * @author Russell Yorke
	 */
	private class FileSelector extends JFileChooser {
		/**
		 * Constructs a default FileSelector
		 */
		public FileSelector() {
			super();
		}
				
		/**
		 * Processes approval option in showOpenDialog and showSaveDialog 
		 *  
		 * For showSaveDialog:
		 * 	Extensions will be added to selected file if All Files is not selected 
		 *  and will use the first file extension found in the FileFilter option. 
		 * 	If file selected exists a prompt will confirm overwrite of file before 
		 *  accepting the selected file. 
		 *  
		 * For showOPenDialog: 
		 * 	Selected file must exist otherwise an error will be displayed. 
		 */
		@Override
		public void approveSelection() {
			if (getFileSelectionMode() == JFileChooser.FILES_ONLY &&
				!isMultiSelectionEnabled() &&
				getDialogType() == JFileChooser.SAVE_DIALOG) {
				
				File selectedFile = getSelectedFile();
				if (getFileFilter() instanceof FileNameExtensionFilter) {
					FileNameExtensionFilter selectedFilter = (FileNameExtensionFilter) getFileFilter();
					
					String filename = selectedFile.getName();
					if (filename.lastIndexOf('.') == -1) {
						String selectedExtension = "." + selectedFilter.getExtensions()[0];
						selectedFile = new File(selectedFile.getPath() + selectedExtension);
						setSelectedFile(selectedFile);
					}							
				}
				
				if (selectedFile.exists()) {
					int overwriteResponse = JOptionPane.showConfirmDialog(null, 
							selectedFile.getName() + " already exists.\nDo you want to replace it?", 
							"Replace file?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					
					if (overwriteResponse == JOptionPane.YES_OPTION) {				
						super.approveSelection();							
					}
				} else {
					super.approveSelection();
				}				
			} else if (getFileSelectionMode() == JFileChooser.FILES_ONLY &&
					!isMultiSelectionEnabled() &&
					getDialogType() == JFileChooser.OPEN_DIALOG) {
				File selectedFile = getSelectedFile();
				if (!selectedFile.exists()) {					
					JOptionPane.showMessageDialog(null, 
							selectedFile.getName() + "\nFile not found.\nCheck the file name and try again.", 
							"File not found", JOptionPane.ERROR_MESSAGE);
				} else {
					super.approveSelection();
				}
			} else {
				super.approveSelection();
			}			
		}
	}
}
