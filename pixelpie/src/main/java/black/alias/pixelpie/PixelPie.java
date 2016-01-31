package black.alias.pixelpie;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import processing.core.*;
import processing.data.StringList;
import processing.opengl.PGraphicsOpenGL;
import black.alias.pixelpie.level.*;
import black.alias.pixelpie.loader.*;
import black.alias.pixelpie.sound.*;
import black.alias.pixelpie.sound.levelSound.envSound;
import black.alias.pixelpie.sound.levelSound.globalSound;
import black.alias.pixelpie.sound.levelSound.levelSound;
import black.alias.pixelpie.sprite.*;
import black.alias.pixelpie.controls.controls;
import black.alias.pixelpie.graphics.*;

/**
 * The main PixelPie class.
 * @author Xuanming Zhou
 *
 */
public class PixelPie {
	public final PApplet app;
	public final soundDevice SoundDevice;
	public final logger log;
	public int displayX, displayY, roomWidth, roomHeight, matrixWidth, matrixHeight,
		pixelSize, minScale, maxScale, index;
	public final PImage canvas;
	public float frameRate, frameProgress;
	public boolean displayFPS, loaded, waitThread, lighting, levelLoading, isPaused;
	public int background, black, white;
	public int[] pixelMatrix;
	public StringList depthBuffer;
	public collisionDetector collider;
	public script currentScript;
	
	// Threaded loaders.
	public levelLoader lvlLoader;
	public dataLoader datLoader;
	
	// Containers for object instances.
	public final ArrayList<gameObject> objects;
	public final ArrayList<decal> decals;
	public final ArrayList<graphics> graphics;
	public final ArrayList<levelSound> sounds;
	
	// Containers for permanent assets.
	public final HashMap<String, sprite> spr;
	public final HashMap<String, level> lvl;

	// Stuff for level.
	public String currentLevelName, loadingText, tileSetRef;
	public String loadLevelTarget = "";
	public int levelZoom, levelBrightness;
	public level currentLevel;
	public tileSet[] tileSetList;

	// Stuff needed to make lighting work.
	public float gamma;
	public float[] lightMapMult;
	public PImage levelBuffer;
	public PGraphics lightMap;
	
	/**
	 * Initialize PixelPie.
	 * @param app
	 * @param minim
	 */
	public PixelPie(PApplet app, soundDevice device) {
		this(app, device, 2, 30.0f);
	}
	
	/**
	 * Initialize PixelPie.
	 * @param app
	 * @param device
	 * @param fps
	 */
	public PixelPie(PApplet app, soundDevice device, float fps) {
		this(app, device, 2, fps);
	}
	
	/**
	 * Initialize PixelPie.
	 * @param app
	 * @param minim
	 * @param PixelSize
	 */
	public PixelPie(PApplet app, soundDevice device, int PixelSize) {
		this(app, device, PixelSize, 30.0f);
	}
	
	/**
	 * Initialize PixelPie.
	 * @param app
	 * @param device
	 * @param PixelSize
	 * @param fps
	 */
	public PixelPie(PApplet app, soundDevice device, int PixelSize, float fps) {
		
		// Keep reference to PApplet.
		this.app = app;
		
		// Set desired frameRate, and keep record of it for animation purposes.
		this.frameRate = fps;
		app.frameRate(fps);
		
		// Set PApplet openGL rendering parameters.
		((PGraphicsOpenGL)app.g).textureSampling(3);
		
		// Essential Parameters.
		app.background(0);

		// Set default values of variables.
		pixelSize = PixelSize;
		black = app.color(0, 0, 0);
		white = app.color(255, 255, 255);
		background = black;                 // Background color.
		maxScale = 20;                      // Maximum pixel scale.
		roomWidth = 1000;                   // Default width of game level.
		roomHeight = 1000;                  // Default height of game level.

		// Set floor for pixelSize, also pixel size for UI.
		minScale = pixelSize;
		
		// Initiate controls.
		new controls(this);

		// Initiate pixelMatrix.
		matrixWidth = Math.round(app.width / pixelSize);
		matrixHeight = Math.round(app.height / pixelSize);
		canvas = app.createImage(matrixWidth, matrixHeight, PConstants.ARGB);
		pixelMatrix = canvas.pixels;

		// Initiate levelBuffer.
		levelBuffer = app.createImage(1, 1, PConstants.ARGB);

		// Initiate collision detector.
		collider = new collisionDetector(this);
		collider.start();

		// Initiate depthBuffers.
		depthBuffer = new StringList();

		// Grab reference to soundDevice.
		this.SoundDevice = device;
		
		// Initiate logger.
		this.log = new logger(app);
		
		// Initiate dataLoader.
		datLoader = new dataLoader(this);
		datLoader.start();
		this.spr = datLoader.getSpr();
		this.lvl = datLoader.getLvl();
		
		// Initiate levelLoader.
		lvlLoader = new levelLoader(this); 
		lvlLoader.start();
		
		// Initiate containers.
		objects = new ArrayList<gameObject>();
		decals = new ArrayList<decal>();
		graphics = new ArrayList<graphics>();
		sounds = new ArrayList<levelSound>();
		
		// Register methods with Processing.
		app.registerMethod("draw", this);
		
		// Start loading assets.
		loaded = false;
		datLoader.run();
	}
	
