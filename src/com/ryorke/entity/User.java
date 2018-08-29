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
package com.ryorke.entity;

/**
 * User entity class providing details for a user
 * 
 * @author Russell Yorke
 */
public class User {
	private int userId;
	private String username;
	private String password; 
	private String firstName;
	private String lastName;
	private boolean administrator;
	
	/**
	 * Creates a new user account
	 * 
	 * @param username Username to set
	 * @param password Password to set
	 * @param firstName Users first name
	 * @param lastName Users last name
	 * @param isAdministator True if user is administrator, false otherwise. 
	 */
	public User(String username, String password, String firstName, String lastName, boolean isAdministator) {
		this(0, username, password, firstName, lastName, isAdministator);		
	}
	
	/**
	 * Creates a new user account
	 * 
	 * @param username Username to set
	 * @param password Password to set
	 * @param firstName Users first name
	 * @param lastName Users last name
	 * @param isAdministator True if user is administrator, false otherwise. 
	 */
	public User(int userId, String username, String password, String firstName, String lastName, boolean isAdministator) {
		setUserId(userId);
		setUsername(username);
		setPassword(password);
		setFirstName(firstName);
		setLastName(lastName);
		setAdministrator(isAdministator);
	}
	
	/**
	 * Retrieves the user id
	 * @return the user id
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * Sets the user id
	 * 
	 * @param userId new user id
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	/**
	 * Retrieves the username
	 * @return the username
	 */
	
	public String getUsername() {
		return username;
	}
	
	/**
	 * Updates the username
	 * @param newUsername the new username to set
	 */
	public void setUsername(String newUsername) {
		this.username = newUsername;
	}
	
	/**
	 * Performs password validation
	 * @return True if passwords match, false otherwise. 
	 */
	public boolean isValidPassword(String password) {
		return password.equals(this.password);
	}
	
	/**
	 * Sets the user password
	 * @param newPassword a new password to set
	 */
	public void setPassword(String newPassword) {
		this.password = newPassword;
	}
	
	/**
	 * Retrieves users first name
	 * @return the first name
	 */
	public String getFirstName() {
		return firstName;
	}
	
	/**
	 * Sets the users first name
	 * @param firstName the first name to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	/**
	 * Retrieves users last name. 
	 * @return the last name
	 */
	public String getLastName() {
		return lastName;
	}
	
	/**
	 * Sets the users last name. 
	 * @param lastName the last name to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	/**
	 * Indicates if the user is an administrator or not. 
	 * 
	 * @return True if user is an administrator, false otherwise. 
	 */
	public boolean isAdministrator() {
		return administrator;
	}
	
	/**
	 * Enables/Disables user administrator rights
	 * @param administrator True if user is an admin false otherwise. 
	 */
	public void setAdministrator(boolean administrator) {
		this.administrator = administrator;
	}
	/**
	 * Converts user to string
	 * 
	 * return A username
	 */
	public String toString() {
		return this.username;
	}
}
