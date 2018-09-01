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

import javax.management.InvalidAttributeValueException;

/**
 * Item's package dimensions
 * 
 * @author Russell Yorke
 */
public class PackageDimension {
	private float height;
	private float width;
	private float depth;
	private float weight;
	
	/**
	 * Create a new instance with all values initialized to 0
	 */
	public PackageDimension() {
		this(0, 0, 0, 0);
	}
	
	/**
	 * Creates a new package dimension with desired dimensions
	 * 
	 * @param height Package height
	 * @param width Package width
	 * @param depth Package depth
	 * @param weight Package weight 
	 */
	public PackageDimension(float height, float width, float depth, float weight) {
		setHeight(height);
		setWidth(width);
		setDepth(depth);
		setWeight(weight);
	}
	
	/**
	 * Gets the height of the package
	 * @return the height
	 */	
	public float getHeight() {
		return height;
	}
	
	/**
	 * Sets the height of the package
	 * @param height the height to set
	 */	
	public void setHeight(float height) {
		this.height = height;
	}
		
	/**
	 * Gets the width of the package
	 * @return the width
	 */	
	public float getWidth() {
		return width;
	}
	
	/**
	 *  Sets the width of the package
	 * @param width the width to set
	 */
	public void setWidth(float width) {
		this.width = width;
	}
	
	/**
	 * Gets the depth of the package
	 * @return the depth
	 */
	public float getDepth() {
		return depth;
	}
	
	/**
	 *  Gets the depth of the package
	 * @param depth the depth to set
	 */
	public void setDepth(float depth)  {
		this.depth = depth;
	}
	
	/**
	 * Gets the weight of the package
	 * @return the weight
	 */
	public float getWeight() {
		return weight;
	}
	
	/**
	 *  Sets the weight of the package
	 * @param weight the weight to set
	 */
	public void setWeight(float weight) {
		this.weight = weight;
	} 
}
