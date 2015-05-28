/*******************************************************************************
 * Copyright (c) 2012 - 2015 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     hangum - initial API and implementation
 ******************************************************************************/
package com.gaia3d.tadpole.spatial.data.core.spaitaldb.dao;

import java.util.ArrayList;
import java.util.List;

/**
 * rquest spatial query
 *
 *
 * @author hangum
 * @version 1.6.1
 * @since 2015. 5. 27.
 *
 */
public class RequestSpatialQueryDAO {
	String origianlQuery = "";
	String spatialQuery = "";
	
	String tadpoleFullyQuery = "";
	List<Integer> listRealGisColumnIndex = new ArrayList<Integer>();

	/**
	 * 
	 */
	public RequestSpatialQueryDAO(String origianlQuery) {
		this.origianlQuery = origianlQuery;
	}

	/**
	 * @return the origianlQuery
	 */
	public String getOrigianlQuery() {
		return origianlQuery;
	}

	/**
	 * @param origianlQuery the origianlQuery to set
	 */
	public void setOrigianlQuery(String origianlQuery) {
		this.origianlQuery = origianlQuery;
	}

	/**
	 * @return the spatialQuery
	 */
	public String getSpatialQuery() {
		return spatialQuery;
	}

	/**
	 * @param spatialQuery the spatialQuery to set
	 */
	public void setSpatialQuery(String spatialQuery) {
		this.spatialQuery = spatialQuery;
	}

	/**
	 * @return the listRealGisColumnIndex
	 */
	public List<Integer> getListRealGisColumnIndex() {
		return listRealGisColumnIndex;
	}

	/**
	 * @param listRealGisColumnIndex the listRealGisColumnIndex to set
	 */
	public void setListRealGisColumnIndex(List<Integer> listRealGisColumnIndex) {
		this.listRealGisColumnIndex = listRealGisColumnIndex;
	}

	/**
	 * @return the tadpoleFullyQuery
	 */
	public String getTadpoleFullyQuery() {
		return tadpoleFullyQuery;
	}

	/**
	 * @param tadpoleFullyQuery the tadpoleFullyQuery to set
	 */
	public void setTadpoleFullyQuery(String tadpoleFullyQuery) {
		this.tadpoleFullyQuery = tadpoleFullyQuery;
	}
	
}
