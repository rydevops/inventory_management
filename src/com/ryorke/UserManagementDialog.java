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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

import com.ryorke.database.UserEntityManager;
import com.ryorke.entity.User;
import com.ryorke.entity.exception.InvalidUserAttributeException;

/**
 * User management provides functionality to adding, deleting and modifying
 * users. 
 * 
 * @author Russell Yorke
 */
@SuppressWarnings("serial")
public class UserManagementDialog extends JDialog {
	public final static String WINDOW_TITLE = "User Management";	
	
	private JTable userTable;
	private JButton addUser;
	private JButton deleteUser;
	private JButton editUser;
	private JButton closeWindow;
	private JTextField username;
	private JPasswordField password;
	private JPasswordField confirmPassword;	
	private JTextField firstName;
	private JTextField lastName;
	private JCheckBox isAdministrator;
	
	private UserTableModel userTableModel;
	
	// Flag to monitor the status of the password fields
	// when editing a user. If true, a password check will
	// be performed otherwise the password will be unchanged
	// enabling modifications without changing the password. 
	private boolean passwordModified = false; 
	
	/**
	 * Creates a new window with a default title
	 * 
	 * @throws SQLException If a database error occurs during processing
	 * @throws IOException If database file cannot be accessed
	 * @throws InvalidUserAttributeException If database manager was unable to process users
	 */
	public UserManagementDialog(Frame owner) throws SQLException, IOException, InvalidUserAttributeException {
		this(owner, UserManagementDialog.WINDOW_TITLE);
	}
	