	/**
	 * Method called by processing at the end of draw method.
	 */
	public void draw() {
		
		// Prepare canvas for drawing.
		canvas.loadPixels();

		// If assets are loaded, run loop functions.
		if (loaded && !levelLoading) {
			waitThread = true;
			collider.run();
			updateScript();
			updateLevel();
			updateDecals();
			updateObjects();
			updateGraphics();
			updateSounds();
		}

		// Else, show loading screen.
		else {}

		// Draw depthBuffer to pixelMatrix.
		depthBuffer.sort();
		for (String str : depthBuffer) {
			switch(toInt(str.substring(4,5))) {  

			// If entry is a sprite.
			case 0:
				drawSpriteToMatrix (
						intToSign(toInt(str.substring(5,6))) * toInt(str.substring(6,10)),
						intToSign(toInt(str.substring(10,11))) * toInt(str.substring(11,15)),
						toInt(str.substring(15,19)),
						toInt(str.substring(19,22)),
						toInt(str.substring(22,23)),
						toInt(str.substring(23,24)),
						str.substring(24)
						);
				break;

				// If entry is a tile.
			case 1:
				int gid = toInt(str.substring(16,20));
				drawTile (
						intToSign(toInt(str.substring(5,6))) * toInt(str.substring(6,10)),
						intToSign(toInt(str.substring(10,11))) * toInt(str.substring(11,15)),
						gid,
						toInt(str.substring(15,16)),
						tileSetList[toInt(tileSetRef.substring((gid - 1) * 2, gid * 2))]
						);
				break;

			case 2:
				drawGraphicToMatrix (
						intToSign(toInt(str.substring(5,6))) * toInt(str.substring(6,10)),
						intToSign(toInt(str.substring(10,11))) * toInt(str.substring(11,15)),
						toInt(str.substring(15,16)),
						toInt(str.substring(16))
						);
				break;
			}
		}
		depthBuffer.clear();
		
		// End canvas editing and draw to screen.
		canvas.updatePixels();
		app.image(canvas, 0, 0, app.width, app.height);
		
		// Level loading screen.
		if (levelLoading) {
			app.fill(black);
			app.rect(0, 0, app.width, app.height);
			app.fill(white);
			app.textAlign(PConstants.CENTER, PConstants.BOTTOM);
			app.text("Loading...", app.width * 0.5f, app.height * 0.5f - 2);
			app.textAlign(PConstants.CENTER, PConstants.TOP);
			app.text(loadingText, app.width * 0.5f, app.height * 0.5f + 2);
		}

		// Show FPS
		if (displayFPS) {
			app.textAlign(PConstants.LEFT, PConstants.TOP); 
			app.fill(white);
			app.text(app.frameRate, 20, 20);
		}

		// Wait for controller class to sync.
		while (waitThread) {};
	}
	
	/**
	 * Draw a pixel onto pixelMatrix array.
	 * @param x
	 * @param y
	 * @param rgb
	 */
	private void drawPixel(int x, int y, int rgb) {
		drawPixel(x, y, false, rgb, 0);
	}

	/**
	 * Draw a pixel onto pixelMatrix array.
	 * @param x
	 * @param y
	 * @param lighted
	 * @param rgb
	 */
	private void drawPixel(int x, int y, boolean lighted, int rgb) {
		drawPixel(x, y, lighted, rgb, 0);
	}

	/**
	 * Draw a pixel onto pixelMatrix array.
	 * @param x
	 * @param y
	 * @param lighted
	 * @param rgb
	 * @param ilum
	 */
	private void drawPixel(int x, int y, boolean lighted, int rgb, float ilum) {

		// Test for alpha value of pixel.
		switch ((rgb >> 24) & 0xFF) {

		// If pixel is completely transparent, ignore.
		case 0:
			break;

		// If pixel is opaque, replace original pixel.
		case 255:
			if (lighted) {
				pixelMatrix[(x - displayX) + ((y - displayY) * matrixWidth)] = applyLight(x, y, rgb, ilum);
			} else {
				pixelMatrix[(x - displayX) + ((y - displayY) * matrixWidth)] = rgb;
			}
			break;

		// If pixel has alpha value, modify original pixel instead.
		default:
			int i = (x - displayX) + ((y - displayY) * matrixWidth);

			// If pixel is lighted, add environmental lighting to it.
			if (lighted) {

				// Get gamma value of this pixel from lightmap.
				float gamma = lightMapMult[x + roomWidth * y];

				// If sprite has an IlumMap, apply IlumMap values of this pixel.
				if (ilum > 0) {
					gamma = PApplet.lerp(gamma, 1, ilum);
				}

				// Set the RGB value of pixel.
				pixelMatrix[i] = app.lerpColor(pixelMatrix[i], app.lerpColor(black, rgb, gamma), ((rgb >> 24) & 0xFF) / 255.0f);

				// Else, just calculated modified color without lighting.
			} else {
				pixelMatrix[i] = app.lerpColor(pixelMatrix[i], rgb, ((rgb >> 24) & 0xFF) / 255.0f);
			}
			break;
		}
	}

	/**
	 * Draw sprite onto pixelMatrix array.
	 * @param x
	 * @param y
	 * @param frame
	 * @param alpha
	 * @param testResult
	 * @param spriteName
	 */
	public void drawSpriteToMatrix(int x, int y, int frame, int alpha, int testResult, String spriteName) {
		drawSpriteToMatrix(x, y, frame, alpha, testResult, 0, spriteName);
	}

