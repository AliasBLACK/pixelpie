package black.alias.pixelpie.loader;

import java.util.HashMap;

import processing.data.*;
import black.alias.pixelpie.*;
import black.alias.pixelpie.level.level;
import black.alias.pixelpie.sprite.*;

/**
 * Data loader.
 * @author Xuanming
 *
 */
public class dataLoader extends Thread{
	final HashMap<String, sprite> spr = new HashMap<String, sprite>();
	final HashMap<String, level> lvl = new HashMap<String, level>();
	final PixelPie pie;
	
	public dataLoader(PixelPie pie) {
		this.pie = pie;
	}
	
	public void start() {
		super.start();
	}

	/**
	 * Method to load all the defined assets from json files.
	 */
	public void run() {
		String path;

		// Load Sprites.json
		path = "Sprites/Sprites.json";
		if (pie.fileExists(pie.app.dataPath(path))) {
			JSONArray data = pie.app.loadJSONArray(path);
			for (int i = 0; i < data.size(); i++) {    
				JSONObject sprite = data.getJSONObject(i); 
				spr.put(
						sprite.getString("Name"),
						new sprite(
								sprite.getInt("Frames"),
								sprite.getInt("FPS"),
								PixelPie.toBoolean(sprite.getInt("FlipX")),
								PixelPie.toBoolean(sprite.getInt("FlipY")),
								sprite.getString("Sprite"),
								sprite.getString("IlumMap"),
								pie
								)
						);
			}
		} else {
			pie.log.printlg("JSON file " + pie.app.dataPath(path) + " not found.");
		}

		// Load Levels.json
		path = "Levels/Levels.json";
		if (pie.fileExists(pie.app.dataPath(path))) {
			JSONArray data = pie.app.loadJSONArray(path);
			for (int i = 0; i < data.size(); i++) {
				JSONObject level = data.getJSONObject(i);
				pie.lvl.put(level.getString("Name"), new level(level.getString("Location"), pie));
				pie.lvl.get(level.getString("Name")).levelName = level.getString("Name");
			}
		} else {
			pie.log.printlg("JSON file " + pie.app.dataPath(path) + " not found.");
		}

		// Start game.
		pie.loaded = true;
	}

	/**
	 * @return the sprite database.
	 */
	public final HashMap<String, sprite> getSpr() {
		return spr;
	}

	/**
	 * @return the level database.
	 */
	public final HashMap<String, level> getLvl() {
		return lvl;
	}
}