	/**
	 * Creates a new user management window and initializes the user list from
	 * the database
	 *  
	 * @param title Window title
	 * 
	 * @throws SQLException If a database error occurs during processing
	 * @throws IOException If database file cannot be accessed
	 */
	public UserManagementDialog(Frame owner, String title) throws SQLException, IOException, InvalidUserAttributeException {
		super(owner, title, true);
		
		// Initializing the table model early to avoid creating the 
		// dialog if an error occurs
		userTableModel = new UserTableModel();
				
		Container contentPane = getContentPane();
		((JPanel)contentPane).setBorder(BorderFactory.createEmptyBorder(5, 5,5, 5));	

		createUserEntryForm(contentPane);
		createUserTable(contentPane);
		createButtonsPanel(contentPane);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);		
		setTitle(title);
		setMinimumSize(new Dimension(600, 250));
		setSize(getMinimumSize());		
		setLocationRelativeTo(null);		
	}
		
	/**
	 * Creates the user entry form for add/updating a user
	 * 
	 * @param contentPane The content pane configured with BorderLayout. Buttons will be added to the NORTH zone
	 */
	private void createUserEntryForm(Container contentPane) {
		GridBagLayout formLayout = new GridBagLayout();
		GridBagConstraints constraint = new GridBagConstraints();		
		constraint.weightx = 0;
		constraint.weighty = 0;
		constraint.gridwidth = 1;
		constraint.gridheight = 1;
		constraint.insets = new Insets(2,2,2,2);
		constraint.fill = GridBagConstraints.HORIZONTAL;
		
		JPanel form = new JPanel(formLayout);		
		
		username = new JTextField();
		JLabel usernameLabel = createLabel("Username:", SwingConstants.RIGHT, KeyEvent.VK_U, username);
		addComponent(form, formLayout, constraint, usernameLabel);
		setConstraint(constraint, 1, 0, 1, 1);
		addComponent(form, formLayout, constraint, username);
				
		password = new JPasswordField();
		JLabel passwordLabel = createLabel("Password:", SwingConstants.RIGHT, KeyEvent.VK_P, password);
		setConstraint(constraint, 0, 0, 1, 1);
		addComponent(form, formLayout, constraint, passwordLabel );
		setConstraint(constraint, 1, 0, 1, 1);
		addComponent(form, formLayout, constraint, password);
		password.addKeyListener(new KeyListener() {
			/**
			 * Updates the password modified flag to be true after any key-press event
			 * @param e Event details
			 */
			@Override
			public void keyPressed(KeyEvent e) {
				passwordModified = true;
			}
			
			/**
			 * Unused/not implemented
			 * @param e
			 */
			@Override
			public void keyTyped(KeyEvent e) {}
			
			/**
			 * Unused/not implemented
			 * @param e Event details
			 */
			@Override
			public void keyReleased(KeyEvent e) {}			
		});
		
		confirmPassword = new JPasswordField();
		JLabel confirmPasswordLabel = createLabel("Confirm Password:", SwingConstants.RIGHT, KeyEvent.VK_C, confirmPassword);
		setConstraint(constraint, 0, 0, 1, 1);
		addComponent(form, formLayout, constraint, confirmPasswordLabel);
		setConstraint(constraint, 1, 0, GridBagConstraints.REMAINDER, 1);
		addComponent(form, formLayout, constraint, confirmPassword);
		confirmPassword.addKeyListener(new KeyListener() {
			/**
			 * Updates the password modified flag to be true after any key-press event
			 * @param e Event details
			 */
			@Override
			public void keyPressed(KeyEvent e) {
				passwordModified = true;
			}
			
			/**
			 * Unused/not implemented
			 * @param e
			 */
			@Override
			public void keyTyped(KeyEvent e) {}
			
			/**
			 * Unused/not implemented
			 * @param e Event details
			 */
			@Override
			public void keyReleased(KeyEvent e) {}			
		});
		
		firstName = new JTextField();
		JLabel firstNameLabel = createLabel("First Name:", SwingConstants.RIGHT, KeyEvent.VK_F, firstName);
		setConstraint(constraint, 0, 0, 1, 1);
		addComponent(form, formLayout, constraint, firstNameLabel);
		setConstraint(constraint, 1, 0, 1, 1);
		addComponent(form, formLayout, constraint, firstName);
				
		lastName = new JTextField();
		JLabel lastNameLabel = createLabel("Last Name:", SwingConstants.RIGHT, KeyEvent.VK_L, lastName);
		setConstraint(constraint, 0, 0, 1, 1);
		addComponent(form, formLayout, constraint, lastNameLabel);
		setConstraint(constraint, 1, 0, 1, 1);
		addComponent(form, formLayout, constraint, lastName);
				
		isAdministrator = new JCheckBox();
		JLabel administratorLabel = createLabel("Administrator:", SwingConstants.RIGHT, KeyEvent.VK_A, isAdministrator);
		setConstraint(constraint, 0, 0, 1, 1);
		addComponent(form, formLayout, constraint, administratorLabel);
		setConstraint(constraint, 1, 0, GridBagConstraints.REMAINDER, 1);
		addComponent(form, formLayout, constraint, isAdministrator);
		
		enableFormEntry(false);
		
		contentPane.add(form, BorderLayout.NORTH);
	}
	
	/**
	 * Helper method for creating JLabels
	 * 
	 * @param text The label value
	 * @param alignment Text alignment using SwingConstants
	 * @param mnemonic ALT key attached to label  (from KeyEvent)
	 * @param labelFor Associates label with component (sends focus to that component when label gets focus)
	 * @return A fully configured JLabel
	 */
	private JLabel createLabel(String text, int alignment, int mnemonic, Component labelFor) {
		JLabel label = new JLabel(text, alignment);
		label.setDisplayedMnemonic(mnemonic);
		label.setLabelFor(labelFor);
		return label;
	}
	
	/**
	 * Creates the existing user table displayed on the form
	 * 
	 * @param contentPane The content pane configured with BorderLayout. Buttons will be added to the CENTER zone
	 */
	private void createUserTable(Container contentPane) {
		userTable = new JTable(userTableModel);
		userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane userTableScrollView = new JScrollPane(userTable);		
		contentPane.add(userTableScrollView, BorderLayout.CENTER);
		
		userTable.addMouseListener(new MouseListener() {
			
			/**
			 * Captures double click events on the table moving into
			 * edit user mode. 
			 * 
			 * @param e Details about the mouse click event. 
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!(addUser.getText().equals("Save user")) && e.getClickCount() == 2 && !e.isConsumed()) {					
					editUser();
				}				
			}
			
			/**
			 * Not implemented
			 * @param e Details about the mouse click event. 
			 */
			@Override
			public void mouseReleased(MouseEvent e) {}
			
			/**
			 * Not implemented
			 * @param e Details about the mouse click event. 
			 */
			@Override
			public void mousePressed(MouseEvent e) {}
			
			/**
			 * Not implemented
			 * @param e Details about the mouse click event. 
			 */
			@Override
			public void mouseExited(MouseEvent e) {}
			
			/**
			 * Not implemented
			 * @param e Details about the mouse click event. 
			 */
			@Override
			public void mouseEntered(MouseEvent e) {}		
		});
	}
	
	/**
	 * Creates the button panel displayed on the screen
	 * 
	 * @param contentPane The content pane configured with BorderLayout. Buttons will be added to the SOUTH zone
	 */
	private void createButtonsPanel(Container contentPane) {
		GridLayout buttonLayout = new GridLayout(1, 4);
		buttonLayout.setHgap(5); // padding
		
		JPanel buttonPanel = new JPanel(buttonLayout);	
		
		addUser = createButton("Add user", KeyEvent.VK_D);
		editUser = createButton("Edit user", KeyEvent.VK_I);
		deleteUser = createButton("Delete User", KeyEvent.VK_E);
		closeWindow = createButton("Close", KeyEvent.VK_O);
		
		addUser.addActionListener(new ActionListener() {
			/**
			 * Switches to add user mode
			 * @param e Event details
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = ((JButton) e.getSource()).getText();
				
				if (text.equals("Add user")) {
					addUser();
				} else {
					saveUser();
				}
			}
		});
		
		editUser.addActionListener(new ActionListener() {
			/**
			 * Switches to edit user mode for selected user
			 * @param e Event details
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				editUser();
			}
		});
		
		deleteUser.addActionListener(new ActionListener() {
			/**
			 * Deletes selected user
			 * @param e Event details
			 */
			@Override
			public void actionPerformed(ActionEvent e) {				
				deleteUser();
			}
		});
		
		closeWindow.addActionListener(new ActionListener() {
			/**
			 * Closes this window when button pressed
			 * 
			 * @param e Source of the event
			 */
			@Override
			public void actionPerformed(ActionEvent e) {				
				UserManagementDialog.this.dispatchEvent(new WindowEvent(UserManagementDialog.this, WindowEvent.WINDOW_CLOSING));
			}
		});
		
		buttonPanel.add(addUser);
		buttonPanel.add(editUser);
		buttonPanel.add(deleteUser);
		buttonPanel.add(closeWindow);
		
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Helper to create buttons
	 * 
	 * @param text Value of the button
	 * @param mnemonic Desired mnemonic (from KeyEvent)
	 * @return
	 */
	private JButton createButton(String text, int mnemonic) {
		JButton button = new JButton(text);
		button.setMnemonic(mnemonic);
		return button;
	}
	
	/**
	 * Helper method for easily modifying GridBagLayout constraints
	 * 
	 * @param constraint An existing constraint to be modified
	 * @param weightx new x-weight
	 * @param weighty new y-weight
	 * @param gridwidth new width
	 * @param gridheight new height
	 */
	private void setConstraint(GridBagConstraints constraint, int weightx, int weighty, int gridwidth, int gridheight) {
		constraint.weightx = weightx;
		constraint.weighty = weighty;
		constraint.gridwidth = gridwidth;
		constraint.gridheight = gridheight;
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
	 * Enables/Disables the individual input form elements within the user entry form
	 * 
	 * @param enabled True if fields should be enabled (allowing input), false otherwise. 
	 */
	private void enableFormEntry(boolean enabled) {
		username.setEnabled(enabled);
		password.setEnabled(enabled);
		confirmPassword.setEnabled(enabled);
		firstName.setEnabled(enabled);
		lastName.setEnabled(enabled);
		isAdministrator.setEnabled(enabled);		
	}
	
	/**
	 * Clears all form entry fields of any data they may have 
	 */
	private void clearFormEntry() {
		username.setText("");
		password.setText("");
		confirmPassword.setText("");
		firstName.setText("");
		lastName.setText("");
		isAdministrator.setSelected(false);		
	}
	
	/**
	 * Transitions the view to allow new user details to be provided.  
	 */
	private void addUser() {
		enableFormEntry(true);
		clearFormEntry();		
		
		toggleAddUserLabel();
		editUser.setEnabled(false);
		deleteUser.setEnabled(false);
		
		username.requestFocus();
		
		passwordModified = true; // Force a password to be provided
	}
	
	/**
	 * Saves the user information to the database when adding a new user or editing
	 * an existing user. 
	 */
	private void saveUser() {
		String username = this.username.getText();
		String password = new String(this.password.getPassword());
		String confirmPassword = new String(this.confirmPassword.getPassword());
		String firstName = this.firstName.getText();
		String lastName = this.lastName.getText();
		boolean isAdministrator = this.isAdministrator.isSelected();		 
		
		int errorDisplayOptions = JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE;
		String errorTitle = "Invalid user details";
		
		if (passwordModified && !password.equals(confirmPassword)) {
			JOptionPane.showMessageDialog(this, "Password and confirm password do not match", errorTitle, errorDisplayOptions);
			this.password.setText("");
			this.confirmPassword.setText("");
			this.password.requestFocus();
			return;
		}
		
		try {
			// Determine if this is a new user add or an existing user edit
			// based on the state of the userTable. If disabled, this only
			// occurs when a user is being edited to prevent the selection from
			// being changed
			User user = null;
			if (userTable.isEnabled()) {
				user = new User(username, password, firstName, lastName, isAdministrator);
				userTableModel.addUser(user);
			} else {
				int selectedRow = userTable.getSelectedRow();				
				User modifiedUser = userTableModel.getUser(selectedRow).clone();
				modifiedUser.setUsername(this.username.getText());
				modifiedUser.setFirstName(this.firstName.getText());
				modifiedUser.setLastName(this.lastName.getText());
				modifiedUser.setAdministrator(this.isAdministrator.isSelected());
				if (passwordModified) {
					modifiedUser.setPassword(new String(this.password.getPassword()));
				}
				userTableModel.updateUser(modifiedUser);
			}			
		} catch (InvalidUserAttributeException exception) {
			JOptionPane.showMessageDialog(this, exception.getMessage(), errorTitle, errorDisplayOptions);
			this.username.requestFocus();
			return;
		} catch (SQLException sqlException) {
			String sqlErrorMessage = String.format("Unable to create user. If problem persist contact System Administrator.\nReason:\n%s", 
					sqlException.getMessage());
			JOptionPane.showMessageDialog(this, sqlErrorMessage, errorTitle, errorDisplayOptions);
			return;
		}
		
		toggleAddUserLabel();
		enableFormEntry(false);
		clearFormEntry();
		
		editUser.setEnabled(true);
		deleteUser.setEnabled(true);
		userTable.setEnabled(true);
	}
	
	/**
	 * Transitions the form into edit user mode allowing the selected user to be modified. 
	 * The password is optional and only checked/changed if the password field is changed otherwise
	 * the fill data in these fields are ignored. 
	 */
	private void editUser() {
		int selectedRow = userTable.getSelectedRow(); 
		if (selectedRow > -1) {
			User selectedUser = userTableModel.getUser(selectedRow);		
			
			username.setText(selectedUser.getUsername());
			password.setText("***********");
			confirmPassword.setText("***********");
			firstName.setText(selectedUser.getFirstName());
			lastName.setText(selectedUser.getLastName());
			isAdministrator.setSelected(selectedUser.isAdministrator());		
			
			enableFormEntry(true);
			editUser.setEnabled(false);
			deleteUser.setEnabled(false);
			userTable.setEnabled(false);		
			toggleAddUserLabel();
			
			passwordModified = false;		
			username.requestFocus();
		} else {
			String errorMessage = "A user must be selected before performing an edit operation.";
			int options = JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE;
			JOptionPane.showMessageDialog(this, errorMessage, "No user selected", options);
		}
	}
	
	/**
	 * Toggles the label that appears on the add user button
	 */
	private void toggleAddUserLabel() {
		final String[] values = { "Add user", "Save user" };
		if (addUser.getText().equals(values[0])) {
			addUser.setText(values[1]);			
			addUser.setMnemonic(KeyEvent.VK_S);
		} else {
			addUser.setText(values[0]);
			addUser.setMnemonic(KeyEvent.VK_D);
		}		
	}
	
	/**
	 * Deletes the selected user after confirming this operation
	 */
	private void deleteUser() {
		int selectedRow = userTable.getSelectedRow();
		String message = "";		
		
		if (selectedRow > -1) {
			message = "Are you sure you want to delete the selected user?";			
			
			int response = JOptionPane.showConfirmDialog(this, message, "Delete user?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.YES_OPTION) {
				try {
					userTableModel.deleteUser(selectedRow);
				} catch (SQLException | InvalidUserAttributeException exception) {
					int errorDisplayOptions = JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE;
					String errorTitle = "User deletion failed";					
					String sqlErrorMessage = String.format("Unable to delete user. If problem persist contact System Administrator.\nReason:\n%s", 
							exception.getMessage());
					JOptionPane.showMessageDialog(this, sqlErrorMessage, errorTitle, errorDisplayOptions);
				}				
			}
		} else {
			message = "You must selected a user to delete.";			
			JOptionPane.showMessageDialog(this, message, "No user selected", JOptionPane.OK_OPTION | JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	/**
	 * Manages a collection of users to be displayed in a JTable
	 * 
	 * @author Russell Yorke
	 */
	class UserTableModel extends AbstractTableModel {
		public static final int FIELD_USERNAME = 0;
		public static final int FIELD_FIRST_NAME = 1;
		public static final int FIELD_LAST_NAME = 2;
		public static final int FIELD_ADMINISTRATOR = 3;
		
		private String[] columnNames = { "Username", "First Name", "Last Name", "Administrator" };
		private ArrayList<User> users; 
		private UserEntityManager manager;
		
		/**
		 * Inserts a new user into the record set
		 * @param user New user to insert into the database
		 * @throws SQLException If an error occurs while attempting to insert the record 
		 */
		public void addUser(User user) throws SQLException {
			manager.createUser(user);
			users.add(user);
			fireTableRowsInserted(users.size() - 1, users.size() - 1);			
		}
		
		/**
		 * Retrieves an existing user
		 * @param rowIndex A model row index used to retrieve the selected users
		 * @return An existing user or null if rowIndex is invalid
		 */
		public User getUser(int rowIndex) {
			User user = null;
			if (rowIndex >= 0 && rowIndex < users.size()) {
				user = users.get(rowIndex);
			}
			return user;
		}
		
		/**
		 * Pushes the existing user information into the database and updates the 
		 * table model to reflect the changes.  
		 * 
		 * @param rowIndex The row to update (if invalid, nothing occurs)
		 * @throws SQLException If an error occurs while attempting to update the record 
		 * @throws InvalidUserAttributeException if user being delete is the last administrator
		 */
		public void updateUser(User modifiedUser) throws SQLException, InvalidUserAttributeException {
			User selectedUser = null;
			int selectedIndex = -1;
			for (User user : users) {
				if (user.getUserId() == modifiedUser.getUserId()) {
					selectedUser = user; 
					break;
				}
			}
			selectedIndex = users.indexOf(selectedUser);
			
			if (modifiedUser.isAdministrator() == true || administratorCount(selectedUser) > 0) {
				manager.updateUser(modifiedUser);
				users.remove(selectedIndex);
				users.add(selectedIndex, modifiedUser);
				fireTableRowsUpdated(selectedIndex, selectedIndex);
			} else {
				throw new InvalidUserAttributeException("At least one administrator must exist in the system.");
			}
		}
		
		/**
		 * Deletes the selected row from the database and model
		 * 
		 * @throws SQLException If a database error occurs
		 * @throws InvalidUserAttributeException if user being delete is the last administrator
		 */
		public void deleteUser(int rowIndex) throws SQLException, InvalidUserAttributeException {
			if (rowIndex >= 0 && rowIndex < users.size()) {				
				User user = users.get(rowIndex);
				
				if (administratorCount(user) > 0) {
					manager.deleteUser(user);
					users.remove(rowIndex);
					fireTableRowsDeleted(rowIndex, rowIndex);
				} else {
					throw new InvalidUserAttributeException("At least one administrator must exist in the system.");
				}
			}
		}
		
		/**
		 * Counts the total number of administrators excluding the provided user
		 * 
		 * @param user The user to not count
		 * @return The total number of admins not including user
		 */
		private int administratorCount(User user) {
			int count = 0; 
			for (User existingUser : users) {
				if (existingUser != user && existingUser.isAdministrator()) {
					count++;
				}
			}
			return count;
		}
		
		/**
		 * Creates a new user table row model initializing the list of available users.
		 * @throws SQLException If an error occurs while trying to open the SQL database
		 * @throws IOException If an error occurs while accessing the SQL database
		 * @throws InvalidUserAttributeException If the database manager had problems creating a user
		 */
		public UserTableModel() throws SQLException, IOException, InvalidUserAttributeException {			
			manager = UserEntityManager.getManager();
			users = manager.getUsers();			
		}
		
		/**
		 * Reports the number of columns in the user model
		 * @return The total number of rows in header
		 */
		@Override
		public int getColumnCount() {
			return columnNames.length;			
		}

		/**
		 * Reports the number of users within the model
		 * @return Total rows of data available
		 */
		@Override
		public int getRowCount() {
			int totalRows = 0;
			
			if (users != null)
				totalRows = users.size();
				
			return totalRows;
		}

		/**
		 * Returns the name of the requested column
		 * @param column column index
		 * @return A column name
		 */
		@Override
		public String getColumnName(int column) {			
			return columnNames[column];
		}
		
		/**
		 * Retrieves data for a specific cell
		 * @param rowIndex Row of the specific data
		 * @param columnIndex Column of the specific data
		 * @return A new cell object
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			User selectedUser = users.get(rowIndex);
			Object result = null;
			
			switch (columnIndex) {
			case FIELD_USERNAME:
				result = selectedUser.getUsername();
				break;
			case FIELD_FIRST_NAME:
				result = selectedUser.getFirstName();
				break;
			case FIELD_LAST_NAME:
				result = selectedUser.getLastName();
				break;
			case FIELD_ADMINISTRATOR:
				result = (selectedUser.isAdministrator()) ? "YES" : "NO";
				break;
			default:
				// It should not be possible to reach this statement. If this occurs, 
				// it means a new column has been added that hasn't been accounted for. 
				assert (columnIndex < FIELD_USERNAME || columnIndex > FIELD_ADMINISTRATOR): "Invalid column index requested in UserTableModel";	
			}
			
			return result;
		}
	}
	
	
}