	/**
	 * Draw sprite onto pixelMatrix array.
	 * @param x
	 * @param y
	 * @param frame
	 * @param alpha
	 * @param testResult
	 * @param lighted
	 * @param spriteName
	 */
	public void drawSpriteToMatrix (int x, int y, int frame, int alpha, int testResult, int lighted, String spriteName) {

		// Get sprite.
		sprite sprite = spr.get(spriteName);

		// Fail safe in case sprite is missing.
		if (sprite == null) {testResult = 0;}

		switch (testResult) {    
		// If partially on screen...
		case 1:    
			// Get margins.
			int startX = (x < displayX) ? (displayX - x) : 0;
			int endX = (x + sprite.pixWidth >= Math.round(app.width/pixelSize) + displayX) ? ((Math.round(app.width/pixelSize) + displayX) - x) : sprite.pixWidth;
			int startY = (y < displayY) ? (displayY - y) : 0;
			int endY = (y + sprite.sprite.height >= Math.round(app.height/pixelSize) + displayY) ? ((Math.round(app.height/pixelSize) + displayY) - y) : sprite.sprite.height;

			// Render partially to screen.
			for (int i = startX; i < endX; i++) {
				for (int k = startY; k < endY; k++) {

					// If sprite has transparency.
					if (alpha < 255) {

						// If lighted.
						if (lighted == 1) {
							drawPixel(
									i + x, k + y, true,
									applyAlpha(alpha, sprite.sprite.pixels[i + (sprite.pixWidth * frame) + (k * sprite.sprite.width)]),
									(sprite.hasIlum) ? sprite.IlumMap[i + (sprite.pixWidth * frame) + k * sprite.sprite.width] : 0
									);

							// If not lighted.
						} else {drawPixel(i + x, k + y, applyAlpha(alpha, sprite.sprite.pixels[i + (sprite.pixWidth * frame) + (k * sprite.sprite.width)]));}

					// If sprite is opaque.
					} else {

						// If lighted.
						if (lighted == 1) {
							drawPixel(
									i + x, k + y, true,
									sprite.sprite.pixels[i + (sprite.pixWidth * frame) + (k * sprite.sprite.width)],
									(sprite.hasIlum) ? sprite.IlumMap[i + (sprite.pixWidth * frame) + k * sprite.sprite.width] : 0
									);

						// If not lighted.
						} else {drawPixel(i + x, k + y, sprite.sprite.pixels[i + (sprite.pixWidth * frame) + (k * sprite.sprite.width)]);}
					}
				}
			}
			break;

		// If entirely on screen, render all.
		case 2:
			for (int i = 0; i < sprite.pixWidth; i++) {
				for (int k = 0; k < sprite.sprite.height; k++) {

					// If sprite has transparency.
					if (alpha < 255) {

						// If lighted.
						if (lighted == 1) {
							drawPixel(
									i + x, k + y, true,
									applyAlpha(alpha, sprite.sprite.pixels[PApplet.constrain(i + (sprite.pixWidth * frame) + (k * sprite.sprite.width), 0, sprite.sprite.pixels.length - 1)]),
									(sprite.hasIlum) ? sprite.IlumMap[i + (sprite.pixWidth * frame) + k * sprite.sprite.width] : 0
									);

						// If not lighted.
						} else {drawPixel(i + x, k + y, applyAlpha(alpha, sprite.sprite.pixels[PApplet.constrain(i + (sprite.pixWidth * frame) + (k * sprite.sprite.width), 0, sprite.sprite.pixels.length - 1)]));}

					// If sprite is opaque.
					} else {

						// If lighted.
						if (lighted == 1) {
							drawPixel(
									i + x, k + y, true,
									sprite.sprite.pixels[PApplet.constrain(i + (sprite.pixWidth * frame) + (k * sprite.sprite.width), 0, sprite.sprite.pixels.length - 1)],
									(sprite.hasIlum) ? sprite.IlumMap[i + (sprite.pixWidth * frame) + k * sprite.sprite.width] : 0
									);

						// If not lighted.
						} else {drawPixel(i + x, k + y, sprite.sprite.pixels[PApplet.constrain(i + (sprite.pixWidth * frame) + (k * sprite.sprite.width), 0, sprite.sprite.pixels.length - 1)]);}
					}
				}
			}
			break;
		}
	}

	/**
	 * Add sprite to depthBuffer.
	 * @param x
	 * @param y
	 * @param depth
	 * @param frame
	 * @param spriteName
	 */
	public void drawSprite(int x, int y, int depth, int frame, String spriteName) {
		drawSprite(x, y, depth, frame, 255, 0, spriteName);
	}

	/**
	 * Add sprite to depthBuffer.
	 * @param x
	 * @param y
	 * @param depth
	 * @param frame
	 * @param alpha
	 * @param spriteName
	 */
	public void drawSprite(int x, int y, int depth, int frame, int alpha, String spriteName) {
		drawSprite(x, y, depth, frame, alpha, 0, spriteName);
	}

	/**
	 * Add sprite to depthBuffer.
	 * @param x
	 * @param y
	 * @param depth
	 * @param frame
	 * @param alpha
	 * @param lighted
	 * @param spriteName
	 */
	public void drawSprite(int x, int y, int depth, int frame, int alpha, int lighted, String spriteName) {

		// Get sprite.
		sprite sprite = spr.get(spriteName);

		// Test if all edges are on screen.
		int testResult = toInt(testOnScreen(x, y))
				+ toInt(testOnScreen(x + sprite.pixWidth, y + sprite.sprite.height));

		// If partially or completely on-screen, render it.
		if (testResult > 0) {
			if (depth == 0) {
				drawSpriteToMatrix(x, y, frame, alpha, testResult, lighted, spriteName);

				// Else, add to depthBuffer.
			} else {
				depthBuffer.append(PApplet.nf(depth, 4)
						+ "0" + PApplet.str(signToInt(x))
						+ PApplet.nf(PApplet.abs(x), 4)
						+ PApplet.str(signToInt(y))
						+ PApplet.nf(PApplet.abs(y), 4)
						+ PApplet.nf(frame, 4)
						+ PApplet.nf(alpha, 3)
						+ PApplet.str(testResult)
						+ PApplet.str(lighted)
						+ spriteName
				);
			}
		}
	}

	/**
	 * Apply alpha to color.
	 * @param alpha
	 * @param rgb
	 * @return
	 */
	public static int applyAlpha(int alpha, int rgb) {
		rgb = ((((rgb >> 24) & 0xFF) * alpha / 255) << 24) | rgb & 0xFFFFFF;
		return rgb;
	}

	/**
	 * Test if pixel is on screen.
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean testOnScreen(int x, int y) {
		if (x < displayX) {
			return false;
		} else if (y < displayY) {
			return false;
		} else if (x > (app.width / pixelSize) + displayX) {
			return false;
		} else if (y > (app.height / pixelSize) + displayY) {
			return false;
		} else
			return true;
	}

	/**
	 * Test if pixel is on level.
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean testOnLevel(int x, int y) {
		return ((x >= 0) && (x < roomWidth) && (y >= 0) && (y < roomHeight)) ? true : false;
	}
	
	/**
	 * Apply gamma on lightMap to pixel.
	 * @param x
	 * @param y
	 * @param originalColor
	 * @return
	 */
	public int applyLight(int x, int y, int originalColor) {
		return applyLight(x, y, originalColor, 0);
	}

	/**
	 * Apply gamma on lightMap to pixel.
	 * @param x
	 * @param y
	 * @param originalColor
	 * @param ilum
	 * @return
	 */
	public int applyLight(int x, int y, int originalColor, float ilum) {

		gamma = lightMapMult[x + roomWidth * y];
		if (ilum > 0) {
			gamma = PApplet.lerp(gamma, 1, ilum);
		}

		// Recalculate resultant color value.
		return app.lerpColor(black, originalColor, gamma);
	}
	
	/**
	 *  Check if file exists.
	 * @param filename
	 * @return
	 */
	public boolean fileExists(String filename) {
		boolean result;
		
		if (filename != null && !filename.isEmpty()) {
			File path = new File(filename);
			result = path.exists();
		} else {
			result = false;
		}
		return result;
	}
	
	/**
	 * Convert integer to boolean (true if > 0, false if <= 0) 
	 * @param i
	 * @return
	 */
	public static boolean toBoolean(int i) {
		return (i > 0) ? true : false;
	}
	
