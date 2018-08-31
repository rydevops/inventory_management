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

import com.ryorke.database.UserEntityManager;
import com.ryorke.entity.User;
import com.ryorke.entity.exception.InvalidUserAttributeException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Provides basic user and password authentication before
 * loading the main application
 * 
 * @author Russell Yorke
 */
@SuppressWarnings("serial")
public class AuthenticationFrame extends JFrame {
	public final static int MAX_LOGIN_ATTEMPTS = 3;
	public final static String WINDOW_TITLE = "Inventory Manager - Login";
	
	private JTextField username;
	private JPasswordField password; 
	private JButton login;
	private JButton exit;
	private JLabel statusMessage;
	private int invalidLoginCount = 0;
	
	private UserEntityManager userEntityManager = null;
	/**
	 * Create a new authentication window with a default title
	 */
	public AuthenticationFrame() {
		this(AuthenticationFrame.WINDOW_TITLE);
	}
	
	/**
	 * Create a new authentication window
	 * 
	 * @param title Sets the frame title
	 */
	public AuthenticationFrame(String title) {
		initializeUserEntityManager(); 
		
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		
		statusMessage = new JLabel();
		contentPane.add(statusMessage, BorderLayout.NORTH);			
		
		GridBagLayout inputPanelLayoutManager = new GridBagLayout();
		GridBagConstraints inputPanelConstraints;
		JPanel inputFields = new JPanel(inputPanelLayoutManager);
		
		JLabel usernameLabel = new JLabel("Username:", SwingConstants.RIGHT);
		username = new JTextField(25);
		inputPanelConstraints = new GridBagConstraints();
		inputPanelConstraints.fill = GridBagConstraints.BOTH;
		inputPanelConstraints.weightx = 0.0;
		inputPanelConstraints.insets = new Insets(5, 5, 5, 5);
		inputPanelLayoutManager.setConstraints(usernameLabel, inputPanelConstraints);
		inputPanelConstraints.weightx = 1.0; // Force remainder of the row to be filled by this field
		inputPanelConstraints.gridwidth = GridBagConstraints.REMAINDER;
		inputPanelLayoutManager.setConstraints(username, inputPanelConstraints);
		usernameLabel.setDisplayedMnemonic(KeyEvent.VK_U);
		usernameLabel.setLabelFor(username);
		inputFields.add(usernameLabel);
		inputFields.add(username);
		
		
		JLabel passwordLabel = new JLabel("Password:", SwingConstants.RIGHT);
		password = new JPasswordField(25);
		inputPanelConstraints = new GridBagConstraints();
		inputPanelConstraints.fill = GridBagConstraints.BOTH;		
		inputPanelConstraints.weightx = 0.0;
		inputPanelConstraints.insets = new Insets(5, 5, 5, 5);
		inputPanelLayoutManager.setConstraints(passwordLabel, inputPanelConstraints);
		inputPanelConstraints.weightx = 1.0; // Force remainder of the row to be filled by this field
		inputPanelConstraints.gridwidth = GridBagConstraints.REMAINDER;
		inputPanelLayoutManager.setConstraints(password, inputPanelConstraints);
		passwordLabel.setLabelFor(password);
		passwordLabel.setDisplayedMnemonic(KeyEvent.VK_P);
		inputFields.add(passwordLabel);
		inputFields.add(password);
		
		contentPane.add(inputFields, BorderLayout.CENTER);
		
		JPanel buttons = new JPanel(new GridLayout(1, 2));
		login = new JButton("Login");
		login.setEnabled(false);
		login.setMnemonic(KeyEvent.VK_L);
		exit = new JButton("Exit");
		exit.setMnemonic(KeyEvent.VK_X);
		buttons.add(login);
		buttons.add(exit);
		contentPane.add(buttons, BorderLayout.SOUTH);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle(title);
		pack();
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
		username.requestFocus();
		
		// Event handlers
		username.getDocument().addDocumentListener(new InputHandler());		
		username.addActionListener(new ActionListener() {
			/**
			 * Perform login validation when user presses enter
			 * 
			 * @param e Event information
			 */
			public void actionPerformed(ActionEvent e) {
				validateUserCredentials();
			}
		});
		password.getDocument().addDocumentListener(new InputHandler());
		password.addActionListener(new ActionListener() {
			/**
			 * Perform login validation when user presses enter
			 * 
			 * @param e Event information
			 */
			public void actionPerformed(ActionEvent e) {
				validateUserCredentials();
			}
		});
		

		exit.addActionListener(new ActionListener() {
			/**
			 * Generates a window closing event to exit
			 * application
			 * @param e Event information
			 */
			public void actionPerformed(ActionEvent e) {
				System.exit(0);		
			}			
		});
		
		login.addActionListener(new ActionListener() {
			/**
			 * Perform login validation
			 * 
			 * @param e Event information
			 */
			public void actionPerformed(ActionEvent e) {
				validateUserCredentials();
			}
		});
	}
	
