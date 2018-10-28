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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.ryorke.database.SQLiteDBManager;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
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
public class InventoryDBManagementFrame extends JDialog {
	public final static String WINDOW_TITLE = "Database Management";
	private static File lastDirectoryBrowsed = null;
	
	private JTextField fileLocation; 
	private JProgressBar progressBar;
	private JButton fileBrowser;
	private JRadioButton exportDatabase;
	private JRadioButton importDatabase;
	private JCheckBox overwriteDatabase;
	private JButton executeAction;
	private JButton close;
	
	/**
	 * Creates a new inventory management window with a default
	 * title and no owner
	 */
	public InventoryDBManagementFrame(Frame owner) {
		this(owner, InventoryDBManagementFrame.WINDOW_TITLE);
	}

	
	/**
	 * Create a new inventory DB management window with the specified
	 * title as a modal dialog
	 * 
	 * @param owner The owner of this dialog
	 * @param title Window title
	 */
	public InventoryDBManagementFrame(Frame owner, String title) {
		super(owner, title, true);
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
		setResizable(false);
	}
	
	/**
	 * Creates the progress bar for import/export operations
	 * @return A new panel containing the progress bar
	 */
	private JPanel createProgressBar() {
		BorderLayout layoutManager = new BorderLayout(); 
		JPanel panel = new JPanel(layoutManager);
		
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setVisible(false);	
		
		panel.add(progressBar, BorderLayout.CENTER);
		
		return panel;		
	}
	
	/**
	 * Creates the action buttons
	 * 
	 * @return A new panel containing action buttons
	 */
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
				if (importDatabase.isSelected())
					executeAction.setText("Import");
				else
					executeAction.setText("Export");
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
				File selectedFile = new File(fileLocation.getText());
				
				if (selectedFile.getPath().length() > 0) {
					if (exportDatabase.isSelected()) {
						exportDatabase(selectedFile);
					} else if (selectedFile.exists()) {
						importDatabase(selectedFile);
					} else {
						JOptionPane.showMessageDialog(null, 
							"Selected file for import does not exist.", 
							"File not found", JOptionPane.ERROR_MESSAGE);
					}
				} else {

					JOptionPane.showMessageDialog(null, 
						"File location not set.\nSet file location and try again.", 
						"File not set", JOptionPane.ERROR_MESSAGE);
				}				
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
				fileSelector.setCurrentDirectory( (lastDirectoryBrowsed != null) ? lastDirectoryBrowsed : defaultLocation );
				
				int optionSelected = 0;
				if (exportDatabase.isSelected()) {
					fileSelector.setDialogTitle("Export database");
					optionSelected = fileSelector.showSaveDialog(InventoryDBManagementFrame.this);				
				} else {
					fileSelector.setDialogTitle("Import database");
					optionSelected = fileSelector.showOpenDialog(InventoryDBManagementFrame.this);						
				}
				 
