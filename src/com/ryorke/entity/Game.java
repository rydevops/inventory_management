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
 * Video game item details
 * 
 * @author Russell Yorke
 */
public class Game extends Item {
	private int numberOfDiscs;
	private int numberOfPlayers;
	private int platformId;
	private String esrbRating;
	
	/**
	 * Creates a new video game item
	 * 
	 * @param itemNumber An item number
	 * @param productName A name for the item
	 * @param productDescription The descriptio not the item
	 * @param unitsInStock The number of units in stock
	 * @param unitCost The cost of individual units
	 * @param manufacture The manufacture that produces the item
	 * @param releaseDate The date the item will be release on
	 * @param packageDimension The package dimensions for shipping the item
	 * @param numberOfDiscs The number of discs in the package
	 * @param numberOfPlayers The number of players that can play the game together
	 * @param platformId The platform Id the game is for
	 * @param esrbRating The ESRB rating of the game
	 */
	public Game(int itemNumber, String productName, String productDescription, int unitsInStock, double unitCost,
			String manufacture, Date releaseDate, PackageDimension packageDimension,
			int numberOfDiscs, int numberOfPlayers, int platformId, String esrbRating) {
		super(itemNumber, productName, productDescription, unitsInStock, unitCost, manufacture, releaseDate, packageDimension);
		
		this.numberOfDiscs = numberOfDiscs;
		this.numberOfPlayers = numberOfPlayers;
		this.platformId = platformId;
		this.esrbRating = esrbRating;
	}
		
	/**
	 * Creates a new unitialized game object
	 */
	public Game() {
		this(-1, "", "", 0, 0, "", new Date(), new PackageDimension(), 1, 1, -1, "");		
	}
	
	/**
	 * Gets the total number of discs in the game package
	 * @return the numberOfDics
	 */
	public int getNumberOfDiscs() {
		return numberOfDiscs;
	}
	
	/**
	 * Sets the total numbe of discs in the game package
	 * @param numberOfDics the number of discs to set
	 */
	public void setNumberOfDiscs(int numberOfDiscs) {
		this.numberOfDiscs = numberOfDiscs;
	}
	
	/**
	 * Gets the total number of players that can play this game
	 * 
	 * @return the number of players
	 */
	public int getNumberOfPlayers() {
		return numberOfPlayers;
	}
	
	/**
	 * Sets to the total number of players for this game
	 * 
	 * @param numberOfPlayers the number of players to set
	 */
	public void setNumberOfPlayers(int numberOfPlayers) {
		this.numberOfPlayers = numberOfPlayers;
	}
	
	/**
	 * Gets the platform ID for this game
	 * 
	 * @return the platformId
	 */
	public int getPlatformId() {
		return platformId;
	}
	
	/**
	 * Sets the platform ID for this game
	 * @param platformId the platformId to set
	 */
	public void setPlatformId(int platformId) {
		this.platformId = platformId;
	}
	/**
	 * Gets the ERSB game rating
	 * 
	 * @return the esrbRating
	 */
	public String getEsrbRating() {
		return esrbRating;
	}
	/**
	 * Sets the ERSB game rating
	 * @param esrbRating the esrbRating to set
	 */
	public void setEsrbRating(String esrbRating) {
		this.esrbRating = esrbRating;
	}
	
	
}