	/**
	 * Convert boolean to integer. (1 if true, 0 if false)
	 * @param b
	 * @return
	 */
	public static int toInt(boolean b) {
		return b ? 1 : 0;
	}
	
	/**
	 * Convert sign to integer. (1 if > 0, 0 if <= 0)
	 * @param i
	 * @return
	 */
	public static int signToInt(int i) {
		return (i > 0) ? 1 : 0;
	}

	/**
	 * Convert integer to sign. (1 if > 0, -1 if <= 0);
	 * @param i
	 * @return
	 */
	public static int intToSign(int i) {
		return (i > 0) ? 1 : -1;
	}
	
	/**
	 *  Get X Offset.
	 * @param origin 1-9, corresponding to positions on the numpad.
	 * @param objWidth
	 * @return
	 */
	public static int getXOffset(int origin, int objWidth) {
		int xOffset;

		switch (origin) {

		// Top-left.
		case 1:
			xOffset = 0;
			break;

			// Top-center.
		case 2:
			xOffset = objWidth / 2;
			break;

			// Top-right.
		case 3:
			xOffset = objWidth;
			break;

			// Center-left.
		case 4:
			xOffset = 0;
			break;

			// Center.
		case 5:
			xOffset = objWidth / 2;
			break;

			// Center-right.
		case 6:
			xOffset = objWidth;
			break;

			// Bottom-left.
		case 7:
			xOffset = 0;
			break;

			// Bottom-center.
		case 8:
			xOffset = objWidth / 2;
			break;

			// Bottom-right.
		case 9:
			xOffset = objWidth;
			break;

		default:
			xOffset = 0;
			break;
		}
		return xOffset;
	}

	/**
	 * Get Y Offset.
	 * @param origin
	 * @param objHeight
	 * @return
	 */
	public static int getYOffset(int origin, int objHeight) {
		int yOffset;

		switch (origin) {

		// Top-left.
		case 1:
			yOffset = 0;
			break;

			// Top-center.
		case 2:
			yOffset = 0;
			break;

			// Top-right.
		case 3:
			yOffset = 0;
			break;

			// Center-left.
		case 4:
			yOffset = objHeight / 2;
			break;

			// Center.
		case 5:
			yOffset = objHeight / 2;
			break;

			// Center-right.
		case 6:
			yOffset = objHeight / 2;
			break;

			// Bottom-left.
		case 7:
			yOffset = objHeight;
			break;

			// Bottom-center.
		case 8:
			yOffset = objHeight;
			break;

			// Bottom-right.
		case 9:
			yOffset = objHeight;
			break;

		default:
			yOffset = 0;
			break;
		}
		return yOffset;
	}
	


	/**
	 * Collision Single Point. (return object collided with)
	 * @param x
	 * @param y
	 * @return
	 */
	public gameObject ptCollision(int x, int y) {
		String[] noStrings = {};
		return ptCollision(x, y, noStrings);
	}

	/**
	 * Collision Single Point. (return object collided with)
	 * @param x
	 * @param y
	 * @param ignore
	 * @return
	 */
	public gameObject ptCollision(int x, int y, String[] ignore) {
		for (gameObject obj : objects) {
			if (strMatch(obj.type, ignore)) {
				continue;
			} else if (left(obj) >= x) {
				continue;
			} else if (top(obj) >= y) {
				continue;
			} else if (right(obj) <= x) {
				continue;
			} else if (btm(obj) <= y) {
				continue;
			} else {
				return obj;
			}
		}
		return null;
	}

	/**
	 *  Horizontal Collision (by Points)
	 * @param x1
	 * @param x2
	 * @param x3
	 * @param x4
	 * @return
	 */
	public static boolean horzCollision(int x1, int x2, int x3, int x4) {
		return (((x1 < x3) && (x2 < x3)) || ((x4 < x1) && (x4 < x2))) ? false : true;
	}

	/**
	 *  Vertical Collision (by Points)
	 * @param y1
	 * @param y2
	 * @param y3
	 * @param y4
	 * @return
	 */
	public static boolean vertCollision(int y1, int y2, int y3, int y4) {
		return (((y1 < y3) && (y2 < y3)) || ((y4 < y1) && (y4 < y2))) ? false : true;
	}

