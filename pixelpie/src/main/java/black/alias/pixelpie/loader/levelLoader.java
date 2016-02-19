package black.alias.pixelpie.loader;

import java.util.HashMap;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.data.StringDict;
import processing.data.XML;
import black.alias.pixelpie.PixelPie;
import black.alias.pixelpie.GameObject;
import black.alias.pixelpie.audio.levelaudio.LevelAudio;
import black.alias.pixelpie.level.Level;
import black.alias.pixelpie.level.TileSet;
import black.alias.pixelpie.sprite.Decal;

/**
 * Level loader, runs on a separate thread.
 * Sets "levelLoading" boolean to true in main PixelPie class when active.
 * @author Xuanming
 *
 */
public class levelLoader extends Thread {
	final PixelPie pie;
	String currentLevelName;
	
	public levelLoader(PixelPie pie) {
		this.pie = pie;
	}
	
	public void start() {
		super.start();
	}
	
	public void run() {
		
		// Wait for assets to finish loading if called too early.
		while (!pie.loaded){}

		// Reset index.
		pie.index = 0;

		// Set loading flag to true (shows loading screen).
		pie.levelLoading = true;

		// Clear the current level.
		clearAllObjects();

		// Get the level file.
		String levelName = pie.loadLevelTarget;
		Level level = pie.lvl.get(levelName);

		// Set the room dimensions.
		pie.roomWidth = level.levelWidth;
		pie.roomHeight = level.levelHeight;

		// Set loading text to "Loading".
		pie.loadingText = "Loading Audio";

		// Grab level properties.
		if (level.properties != null) {

			// Grab Brightness.
			if (level.properties.hasKey("brightness") == true) {
				pie.levelBrightness = Integer.parseInt(level.properties.get("brightness"));
				pie.lighting = true;
			} else {
				pie.lighting = false;
			}
		}

		// Set the lightMap to this map's size.
		pie.loadingText = "Generating Lightmap";
		if ((pie.lighting) && !levelName.equals(currentLevelName)) {
			pie.lightMap = pie.app.createGraphics(pie.roomWidth, pie.roomHeight);
			pie.lightMap.beginDraw();
			pie.lightMap.background(pie.levelBrightness);
			pie.lightMap.endDraw();
		}

		// Load tileSets.
		if (!levelName.equals(currentLevelName)) {
			pie.loadingText = "Retrieving Tilesets";

			// Resize tileSetList according to tileSet count.
			pie.tileSetList = new TileSet[ level.tileSets.length ];

			// Empty tileSetRef.
			pie.tileSetRef = "";

			for (int i = 0; i < level.tileSets.length; i++) {

				// Load each tileSet.
				XML data = level.tileSets[i];

				// Load tileSet image info.
				XML image = data.getChild("image");

				// Process tileSet filename.
				String[] nameParts = PApplet.split(image.getString("source"), "/");

				// Get tile count.
				int tileColumns = image.getInt("width") / level.tileWidth;
				int tileRows = image.getInt("height") / level.tileHeight;

				// Create new tileSet.
				pie.tileSetList[i] = new TileSet(
						tileRows,										// Tile Rows.
						tileColumns,									// Tile Columns.
						level.tileWidth,								// Tile Width.
						level.tileHeight,								// Tile Height.
						data.getInt("firstgid"),						// First GID of tile set.
						"Tilesets/" + nameParts[nameParts.length - 1],	// Extract filename of tile set.
						pie												// Reference to PixelPie.
						);

				// Append to tileRef.
				for (int k = 0; k < tileRows * tileColumns; k++){
					pie.tileSetRef += PApplet.nf(i,2);
				}
			}
		}

		// Load room objects.
		pie.loadingText = "Populating Level";
		for (XML objectType : level.objects) {
			String objectName = objectType.getString("name");

			// Check type of object.
			if (objectName.substring(0,1).equals("(")) {

				// Break up name into parts.
				String[] nameParts = PApplet.split(objectName, " ");

				// If light.
				if ((nameParts[0].equals("(light)")) && pie.lighting && !levelName.equals(currentLevelName)) {

					// Process each object in light layer.
					for (XML object : objectType.getChildren("object")) {

						// If there's a setting for brightness, use it...
						try {
							if (object.getChild("properties").getChild("property").getString("name").equals("brightness")) {
								drawLight (
										Math.round(object.getFloat("x") + object.getFloat("width")/2),
										Math.round(object.getFloat("y") + object.getFloat("height")/2),
										Math.round(object.getFloat("width")),
										Math.round(object.getFloat("height")),
										object.getChild("properties").getChild("property").getInt("value"),
										pie.lightMap
										);
							}

							// ...else, default to 255.
						} catch (Exception e) {
							drawLight (
									Math.round(object.getFloat("x") + object.getFloat("width")/2),
									Math.round(object.getFloat("y") + object.getFloat("height")/2),
									Math.round(object.getFloat("width")),
									Math.round(object.getFloat("height")),
									255,
									pie.lightMap
									);
						}
					}
				}

				// If Sound.
				else if (nameParts[0].equals("(sound)")) {

					// Set default layer properties.
					int layerRange = 0;
					String layerFile = "";

					// Grab layer properties, if any.
					if (objectType.getChild("properties") != null) {
						HashMap<String, String> prop = new HashMap<String, String>();
						for (XML property : objectType.getChild("properties").getChildren("property")) {
							prop.put(property.getString("name"), property.getString("value"));
						}

						layerRange = Integer.parseInt(PixelPie.getProp(prop, "range"));
						layerFile = PixelPie.getProp(prop, "file");
					}

					// Process each object in sound layer.
					for (XML object : objectType.getChildren("object")) {

						// Grab local properties if any.
						HashMap<String, String> localProp = new HashMap<String, String>();
						if (object.getChild("properties") != null) {
							for (XML property : object.getChild("properties").getChildren("property")) {
								localProp.put(property.getString("name"), property.getString("value"));
							}
						}

						// Overwrite layer properties with local one if any.
						int localRange = PApplet.max(layerRange, Integer.parseInt(PixelPie.getProp(localProp, "range")));
						String localFile = "";
						if (PixelPie.getProp(localProp, "file").equals("")) {
							localFile = layerFile;
						} else {
							localFile = PixelPie.getProp(localProp, "file");
						}

						// If range is NOT specified.
						if (localRange == 0) {
							pie.createEnvSound(
									Math.round(object.getFloat("x")),
									Math.round(object.getFloat("y")),
									localFile,
									true
									);

							// If range is specified.
						} else {
							pie.createEnvSound(
									Math.round(object.getFloat("x")),
									Math.round(object.getFloat("y")),
									localFile,
									localRange,
									true
									);
						}
					}
				}

				// If Decal.
				else if (nameParts[0].equals("(decal)")) {

					// Set default layer properties.
					int layerDepth = 0;
					int layerOrigin = 0;
					String layerSprite = "";

					// Grab layer properties, if any.
					if (objectType.getChild("properties") != null) {
						HashMap<String, String> prop = new HashMap<String, String>();
						for (XML property : objectType.getChild("properties").getChildren("property")) {
							prop.put(property.getString("name"), property.getString("value"));
						}

						// Set layer properties.
						layerDepth = Integer.parseInt(PixelPie.getProp(prop, "depth"));
						layerOrigin = Integer.parseInt(PixelPie.getProp(prop, "origin"));
						layerSprite = PixelPie.getProp(prop, "sprite");
					}

					// Process each object in decal layer.
					for (XML object : objectType.getChildren("object")) {

						// Grab local properties if any.
						HashMap<String, String> localProp = new HashMap<String, String>();
						if (object.getChild("properties") != null) {
							for (XML property : object.getChild("properties").getChildren("property")) {
								localProp.put(property.getString("name"), property.getString("value"));
							}
						}

						int myDepth = PApplet.max(layerDepth, Integer.parseInt(PixelPie.getProp(localProp, "depth")));
						int myOrigin = PApplet.max(layerOrigin, Integer.parseInt(PixelPie.getProp(localProp, "origin")));
						String mySprite = "";
						if (PixelPie.getProp(localProp, "sprite").equals("")) {
							mySprite = layerSprite;
						} else {
							mySprite = PixelPie.getProp(localProp, "sprite");
						}

						// If not sprite is specified.
						if (mySprite.equals("")){

							// If decal has a GID, create a tile decal.
							if (object.getInt("gid") != 0) {
								pie.decals.add(
										new Decal(
												Math.round(object.getFloat("x")),
												Math.round(object.getFloat("y")),
												myDepth,
												Math.round(object.getFloat("gid")), 
												pie
												)
										);

								// Else, throw error for this decal.
							} else {pie.log.printlg("Decal has no tile or sprite value attached. Ignoring.");}

							// If decal is a sprite.
						} else if (pie.spr.get(mySprite) != null){
							pie.decals.add(
									new Decal(
											Math.round(object.getFloat("x")),
											Math.round(object.getFloat("y")),
											myDepth,
											myOrigin,
											mySprite,
											pie
											)
									);
						} else {pie.log.printlg("Decal " + mySprite + " cannot be found.");}
					}
				}                

				// Else, create contained objects as gameObjects.
			} else {    
				XML[] objects = objectType.getChildren("object");
				for (XML object : objects) {

					// Create temp object using Java reflection.
					Object obj = null;
					String ClassName = pie.app.getClass().getName() + "$" + objectName;
					try {obj = Class.forName(ClassName).getDeclaredConstructors()[0].newInstance(pie.app);}
					catch (Exception e) {pie.log.printlg(e);}

					if (obj != null) {

						// Create temporary object for modification.
						GameObject gameObject = (GameObject)obj;

						// Check if object has any properties.
						if (object.hasChildren()) {
							StringDict localProp = new StringDict();
							for (XML property : object.getChild("properties").getChildren("property")) {
								localProp.set(property.getString("name"), property.getString("value"));
							}
							gameObject.setParameters(localProp);
						}

						// Set parameters and create object as gameObject.
						if (pie.lighting) {gameObject.setLighted(true);}
						gameObject.setX ( Math.round(object.getFloat("x")) );
						gameObject.setY ( Math.round(object.getFloat("y")) );
						gameObject.setWidth ( Math.round(object.getFloat("width")) );
						gameObject.setHeight ( Math.round(object.getFloat("height")) );
						gameObject.setType ( objectName.intern() );
						gameObject.init();

						// Add object to objects array.
						pie.objects.add(gameObject);
					}
				}
			}
		}

		// Set current level.
		pie.currentLevel = level;

		// If lighting is turned on and this is a new level.
		if ((pie.lighting) && !levelName.equals(currentLevelName)) {

			// Finalize the light map.
			pie.loadingText = "Finalizing Lightmap";

			// Blur the light map.
			fastblur(pie.lightMap, 5);

			// Burn lighting into all decals.
			for (Decal decal : pie.decals) {
				decal.light();
			}
		}

		// Reset levelBuffer for current level.
		generateLevelBuffer();

		// Set current level name.
		currentLevelName = levelName;

		// Force garbage collection.
		System.gc();

		// Set loading flag to false (remove loading screen).
		pie.levelLoading = false;
		pie.loadLevelTarget = "";
	}
	
