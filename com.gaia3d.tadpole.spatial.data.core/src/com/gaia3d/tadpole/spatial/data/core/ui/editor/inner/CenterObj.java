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
