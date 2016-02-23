package black.alias.pixelpie.effect;

import processing.core.PApplet;
import black.alias.pixelpie.PixelPie;
import black.alias.pixelpie.sprite.Sprite;

public class Effect {
	public int objFrames, waitFrames, currentFrame, currentWait;  // Animation variables.
	public int x, y, xOffset, yOffset, origin, depth, objWidth, objHeight; 
	public final String sprite;
	public final PixelPie pie;
	public boolean destroyed;

	// Constructor.
	public Effect (int x, int y, int depth, int origin, String sprite, PixelPie pie) {

		// Set required variables.
		this.x = x;
		this.y = y;
		this.depth = depth;
		this.origin = origin;
		this.pie = pie;
		pie.effects.add(this);

		// Process sprite.
		this.sprite = sprite;
		Sprite pix = pie.spr.get(sprite);
		objWidth = pix.pixWidth;
		objHeight = pix.sprite.height;
		objFrames = pix.pixFrames;
		waitFrames = pix.waitFrames;

		// Refresh offsets.
		xOffset = PixelPie.getXOffset(origin, objWidth);
		yOffset = PixelPie.getYOffset(origin, objHeight);
	}

	public void animate() {
		// Update frames according to FPS.
		if (currentWait < waitFrames) {
			currentWait++;
		} else {
			currentWait = 0;
			if (currentFrame < objFrames) {
				currentFrame++;
			} else {
				destroy();
			}
		}
	}

	/**
	 * Add Effect to render queue.
	 * @param index
	 */
	public void draw(int index) { 
		
		if ( // If it's not on screen, skip draw method.
				PixelPie.toInt(pie.testOnScreen(x - xOffset, y - yOffset))
				+ PixelPie.toInt(pie.testOnScreen(x - xOffset + objWidth, y - yOffset))
				+ PixelPie.toInt(pie.testOnScreen(x - xOffset, y - yOffset + objHeight))
				+ PixelPie.toInt(pie.testOnScreen(x - xOffset + objWidth, y - yOffset + objHeight)) == 0) {
			return;
		}
		
		// Add object to render queue.
		pie.depthBuffer.append(PApplet.nf(depth, 4)	+ 3	+ index);
	}
	
	/**
	 * Draw effect to screen.
	 */
	public void render() {
		
		// Draw image to screen.
		int startX = PApplet.max(0, -(x - xOffset - pie.displayX));
		int startY = PApplet.max(0, -(y - yOffset - pie.displayY));
		int drawWidth = objWidth - PApplet.max(0, x - xOffset + objWidth - pie.displayX - pie.matrixWidth) - startX;
		int drawHeight = objHeight - PApplet.max(0, y - yOffset + objHeight - pie.displayY - pie.matrixHeight) - startY;
		
		pie.app.copy(
				pie.spr.get(sprite).sprite,
				startX + (currentFrame * objWidth),
				startY,
				drawWidth,
				drawHeight,
				(startX > 0) ? 0 : (x - xOffset - pie.displayX) * pie.pixelSize,
				(startY > 0) ? 0 : (y - yOffset - pie.displayY) * pie.pixelSize,
				drawWidth * pie.pixelSize,
				drawHeight * pie.pixelSize
				);
	}

	/**
	 * Remove effect from memory.
	 */
	private void destroy() {
		destroyed = true;
	}
}