	/**
	 * Generate Level Buffers.
	 */
	private void generateLevelBuffer() {
		
		// Process each background layer.
		pie.backgroundBuffer = new PImage[pie.currentLevel.backgroundLayers];
		for (int h = 0; h < pie.currentLevel.backgroundLayers; h++) {
			
			// Generate new buffer PImage for current background layer.
			int bgTileColumns = (int)Math.ceil(pie.currentLevel.levelColumns * pie.currentLevel.bgScroll[h]);
			int bgTileRows = (int)Math.ceil(pie.currentLevel.levelRows * pie.currentLevel.bgScroll[h]);
			pie.backgroundBuffer[h] = pie.app.createImage(
					bgTileColumns * pie.currentLevel.tileWidth,
					bgTileRows * pie.currentLevel.tileHeight,
					PConstants.RGB
					);
			
			// Process each tile.
			for (int i = 0; i < bgTileColumns; i++) {
				pie.loadingText = "Loading Backgrounds (" + (h + 1) + "/" + pie.currentLevel.backgroundLayers + ") " + ((i * 100)/bgTileColumns) + "%";
				for (int k = 0; k < bgTileRows; k++) {
					
					// Get GID of tile.
					int index = PApplet.constrain(i + (k * pie.currentLevel.levelColumns), 0, pie.currentLevel.background[h].length - 1);
					int gid = pie.currentLevel.background[h][index];
					
					if (gid != 0) {
						drawTileBGBuffer (
								h,
								i * pie.currentLevel.tileWidth,
								k * pie.currentLevel.tileHeight,
								gid,
								pie.tileSetList[PixelPie.toInt(pie.tileSetRef.substring((gid - 1) * 2, gid * 2))]
								);
					}
				}
			}
		}

		// Process each level layer.
		pie.levelBuffer = pie.app.createImage(pie.roomWidth, pie.roomHeight, PConstants.ARGB);
		for (int h = 0; h < pie.currentLevel.levelLayers; h++) {

			// Process each tile.
			for (int i = 0; i < pie.currentLevel.levelColumns; i++) {
				pie.loadingText = "Loading Foregrounds (" + (h + 1) + "/" + pie.currentLevel.levelLayers + ") " + ((i * 100)/pie.currentLevel.levelColumns) + "%";
				for (int k = 0; k < pie.currentLevel.levelRows; k++) {

					// Get GID of tile.
					int index = PApplet.constrain(i + (k * pie.currentLevel.levelColumns), 0, pie.currentLevel.levelMap[h].length - 1);
					int gid = pie.currentLevel.levelMap[h][index];

					// Draw tile in game level.
					if (gid != 0) {
						drawTileLevelBuffer (
								i * pie.currentLevel.tileWidth,
								k * pie.currentLevel.tileHeight,
								gid,
								pie.tileSetList[PixelPie.toInt(pie.tileSetRef.substring((gid - 1) * 2, gid * 2))]
								);
					}
				}
			}
		}
		
		// Draw lighting if lighting is turned on.
		pie.loadingText = "Burning Lights Onto Level";
		if (pie.lighting) {
			
			// Grab alpha mask.
			PImage alpha = pie.levelBuffer.get();
			alpha.loadPixels();
			for (int i = 0; i < alpha.pixels.length; i ++) {
				alpha.pixels[i] = alpha.pixels[i] & 0xFFFFFF | (alpha.pixels[i] >> 24) & 0xFF;
			}
			alpha.updatePixels();
			
			// Prepare the light map.
			PImage lightMapCopy = pie.app.createImage(pie.roomWidth, pie.roomHeight, PConstants.ARGB);
			lightMapCopy.loadPixels();
			lightMapCopy.pixels = pie.lightMap.pixels;
			lightMapCopy.updatePixels();
			lightMapCopy.mask(alpha);
			
			// Burn light map onto level buffer.
			pie.levelBuffer.blend(lightMapCopy, 0, 0, pie.roomWidth, pie.roomHeight, 0, 0, pie.roomWidth, pie.roomHeight, PConstants.MULTIPLY);
		}
	}
	
