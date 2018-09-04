package com.ryorke.entity.exception;

/**
 * A new exception when a user attribute is set incorrectly
 * 
 * @author Russell Yorke
 */
@SuppressWarnings("serial")
public class InvalidUserAttributeException extends Exception {
	/**
	 * Creates a new exception
	 * @param message The message to set
	 */
	public InvalidUserAttributeException(String message) {
		super(message);
	}
}
