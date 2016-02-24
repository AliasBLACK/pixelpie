package black.alias.pixelpie.graphics;

import black.alias.pixelpie.PixelPie;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.opengl.PGraphicsOpenGL;

public class Graphics {
	public int x, y, depth, index;
	public PGraphics graphic;
	final PixelPie pie;

	public Graphics(int PosX, int PosY, int objWidth, int objHeight, int Depth, PixelPie pie) {
		this.pie = pie;
		x = PosX;
		y = PosY;
		depth = Depth;
		graphic = pie.app.createGraphics(objWidth, objHeight);
		pie.graphics.add(this);

		// Set default transparent bg.
		graphic.noSmooth();
		graphic.beginDraw();
		graphic.background(0, 0);
		graphic.endDraw();
	}

	public void draw(int index) {
		
		if ( // If it's not on screen, skip draw method.
				PixelPie.toInt(pie.testOnScreen(x, y))
				+ PixelPie.toInt(pie.testOnScreen(x + graphic.width, y))
				+ PixelPie.toInt(pie.testOnScreen(x , y + graphic.height))
				+ PixelPie.toInt(pie.testOnScreen(x + graphic.width, y + graphic.height)) == 0) {
			return;
		}
		
		// Add graphic to render queue.
		pie.depthBuffer.append(PApplet.nf(depth, 4)	+ 2	+ index);
	}
	
	public void render() {
		
		// Draw graphic to screen.
		int startX = PApplet.max(0, -(x - pie.displayX));
		int startY = PApplet.max(0, -(y - pie.displayY));
		int drawWidth = graphic.width - PApplet.max(0, x + graphic.width - pie.displayX - pie.matrixWidth) - startX;
		int drawHeight = graphic.height - PApplet.max(0, y + graphic.height - pie.displayY - pie.matrixHeight) - startY;
		
		if (pie.app.g instanceof PGraphicsOpenGL) {		
			pie.app.copy(
					graphic,
					startX,
					startY,
					drawWidth,
					drawHeight,
					(startX > 0) ? 0 : (x - pie.displayX) * pie.pixelSize,
					(startY > 0) ? 0 : (y - pie.displayY) * pie.pixelSize,
					drawWidth * pie.pixelSize,
					drawHeight * pie.pixelSize
					);
		} else {
			pie.app.copy(
					graphic.get(),
					startX,
					startY,
					drawWidth,
					drawHeight,
					(startX > 0) ? 0 : (x - pie.displayX) * pie.pixelSize,
					(startY > 0) ? 0 : (y - pie.displayY) * pie.pixelSize,
					drawWidth * pie.pixelSize,
					drawHeight * pie.pixelSize
					);
		}
	}
}
