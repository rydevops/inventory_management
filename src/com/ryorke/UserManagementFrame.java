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
import java.awt.Container;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.ryorke.database.UserEntityManager;
import com.ryorke.entity.User;

/**
 * User management provides functionality to adding, deleting and modifying
 * users. 
 * 
 * @author Russell Yorke
 */
@SuppressWarnings("serial")
public class UserManagementFrame extends JFrame {
	public final static String WINDOW_TITLE = "User Management";
	
	private JList<User> userList;
	private JButton addUser;
	private JButton deleteUser;
	private JButton modifyUser;
	private JButton changePassword;
	private JTextField username;
	private JTextField firstName;
	private JTextField lastName;
	private JCheckBox isAdministrator;
	
	private UserEntityManager manager;
	
	/**
	 * Creates a new window with a default title
	 */
	public UserManagementFrame() {
		this(UserManagementFrame.WINDOW_TITLE);
	}
	
	/**
	 * Creates a new user management frame
	 *  
	 * @param title Window title
	 */
	public UserManagementFrame(String title) {
		
		Container contentPane = getContentPane();
		((JPanel)contentPane).setBorder(BorderFactory.createEmptyBorder(5, 5,5, 5));
		
		contentPane.add(createUserListPanel(), BorderLayout.WEST);
		contentPane.add(createUserForm(), BorderLayout.CENTER);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(680,230);
		setLocationRelativeTo(null);
		setTitle(title);
		setResizable(false);
		setVisible(true);
	}
	
	/**
	 * Create a panel with a list of users
	 * @return a panel containing a list of users
	 */
	private JPanel createUserListPanel() {
		ArrayList<User> databaseUserList = null; 
		JPanel userListPanel = null; 
		
		try {
			// Poll database for user list
			manager = UserEntityManager.getManager();
			databaseUserList = manager.getUsers();
			
			// Configure panel
			BorderLayout layoutManager = new BorderLayout();
			layoutManager.setHgap(5);
			layoutManager.setVgap(5);
			
			JLabel usersLabel = new JLabel("Users:");
			DefaultListModel<User> listModel = new DefaultListModel<User>();
			for (User user : databaseUserList) {
				listModel.addElement(user);
			}
			listModel.addElement(new User(5, "ryorke1", "P@ssword", "Russell", "Yorke", true));
			userList = new JList<User>(listModel);
			userList.setFixedCellWidth(150);
			userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			userList.addListSelectionListener(new ListSelectionListener() {				
				@Override
				public void valueChanged(ListSelectionEvent e) {
					User user = userList.getSelectedValue();					
					username.setText(user.getUsername());
				    firstName.setText(user.getFirstName());
				    lastName.setText(user.getLastName());
				    isAdministrator.setSelected(user.isAdministrator());					
				}
			});		
			
			JScrollPane userlistScroller = new JScrollPane();
			userlistScroller.setViewportView(userList);
			
			userListPanel = new JPanel(layoutManager);
			
			userListPanel.add(usersLabel, BorderLayout.NORTH);
			userListPanel.add(userlistScroller);			
		} catch (IOException | SQLException exception) {
			JOptionPane.showMessageDialog(this, "Failed to query database for user list", "Database error", 
					JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE);
		}
				
		return userListPanel;		
	}
	
