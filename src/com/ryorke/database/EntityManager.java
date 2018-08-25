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
package com.ryorke.database;

import java.util.ArrayList;
import java.sql.SQLException;

/**
 * Provides generic methods for a DatabaseManager to interact with an EntityManager
 * type.  
 * 
 * @author Russell Yorke
 */
public interface EntityManager {
	/**
	 * Idempotent method for creating a database table schema if it
	 * doesn't already exists. 
	 * 
	 * @throws SQLException If a database error occurs while processing the request. 
	 */
	public void createTable() throws SQLException;
	
	/**
	 * Provides a list of SQL commands used to restore a database table(s)
	 * for this entity.
	 * 
	 * @return A list of SQL commands
	 * @throws SQLException if a database error occurs while processing the request.
	 */
	public ArrayList<String> exportTable() throws SQLException;
}
