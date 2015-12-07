/*******************************************************************************
 * Copyright 2015 hangum
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.gaia3d.tadpole.spatial.data.core.ui.dialogs.shapes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shape import dto
 * 
 * @author hangum
 *
 */
public class ShapeImportDTO {

	String tableName = "";
	String srid = "";
	String create_statement = "";
	List<Map<String, Object>> listShape = new ArrayList<Map<String,Object>>();
	
	public ShapeImportDTO() {
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getSrid() {
		return srid;
	}

	public void setSrid(String srid) {
		this.srid = srid;
	}

	public String getCreate_statement() {
		return create_statement;
	}

	public void setCreate_statement(String create_statement) {
		this.create_statement = create_statement;
	}

	public List<Map<String, Object>> getListShape() {
		return listShape;
	}

	public void setListShape(List<Map<String, Object>> listShape) {
		this.listShape = listShape;
	}
	
}
