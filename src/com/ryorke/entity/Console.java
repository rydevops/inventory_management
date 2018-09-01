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

import java.util.Date;

/**
 * Console item details
 * 
 * @author Russell Yorke
 *
 */
public class Console extends Item {
	private String color;
	private String diskSpace;
	private String modelNumber;
	private int[] includedGameId;
	private int controlersIncluded;
	
	/**
	 * Creates a new game console. 
	 * 
	 * @param itemNumber An item number
	 * @param productName A name for the item
	 * @param productDescription The descriptio not the item
	 * @param unitsInStock The number of units in stock
	 * @param unitCost The cost of individual units
	 * @param manufacture The manufacture that produces the item
	 * @param releaseDate The date the item will be release on
	 * @param packageDimension The package dimensions for shipping the item
	 * @param color Color of the console
	 * @param diskSpace Available disk space within the console
	 * @param modelNumber Model number of the console
	 * @param includedGameId List of included game IDs with the console
	 * @param controlersIncluded Number of included game controllers with the console
	 */
	public Console(int itemNumber, String productName, String productDescription, int unitsInStock, double unitCost,
		String manufacture, Date releaseDate, PackageDimension packageDimension, 
		String color, String diskSpace, String modelNumber, int[] includedGameId, int controlersIncluded) {
		super(itemNumber, productName, productDescription, unitsInStock, unitCost, manufacture, releaseDate, packageDimension);
		this.color = color;
		this.diskSpace = diskSpace;
		this.modelNumber = modelNumber;
		this.includedGameId = (includedGameId != null) ? includedGameId.clone() : null;
		this.controlersIncluded = controlersIncluded;		
	}

	/**
	 * Creates a new unitialized console object
	 */
	public Console() {
		this(0, "", "", 0, 0, "", new Date(), new PackageDimension(), "", "", "", null, 1);		
	}
	
	/**
	 * Gets the consoles color
	 * @return the color
	 */
	public String getColor() {
		return color;
	}

	/**
	 * Sets the consoles color
	 * @param color the color to set
	 */
	public void setColor(String color) {
		this.color = color;
	}

	/**
	 * Gets the consoles available disk space
	 * @return the disk space
	 */
	public String getDiskSpace() {
		return diskSpace;
	}

	/**
	 * Sets the consoles available disk space
	 * @param diskSpace the disk space to set
	 */
	public void setDiskSpace(String diskSpace) {
		this.diskSpace = diskSpace;
	}

	/**
	 * Gets the consoles model number
	 * @return the model number
	 */
	public String getModelNumber() {
		return modelNumber;
	}

	/**
	 * Sets the consoles model number
	 * @param modelNumber the model number to set
	 */
	public void setModelNumber(String modelNumber) {
		this.modelNumber = modelNumber;
	}

	/**
	 * Gets the consoles included game IDs
	 * @return A list of game IDs
	 */
	public int[] getIncludedGameId() {
		return includedGameId;
	}

	/**
	 * sets the consoles included game IDs
	 * @param includedGameId A ist of game IDs to include with the console
	 */
	public void setIncludedGameId(int[] includedGameId) {
		this.includedGameId = includedGameId;
	}

	/**
	 * Gets the number of controllers included with the console
	 * @return number of controlers included
	 */
	public int getControlersIncluded() {
		return controlersIncluded;
	}

	/**
	 * sets the number of controllers included with the console
	 * @param controlersIncluded Number of controlers included
	 */
	public void setControlersIncluded(int controlersIncluded) {
		this.controlersIncluded = controlersIncluded;
	}
	
	
}
