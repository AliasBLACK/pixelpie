package black.alias.pixelpie.ui.render;

import de.lessvoid.nifty.render.BlendMode;
import de.lessvoid.nifty.spi.render.*;
import de.lessvoid.nifty.tools.Color;
import de.lessvoid.nifty.tools.resourceloader.NiftyResourceLoader;
import processing.core.*;

/**
 * Implementation of Nifty's RenderDevice for Processing.
 * @author Xuanming
 */
public class RenderDeviceProcessing implements RenderDevice {
	
	private final PApplet app;
	private NiftyResourceLoader resourceLoader;
	private MouseCursor mouseCursor;
	private final int scale;
	
	/**
	 * Instantiate RenderDeviceProcessing.
	 * @param app PApplet instance that Processing is currently running in.
	 */
	public RenderDeviceProcessing(PApplet app) {
		this(app, 1);
	}
	
	/**
	 * Instantiate RenderDeviceProcessing (verbose version)
	 * @param app PApplet instance that Processing is currently running in.
	 * @param width Desired width of Nifty instance.
	 * @param height Desired height of Nifty instance.
	 */
	public RenderDeviceProcessing(PApplet app, int scale) {
		this.app = app;
		this.scale = scale;		
		
		/* 
		 * All classes in Processing are inner classes of the Processing PApplet instance.
		 * Creating a helper property to assist in finding inner ScreenController classes from XML.
		 * Using this property, users just need to specify 'controller="${PROP.APP}ControllerName"' in
		 * their XML layouts, where ControllerName is the name of the actual ScreenController impl.
		 */
		System.setProperty("APP", app.getClass().getName() + "$");
	}

	public void setResourceLoader(NiftyResourceLoader niftyResourceLoader) {
		this.resourceLoader = niftyResourceLoader;
		resourceLoader.addResourceLocation(new ProcessingLocation(app));
	}

	public RenderImage createImage(String filename, boolean filterLinear) {
		return new RenderImageProcessing(app, filename);
	}

	public RenderFont createFont(String filename) {
		return new RenderFontProcessing(app, filename, scale);
	}
	
	public int getWidth() {
		return app.width / scale;
	}
	
	public int getHeight() {
		return app.height / scale;
	}
	
	public void beginFrame() {
		// Do nothing.
	}
	
	public void endFrame() {
		// Do nothing.
	}
	
	public void clear() {
		app.clear();
	}
	
	public void setBlendMode(BlendMode renderMode) {
		switch (renderMode) {
		case BLEND:
			app.blendMode(PConstants.BLEND);
			break;
			
		case MULIPLY:
			app.blendMode(PConstants.MULTIPLY);
			break;			
		}
	}
	
	public void renderQuad(int x, int y, int width, int height, Color color) {
		
		// Draw rectangle.
		app.noStroke();
		app.fill(convertColor(color));
		app.rect(x * scale, y * scale, width * scale, height * scale);
	}
	
	public void renderQuad(int x, int y, int width, int height, Color topLeft,
			Color topRight, Color bottomRight, Color bottomLeft) {
		
		// Convert colors.
		int topLeftC = convertColor(topLeft);
		int topRightC = convertColor(topRight);
		int bottomLeftC = convertColor(bottomLeft);
		int bottomRightC = convertColor(bottomRight);
		
		// Draw rectangle using pixels[] array.
		PImage rect = app.createImage(width, height, PConstants.ARGB);
		rect.loadPixels();
		for (int k = 0; k < height; k++) {
			for (int i = 0; i < width; i++) {
				float xRange = PApplet.map(i, x, x + width, 0, 1);
				float yRange = PApplet.map(k, y, y + height, 0, 1);
				rect.pixels[k * width + i] = 
					app.lerpColor(
						app.lerpColor(bottomLeftC, bottomRightC, xRange),
						app.lerpColor(topLeftC, topRightC, xRange),
						yRange
					)
				;
			}
		}
		rect.updatePixels();
		app.image(rect, x * scale, y * scale, width * scale, height * scale);
	}
	
	public void renderImage(RenderImage image, int x, int y, int width,
			int height, Color color, float imageScale) {
		PImage img;	
		if (width > 0 && height > 0 && imageScale > 0.0) {
			if (image instanceof RenderImageProcessing){
				img = app.createImage(Math.round(width * imageScale), Math.round(height * imageScale), PConstants.ARGB);
				img.copy(((RenderImageProcessing)image).image, 0, 0, image.getWidth(), image.getHeight(), 0, 0, Math.round(width * imageScale), Math.round(height * imageScale));
				app.tint(convertColor(color));
				app.image(img, x * scale, y * scale, width * scale, height * scale);
				app.noTint();
			}
		}
	}
	
	public void renderImage(RenderImage image, int x, int y, int w, int h,
			int srcX, int srcY, int srcW, int srcH, Color color, float scale,
			int centerX, int centerY) {
		PImage img;
		if (w > 0 && h > 0 && scale > 0.0) {
			if (image instanceof RenderImageProcessing) {
				img = app.createImage(Math.round(w * scale), Math.round(h * scale), PConstants.ARGB);
				img.copy(((RenderImageProcessing)image).image, srcX, srcY, srcW, srcH, 0, 0, Math.round(w * scale), Math.round(h * scale));
				app.tint(convertColor(color));
				app.image(img, x * this.scale, y * this.scale, w * this.scale, h * this.scale);
				app.noTint();
			}
		}
	}
	
	public void renderFont(RenderFont font, String text, int x, int y,
			Color fontColor, float sizeX, float sizeY) {
		if (font instanceof RenderFontProcessing){
			app.textFont(((RenderFontProcessing) font).getFont());
			app.textSize(((RenderFontProcessing) font).getSize() * sizeX);
			app.fill(convertColor(fontColor));
			app.text(text, x * scale, y * scale + Math.round(app.textDescent()));
		}
	}
	
	public void enableClip(int x0, int y0, int x1, int y1) {
		app.clip(x0, y0, x1 - x0, y1 - y0);
	}
	
	public void disableClip() {
		app.noClip();
	}
	
	public MouseCursor createMouseCursor(String filename, int hotspotX, int hotspotY) {
		return new MouseCursorProcessing(app, filename, hotspotX, hotspotY, scale);
	}
	
	public void enableMouseCursor(MouseCursor mouseCursor) {
		this.mouseCursor = mouseCursor;
		mouseCursor.enable();
	}
	
	public void disableMouseCursor() {
		if (mouseCursor != null) {
			mouseCursor.disable();
		}
	}
	
	/**
	 * Convert the Nifty Color data type to the Processing one.
	 * @param c
	 * @return
	 */
	private int convertColor(Color c) {
		return 
			app.color(
				c.getRed() * 255, 
				c.getGreen() * 255,
				c.getBlue() * 255,
				c.getAlpha() * 255
			)
		;
	}
}
