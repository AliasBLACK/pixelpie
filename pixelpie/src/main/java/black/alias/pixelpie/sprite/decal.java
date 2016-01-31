package black.alias.pixelpie.sprite;

import black.alias.pixelpie.*;

/**
 * Decal class.
 * @author Xuanming
 *
 */
public class decal {
	int x, y, origin, xOffset, yOffset, depth, gid, objWidth, objHeight;
	int objFrames, waitFrames, currentFrame, currentWait; // Animation
	final PixelPie pie;
	
	// variables.
	boolean isTile;
	String sprite;

	/**
	 * Construct the decal object.
	 * @param PosX
	 * @param PosY
	 * @param Depth
	 * @param GID
	 */
	public decal(int PosX, int PosY, int Depth, int GID, PixelPie pie) {
		this(PosX, PosY, Depth, GID, 0, true, null, pie);
	}

	/**
	 * Construct the decal object.
	 * @param PosX
	 * @param PosY
	 * @param Depth
	 * @param Origin
	 * @param Sprite
	 */
	public decal(int PosX, int PosY, int Depth, int Origin, String Sprite, PixelPie pie) {
		this(PosX, PosY, Depth, 0, Origin, false, Sprite, pie);
	}

	public decal(int PosX, int PosY, int Depth, int GID, int Origin, boolean IsTile, String Sprite, PixelPie pie) {

		// Set parameters.
		x = PosX;
		y = PosY;
		depth = Depth;
		gid = GID;
		origin = Origin;    
		isTile = IsTile;
		sprite = Sprite;
		this.pie = pie;

		// If it's a sprite...
		if (!isTile) {
			sprite pix = pie.spr.get(sprite);
			objWidth = pix.pixWidth;
			objHeight = pix.sprite.height;
			objFrames = pix.pixFrames;
			waitFrames = pix.waitFrames;

			if (origin != 0) {
				xOffset = PixelPie.getXOffset(origin, objWidth);
				yOffset = PixelPie.getYOffset(origin, objHeight);
			}

			// If it's a tile...
		} else {
			objWidth = pie.tileSetList[Integer.parseInt(pie.tileSetRef.substring((gid - 1) * 2, gid * 2))].tileWidth;
			objHeight = pie.tileSetList[Integer.parseInt(pie.tileSetRef.substring((gid - 1) * 2, gid * 2))].tileHeight;
		}
	}

	// Update currentFrame if it's an animated sprite.
	public void animate() {
		if (!isTile) {
			if (currentWait < waitFrames) {
				currentWait++;
			} else {
				currentWait = 0;
				if (currentFrame < objFrames) {
					currentFrame++;
				} else {
					currentFrame = 0;
				}
			}
		}
	}

	// Draw the tile to screen.
	public void update() {
		// If tile is completely off screen, skip update method.
		if (PixelPie.toInt(pie.testOnScreen(x - xOffset, y - yOffset))
				+ PixelPie.toInt(pie.testOnScreen(x - xOffset + objWidth, y - yOffset))
				+ PixelPie.toInt(pie.testOnScreen(x - xOffset, y - yOffset + objHeight))
				+ PixelPie.toInt(pie.testOnScreen(x - xOffset + objWidth, y - yOffset + objHeight)) == 0) {
			return;
		}

		// If it's on screen, even partially, render.
		if (isTile) {
			if (pie.lighting) {
				pie.drawTileDepth(x, y - pie.currentLevel.tileHeight, depth, 1, gid);
			} else {
				pie.drawTileDepth(x, y - pie.currentLevel.tileHeight, depth, gid);
			}
		} else {
			if (pie.lighting) {
				pie.drawSprite(x - xOffset, y - yOffset, depth, currentFrame, 255, 1, sprite);
			} else {
				pie.drawSprite(x - xOffset, y - yOffset, depth, currentFrame, 255, 0, sprite);
			}
		}
	}
}
