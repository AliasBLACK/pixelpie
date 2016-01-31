package black.alias.pixelpie.sound.levelSound;

import processing.core.PApplet;
import black.alias.pixelpie.PixelPie;
import black.alias.pixelpie.sound.soundFile;

/**
 * Environmental sound that gets louder as it nears the center of the camera.
 * @author Xuanming
 *
 */
public class envSound implements levelSound{
	public soundFile player;
	int x, y, range;
	boolean loop;
	final PixelPie pie;

	/**
	 * Constructor.
	 * @param X
	 * @param Y
	 * @param filename
	 * @param Range
	 * @param Loop
	 * @param pie
	 */
	public envSound(int X, int Y, String filename, int Range, boolean Loop, PixelPie pie) {		
		this.pie = pie;		
		player = pie.SoundDevice.createSound(filename);
		x = X;
		y = Y;
		loop = Loop;
		range = Range;
	}

	/**
	 * Play file.
	 */
	public void play() {
		if (loop) {
			player.setLoop(true);
			player.play();
		} else {
			player.setLoop(false);
			player.play();
		}
	}

	/**
	 * Stop file.
	 */
	public void pause() {
		if (loop) {
			player.pause();
		} else {
			player.pause();
			player.rewind();
		}
	}

	public void update() {
		if (player.isPlaying()) {
			if (distanceFromCamera() <= range) {
				adjustPlayer(); // Update volume and pan.

			// If source is outside of range, stop playing.
			} else {
				player.pause();
			}

		// If it's a looping sound, check if it's in range. If yes,
		// start playing.
		} else if (loop) {
			if (distanceFromCamera() <= range) {
				player.setLoop(true);
				adjustPlayer();
			}
		}
	}

	/**
	 * Adjust player settings based on distance from camera.
	 */
	public void adjustPlayer() {
		
		// Sound's position from left of screen.
		player.setPan(-1 + (float)(((x - pie.displayX) / (pie.app.width / pie.pixelSize)) * 2));

		// Adjust volume depending on distance from center of screen.
		player.setVolume(distanceFromCamera()/range);
	}
	
	/**
	 * Release the player's resources after we're done with it.
	 */
	public void release() {
		player.release();
	}

	/**
	 * Calculate distance from center of screen.
	 * @return distance from center of screen.
	 */
	private float distanceFromCamera() {
		return PApplet.dist(x, y, pie.displayX + (pie.app.width / pie.pixelSize) / 2, pie.displayY + (pie.app.height / pie.pixelSize) / 2);
	}
}
