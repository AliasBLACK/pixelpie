package black.alias.pixelpie.ui.render;

import de.lessvoid.nifty.spi.render.MouseCursor;
import processing.core.*;

/**
 * Implementation of Nifty mouse cursor functions for Processing.
 * @author Xuanming
 */
public class MouseCursorProcessing implements MouseCursor {
	
	private final PApplet app;
	private final PImage img;
	private final int x;
	private final int y;
	
	/**
	 * Instantiate the MouseCursorProcessing object.
	 * @param app PApplet instance that Processing is currently running in.
	 * @param img PImage object to be used as mouse cursor.
	 * @param x Cursor hotspot x-coordinate.
	 * @param y Cursor hotspot y-coordinate.
	 */
	public MouseCursorProcessing(PApplet app, String filename, int x, int y, int scale) {
		
		// Keep reference to the PApplet.
		this.app = app;
		
		// Load the cursor image.
		PImage temp = app.loadImage(filename);		
		
		// Resize the cursor image.
		this.img = app.createImage(temp.width * scale, temp.height * scale, PConstants.ARGB);
		temp.loadPixels(); img.loadPixels();
		for (int i = 0; i < img.width; i += scale) {
			for (int k = 0; k < img.height; k += scale) {
				for (int m = 0; m < scale; m++) {
					for (int n = 0; n < scale; n++) {
						img.pixels[(k + n) * img.width + (i + m)] = temp.pixels[(k/scale) * temp.width + (i/scale)];
					}
				}
			}
		}
		img.updatePixels();
		
		// Resize the offset as well.
		this.x = x * scale;
		this.y = y * scale;		
	}

	public void enable() {
		app.cursor(img, x, y);
	}

	public void disable() {
		//app.cursor(PConstants.ARROW);
	}

	public void dispose() {
		// Do nothing.
	}
}
