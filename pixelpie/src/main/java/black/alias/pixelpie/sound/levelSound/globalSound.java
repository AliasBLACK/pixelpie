package black.alias.pixelpie.sound.levelSound;

import black.alias.pixelpie.PixelPie;
import black.alias.pixelpie.sound.soundFile;

/**
 * Global/ambient sound.
 * @author Xuanming
 *
 */
public class globalSound implements levelSound {
	public soundFile player;
	boolean loop;
	int frames;
	float step;

	/**
	 * Constructor.
	 * @param filename
	 * @param Loop
	 * @param pie
	 */
	public globalSound(String filename, boolean Loop, PixelPie pie) {
		player = pie.SoundDevice.createSound(filename);
		this.loop = Loop;
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

	/**
	 * Empty Functions.
	 */
	public void update() {
	}

	/**
	 * Empty Functions.
	 */
	public void adjustPlayer() {
	}
	
	/**
	 * Release resources when we're done with the player.
	 */
	public void release() {
		player.release();
	}
}