	/**
	 * Creates a user view form for displaying
	 * user objects
	 * 
	 * @return A new user form
	 */
	private JPanel createUserForm() {
		SpringLayout layoutManager = new SpringLayout();
		JPanel userFormPanel = new JPanel(layoutManager);
		final int GAP_SPACING = 5; 
		
		// TODO: 
		// Add event handler for handling user selection after modifications to any of the fields
		// Set the first element in the list of users to selected to pre-populate the fields
		// Change "modify" button to "update" button and disable it until changes are made to the user fields
		// Add the add/remove events
		
		
		JLabel usernameLabel = new JLabel("Username:");
		username = new JTextField(15);
		isAdministrator = new JCheckBox("Administrator");
		layoutManager.putConstraint(SpringLayout.WEST, usernameLabel, GAP_SPACING, SpringLayout.WEST, userFormPanel);
		layoutManager.putConstraint(SpringLayout.NORTH, usernameLabel, GAP_SPACING, SpringLayout.NORTH, userFormPanel);		
		layoutManager.putConstraint(SpringLayout.WEST, username, GAP_SPACING, SpringLayout.EAST, usernameLabel);
		layoutManager.putConstraint(SpringLayout.NORTH, username, GAP_SPACING, SpringLayout.NORTH, userFormPanel);
		layoutManager.putConstraint(SpringLayout.EAST, username, -GAP_SPACING, SpringLayout.WEST, isAdministrator);		
		layoutManager.putConstraint(SpringLayout.NORTH, isAdministrator, GAP_SPACING, SpringLayout.NORTH, userFormPanel);
		layoutManager.putConstraint(SpringLayout.EAST, isAdministrator, -GAP_SPACING, SpringLayout.EAST, userFormPanel);		
		userFormPanel.add(usernameLabel);
		userFormPanel.add(username);
		userFormPanel.add(isAdministrator);		
		
		JLabel firstNameLabel = new JLabel("First Name:");
		JLabel lastNameLabel = new JLabel("Last Name:");
		firstName = new JTextField(15);
		lastName = new JTextField(15);
		layoutManager.putConstraint(SpringLayout.NORTH, firstNameLabel, GAP_SPACING, SpringLayout.SOUTH, isAdministrator);
		layoutManager.putConstraint(SpringLayout.WEST, firstNameLabel, GAP_SPACING, SpringLayout.WEST, userFormPanel);		
		layoutManager.putConstraint(SpringLayout.NORTH, firstName, GAP_SPACING, SpringLayout.SOUTH, isAdministrator);
		layoutManager.putConstraint(SpringLayout.WEST, firstName, GAP_SPACING, SpringLayout.EAST, firstNameLabel);		
		layoutManager.putConstraint(SpringLayout.NORTH, lastNameLabel, GAP_SPACING, SpringLayout.SOUTH, isAdministrator);
		layoutManager.putConstraint(SpringLayout.WEST, lastNameLabel, GAP_SPACING, SpringLayout.EAST, firstName);		
		layoutManager.putConstraint(SpringLayout.NORTH, lastName, GAP_SPACING, SpringLayout.SOUTH, isAdministrator);
		layoutManager.putConstraint(SpringLayout.WEST, lastName, GAP_SPACING, SpringLayout.EAST, lastNameLabel);		
		layoutManager.putConstraint(SpringLayout.EAST, lastName, -GAP_SPACING, SpringLayout.EAST, userFormPanel);			
		userFormPanel.add(firstNameLabel);
		userFormPanel.add(firstName);
		userFormPanel.add(lastNameLabel);
		userFormPanel.add(lastName);
		
		changePassword = new JButton("Change password");
		addUser = new JButton("Add new user");
		modifyUser = new JButton("Modify user");
		deleteUser = new JButton("Delete user");
		layoutManager.putConstraint(SpringLayout.NORTH, changePassword, GAP_SPACING, SpringLayout.SOUTH, firstName);
		layoutManager.putConstraint(SpringLayout.WEST, changePassword, GAP_SPACING, SpringLayout.WEST, userFormPanel);
		layoutManager.putConstraint(SpringLayout.EAST, changePassword, -GAP_SPACING, SpringLayout.EAST, userFormPanel);		
		layoutManager.putConstraint(SpringLayout.NORTH, addUser, GAP_SPACING, SpringLayout.SOUTH, changePassword);
		layoutManager.putConstraint(SpringLayout.WEST, addUser, GAP_SPACING, SpringLayout.WEST, userFormPanel);
		layoutManager.putConstraint(SpringLayout.EAST, addUser, -GAP_SPACING, SpringLayout.EAST, userFormPanel);		
		layoutManager.putConstraint(SpringLayout.NORTH, modifyUser, GAP_SPACING, SpringLayout.SOUTH, addUser);
		layoutManager.putConstraint(SpringLayout.WEST, modifyUser, GAP_SPACING, SpringLayout.WEST, userFormPanel);
		layoutManager.putConstraint(SpringLayout.EAST, modifyUser, -GAP_SPACING, SpringLayout.EAST, userFormPanel);		
		layoutManager.putConstraint(SpringLayout.NORTH, deleteUser, GAP_SPACING, SpringLayout.SOUTH, modifyUser);
		layoutManager.putConstraint(SpringLayout.WEST, deleteUser, GAP_SPACING, SpringLayout.WEST, userFormPanel);
		layoutManager.putConstraint(SpringLayout.EAST, deleteUser, -GAP_SPACING, SpringLayout.EAST, userFormPanel);
		userFormPanel.add(changePassword);
		userFormPanel.add(addUser);
		userFormPanel.add(modifyUser);
		userFormPanel.add(deleteUser);
		
		return userFormPanel;		
	}
	
	/**
	 * Saves user to the database
	 * @return true if successful false otherwise
	 */
	private Boolean saveUser() {
		return false;
	}
	
	/**
	 * Deletes user from the database
	 * 
	 * @return true if successful false otherwise
	 */
	private Boolean deleteUser() {
		return false;
	}
	
	/**
	 * Updates user within the database
	 * 
	 * @return true if successful false otherwise
	 */
	private Boolean updateUser() {
		return false;
	}
}
