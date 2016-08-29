package black.alias.pixelpie;

import java.util.ArrayList;
import java.util.HashMap;

import processing.core.*;
import processing.data.StringList;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PJOGL;
import black.alias.pixelpie.level.*;
import black.alias.pixelpie.loader.*;
import black.alias.pixelpie.sprite.*;
import black.alias.pixelpie.audio.*;
import black.alias.pixelpie.audio.levelaudio.*;
import black.alias.pixelpie.controls.Controls;
import black.alias.pixelpie.effect.Effect;
import black.alias.pixelpie.file.FileManager;
import black.alias.pixelpie.graphics.*;

/**
 * The main PixelPie class.
 * @author Xuanming Zhou
 *
 */
public class PixelPie {
	public ArrayList<GameObject> objectArray = new ArrayList<GameObject>();
	public final PApplet app;
	public final AudioDevice SoundDevice;
	public final Logger log;
	public final FileManager FileSystem;
	public int displayX, displayY, roomWidth, roomHeight, matrixWidth, matrixHeight,
		pixelSize, index;
	public float frameRate, frameProgress;
	public volatile boolean loaded;
	public boolean displayFPS, waitThread, lighting, levelLoading, isPaused;
	public int background, black, white;
	public int[] pixelMatrix;
	public StringList depthBuffer;
	public Script currentScript;
	
	// Containers for object instances.
	// These are temporary objects that are cleared and reloaded every level.
	public final ArrayList<GameObject> objects;
	public final ArrayList<Decal> decals;
	public final ArrayList<Graphics> graphics;
	public final ArrayList<LevelAudio> sounds;
	public final ArrayList<Effect> effects;
	
	// Containers for permanent assets.
	// These are all loaded into memory at the start of the game, and never released.
	public final HashMap<String, Sprite> spr;
	public final HashMap<String, Level> lvl;

	// Stuff for level.
	public String loadingText, tileSetRef;
	public String loadLevelTarget = "";
	public int levelZoom, levelBrightness;
	public Level currentLevel;
	public TileSet[] tileSetList;

	// Stuff needed to make lighting work.
	public float gamma;
	public PImage levelBuffer;
	public PImage[] backgroundBuffer;
	public PGraphics lightMap;
	public String currentLevelName;
	
	/**
	 * Initialize PixelPie.
	 * @param app
	 * @param minim
	 */
	public PixelPie(PApplet app, PixelOven oven) {
		this(app, oven, 2, 60.0f);
	}
	
	/**
	 * Initialize PixelPie.
	 * @param app
	 * @param device
	 * @param fps
	 */
	public PixelPie(PApplet app, PixelOven oven, float fps) {
		this(app, oven, 2, fps);
	}
	
	/**
	 * Initialize PixelPie.
	 * @param app
	 * @param minim
	 * @param PixelSize
	 */
	public PixelPie(PApplet app, PixelOven oven, int PixelSize) {
		this(app, oven, PixelSize, 60.0f);
	}
	
	/**
	 * Initialize PixelPie.
	 * @param app
	 * @param device
	 * @param PixelSize
	 * @param fps
	 */
	public PixelPie(PApplet app, PixelOven oven, int PixelSize, float fps) {
		
		// Keep reference to PApplet.
		this.app = app;
		
		// Set desired frameRate, and keep record of it in PixelPie for animation purposes.
		this.frameRate = fps;
		app.frameRate(fps);
		
		// Set OpenGL specific settings to retain pixel art instead of smoothing it.
		if (app.g instanceof PGraphicsOpenGL) {
			if (oven.getPlatform().equals("Java")) {
				((PGraphicsOpenGL)app.g).textureSampling(3);			
				((PJOGL)((PGraphicsOpenGL)app.g).pgl).gl.setSwapInterval(1);
			}
		}
		
		// Essential Parameters.
		app.noStroke();
		app.background(0);
		app.textSize(32);

		// Set default values of variables.
		pixelSize = PixelSize;
		black = app.color(0, 0, 0);
		white = app.color(255, 255, 255);
		background = black;                 // Background color.
		roomWidth = 1000;                   // Default width of game level.
		roomHeight = 1000;                  // Default height of game level.
		
		// Initiate controls.
		new Controls(this);

		// Initiate pixelMatrix.
		matrixWidth = app.width / pixelSize;
		matrixHeight = app.height / pixelSize;

		// Initiate depthBuffers.
		depthBuffer = new StringList();

		// Grab reference to AudioDevice.
		this.SoundDevice = oven.getAudio();
		
		// Grab reference to FileManager.
		this.FileSystem = oven.getManager();
		
		// Initiate logger.
		this.log = new Logger(app, this);
		
		// Initiate containers.
		objects = new ArrayList<GameObject>();
		decals = new ArrayList<Decal>();
		graphics = new ArrayList<Graphics>();
		sounds = new ArrayList<LevelAudio>();
		effects = new ArrayList<Effect>();
		
		// Register methods with Processing.
		app.registerMethod("pre", this);
		
		// Create asset holders.
		spr = new HashMap<String, Sprite>();
		lvl = new HashMap<String, Level>();
		
		// Start loading assets.
		loaded = false;
		new dataLoader(this).start();
	}
	