	/**
	 *  Horizontal Collision (by Object)
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public static boolean objHorzCollision(gameObject obj1, gameObject obj2) {
		return horzCollision(obj1.x - obj1.bBoxXOffset, obj1.x + obj1.bBoxWidth - obj1.bBoxXOffset,
				obj2.x - obj2.bBoxXOffset, obj2.x + obj2.bBoxWidth - obj2.bBoxXOffset);
	}

	/**
	 *  Vertical Collision (by Object)
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public static boolean objVertCollision(gameObject obj1, gameObject obj2) {
		return vertCollision(obj1.y - obj1.bBoxYOffset, obj1.y + obj1.bBoxHeight - obj1.bBoxYOffset,
				obj2.y - obj2.bBoxYOffset, obj2.y + obj2.bBoxHeight - obj2.bBoxYOffset);
	}

	/**
	 *  Current-frame object collision.
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public static boolean objCollision(gameObject obj1, gameObject obj2) {
		return (toInt(objHorzCollision(obj1, obj2)) + toInt(objVertCollision(obj1, obj2)) == 2) ? true
				: false;
	}

	/**
	 *  Predictive object collision.
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public static boolean objColPredictive (gameObject obj1, gameObject obj2) {
		if (
				toInt(horzCollision (
						obj1.x - obj1.bBoxXOffset + Math.round(obj1.xSpeed),
						obj1.x + obj1.bBoxWidth - obj1.bBoxXOffset + PApplet.ceil(obj1.xSpeed),
						obj2.x - obj2.bBoxXOffset + Math.round(obj2.xSpeed),
						obj2.x + obj2.bBoxWidth - obj2.bBoxXOffset + PApplet.ceil(obj2.xSpeed))) + 
				toInt(vertCollision (
						obj1.y - obj1.bBoxYOffset + Math.round(obj1.ySpeed),
						obj1.y + obj1.bBoxHeight - obj1.bBoxYOffset + PApplet.ceil(obj1.ySpeed),
						obj2.y - obj2.bBoxYOffset + Math.round(obj2.ySpeed),
						obj2.y + obj2.bBoxHeight - obj2.bBoxYOffset + PApplet.ceil(obj2.ySpeed))) == 2
				) {
			return true;
		} else { return false; }
	}
	
	/**
	 *  Has value in string array.
	 * @param str
	 * @param strList
	 * @return
	 */
	public static boolean strMatch(String str, String[] strList) {
		for (String strListEntry : strList) {
			if (str == strListEntry) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Calculate bottom bBox face positions of object.
	 * @param obj
	 * @return
	 */
	public static int btm(gameObject obj) {
		return obj.y - obj.bBoxYOffset + obj.bBoxHeight;
	}
	
	/**
	 * Calculate top bBox face positions of object.
	 * @param obj
	 * @return
	 */
	public static int top(gameObject obj) {
		return obj.y - obj.bBoxYOffset;
	}

	/**
	 * Calculate left bBox face positions of object.
	 * @param obj
	 * @return
	 */
	public static int left(gameObject obj) {
		return obj.x - obj.bBoxXOffset;
	}

	/**
	 * Calculate right bBox face positions of object.
	 * @param obj
	 * @return
	 */
	public static int right(gameObject obj) {
		return obj.x - obj.bBoxXOffset + obj.bBoxWidth;
	}
	
	/**
	 * Pause the game.
	 */
	public void pause() {
		isPaused = (isPaused) ? false : true;
	}

	/**
	 * Get substring from right.
	 * @param str
	 * @param chars
	 * @return
	 */
	public static String endString(String str, int chars) {
		return str.substring(str.length() - chars);
	}

	/**
	 * Grab object parameters.
	 * @param str
	 * @param obj
	 * @return
	 */
	public static String getParam(String str, gameObject obj) {
		if (obj.parameters == null) {
			return "";
		} else {
			return (obj.parameters.hasKey(str)) ? obj.parameters.get(str) : "";
		}
	}

	/**
	 * Mouse position in level.
	 * @return
	 */
	public int mouseX() {return Math.round((app.mouseX/pixelSize) + displayX);}

	/**
	 * Mouse position in level.
	 * @return
	 */
	public int mouseY() {return Math.round((app.mouseY/pixelSize) + displayY);}

	/**
	 * Calculate vertical distance between objects.
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public static int vertDist(gameObject obj1, gameObject obj2) {
		int result1 = (obj2.y - obj2.bBoxYOffset) - (obj1.y - obj1.bBoxYOffset + obj1.bBoxHeight);
		int result2 = (obj2.y - obj2.bBoxYOffset + obj2.bBoxHeight) - (obj1.y - obj1.bBoxYOffset);
		return (PApplet.min(PApplet.abs(result1), PApplet.abs(result2)) == PApplet.abs(result1)) ? result1 : result2;
	}

	/**
	 * Calculate horizontal distance between objects.
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public static int horzDist(gameObject obj1, gameObject obj2) {
		int result1 = (obj2.x - obj2.bBoxXOffset) - (obj1.x - obj1.bBoxXOffset + obj1.bBoxWidth);
		int result2 = (obj2.x - obj2.bBoxXOffset + obj2.bBoxWidth) - (obj1.x - obj1.bBoxXOffset);
		return (PApplet.min(PApplet.abs(result1), PApplet.abs(result2)) == PApplet.abs(result1)) ? result1 : result2;
	}

	/**
	 * Calculate distance between nearest bounding box faces of objects.
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public static float dist(gameObject obj1, gameObject obj2) {
		float result = 0;
		switch (toInt(objHorzCollision(obj1, obj2)) + (2 * toInt(objVertCollision(obj1, obj2)))) {

		// If no horizontal or vertical collisions.
		case 0:
			result = PApplet.sqrt(PApplet.sq(vertDist(obj1, obj2)) + PApplet.sq(horzDist(obj1, obj2)));
			break;

			// If horizontal collision (one is vertically over the other).
		case 1:
			result = vertDist(obj1, obj2);
			break;

			// If vertical collision (one is horizontally beside the other).
		case 2:
			result = horzDist(obj1, obj2);
			break;
		}
		return result;
	}
	
	/**
	 * Update method for current Script.
	 */
	public void updateScript() {
		if (scriptRunning()) {
			currentScript.update();
		}
	}

	/**
	 * Grab frame number from XML level data.
	 * @param str
	 * @return
	 */
	public static int getScriptFrame(String str) {
		if (str.indexOf(" ") == -1) {return toInt(str);}
		else {return toInt(str.substring(0, str.indexOf(" ")));}
	}

	/**
	 * Check if a script is running.
	 * @return
	 */
	public boolean scriptRunning() {
		return (currentScript != null) ? true : false;
	}

	/**
	 * Create array value of 2 integer coordinate value.
	 * @param x
	 * @param y
	 * @param level
	 * @return
	 */
	public static int coordToArray(int x, int y, level level) {
		return x + y * level.levelWidth;
	}

	/**
	 * Get X coordinate value from array value.
	 * @param value
	 * @param level
	 * @return
	 */
	public static int getCoordX(int value, level level) {
		return value % level.levelWidth;
	}

	/**
	 * Get Y coordinate value from array value.
	 * @param value
	 * @param level
	 * @return
	 */
	public static int getCoordY(int value, level level) {
		return Math.round(value / level.levelWidth);
	}
	
	/**
	 * Get properties from map.
	 * @param prop
	 * @param index
	 * @return
	 */
	public static String getProp(HashMap<String, String> prop, String index) {
		String result = prop.get(index);
		return (result == null) ? "0" : result;
	}
	
	/**
	 * Draw tile onto PixelMatrix (with depth buffer).
	 * @param x
	 * @param y
	 * @param depth
	 * @param gid
	 */
	public void drawTileDepth(int x, int y, int depth, int gid) {
		drawTileDepth(x, y, depth, 0, gid);
	}

	/**
	 * Draw tile onto PixelMatrix (with depth buffer).
	 * @param x
	 * @param y
	 * @param depth
	 * @param lighted
	 * @param gid
	 */
	public void drawTileDepth(int x, int y, int depth, int lighted, int gid) {
		depthBuffer.append(PApplet.nf(depth, 4) + "1" + PApplet.str(signToInt(x)) + PApplet.nf(PApplet.abs(x), 4) + PApplet.str(signToInt(y))
				+ PApplet.nf(PApplet.abs(y), 4) + PApplet.str(lighted) + PApplet.nf(gid, 4));
	}
	
	/**
	 * Update all decals.
	 */
	public void updateDecals() {
		for (decal decal : decals) {
			if (!isPaused) {
				decal.animate();
			}
			decal.update();
		}
	}
	
	/**
	 * Load a new level.
	 * @param levelName
	 */
	public void loadLevel(String levelName) {
		loadLevelTarget = levelName;
		lvlLoader.run();
	}
	
	/**
	 * Draw light spots on lightMap.
	 * @param x
	 * @param y
	 * @param Width
	 * @param Height
	 * @param brightness
	 * @param pg
	 */
	public static void drawLight(int x, int y, int Width, int Height, int brightness, PGraphics pg) {
		pg.beginDraw();
		pg.noStroke();
		pg.fill(255, brightness / 4);
		for (int i = 0; i < 10; i++) {
			pg.ellipse(x, y, Width / (i + 1), Height / (i + 1));
		}
		pg.endDraw();
	}
	
	/**
	 * Super Fast Blur (by Mario Klingemann <http://incubator.quasimondo.com>)
	 * @param img
	 * @param radius
	 */
	public static void fastblur(PImage img, int radius) {
		if (radius < 1) {
			return;
		}
		img.loadPixels();
		int w = img.width;
		int h = img.height;
		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;
		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, p1, p2, yp, yi, yw;
		int vmin[] = new int[PApplet.max(w, h)];
		int vmax[] = new int[PApplet.max(w, h)];
		int[] pix = img.pixels;
		int dv[] = new int[256 * div];
		for (i = 0; i < 256 * div; i++) {
			dv[i] = (i / div);
		}
		yw = yi = 0;
		for (y = 0; y < h; y++) {
			rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + PApplet.min(wm, PApplet.max(i, 0))];
				rsum += (p & 0xff0000) >> 16;
			gsum += (p & 0x00ff00) >> 8;
		bsum += p & 0x0000ff;
			}
			for (x = 0; x < w; x++) {
				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];
				if (y == 0) {
					vmin[x] = PApplet.min(x + radius + 1, wm);
					vmax[x] = PApplet.max(x - radius, 0);
				}
				p1 = pix[yw + vmin[x]];
				p2 = pix[yw + vmax[x]];
				rsum += ((p1 & 0xff0000) - (p2 & 0xff0000)) >> 16;
			gsum += ((p1 & 0x00ff00) - (p2 & 0x00ff00)) >> 8;
		bsum += (p1 & 0x0000ff) - (p2 & 0x0000ff);
		yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = PApplet.max(0, yp) + x;
				rsum += r[yi];
				gsum += g[yi];
				bsum += b[yi];
				yp += w;
			}
			yi = x;
			for (y = 0; y < h; y++) {
				pix[yi] = 0xff000000 | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];
				if (x == 0) {
					vmin[y] = PApplet.min(y + radius + 1, hm) * w;
					vmax[y] = PApplet.max(y - radius, 0) * w;
				}
				p1 = x + vmin[y];
				p2 = x + vmax[y];
				rsum += r[p1] - r[p2];
				gsum += g[p1] - g[p2];
				bsum += b[p1] - b[p2];
				yi += w;
			}
		}
		img.updatePixels();
	}
	
	/**
	 * Clear all objects.
	 */
	public void clearAllObjects() {
		objects.clear();
		collider.objectArray.clear();
		decals.clear();
		graphics.clear();
		for (levelSound sound : sounds){
			sound.release();
		}
		sounds.clear();
	}

	/**
	 * Get object reference.
	 * @param name
	 * @return
	 */
	public gameObject getObject(String name) {
		gameObject obj = null;
		for (gameObject object : objects) {
			if (object.type.equals(name)) {
				obj = object;
				break;
			}
		}
		return obj;
	}
	
	/**
	 * Create graphics object.
	 * @param PosX
	 * @param PosY
	 * @param objWidth
	 * @param objHeight
	 * @param Depth
	 * @return
	 */
	public graphics createGraphics(int PosX, int PosY, int objWidth, int objHeight, int Depth) {
		graphics.add(new graphics(PosX, PosY, objWidth, objHeight, Depth, this));
		graphics.get(graphics.size() - 1).index = graphics.size() - 1;
		return graphics.get(graphics.size() - 1);
	}

	/**
	 * Update all the graphics objects.
	 */
	public void updateGraphics() {
		for (graphics graphic : graphics) {
			graphic.draw();
		}
	}

	/**
	 * Draw graphics onto pixel matrix.
	 * @param x
	 * @param y
	 * @param testResult
	 * @param graphicIndex
	 */
	public void drawGraphicToMatrix (int x, int y, int testResult, int graphicIndex) {

		// Grab graphic from Arraylist.
		PGraphics graphic = graphics.get(graphicIndex).graphic;

		// Fail safe in case graphic is missing.
		if (graphic == null) {testResult = 0;}

		switch (testResult) {    
		// If partially on screen...
		case 1:    
			// Get margins.
			int startX = (x < displayX) ? (displayX - x) : 0;
			int endX = (x + graphic.width >= Math.round(app.width/pixelSize) + displayX) ? ((Math.round(app.width/pixelSize) + displayX) - x) : graphic.width;
			int startY = (y < displayY) ? (displayY - y) : 0;
			int endY = (y + graphic.height >= Math.round(app.height/pixelSize) + displayY) ? ((Math.round(app.height/pixelSize) + displayY) - y) : graphic.height;

			// Render partially to screen.
			for (int i = startX; i < endX; i++) {
				for (int k = startY; k < endY; k++) {
					drawPixel(i + x, k + y, graphic.pixels[i + (k * graphic.width)]);
				}
			}
			break;

			// If entirely on screen, render all.
		case 2:
			for (int i = 0; i < graphic.width; i++) {
				for (int k = 0; k < graphic.height; k++) {
					drawPixel(i + x, k + y, graphic.pixels[i + (k * graphic.width)]);
				}
			}
			break;
		}
	}

	/**
	 * Draw graphics.
	 * @param x
	 * @param y
	 * @param depth
	 * @param graphicIndex
	 */
	public void drawGraphic(int x, int y, int depth, int graphicIndex) {

		// Grab graphics from Arraylist.
		PGraphics graphic = graphics.get(graphicIndex).graphic;

		// Test if all edges are on screen.
		int testResult = toInt(testOnScreen(x, y))
				+ toInt(testOnScreen(x + graphic.width, y + graphic.height));

		// If partially or completely on-screen, render it.
		if (testResult > 0) {
			if (depth == 0) {
				drawGraphicToMatrix(x, y, testResult, graphicIndex);

				// Else, add to depthBuffer.
			} else {
				depthBuffer.append(PApplet.nf(depth, 4) + "2" + PApplet.str(signToInt(x)) + PApplet.nf(PApplet.abs(x), 4) + PApplet.str(signToInt(y))
						+ PApplet.nf(PApplet.abs(y), 4) + PApplet.str(testResult) + PApplet.nf(graphicIndex, 4));
			}
		}
	}
	
	/**
	 * Update level.
	 */
	public void updateLevel() {
		if (currentLevel != null) {
			int renderTileStartX, renderTileStartY, renderTileEndX, renderTileEndY;

			// Process each background layer.
			for (int h = 0; h < currentLevel.backgroundLayers; h++) {
				// Get scroll rate for this layer.
				float bgScrollRate = currentLevel.bgScroll[h];

				// Gather some offset'ed dimensions.
				int bgDisplayX = Math.round(displayX * bgScrollRate);
				int bgDisplayY = Math.round(displayY * bgScrollRate);

				// Determine how much of the background is seen.
				renderTileStartX = Math.round(bgDisplayX / currentLevel.tileWidth);
				renderTileStartY = Math.round(bgDisplayY / currentLevel.tileHeight);
				renderTileEndX = Math.round((bgDisplayX + (app.width / pixelSize)) / currentLevel.tileWidth);
				renderTileEndY = Math.round((bgDisplayY + (app.height / pixelSize)) / currentLevel.tileHeight);

				// Render visible background tiles.
				// Process each background tile.
				for (int i = renderTileStartX; i <= renderTileEndX; i++) {
					for (int k = renderTileStartY; k <= renderTileEndY; k++) {

						// Get GID of tile.
						int index = PApplet.constrain(i + (k * currentLevel.levelColumns), 0, currentLevel.background[h].length - 1);
						int gid = currentLevel.background[h][index];

						// Draw tile in game level.
						if (gid != 0) {
							drawTile(
									(i * currentLevel.tileWidth) - bgDisplayX + displayX,
									(k * currentLevel.tileHeight) - bgDisplayY + displayY,
									gid,
									tileSetList[toInt(tileSetRef.substring((gid - 1) * 2, gid * 2))]
									);
						}
					}
				}
			}

			// If lighting is enabled, draw from levelBuffer.
			if (lighting) {
				for (int i = displayX; i < displayX + (app.width/pixelSize); i++ ) {
					for (int k = displayY; k < displayY + (app.height/pixelSize); k++) {
						drawPixel(i, k, levelBuffer.pixels[PApplet.constrain(i + k * roomWidth, 0, levelBuffer.pixels.length - 1)]);
					}
				}

				// Else, draw normally from tiles.
			} else {

				// Determine how much of the level is seen.
				renderTileStartX = PApplet.max(0, Math.round(displayX / currentLevel.tileWidth));
				renderTileStartY = PApplet.max(0, Math.round(displayY / currentLevel.tileHeight));
				renderTileEndX = PApplet.min(currentLevel.levelColumns, PApplet.ceil((displayX + (app.width/pixelSize)) / currentLevel.tileWidth));
				renderTileEndY = PApplet.min(currentLevel.levelRows, PApplet.ceil((displayY + (app.height/pixelSize)) / currentLevel.tileHeight));

				// Render visible tiles.
				// Process each layer.
				for (int h = 0; h < currentLevel.levelLayers; h++) {

					// Process each tile.
					for (int i = renderTileStartX; i <= renderTileEndX; i++) {
						for (int k = renderTileStartY; k <= renderTileEndY; k++) {

							// Get GID of tile.
							int index = PApplet.constrain(i + (k * currentLevel.levelColumns), 0, currentLevel.levelMap[h].length - 1);
							int gid = currentLevel.levelMap[h][index];

							// Draw tile in game level.
							if (gid != 0) {
								drawTile(
										i * currentLevel.tileWidth,
										k * currentLevel.tileHeight,
										gid,
										tileSetList[toInt(tileSetRef.substring((gid - 1) * 2, gid * 2))]
										);
							}}
					}
				}
			}
		}
	}
	
	/**
	 * Generate Level Buffer.
	 */
	public void generateLevelBuffer() {

		// Process each layer.
		for (int h = 0; h < currentLevel.levelLayers; h++) {

			// Process each tile.
			for (int i = 0; i < currentLevel.levelColumns; i++) {
				for (int k = 0; k < currentLevel.levelRows; k++) {

					// Get GID of tile.
					int index = PApplet.constrain(i + (k * currentLevel.levelColumns), 0, currentLevel.levelMap[h].length - 1);
					int gid = currentLevel.levelMap[h][index];

					// Draw tile in game level.
					if (gid != 0) {
						drawTileLevelBuffer (
								i * currentLevel.tileWidth,
								k * currentLevel.tileHeight,
								gid,
								tileSetList[toInt(tileSetRef.substring((gid - 1) * 2, gid * 2))]
								);
					}
				}
			}
		}
	}

	/**
	 * Draw tile on the level buffer.
	 * @param x
	 * @param y
	 * @param gid
	 * @param tileSet
	 */
	public void drawTileLevelBuffer (int x, int y, int gid, tileSet tileSet) {

		// Get tile coordinate on tileSet image.
		gid -= tileSet.firstGID;
		int tileX = (gid % tileSet.tileColumns) * tileSet.tileWidth;
		int tileY = Math.round(gid / tileSet.tileColumns) * tileSet.tileHeight;

		for (int i = 0; i < tileSet.tileWidth; i++) {
			for (int k = 0; k < tileSet.tileHeight; k++) {
				drawPixelLevelBuffer(i + x, k + y, tileSet.tileSet.pixels[i + tileX + ((tileY + k) * tileSet.tileSet.width)]);
			}
		}
	}

	/**
	 * Draw pixel on the level buffer.
	 * @param x
	 * @param y
	 * @param rgb
	 */
	public void drawPixelLevelBuffer(int x, int y, int rgb) {

		// Test for alpha value of pixel.
		switch ((rgb >> 24) & 0xFF) {

		// If pixel is completely transparent, ignore.
		case 0:
			break;

			// If pixel is opaque, replace original pixel.
		case 255:
			if (lighting) {
				levelBuffer.pixels[x + (y * roomWidth)] = applyLight(x, y, rgb);
			} else {
				levelBuffer.pixels[x + (y * roomWidth)] = rgb;
			}
			break;

			// If pixel has alpha value, modify original pixel instead.
		default:
			int i = x + (y * roomWidth);
			if (lighting) {
				levelBuffer.pixels[i] = app.lerpColor(levelBuffer.pixels[i], applyLight(x, y, rgb), ((rgb >> 24) & 0xFF) / 255.0f);
			} else {
				levelBuffer.pixels[i] = app.lerpColor(levelBuffer.pixels[i], rgb, ((rgb >> 24) & 0xFF) / 255.0f);
			}
			break;
		}
	}

	/**
	 * Draw tile.
	 * @param x
	 * @param y
	 * @param gid
	 * @param tileSet
	 */
	void drawTile(int x, int y, int gid, tileSet tileSet) {
		drawTile(x, y, gid, 0, tileSet);
	}

	/**
	 * Draw tile.
	 * @param x
	 * @param y
	 * @param gid
	 * @param lighted
	 * @param tileSet
	 */
	void drawTile (int x, int y, int gid, int lighted, tileSet tileSet) {

		// Get tile coordinate on tileSet image.
		gid -= tileSet.firstGID;
		int tileX = (gid % tileSet.tileColumns) * tileSet.tileWidth;
		int tileY = Math.round(gid / tileSet.tileColumns) * tileSet.tileHeight;

		// Test if tile is on-screen.
		int testResult = 
				toInt(testOnScreen(x, y)) + 
				toInt(testOnScreen(x + tileSet.tileWidth, y)) +
				toInt(testOnScreen(x, y + tileSet.tileHeight)) +
				toInt(testOnScreen(x + tileSet.tileWidth, y + tileSet.tileHeight));

		// If entirely off-screen.
		if (testResult == 0) {}    

		// If partially on screen...
		else if (testResult < 4) {
			// Get margins.
			int startX = (x < displayX) ? (displayX - x) : 0;
			int endX = (x + tileSet.tileWidth >= Math.round(app.width/pixelSize) + displayX) ? ((Math.round(app.width/pixelSize) + displayX) - x) : tileSet.tileWidth;
			int startY = (y < displayY) ? (displayY - y) : 0;
			int endY = (y + tileSet.tileHeight >= Math.round(app.height/pixelSize) + displayY) ? ((Math.round(app.height/pixelSize) + displayY) - y) : tileSet.tileHeight;

			// Render partially to screen.
			for (int i = startX; i < endX; i++) {
				for (int k = startY; k < endY; k++) {
					if (lighted == 1) { drawPixel(i + x, k + y, true, tileSet.tileSet.pixels[i + tileX + ((tileY + k) * tileSet.tileSet.width)]); }
					else { drawPixel(i + x, k + y, tileSet.tileSet.pixels[i + tileX + ((tileY + k) * tileSet.tileSet.width)]); }
				}
			}

			// Else, render all.
		} else {
			for (int i = 0; i < tileSet.tileWidth; i++) {
				for (int k = 0; k < tileSet.tileHeight; k++) {
					if (lighted == 1) { drawPixel(i + x, k + y, true, tileSet.tileSet.pixels[i + tileX + ((tileY + k) * tileSet.tileSet.width)]); }
					else { drawPixel(i + x, k + y, tileSet.tileSet.pixels[i + tileX + ((tileY + k) * tileSet.tileSet.width)]); }
				}
			}
		}
	}

	/**
	 * Update all objects.
	 */
	void updateObjects() {
		for (int i = 0; i < objects.size(); i++) {

			// Get object.
			gameObject obj = objects.get(i);

			// If destroyed, remove from list.
			if (obj.destroyed) {
				objects.remove(i);
				i--;

				// Else, update and render.
			} else {

				// If there's a velocity, update object position.
				if ((obj.ySpeed != 0) || (obj.xSpeed != 0)) {
					if (!isPaused) {
						obj.move();
					}
				}
				
				// Update object.
				if (!isPaused) {
					obj.update();
				}

				// Test if object has a sprite.
				if (obj.sprite != null) {

					// If more than 1 frame, animate.
					if ((obj.objFrames > 0) && !obj.noAnimate && !isPaused) {
						obj.animate();
					}

					// Render sprite if not invisible and on-screen.
					if (obj.visible) {
						obj.draw();
					}
				}
			}
		}
	}
	
	/**
	 * Convert string to int.
	 * @param str
	 * @return
	 */
	static int toInt(String str) {
		return Integer.parseInt(str);
	}
	
	/**
	 * Update all sounds.
	 */
	public void updateSounds() {
		for (levelSound sound : sounds) {
			sound.update();
		}
	}

	/**
	 * Create global sound.
	 * @param filename
	 * @return
	 */
	public levelSound createGlobalSound(String filename) {
		return createGlobalSound(filename, false);
	}

	public levelSound createGlobalSound(String filename, boolean loop) {
		sounds.add(new globalSound(filename, loop, this));
		return sounds.get(sounds.size() - 1);
	}

	/**
	 * Create environmental sound.
	 * @param X
	 * @param Y
	 * @param filename
	 * @return
	 */
	public levelSound createEnvSound(int X, int Y, String filename) {
		return createEnvSound(X, Y, filename, (app.width / pixelSize) / 2, false);
	}

	/**
	 * Create environmental sound.
	 * @param X
	 * @param Y
	 * @param filename
	 * @param Loop
	 * @return
	 */
	public levelSound createEnvSound(int X, int Y, String filename, boolean Loop) {
		return createEnvSound(X, Y, filename, (app.width / pixelSize) / 2, Loop);
	}

	/**
	 * Create environmental sound.
	 * @param X
	 * @param Y
	 * @param filename
	 * @param Range
	 * @return
	 */
	public levelSound createEnvSound(int X, int Y, String filename, int Range) {
		return createEnvSound(X, Y, filename, Range, false);
	}

	/**
	 * Create environmental sound.
	 * @param X
	 * @param Y
	 * @param filename
	 * @param Range
	 * @param Loop
	 * @return
	 */
	public levelSound createEnvSound(int X, int Y, String filename, int Range, boolean Loop) {
		sounds.add(new envSound(X, Y, filename, Range, Loop, this));
		return sounds.get(sounds.size() - 1);
	}
}