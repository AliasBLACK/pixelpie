package black.alias.pixelpie.loader;

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
	final PixelPie pie;
	
	public dataLoader(PixelPie pie) {
		this.pie = pie;
	}
	
	public void start() {
		super.start();
	}

	/**
	 * Method to load all the defined assets from JSON files.
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
			pie.spr.put(
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
			pie.lvl.put(level.getString("Name"), new Level(level.getString("Location"), pie));
			pie.lvl.get(level.getString("Name")).levelName = level.getString("Name");
		}

		// Start game.
		pie.loaded = true;
	}
}
