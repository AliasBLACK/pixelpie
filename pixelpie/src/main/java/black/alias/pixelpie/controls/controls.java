package black.alias.pixelpie.controls;

import processing.event.KeyEvent;
import processing.event.MouseEvent;
import black.alias.pixelpie.PixelPie;
import black.alias.pixelpie.gameObject;

public class controls {
	final PixelPie pie;

	/**
	 * Initiate controls.
	 * @param pie
	 */
	public controls (PixelPie pie) {
		this.pie = pie;
		
		// Register methods.
		pie.app.registerMethod("mouseEvent", this);
		pie.app.registerMethod("keyEvent", this);
	}
	
	/**
	 * Handle MouseEvents from Processing.
	 * @param event processing.event.MouseEvent class passed from Processing.
	 */
	public void mouseEvent(MouseEvent event) {
		switch (event.getAction()){
		case MouseEvent.PRESS:
			if (!pie.scriptRunning()) {
				for (gameObject obj : pie.objects) {
					obj.mousePressed();
				}
			}
			break;

		case MouseEvent.RELEASE:
			if (!pie.scriptRunning()) {
				for (gameObject obj : pie.objects) {
					obj.mouseReleased();
				}
			}
			break;

		case MouseEvent.WHEEL:
			if (!pie.scriptRunning()) {
				for (gameObject obj : pie.objects) {
					obj.mouseWheel(event);
				}
			}
			break;
		}
	}
	
	/**
	 * Handle KeyEvents from Processing.
	 * @param event processing.event.KeyEvent class passed from Processing.
	 */
	public void keyEvent(KeyEvent event) {
		switch(event.getAction()){
		case KeyEvent.PRESS:
			if (!pie.scriptRunning()) {
				for (gameObject obj : pie.objects) {
					obj.keyPressed();
				}
			}
			break;

		case KeyEvent.RELEASE:
			if (!pie.scriptRunning()) {
				for (gameObject obj : pie.objects) {
					obj.keyReleased();
				}
			}
			break;
		}
	}
}