	/**
	 * Method called by processing at the start of draw method.
	 */
	public void pre() {

		// Erase previous draw.
		app.background(0);

		// If assets are loaded, run loop functions.
		if (loaded && !levelLoading) {
			collisionDetect();
			updateScript();
			updateObjects();
			updateLevel();	
			updateDecals();
			updateEffects();
			updateGraphics();
			updateSounds();
		}

		// Else, show loading screen.
		else {}

		// Draw depthBuffer to screen.
		depthBuffer.sort();
		for (String str : depthBuffer) {
			switch(toInt(str.substring(4,5))) {

			// If entry is a game object.
			case 0:
				objects.get(toInt(str.substring(5))).render();
				break;

				// If entry is a decal.				
			case 1:
				decals.get(toInt(str.substring(5))).render();
				break;

				// If entry is a graphic.
			case 2:
				graphics.get(toInt(str.substring(5))).render();
				break;

				// If entry is an effect.
			case 3:
				effects.get(toInt(str.substring(5))).render();
				break;
			}
		}
		depthBuffer.clear();
		
		// Show FPS
		if (displayFPS) {
			app.textAlign(PConstants.LEFT, PConstants.TOP); 
			app.fill(white);
			app.text(app.frameRate, 20, 20);
		}
	}
	
	/**
	 * Update all scripted sequences.
	 */
	public void updateScript() {
		if (scriptRunning()) {
			currentScript.update();
		}
	}
	
	/**
	 * Update all decals.
	 */
	private void updateDecals() {
		for (int i = 0; i < decals.size(); i++) {
			Decal decal = decals.get(i);
			if (!isPaused) {
				decal.animate();
			}
			decal.update(i);
		}
	}
	
	/**
	 * Update all sprite effects.
	 */
	public void updateEffects() {
		for (int i = 0; i < effects.size(); i++) { 

			// Get object.
			Effect obj = effects.get(i);

			// If destroyed, remove from list.
			if (obj.destroyed){
				effects.remove(i);
				i--;

			// Else, animate and render.
			} else {
				obj.animate();
				obj.draw(i);
			}
		}
	}

	/**
	 * Update all the graphics objects.
	 */
	public void updateGraphics() {
		for (int i = 0; i < graphics.size(); i++) {
			graphics.get(i).draw(i);
		}
	}
	
	/**
	 * Update level.
	 */
	public void updateLevel() {
		if (currentLevel != null) {
			// Process background layers.
			for (int h = 0; h < currentLevel.backgroundLayers; h++) {
				app.copy(
						backgroundBuffer[h],
						Math.round(displayX * currentLevel.bgScroll[h]),
						Math.round(displayY * currentLevel.bgScroll[h]),
						matrixWidth,
						matrixHeight,
						0,
						0,
						app.width,
						app.height
						);
			}
			
			// Process foreground layers.
			app.copy(
					levelBuffer,
					displayX,
					displayY,
					matrixWidth,
					matrixHeight,
					0,
					0,
					app.width,
					app.height
					);
		}
	}

