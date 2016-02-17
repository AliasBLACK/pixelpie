package black.alias.pixelpie.sprite;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import black.alias.pixelpie.*;
import black.alias.pixelpie.level.TileSet;

/**
 * Decal class.
 * @author Xuanming
 *
 */
public class Decal {
	public final PImage sprite;
	final int x, y, origin, xOffset, yOffset, depth, gid, objWidth, objHeight;
	final int objFrames, waitFrames;
	final PixelPie pie;
	final boolean isTile;
	PImage IlumSprite;
	int currentWait, currentFrame;

	/**
	 * Construct the decal object.
	 * @param PosX
	 * @param PosY
	 * @param Depth
	 * @param GID
	 */
	public Decal(int PosX, int PosY, int Depth, int GID, PixelPie pie) {
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
	public Decal(int PosX, int PosY, int Depth, int Origin, String Sprite, PixelPie pie) {
		this(PosX, PosY, Depth, 0, Origin, false, Sprite, pie);
	}

	/**
	 * Construct the decal object.
	 * @param PosX
	 * @param PosY
	 * @param Depth
	 * @param GID
	 * @param Origin
	 * @param IsTile
	 * @param Sprite
	 * @param pie
	 */
	public Decal(int PosX, int PosY, int Depth, int GID, int Origin, boolean IsTile, String Sprite, PixelPie pie) {

		// Set parameters.
		x = PosX;
		y = PosY;
		depth = Depth;
		gid = GID;
		origin = Origin;    
		isTile = IsTile;
		this.pie = pie;		

		// If it's a sprite...
		if (!isTile) {
			sprite = pie.lighting ? pie.spr.get(Sprite).sprite.get() : pie.spr.get(Sprite).sprite;
			objWidth = pie.spr.get(Sprite).pixWidth;
			objHeight = pie.spr.get(Sprite).sprite.height;
			objFrames = pie.spr.get(Sprite).pixFrames;
			waitFrames = pie.spr.get(Sprite).waitFrames;
			IlumSprite = pie.spr.get(Sprite).IlumSprite;

			if (origin != 0) {
				xOffset = PixelPie.getXOffset(origin, objWidth);
				yOffset = PixelPie.getYOffset(origin, objHeight);
			} else {
				xOffset = yOffset = 0;
			}

		// If it's a tile...
		} else {
			
			// Grab tile width and height.
			TileSet tileSet = pie.tileSetList[Integer.parseInt(pie.tileSetRef.substring((gid - 1) * 2, gid * 2))];
			objWidth = tileSet.tileWidth;
			objHeight = tileSet.tileHeight;
			
			// Defaults
			yOffset = xOffset = waitFrames = objFrames = 0;
			IlumSprite = null;
			
			// Cache image of tile.
			sprite = new PImage(objWidth, objHeight, PConstants.ARGB);
			sprite.copy(
					tileSet.tileSet,
					(gid % tileSet.tileColumns) * tileSet.tileWidth,
					(gid / tileSet.tileColumns) * tileSet.tileHeight,
					objWidth,
					objHeight,
					0,
					0,
					objWidth,
					objHeight
					);
		}
	}
	
	/**
	 * Burn lighting onto decal.
	 */
	public void light() {
		
		// Generate alpha map.
		PImage alpha = sprite.get();
		alpha.loadPixels();
		for (int i = 0; i < alpha.pixels.length; i ++) {
			alpha.pixels[i] = alpha.pixels[i] & 0xFFFFFF | (alpha.pixels[i] >> 24) & 0xFF;
		}
		alpha.updatePixels();
		
		// Create the light map.
		PImage lightMapCopy = pie.app.createImage(sprite.width, sprite.height, PConstants.ARGB);
		
		// Copy light map portion from main light map image.
		if (!isTile) {
			for (int i = 0; i <= objFrames; i++) {
				lightMapCopy.copy(pie.lightMap, x, y, objWidth, objHeight, i * objWidth, 0, objWidth, objHeight);
			}
		} else {
			lightMapCopy.copy(pie.lightMap, x, y, objWidth, objHeight, 0, 0, objWidth, objHeight);
		}
		
		// Add on ilumMap if any, using SCREEN blend mode.
		if (IlumSprite != null) {
			lightMapCopy.blend(IlumSprite, 0, 0, sprite.width, sprite.height, 0, 0, sprite.width, sprite.height, PConstants.SCREEN);
			IlumSprite = null;		// We don't need this anymore, free resources.
		}
		
		// Apply alpha mask to light map.
		lightMapCopy.mask(alpha);
		
		// Burn light map onto sprite.
		sprite.blend(lightMapCopy, 0, 0, sprite.width, sprite.height, 0, 0, sprite.width, sprite.height, PConstants.MULTIPLY);
	}

	/**
	 * Animate decal.
	 */
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

	/**
	 * Update the decal every frame.
	 */
	public void update(int index) {
		
		// If tile is completely off screen, skip update method.
		if (PixelPie.toInt(pie.testOnScreen(x - xOffset, y - yOffset))
				+ PixelPie.toInt(pie.testOnScreen(x - xOffset + objWidth, y - yOffset))
				+ PixelPie.toInt(pie.testOnScreen(x - xOffset, y - yOffset + objHeight))
				+ PixelPie.toInt(pie.testOnScreen(x - xOffset + objWidth, y - yOffset + objHeight)) == 0) {
			return;
		}

		// If it's on screen, even partially, render.
		pie.depthBuffer.append(PApplet.nf(depth, 4)	+ 1	+ index);
	}
	
	/**
	 * Draw the decal onto the screen.
	 */
	public void render() {
		pie.app.copy(
				sprite,
				currentFrame * objWidth,
				0,
				objWidth,
				objHeight,
				(x - pie.displayX) * pie.pixelSize,
				(y - pie.displayY) * pie.pixelSize,
				objWidth * pie.pixelSize,
				objHeight * pie.pixelSize
				);
	}
}
