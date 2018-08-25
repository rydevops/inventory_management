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

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

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
	
	private JList<User> users;
	private JButton addUser;
	private JButton deleteUser;
	private JButton modifyUser;
	private JButton changePassword;
	private JTextField username;
	private JTextField firstName;
	private JTextField lastName;
	private JCheckBox isAdministrator;
	
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
		BorderLayout layoutManager = new BorderLayout();
		JPanel userlistPanel = new JPanel(layoutManager);
		JLabel usersLabel = new JLabel("Users:");		
		DefaultListModel<User> listModel = new DefaultListModel<User>();		
		users = new JList<User>(listModel);	
		JScrollPane userlistScroller = new JScrollPane();
		
		userlistScroller.setViewportView(users);
		
		layoutManager.setHgap(5);
		layoutManager.setVgap(5);
		users.setFixedCellWidth(150);
		users.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// === DEFAULT USER - REMOVE BELOW ===
		// TODO: admin/admin to be added to database on first run before
		//		 this ever gets checked and this sample user will be removed
		//		 from this code and replace with a database call. 
		User admin = new User("admin", "admin", "admin","user", true);
		// === END OF DEFAULT USER - REMOVE ABOVE ===
		
		listModel.addElement(admin);
		
		userlistPanel.add(usersLabel, BorderLayout.NORTH);
		userlistPanel.add(userlistScroller);
		
		return userlistPanel;		
	}
	
	/**
	 * Creates a user view form for displaying
	 * user objects
	 * 
	 * @return A new user form
	 */
	private JPanel createUserForm() {
		// TODO: Add mnuemonics and ensure tab order is correct
		SpringLayout layoutManager = new SpringLayout();
		JPanel userFormPanel = new JPanel(layoutManager);
		final int GAP_SPACING = 5; 
		
		
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
