package black.alias.pixelpie.loader;

import java.util.HashMap;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.data.StringDict;
import processing.data.XML;
import black.alias.pixelpie.PixelPie;
import black.alias.pixelpie.GameObject;
import black.alias.pixelpie.level.Level;
import black.alias.pixelpie.level.TileSet;
import black.alias.pixelpie.sprite.Decal;

/**
 * Level loading.
 * @author Xuanming
 *
 */
public class levelLoader extends Thread{
	final PixelPie pie;
	String loadLevelTarget;
	
	public levelLoader(PixelPie pie) {
		this.pie = pie;
	}
	
	public void start() {
		super.start();
	}
	
	public void run() {
		
		// Check if level is specified.
		if (!pie.loadLevelTarget.isEmpty()){

			// Reset index.
			pie.index = 0;

			// Set loading flag to true (shows loading screen).
			pie.levelLoading = true;

			// Clear the current level.
			pie.clearAllObjects();

			// Get the level file.
			String levelName = pie.loadLevelTarget;
			Level level = pie.lvl.get(levelName);

			// Set the room dimensions.
			pie.roomWidth = level.levelWidth;
			pie.roomHeight = level.levelHeight;

			// Set default property values if not specified.
			pie.levelZoom = pie.pixelSize;
			//pie.levelBrightness = 50;

			// Set loading text to "Loading".
			pie.loadingText = "Loading Audio";

			// Grab level properties.
			if (level.properties != null) {

				// Grab zoom.
				if (level.properties.hasKey("zoom") == true) {
					pie.levelZoom = Integer.parseInt(level.properties.get("zoom"));
				}

				// Grab Brightness.
				if (level.properties.hasKey("brightness") == true) {
					pie.levelBrightness = Integer.parseInt(level.properties.get("brightness"));
					pie.lighting = true;
				} else {
					pie.lighting = false;
				}
			}

			// Set zoom level.
			pie.pixelSize = pie.levelZoom;

			// Set the lightMap to this map's size.
			pie.loadingText = "Generating Lightmap";
			if ((pie.lighting) && !levelName.equals(pie.currentLevelName)) {
				pie.lightMap = pie.app.createGraphics(pie.roomWidth, pie.roomHeight);
				pie.lightMap.beginDraw();
				pie.lightMap.background(pie.levelBrightness);
				pie.lightMap.endDraw();
			}

			// Load tileSets.
			if (!levelName.equals(pie.currentLevelName)) {
				pie.loadingText = "Retrieving Tilesets";

				// Resize tileSetList according to tileSet count.
				pie.tileSetList = new TileSet[ level.tileSets.length ];

				// Empty tileSetRef.
				pie.tileSetRef = "";

				for (int i = 0; i < level.tileSets.length; i++) {

					// Load each tileSet.
					XML data = level.tileSets[i];

					// Get first GID.
					int gid = data.getInt("firstgid");

					// Load tileSet image info.
					XML image = data.getChild("image");

					// Extract tileSet filename.
					String[] nameParts = PApplet.split(image.getString("source"), "/");
					String imageFileName = "Tilesets/" + nameParts[nameParts.length - 1];

					// Get tile count.
					int tileColumns = image.getInt("width") / level.tileWidth;
					int tileRows = image.getInt("height") / level.tileHeight;

					// Create new tileSet.
					pie.tileSetList[i] = new TileSet(tileRows, tileColumns, level.tileWidth, level.tileHeight, gid, imageFileName, pie);

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
					if ((nameParts[0].equals("(light)")) && pie.lighting && !levelName.equals(pie.currentLevelName)) {

						// Process each object in light layer.
						for (XML object : objectType.getChildren("object")) {

							// If there's a setting for brightness, use it...
							try {
								if (object.getChild("properties").getChild("property").getString("name").equals("brightness")) {
									PixelPie.drawLight (
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
								PixelPie.drawLight (
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
						// try {obj = Class.forName(ClassName).getDeclaredConstructors()[0].newInstance(new Object[]{this});}
						try {obj = Class.forName(ClassName).getDeclaredConstructors()[0].newInstance(pie.app);}
						catch (Exception e) {pie.log.printlg(e);}

						if (obj != null) {

							// Create temp object for modification.
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
			if ((pie.lighting) && !levelName.equals(pie.currentLevelName)) {

				// Blur the lightMap.
				pie.loadingText = "Finalizing Lightmap";
				PixelPie.fastblur(pie.lightMap, 5);

				pie.loadingText = "Pre-calculating Gamma Values";
				// Calculate lightMap multiply map.
				pie.lightMapMult = new float[pie.lightMap.pixels.length];
				for (int i = 0; i < pie.lightMapMult.length; i++) {pie.lightMapMult[i] = (pie.lightMap.pixels[i] & 0xFF) / 255.0f;}

				// Clear the lightMap reference.
				pie.lightMap = null;

				// Reset levelBuffer for current level.
				pie.loadingText = "Generating Level Buffer";
				//pie.levelBuffer.resize(pie.roomWidth, pie.roomHeight);
				pie.levelBuffer = pie.app.createImage(pie.roomWidth, pie.roomHeight, PConstants.ARGB);
				pie.generateLevelBuffer();
			}

			// Set current level name.
			pie.currentLevelName = levelName;

			// Force garbage collection.
			System.gc();
			
			// Set loading flag to false (remove loading screen).
			pie.levelLoading = false;
		}
	}
}
