package black.alias.pixelpie.ui.render;

import de.lessvoid.nifty.spi.render.RenderFont;
import processing.core.PFont;
import processing.core.PApplet;

/**
 * Implementation of Nifty's RenderFont interface using Processing's own
 * VLW bitmap font format.
 * @author Xuanming
 */
public class RenderFontProcessing implements RenderFont {
	
	private final PFont font;
	private final PApplet app;
	private final int scale;	
	
	/**
	 * Create an instance of RenderFontProcessing.
	 * @param app PApplet instance Processing is currently running in.
	 * @param canvas PGraphics canvas Nifty is being drawn on.
	 * @param filename Path to the .vlw font file.
	 * @throws IOException 
	 */
	public RenderFontProcessing(PApplet app, String filename, int scale) {
		
		this.app = app;
		this.scale = scale;
		
		if ((filename.substring(filename.length() - 3)).equals("vlw")) {				
			this.font = app.loadFont(filename);
		} else {
			this.font = null;
			System.err.println(filename + " is an invalid filetype, only Processing VLW fonts are accepted.");
		}
	}
	
	public int getWidth(String text) {
		app.textFont(font);
		return ((int)app.textWidth(text)) / scale;
	}
	
	public int getWidth(String text, float size) {
		app.textFont(font);
		return ((int)(app.textWidth(text) * size)) / scale;
	}
	
	public int getHeight() {
		app.textFont(font);
		return (int)(app.textDescent() + app.textAscent()) / scale;
	}
	
	public int getCharacterAdvance(char currentCharacter, char nextCharacter,
			float size) {
		app.textFont(font);
		return ((int)(app.textWidth(currentCharacter) * size)) / scale;
	}
	
	public void dispose() {
		// No dispose method.
	}
	
	public PFont getFont() {
		return font;
	}
	
	public int getSize() {
		return font.getSize();
	}
}