	/**
	 * Clear all objects.
	 */
	private void clearAllObjects() {
		pie.objects.clear();
		pie.objectArray.clear();
		pie.decals.clear();
		pie.graphics.clear();
		for (LevelAudio sound : pie.sounds){
			sound.release();
		}
		pie.sounds.clear();
	}
	
	/**
	 * Draw tile on the foreground buffer.
	 * @param x
	 * @param y
	 * @param gid
	 * @param tileSet
	 */
	private void drawTileLevelBuffer (int x, int y, int gid, TileSet tileSet) {
		gid -= tileSet.firstGID;		
		pie.levelBuffer.blend(
				tileSet.tileSet,
				(gid % tileSet.tileColumns) * tileSet.tileWidth,
				(gid / tileSet.tileColumns) * tileSet.tileHeight,
				tileSet.tileWidth,
				tileSet.tileHeight,
				x,
				y,
				tileSet.tileWidth,
				tileSet.tileHeight,
				PConstants.BLEND
				);
	}
	
	/**
	 * Draw tile on the background buffer.
	 * @param layer
	 * @param x
	 * @param y
	 * @param gid
	 * @param tileSet
	 */
	private void drawTileBGBuffer (int layer, int x, int y, int gid, TileSet tileSet) {
		gid -= tileSet.firstGID;		
		pie.backgroundBuffer[layer].blend(
				tileSet.tileSet,
				(gid % tileSet.tileColumns) * tileSet.tileWidth,
				(gid / tileSet.tileColumns) * tileSet.tileHeight,
				tileSet.tileWidth,
				tileSet.tileHeight,
				x,
				y,
				tileSet.tileWidth,
				tileSet.tileHeight,
				PConstants.BLEND
				);
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
	private static void drawLight(int x, int y, int Width, int Height, int brightness, PGraphics pg) {
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
	private static void fastblur(PImage img, int radius) {
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
}
