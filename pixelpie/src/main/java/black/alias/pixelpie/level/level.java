package black.alias.pixelpie.level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import black.alias.pixelpie.PixelPie;
import processing.core.PApplet;
import processing.data.IntDict;
import processing.data.StringDict;
import processing.data.XML;

public class Level {
	public String levelName;
	public HashMap<String, Script> scripts;
	public StringDict properties;
	public int levelWidth, levelHeight, levelColumns, levelRows, levelLayers, backgroundLayers, tileWidth, tileHeight;
	public int[][] levelMap, background;
	public float[] bgScroll;
	public XML[] objects, tileSets;

	public Level(String filename, PixelPie pie) {
		XML file = pie.app.loadXML(filename);

		// Get level properties, if any.
		if (file.getChild("properties") != null) {
			properties = new StringDict();
			for (XML child : file.getChild("properties").getChildren("property")) {
				properties.set(child.getString("name"), child.getString("value"));
			}
		}

		// Get tile dimensions.
		tileWidth = file.getInt("tilewidth");
		tileHeight = file.getInt("tileheight");

		// Get Row and Column count.
		levelColumns = file.getInt("width");
		levelRows = file.getInt("height");

		// Get level dimensions.
		levelWidth = levelColumns * tileWidth;
		levelHeight = levelRows * tileHeight;

		// Get tileSets.
		tileSets = file.getChildren("tileset");

		// Get objects.
		objects = file.getChildren("objectgroup");

		// Get scripts.
		scripts = new HashMap<String, Script>();
		for (XML objectType : objects) {
			String objectName = objectType.getString("name");

			// Check type of object.
			if (objectName.substring(0,1).equals("(")) {

				// Break up name into parts.
				String[] nameParts = PApplet.split(objectName, " ");

				// If script.
				if (nameParts[0].equals("(script)")) {

					// Grab script name.
					String name = nameParts[1];

					// Initialize coordinates array (if any).
					IntDict scriptCoords = new IntDict();

					// Check for objects in script layer. Get coordinates from these objects.
					if (objectType.getChild("object") != null) {
						for (XML coordObj : objectType.getChildren("object")) {                

							// Check if object has a name, if not, skip it.
							if (coordObj.getString("name") == null) {pie.log.printlg("Script coordinate container does not have a name. Ignoring."); continue;}

							// Figure out object type.
							int coordType = 0; if (coordObj.hasChildren()) {coordType = (coordObj.getChild("polyline") != null) ? 2 : 1;}

							// Process coordinates depending on object type.
							switch (coordType) {

							// If rectangle, take top-left corner.
							case 0:
								scriptCoords.set(
										coordObj.getString("name"),
										PixelPie.coordToArray(
												coordObj.getInt("x"),
												coordObj.getInt("y"),
												this
												)
										);
								break;

								// If ellipse, take center.
							case 1:
								scriptCoords.set(
										coordObj.getString("name"),
										PixelPie.coordToArray(
												coordObj.getInt("x") + coordObj.getInt("width")/2,
												coordObj.getInt("y") + coordObj.getInt("height")/2,
												this
												)
										);
								break;

								// If poly line, record each point.
							case 2:
								String[] coordsArray = PApplet.split(coordObj.getChild("polyline").getString("points"), " ");
								int startX = Integer.parseInt(objectType.getChild("object").getString("x"));
								int startY = Integer.parseInt(objectType.getChild("object").getString("y"));                    
								for (int i = 0; i < coordsArray.length; i++) {
									scriptCoords.set(
											coordObj.getString("name") + "_" + PApplet.nf(i, 2),
											PixelPie.coordToArray(
													startX + Integer.parseInt(PApplet.split(coordsArray[i], ",")[0]),
													startY + Integer.parseInt(PApplet.split(coordsArray[i], ",")[1]),
													this
													)
											);
								}
								break;
							}
						}
					} else {
						scriptCoords = null;
					}

					// Get the total amount of frames.
					int endFrame = 0; 
					for (XML obj : objectType.getChild("properties").getChildren("property")) {    
						endFrame = PApplet.max(endFrame, PixelPie.getScriptFrame(obj.getString("name")));
					}

					// Create the scriptAction container "children".
					HashMap<String, ScriptAction[]> children = new HashMap<String, ScriptAction[]> ();

					// Create the script object.
					Script Script = new Script(endFrame, scriptCoords, children, pie);
					scripts.put(name, Script);

					// Create scriptAction temporary container. This one uses a HashMap so we can dynamically add scriptActions to it.
					HashMap<String, ArrayList<ScriptAction>> scriptTemp = new HashMap<String, ArrayList<ScriptAction>>();

					// Add scriptActions to the temporary container.
					for (XML obj : objectType.getChild("properties").getChildren("property")) {
						String frame = PApplet.str(PixelPie.getScriptFrame(obj.getString("name")));

						// Create scriptAction object from parameters.
						String[] actionArray = PApplet.split(obj.getString("value"), "(");
						String params = actionArray[1].substring(0, actionArray[1].length() - 1);
						ScriptAction action = null;
						//try {action = (scriptAction) Class.forName(app.getClass().getName() + "$script_" + actionArray[0]).getDeclaredConstructors()[0].newInstance(new Object[]{app, params, Script});}
						try {
							action = (ScriptAction) Class.forName(pie.app.getClass().getName() + "$script_" + actionArray[0]).getDeclaredConstructors()[0].newInstance(new Object[]{pie.app});
							action.setup(params, Script);
						} catch (Exception e) {
							pie.log.printlg(e);
						}

						// Check whether there is an entry in the scriptTemp array.
						if (scriptTemp.get(frame) != null) {
							scriptTemp.get(frame).add(action);
						} else {
							// Initiate Arraylist.
							scriptTemp.put(frame, new ArrayList<ScriptAction>());
							scriptTemp.get(frame).add(action);
						}
					}

					// Turn over contents of temporary container to "children" array.
					for (@SuppressWarnings("rawtypes") Map.Entry entry : scriptTemp.entrySet()) {
						ScriptAction[] tempActions = new ScriptAction[ scriptTemp.get(entry.getKey()).size() ];
						for (int i = 0; i < scriptTemp.get(entry.getKey()).size(); i++) {
							tempActions[i] = scriptTemp.get(entry.getKey()).get(i);
						}
						children.put(entry.getKey().toString(), tempActions);
					}
				}
			}
		}

		// Load level layers.
		XML[] layers = file.getChildren("layer");

		// Count number of background and level layers.
		for (int i = 0; i < layers.length; i++) {
			if (layers[i].getString("name").substring(0,4).equals("(bg)")) {backgroundLayers++;}
			else {levelLayers++;}
		}

		// Initiate arrays.
		background = new int[backgroundLayers][levelColumns * levelRows];
		levelMap = new int[levelLayers][levelColumns * levelRows];
		bgScroll = new float[backgroundLayers];

		// Process each layer.
		int currentLayer = 0;
		int bgCurrentLayer = 0;
		for (int i = 0; i < layers.length; i++) {

			// If it's a background layer...
			if (layers[i].getString("name").substring(0,4).equals("(bg)")) {

				// Get properties.
				HashMap<String, String> prop = new HashMap<String, String>();
				for (XML property : layers[i].getChild("properties").getChildren("property")) {
					prop.put(property.getString("name"), property.getString("value"));
				}

				// Process the scroll rate.
				bgScroll[bgCurrentLayer] = PApplet.constrain(Float.parseFloat(PixelPie.getProp(prop, "scroll")), 0, 1);

				// Process each background tile.
				XML[] tiles = layers[i].getChild("data").getChildren("tile");
				for (int k = 0; k < tiles.length; k++) {
					background[bgCurrentLayer][k] = tiles[k].getInt("gid");
				}

				// Increase background layer counter.
				bgCurrentLayer++;
			}

			// Else, load layer as normal.
			else {
				// Load each tile.
				XML[] tiles = layers[i].getChild("data").getChildren("tile");
				for (int k = 0; k < tiles.length; k++) {
					levelMap[currentLayer][k] = tiles[k].getInt("gid");
				}
				currentLayer++;
			}
		}
	}
}
