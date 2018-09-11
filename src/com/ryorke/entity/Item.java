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
 * Generic inventory item details
 * 
 * @author Russell Yorke
 */
public abstract class Item {
	private int itemNumber;
	private String productName;
	private String productDescription;
	private int unitsInStock;
	private double unitCost;
	private String manufacture;
	private Date releaseDate;
	private PackageDimension packageDimension;
	
	/**
	 * Creates a new item 
	 * 
	 * @param itemNumber An item number
	 * @param productName A name for the item
	 * @param productDescription The description of the item
	 * @param unitsInStock The number of units in stock
	 * @param unitCost The cost of individual units
	 * @param manufacture The manufacture that produces the item
	 * @param releaseDate The date the item will be release on
	 * @param packageDimension The package dimensions for shipping the item
	 */
	public Item(int itemNumber, String productName, String productDescription, int unitsInStock, double unitCost,
			String manufacture, Date releaseDate, PackageDimension packageDimension) {
		setItemNumber(itemNumber);
		setProductName(productName);
		setProductDescription(productDescription);
		setUnitsInStock(unitsInStock);
		setUnitCost(unitCost);
		setManufacture(manufacture);
		setReleaseDate(releaseDate);
		setPackageDimension(packageDimension);
	}
	
	/**
	 * Sets the item number
	 * @param itemNumber
	 */
	public void setItemNumber(int itemNumber) {
		this.itemNumber = itemNumber;
	}
	
	/**
	 * Gets the item number
	 * @return the item number
	 */
	public int getItemNumber() {
		return itemNumber;
	}

	/**
	 * Gets the product description
	 * @return the product description
	 */
	public String getProductDescription() {
		return productDescription;
	}

	/**
	 * Sets a new production description
	 * @param productDescription new product description
	 */
	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}

	/**
	 * Gets the current units in stock
	 * @return the units in stock
	 */
	public int getUnitsInStock() {
		return unitsInStock;
	}

	/**
	 * Sets the current units in stock
	 * @param unitsInStock new units in stock
	 */
	public void setUnitsInStock(int unitsInStock)  {
		
		this.unitsInStock = unitsInStock;
	}

	/**
	 * Gets the current unit cost
	 * @return the unit cost
	 */
	public double getUnitCost() {
		return unitCost;
	}

	/**
	 * Sets the cost of this item
	 * @param unitCost new unit cost to set
	 */
	public void setUnitCost(double unitCost) {
		this.unitCost = unitCost;
	}

	/**
	 * Gets the item's manufacture name
	 * @return the manufacture name
	 */
	public String getManufacture() {
		return manufacture;
	}

	/**
	 * Changes the manufacture of this item
	 * @param manufacture the new manufacture to set
	 */
	public void setManufacture(String manufacture) {
		this.manufacture = manufacture;
	}

	/**
	 * Gets the items release date
	 * @return the release date
	 */
	public Date getReleaseDate() {
		return releaseDate;
	}

	/**
	 * Sets the items release date
	 * @param releaseDate the release date to set
	 */
	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	/**
	 * Gets the package dimensions
	 * @return the packageDimensions
	 */
	public PackageDimension getPackageDimensions() {
		return packageDimension;
	}

	/**
	 * Sets the items package dimension
	 * @param packageDimension the package dimension to set
	 */
	public void setPackageDimension(PackageDimension packageDimension) {
		this.packageDimension = packageDimension;
	}

	/**
	 * Gets the product name
	 * 
	 * @return the product name
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * Sets the product name
	 * @param productName the product name to set
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}		
}