	/**
	 * Initializes the UserEntityManager for user authorization
	 */
	private void initializeUserEntityManager() {
		try {
			userEntityManager = UserEntityManager.getManager();
		} catch (Exception exception) {
			String errorMessage = "Unable to access database at this time. The application will now exit.\n\nDatabase Errors:\n";
			errorMessage += exception.getMessage() + "\n";
			
			for (Throwable suppressedException : exception.getSuppressed()) {
				errorMessage += suppressedException.getMessage() + "\n";
			}
		
			JOptionPane.showMessageDialog(this, errorMessage, "Unable to access database", JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}
	
	/**
	 * Performs login validation. If validation is successful
	 * the main application will be launched. If validation fails
	 * the maximum amout of times the application will exit otherwise 
	 * the application will clear the username/password field and display 
	 * an error
	 */
	private void validateUserCredentials() {
		if (login.isEnabled()) {
			String username = AuthenticationFrame.this.username.getText();
			String password = new String(AuthenticationFrame.this.password.getPassword());
			
			try {
				User authenticatedUser = userEntityManager.authenticateUser(username, password);
				if (authenticatedUser != null) {
					new InventoryManagementFrame(authenticatedUser);
					dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
				} else {
					invalidLoginCount++;
					if (invalidLoginCount >= MAX_LOGIN_ATTEMPTS ) {
						JOptionPane.showMessageDialog(this, "Too many login attempts. Now exiting...", "Authentication Failure", 
								JOptionPane.ERROR_MESSAGE | JOptionPane.OK_OPTION);
						dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));						
					} else {
						AuthenticationFrame.this.password.setText("");
						statusMessage.setForeground(Color.RED);
						statusMessage.setText("Invalid username or password (Attempt " + 
								Integer.toString(invalidLoginCount) + " of " +
								Integer.toString(MAX_LOGIN_ATTEMPTS) + ")");
						this.username.requestFocus();
						pack();						
					}			
				}
			} catch (SQLException exception) {
				String errorMessage = "An error occured while trying to perform authentication. Please try again.\n\nDatabase Errors:\n";
				errorMessage += exception.getMessage() + "\n";
				
				for (Throwable suppressedException : exception.getSuppressed()) {
					errorMessage += suppressedException.getMessage() + "\n";
				}
			
				JOptionPane.showMessageDialog(this, errorMessage, "Unable to access database", JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE);
			} catch (InvalidUserAttributeException invalidUserException) {
				String errorMessage = String.format("An unknown database error occured while attempting to authenticate user. "
						+ "Contact your System Administrator if problem persists.\nReason:%s", invalidUserException.getMessage());
				JOptionPane.showMessageDialog(this, errorMessage, "Invalid user type", JOptionPane.OK_OPTION | JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	
	/**
	 * Manages the enabling/disabling of the login button based on 
	 * username and password field data. If either field is empty the button
	 * is disabled otherwise the button is enabled. 
	 * 
	 * @author ryorke1
	 *
	 */
	private class InputHandler implements DocumentListener {
		
		/**
		 * Toggles the login button enabled state based
		 * on username and password fields
		 */
		private void toggleLoginState() {
			if (username.getText().length() > 0 && 
					password.getPassword().length > 0) {
				login.setEnabled(true);
			} else {
				login.setEnabled(false);
			}							
		}
		
		/**
		 * Notifies the toggleLoginState method based on 
		 * character input
		 * 
		 * @param e Information about the event that occurred.
		 */
		public void insertUpdate(DocumentEvent e) {
			toggleLoginState();			
		}

		/**
		 * Notifies the toggleLoginState method based on 
		 * character removal
		 * @param e Information about the event that occurred.
		 */
		public void removeUpdate(DocumentEvent e) {
			toggleLoginState();			
		}

		/**
		 * Does nothing, implemented due to interface
		 * @param e Information about the event that occurred.
		 */
		public void changedUpdate(DocumentEvent e) {}		
	}
}
