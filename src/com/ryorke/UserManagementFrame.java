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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

import com.ryorke.database.UserEntityManager;
import com.ryorke.entity.User;
import com.ryorke.entity.exception.InvalidUserAttributeException;

// TODO: 
// 1. Make table sortable
// 2. Edit user functionality (assume table model and view model are sortable)
// 3. Delete user functionality with prompt\
// 	  Ensure at least 1 administrator is left in the system don't allow all users to be deleted. 
// 4. Add catch while in editing mode (either Add user or edit) that prevents accidental
//	  closing of the window by prompting if they are sure. 
/**
 * User management provides functionality to adding, deleting and modifying
 * users. 
 * 
 * @author Russell Yorke
 */
@SuppressWarnings("serial")
public class UserManagementFrame extends JDialog {
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
	
	
	/**
	 * Creates a new window with a default title
	 * 
	 * @throws SQLException If a database error occurs during processing
	 * @throws IOException If database file cannot be accessed
	 * @throws InvalidUserAttributeException If database manager was unable to process users
	 */
	public UserManagementFrame() throws SQLException, IOException, InvalidUserAttributeException {
		this(UserManagementFrame.WINDOW_TITLE);
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
	public UserManagementFrame(String title) throws SQLException, IOException, InvalidUserAttributeException {
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
		
		confirmPassword = new JPasswordField();
		JLabel confirmPasswordLabel = createLabel("Confirm Password:", SwingConstants.RIGHT, KeyEvent.VK_C, confirmPassword);
		setConstraint(constraint, 0, 0, 1, 1);
		addComponent(form, formLayout, constraint, confirmPasswordLabel);
		setConstraint(constraint, 1, 0, GridBagConstraints.REMAINDER, 1);
		addComponent(form, formLayout, constraint, confirmPassword);
				
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
		JScrollPane userTableScrollView = new JScrollPane(userTable);
		contentPane.add(userTableScrollView, BorderLayout.CENTER);
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
		
		closeWindow.addActionListener(new ActionListener() {
			/**
			 * Closes this window when button pressed
			 * 
			 * @param e Source of the event
			 */
			@Override
			public void actionPerformed(ActionEvent e) {				
				UserManagementFrame.this.dispatchEvent(new WindowEvent(UserManagementFrame.this, WindowEvent.WINDOW_CLOSING));
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
	
	private void addUser() {
		enableFormEntry(true);
		clearFormEntry();

		addUser.setText("Save user");
		addUser.setMnemonic(KeyEvent.VK_S);
		
		editUser.setEnabled(false);
		deleteUser.setEnabled(false);
		
		username.requestFocus();
	}
	
	private void saveUser() {
		String username = this.username.getText();
		String password = new String(this.password.getPassword());
		String confirmPassword = new String(this.confirmPassword.getPassword());
		String firstName = this.firstName.getText();
		String lastName = this.lastName.getText();
		boolean isAdministrator = this.isAdministrator.isSelected();		 
		
		int errorDisplayOptions = JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE;
		String errorTitle = "Invalid user details";
		
		if (!password.equals(confirmPassword)) {
			JOptionPane.showMessageDialog(this, "Password and confirm password do not match", errorTitle, errorDisplayOptions);
			this.password.setText("");
			this.confirmPassword.setText("");
			this.password.requestFocus();
			return;
		}
		
		try {
			User newUser = new User(username, password, firstName, lastName, isAdministrator);
			userTableModel.addUser(newUser);
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
		
		enableFormEntry(false);
		clearFormEntry();
		
		editUser.setEnabled(true);
		deleteUser.setEnabled(true);
		
		addUser.setText("Add user");
		addUser.setMnemonic(KeyEvent.VK_D);
		
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