				if (optionSelected == JFileChooser.APPROVE_OPTION) {
					fileLocation.setText(fileSelector.getSelectedFile().getPath());
					lastDirectoryBrowsed = fileSelector.getSelectedFile().getParentFile();
				}	
			}
		});
		
		return panel;
		
	}
	
	/**
	 * Updates a progress bar including the text to display on it. 
	 * 
	 * @param currentValue A value between 0 and 100 to adjust the progress bar
	 * @param exporting If true "% Exported" will be displayed, otherwise "% Imported" will be displayed. 
	 */
	private void updateProgressBar(int currentValue, boolean exporting) {
		String progressStatusMessage = String.format("%d%% %s", currentValue, (exporting) ? "Exported" : "Imported");
		progressBar.setValue(currentValue);
		progressBar.setString(progressStatusMessage);
		progressBar.setVisible(true);
		progressBar.setStringPainted(true);
	}
	
	/**
	 * Performs database export to SQL file
	 * @param exportFile A file to save the database SQL statements to
	 * @return true if successful, false otherwise
	 */
	private void exportDatabase(File exportFile) {
		// Adjust controls 
		updateProgressBar(0, true);
		setEnableControls(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		// Gather SQL instruction set
		ArrayList<String> exportStatements = null;
		try {
			exportStatements = SQLiteDBManager.getManager().exportDatabase();
			updateProgressBar(50, true);
		} catch (SQLException | IOException exception) { 
			JOptionPane.showMessageDialog(null, 
				String.format("An error occurred while generating backup (Reason: %s)", exception.getMessage()), 
				"Export failed", JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE);
		} 
		
		// Write data to file
		if (exportStatements != null) {
			try (FileWriter fileWriter = new FileWriter(exportFile);
				 BufferedWriter backupWriter = new BufferedWriter(fileWriter)){
				for (int lineNumber = 0; lineNumber < exportStatements.size(); lineNumber++) {					
					backupWriter.write(exportStatements.get(lineNumber) + "\n");
					
					// Calculate percentage of data written assuming 50% completed during
					// database sql statement generation
					double writtenPercentage = (((((float)lineNumber + 1) / ((float)exportStatements.size())) * 0.5) + 0.5) * 100.0;
					updateProgressBar((int) writtenPercentage, true);
				}
			} catch (IOException exception) {
				JOptionPane.showMessageDialog(null, 
					String.format("An error occured while writing backup data to %s (Reason: %s)", 
							exportFile.getAbsolutePath(), exception.getMessage()), 
					"Export failed", JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE);
			}
		}
		
		// Restore window functionality
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setEnableControls(true);		
	}
	
	
	
	
	/**
	 * Performs database import from SQL file
	 * 
	 * @param importFile A file to load SQL statements from
	 */
	private void importDatabase(File importFile) {
		int response = JOptionPane.showConfirmDialog(this, 
				"You are about to perform an irreversible operation.\nYou should ensure you have a backup "
				+ "before continuing with this operation.\n\nDo you wish to continue?", 
				"Database Import Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if (response == JOptionPane.YES_OPTION) {
			// Adjust controls 
			updateProgressBar(0, false);
			setEnableControls(false);
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			
			// Perform database import within a separate thread.  
			ImportRunner importer = new ImportRunner(overwriteDatabase.isSelected(), importFile, 
					progressBar, this);
			Thread importerThread = new Thread(importer);
			importerThread.start();			
		}
	}
	
	/**
	 * Callback function executed once an import operation 
	 * has completed. Called from the ImportRunner
	 * 
	 * @param importer An ImportRunner used to validate the completion status
	 */
	public void importCompleted(ImportRunner importer) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setEnableControls(true);
		
		String message = ""; 
		String title = "";
		int dialogOptions = 0;
		
		if (importer.dropTablesFailed()) {
			message = "Failed to drop/truncate database. Contact System Administrators if "
					+ "problems persist. Now exiting the application.\n\nReason: %s";
			message = String.format(message, importer.getException().getMessage());
			title = "Database drop/truncate failed";
			dialogOptions = JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE;
			
		} else if (importer.databaseErrorOccured()) {
			message = "A failue occured while importing to the database. Contact System "
					+ "Administrators if problems persist. Now exiting the application.\n\nReason: %s";
			message = String.format(message, importer.getException().getMessage());
			title = "Database import occured failed";
			dialogOptions = JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE;
			
		} else if (importer.importFileReadErrorOccured()) {
			message = "Unable to read import file. Contact System "
					+ "Administrators if problems persist. Now exiting the application.\n\nReason: %s";
			message = String.format(message, importer.getException().getMessage());
			title = "Import file unreadable";
			dialogOptions = JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE;
			
		} else {
			message = "Import process completed. The application will now exit to apply changes.";
			title = "Import completed successfully";
			dialogOptions = JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE;
			
		}
		
		JOptionPane.showMessageDialog(this, message, title, dialogOptions);
		System.exit(0);
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
	
	/**
	 * Performs import operations in a separate thread
	 * to allow the UI to continue processing events. 
	 * 
	 * @author Russell Yorke
	 *
	 */
	@SuppressWarnings("unused")
	private class ImportRunner implements Runnable {
		private Integer totalOperations = 0;
		private Integer currentOperation = 0; 
		private boolean dropTables; 
		private File importFile;		
		private JProgressBar progressBar; 		
		private boolean dropTablesFailed = false; 
		private boolean databaseErrorOccured = false; 
		private boolean importFileReadErrorOccured = false; 
		private Exception exceptionCaught = null; 
		private InventoryDBManagementFrame owner = null;
		
		/**
		 * Configures a new database import runner
		 * 
		 * @param dropTables If true the database tables will be dropped first
		 * @param importFile A File to import into the database
		 */
		public ImportRunner(boolean dropTables, File importFile, JProgressBar progressBar, 
				InventoryDBManagementFrame owner) {
			this.dropTables = dropTables;			
			this.importFile = importFile;
			this.progressBar = progressBar;
			this.owner = owner;
		}
		
		/**
		 * Provides access to an exception if one occured
		 * 
		 * @return An exception that occurred during execution
		 */
		public Exception getException() {
			return exceptionCaught;
		}
		
		/** 
		 * Provides the status of the drop tables operations. When this 
		 * is true an exception will be set. 
		 * 
		 * @return True if dropping tables failed, false otherwise. 
		 */
		public boolean dropTablesFailed() {
			return dropTablesFailed;
		}
		
		/**
		 * Provides the status of the database import operations. 
		 * When this is true an exception will be set. 
		 * 
		 * @return True if a database error occurred during import, false otherwise. 
		 */
		public boolean databaseErrorOccured() {
			return databaseErrorOccured;
		}
		
		/**
		 * Provides the status of the file reading operations for the import file. 
		 * When this is true an exception will be set. 
		 * 
		 * @return True if a file read operation failed, false otherwise. 
		 */
		public boolean importFileReadErrorOccured() {
			return importFileReadErrorOccured;
		}
		
		/**
		 * The total number of operations required to complete
		 * this execution. 
		 * @return The total number of operations to complete execution
		 */
		public Integer getTotalOperations() {
			return totalOperations;
		}
		
		/**
		 * Returns the current operation number completed
		 * so far. 
		 * 
		 * @return An Integer representing the current operation completed. 
		 */
		public Integer getCurrentOperation() {
			return currentOperation;
		}
		
		/**
		 * Performs a database import operations when called. 
		 * This will updated the current operation as well as the
		 * total number of operations within this process. 
		 */
		@Override
		public void run() {
			// Truncate tables if requested
			if (dropTables) {
				try {
					SQLiteDBManager.getManager().dropAllTables();
				} catch (SQLException | IOException exception) {
					dropTablesFailed = true;
					exceptionCaught = exception;					
				}
			}

			// Import data
			if (!dropTablesFailed) {
				try (FileReader fileReader = new FileReader(importFile);
					 BufferedReader dataReader = new BufferedReader(fileReader)) {
					
					// Intaialization metadata about import
					totalOperations = countImportFileLines(importFile) * 2;

					
					// Read the data and import it into the database
					// incrementing the number of completed operations between each stage
					try (Connection dbConnection = SQLiteDBManager.getManager().getConnection(false)) {
						String sqlStatement = null;
						try {
							 sqlStatement = dataReader.readLine();
						} catch (IOException exception) {
							importFileReadErrorOccured = true;
							exceptionCaught = exception;
						}
						
						while (sqlStatement != null) {
							currentOperation++;
							updateProgressBar();
							
							if (sqlStatement.trim().length() > 0) {
								SQLiteDBManager.getManager().executeRawStatement(dbConnection, 
										sqlStatement);
							}
							currentOperation++;
							updateProgressBar();
							
							try {
								 sqlStatement = dataReader.readLine();
							} catch (IOException exception) {
								importFileReadErrorOccured = true;
								exceptionCaught = exception;
								break; 
							}
						}						
					} catch (SQLException | IOException exception) {
						databaseErrorOccured = true;
						exceptionCaught = exception;
					}
				} catch (IOException exception) {
					importFileReadErrorOccured = true;
					exceptionCaught = exception;
				}										
			}
			
			owner.importCompleted(this);
		}
		
		/**
		 * Counts the total number of lines within an import file (to be 
		 * used for the progress bar import progress)
		 * 
		 * @param importFile A file containing lines of SQL statements
		 * @return Total number of lines in a file
		 * @throws IOException If an error occurs while reading the importFile
		 */
		private int countImportFileLines(File importFile) throws IOException {
			int lineCount = 0;
			try (FileReader fileReader = new FileReader(importFile);
				 BufferedReader dataReader = new BufferedReader(fileReader)) {
				while (dataReader.readLine() != null) lineCount++;			
			}
			
			return lineCount;
		}
		
		/**
		 * Updates a progress bar including the text to display on it. 
		 * 
		 * @param currentValue A value between 0 and 100 to adjust the progress bar
		 * @param exporting If true "% Exported" will be displayed, otherwise "% Imported" will be displayed. 
		 */
		private void updateProgressBar() {
			int completed = (int) (((float)currentOperation) / ((float)totalOperations) * 100.0F);
			String progressStatusMessage = String.format("%d of %d (%d%%) import operations completed", currentOperation / 2, totalOperations / 2, completed);
			progressBar.setValue(completed);
			progressBar.setString(progressStatusMessage);
			progressBar.setVisible(true);
			progressBar.setStringPainted(true);
		}
	}
}