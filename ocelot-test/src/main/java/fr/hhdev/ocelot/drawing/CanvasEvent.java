/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package fr.hhdev.ocelot.drawing;

/**
 *
 * @author hhfrancois
 */
public class CanvasEvent {

	public CanvasEvent() {
	}

	public CanvasEvent(int x, int y, String type) {
		this.x = x;
		this.y = y;
		this.type = type;
	}
	
	
	
	private int x;
	private int y;
	private String type;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
