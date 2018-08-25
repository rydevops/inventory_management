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
 * Accessory item
 * 
 * @author Russell Yorke
 */
public class Accessory extends Item {
	private String color;
	private String modelNumber;
	private int platformId;
	
	/**
	 * Creates a new accessory
	 * 
	 * @param itemNumber An item number
	 * @param productName A name for the item
	 * @param productDescription The descriptio not the item
	 * @param unitsInStock The number of units in stock
	 * @param unitCost The cost of individual units
	 * @param manufacture The manufacture that produces the item
	 * @param releaseDate The date the item will be release on
	 * @param packageDimension The package dimensions for shipping the item
	 * @param color The color of the accessory
	 * @param modelNumber THe model number of the accessory 
	 * @param platformId The platform Id this accessory is for
	 */
	public Accessory(int itemNumber, String productName, String productDescription, int unitsInStock, double unitCost,
			String manufacture, Date releaseDate, PackageDimension packageDimension,
			String color, String modelNumber, int platformId) {
		super(itemNumber, productName, productDescription, unitsInStock, unitCost, manufacture, releaseDate, packageDimension);
		
		this.color = color;
		this.modelNumber = modelNumber;
		this.platformId = platformId;
	}
	
	/**
	 * Creates a new unitialized Accessory object
	 */
	public Accessory() {
		this(-1, "", "", 0, 0, "", new Date(), new PackageDimension(), "", "", -1);		
	}
	
	/**
	 * Gets the accessory color
	 * @return the color
	 */
	public String getColor() {
		return color;
	}
	
	/**
	 * Sets the accessory color
	 * @param color the color to set
	 */
	public void setColor(String color) {
		this.color = color;
	}
	
	/**
	 * Gets the accessory model number
	 * @return the modelNumber
	 */	
	public String getModelNumber() {
		return modelNumber;
	}
	
	/**
	 * Sets the accessory model number
	 * @param modelNumber the modelNumber to set
	 */
	public void setModelNumber(String modelNumber) {
		this.modelNumber = modelNumber;
	}
	
	/**
	 * Gets the accessory platformId
	 * @return the platformId
	 */
	public int getPlatformId() {
		return platformId;
	}
	
	
	/**
	 * Sets the accessory platformId
	 * @param platformId the platformId to set
	 */
	public void setPlatformId(int platformId) {
		this.platformId = platformId;
	}
}
