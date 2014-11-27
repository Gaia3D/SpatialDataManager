/*******************************************************************************
 * Copyright 2014 hangum
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
package com.gaia3d.tadpole.spatial.data.core.ui.editor.inner;

/**
 * map의 center를 설정하는 object
 * 
 * @author hangum
 *
 */
public class CenterObj {
	String centerX 	= "37.55";
	String centerY 	= "127.07";
	String zoom		= "3";

	public CenterObj() {
	}

	public CenterObj(String centerX, String centerY, String zoom) {
		super();
		this.centerX = centerX;
		this.centerY = centerY;
		this.zoom = zoom;
	}

	public String getCenterX() {
		return centerX;
	}

	public void setCenterX(String centerX) {
		this.centerX = centerX;
	}

	public String getCenterY() {
		return centerY;
	}

	public void setCenterY(String centerY) {
		this.centerY = centerY;
	}

	public String getZoom() {
		return zoom;
	}

	public void setZoom(String zoom) {
		this.zoom = zoom;
	}

	
}
