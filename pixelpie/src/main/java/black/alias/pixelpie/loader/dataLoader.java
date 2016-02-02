package black.alias.pixelpie.loader;

import java.util.HashMap;

import processing.data.*;
import black.alias.pixelpie.*;
import black.alias.pixelpie.level.Level;
import black.alias.pixelpie.sprite.*;

/**
 * Data loader.
 * @author Xuanming
 *
 */
public class dataLoader extends Thread{
	final HashMap<String, Sprite> spr = new HashMap<String, Sprite>();
	final HashMap<String, Level> lvl = new HashMap<String, Level>();
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
		String dataString;
		String[] dataList;
		JSONArray data;

		// Load Sprites.json
		dataList = pie.app.loadStrings("Sprites/Sprites.json");
		dataString = "";
		for (int i = 0; i < dataList.length; i++) {
			dataString += dataList[i];
		}
		data = JSONArray.parse(dataString);
		for (int i = 0; i < data.size(); i++) {    
			JSONObject sprite = data.getJSONObject(i); 
			spr.put(
					sprite.getString("Name"),
					new Sprite(
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

		// Load Levels.json
		dataList = pie.app.loadStrings("Levels/Levels.json");
		dataString = "";
		for (int i = 0; i < dataList.length; i++) {
			dataString += dataList[i];
		}
		data = JSONArray.parse(dataString);
		for (int i = 0; i < data.size(); i++) {
			JSONObject level = data.getJSONObject(i);
			lvl.put(level.getString("Name"), new Level(level.getString("Location"), pie));
			lvl.get(level.getString("Name")).levelName = level.getString("Name");
		}

		// Start game.
		pie.loaded = true;
	}

	/**
	 * @return the sprite database.
	 */
	public final HashMap<String, Sprite> getSpr() {
		return spr;
	}

	/**
	 * @return the level database.
	 */
	public final HashMap<String, Level> getLvl() {
		return lvl;
	}
}
