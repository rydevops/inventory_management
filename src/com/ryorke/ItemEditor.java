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

import com.ryorke.entity.Item;

/**
 * Provides generic methods for interacting with an 
 * item editor
 * 
 * @author Russell Yorke
 *
 */
public interface ItemEditor {
	/**
	 * Changes the item being modified within the item editor
	 * 
	 * @param item A new item
	 */
	public void setItem(Item item);
	
	/**
	 * Causes the item editor to replace all fields
	 * with details in the provided item.
	 */
	public void displayItem();
	
	/**
	 * Causes the item editor to update the item with
	 * the currently set field values. 
	 * 
	 * @return true if all fields were saved to the item, false otherwise
	 */
	public boolean updateItem();
}
