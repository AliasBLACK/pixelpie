package black.alias.pixelpie.ui.render;

import de.lessvoid.nifty.spi.render.RenderImage;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * Implementation of Nifty RenderImage using Processing's PImage class.
 * @author Xuanming
 */

public class RenderImageProcessing implements RenderImage {
	public final PImage image;
	
	public RenderImageProcessing (PApplet app, String filename){
		this.image = app.loadImage(filename);
	}

	public int getWidth() {
		return image.width;
	}

	public int getHeight() {
		return image.height;
	}

	public void dispose() {
		// no such function in processing.
	}
}