	/**
	 * Update all objects.
	 */
	void updateObjects() {
		for (int i = 0; i < objects.size(); i++) {

			// Get object.
			GameObject obj = objects.get(i);

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
						obj.draw(i);
					}
				}
			}
		}
	}
	
	/**
	 * Update all sounds.
	 */
	private void updateSounds() {
		for (LevelAudio sound : sounds) {
			sound.update();
		}
	}
	
	/**
	 * Collision Detection.
	 */
	private void collisionDetect() {
		
		// Go through the list and detect collisions.
		if (objectArray.size() != 0) {
			for (int i = 0; i < objectArray.size(); i++) {

				// Select object 1.
				GameObject obj1 = objectArray.get(i);

				// Test if object 1 is flagged as destroyed. Remove it from list.
				if (obj1.destroyed) {
					objectArray.remove(i);
					i--;

				// Else, proceed with collision testing if noCollide is false.
				} else if (!obj1.noCollide) {
					for (int k = i + 1; k < objectArray.size(); k++) {

						// Get object 2.
						GameObject obj2 = objectArray.get(k);

						// If object 2's noCollide is also false...
						if (!obj2.noCollide) {

							// See if they collide.
							if (PixelPie.objCollision(obj1, obj2)) {

								// Run the collision event in each object in
								// event of collision.
								obj1.other = obj2;
								obj1.collide();

								obj2.other = obj1;
								obj2.collide();
							}

							// If there's a velocity vector, see if it will
							// collide with anything next frame.
							if ((obj1.xSpeed != 0) || (obj1.ySpeed != 0) || (obj2.xSpeed != 0)
									|| (obj2.ySpeed != 0)) {
								if (PixelPie.objColPredictive(obj1, obj2)) {

									// Run the collision event in each
									// object in event of collision.
									obj1.otherPredict = obj2;
									obj1.colPredict();

									obj2.otherPredict = obj1;
									obj2.colPredict();
								}
							}
						}
					}
				}
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
	public GameObject ptCollision(int x, int y) {
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
	public GameObject ptCollision(int x, int y, String[] ignore) {
		for (GameObject obj : objects) {
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
	public static boolean objHorzCollision(GameObject obj1, GameObject obj2) {
		return horzCollision(obj1.x - obj1.bBoxXOffset, obj1.x + obj1.bBoxWidth - obj1.bBoxXOffset,
				obj2.x - obj2.bBoxXOffset, obj2.x + obj2.bBoxWidth - obj2.bBoxXOffset);
	}

	/**
	 *  Vertical Collision (by Object)
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public static boolean objVertCollision(GameObject obj1, GameObject obj2) {
		return vertCollision(obj1.y - obj1.bBoxYOffset, obj1.y + obj1.bBoxHeight - obj1.bBoxYOffset,
				obj2.y - obj2.bBoxYOffset, obj2.y + obj2.bBoxHeight - obj2.bBoxYOffset);
	}

	/**
	 *  Current-frame object collision.
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public static boolean objCollision(GameObject obj1, GameObject obj2) {
		return (toInt(objHorzCollision(obj1, obj2)) + toInt(objVertCollision(obj1, obj2)) == 2) ? true : false;
	}

	/**
	 *  Predictive object collision.
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public static boolean objColPredictive (GameObject obj1, GameObject obj2) {
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
	public static int btm(GameObject obj) {
		return obj.y - obj.bBoxYOffset + obj.bBoxHeight;
	}
	
	/**
	 * Calculate top bBox face positions of object.
	 * @param obj
	 * @return
	 */
	public static int top(GameObject obj) {
		return obj.y - obj.bBoxYOffset;
	}

	/**
	 * Calculate left bBox face positions of object.
	 * @param obj
	 * @return
	 */
	public static int left(GameObject obj) {
		return obj.x - obj.bBoxXOffset;
	}

	/**
	 * Calculate right bBox face positions of object.
	 * @param obj
	 * @return
	 */
	public static int right(GameObject obj) {
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
	public static String getParam(String str, GameObject obj) {
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
	public static int vertDist(GameObject obj1, GameObject obj2) {
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
	public static int horzDist(GameObject obj1, GameObject obj2) {
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
	public static float dist(GameObject obj1, GameObject obj2) {
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
	public static int coordToArray(int x, int y, Level level) {
		return x + y * level.levelWidth;
	}

	/**
	 * Get X coordinate value from array value.
	 * @param value
	 * @param level
	 * @return
	 */
	public static int getCoordX(int value, Level level) {
		return value % level.levelWidth;
	}

	/**
	 * Get Y coordinate value from array value.
	 * @param value
	 * @param level
	 * @return
	 */
	public static int getCoordY(int value, Level level) {
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
	 * Get object reference.
	 * @param name
	 * @return
	 */
	public GameObject getObject(String name) {
		GameObject obj = null;
		for (GameObject object : objects) {
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
	public Graphics createGraphics(int PosX, int PosY, int objWidth, int objHeight, int Depth) {
		graphics.add(new Graphics(PosX, PosY, objWidth, objHeight, Depth, this));
		graphics.get(graphics.size() - 1).index = graphics.size() - 1;
		return graphics.get(graphics.size() - 1);
	}
	
	/**
	 * Load a new game level.
	 * @param lvl
	 */
	public void loadLevel(String lvl) {
		loadLevelTarget = lvl;
		new levelLoader(this).start();
	}
	
	/**
	 * Convert string to int.
	 * @param str
	 * @return
	 */
	public static int toInt(String str) {
		return Integer.parseInt(str);
	}

	/**
	 * Create global sound.
	 * @param filename
	 * @return
	 */
	public LevelAudio createGlobalSound(String filename) {
		return createGlobalSound(filename, false);
	}

	public LevelAudio createGlobalSound(String filename, boolean loop) {
		sounds.add(new GlobalAudio(filename, loop, this));
		return sounds.get(sounds.size() - 1);
	}

	/**
	 * Create environmental sound.
	 * @param X
	 * @param Y
	 * @param filename
	 * @return
	 */
	public LevelAudio createEnvSound(int X, int Y, String filename) {
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
	public LevelAudio createEnvSound(int X, int Y, String filename, boolean Loop) {
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
	public LevelAudio createEnvSound(int X, int Y, String filename, int Range) {
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
	public LevelAudio createEnvSound(int X, int Y, String filename, int Range, boolean Loop) {
		sounds.add(new EnvAudio(X, Y, filename, Range, Loop, this));
		return sounds.get(sounds.size() - 1);
	}
}