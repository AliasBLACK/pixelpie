package black.alias.pixelpie.level;

import processing.core.*;
import black.alias.pixelpie.*;

public class tileSet {
	public int tileRows, tileColumns;
	public int tileWidth;
	public int tileHeight;
	public int firstGID;
	public PImage tileSet;

	public tileSet(int rows, int cols, int tWidth, int tHeight, int gid, String filename, PixelPie pie) {

		// Test if file exists.
		if (pie.fileExists(pie.app.dataPath(filename))) {

			// Load the sprite.
			tileSet = pie.app.loadImage(pie.app.dataPath(filename));
			tileRows = rows;
			tileColumns = cols;
			tileWidth = tWidth;
			tileHeight = tHeight;
			firstGID = gid;

		// Else, print error.
		} else {
			pie.log.printlg("Image " + pie.app.dataPath(filename) + " not found.");
		}
	}
}
