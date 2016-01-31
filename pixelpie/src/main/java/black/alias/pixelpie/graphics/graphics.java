package black.alias.pixelpie.graphics;

import black.alias.pixelpie.PixelPie;
import processing.core.PGraphics;

public class graphics {
	public int x, y, depth, index;
	public PGraphics graphic;
	final PixelPie pie;

	public graphics(int PosX, int PosY, int objWidth, int objHeight, int Depth, PixelPie pie) {
		this.pie = pie;
		x = PosX;
		y = PosY;
		depth = Depth;
		graphic = pie.app.createGraphics(objWidth, objHeight);

		// Set default transparent bg.
		graphic.beginDraw();
		graphic.background(0, 0);
		graphic.endDraw();
	}

	public void draw() {
		pie.drawGraphic(x, y, depth, index);
	}
}
