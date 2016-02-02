package black.alias.pixelpie.java.audio;

import black.alias.pixelpie.audio.AudioFile;
import ddf.minim.*;

public class AudioFileMinim implements AudioFile {
	
	final AudioPlayer player;
	boolean loop;
	float volume = 1;
	
	public AudioFileMinim (Minim minim, String filename) {
		this.player = minim.loadFile(filename);
		this.player.setGain(6);
	}

	public void play() {
		if (loop) {
			player.loop();
		} else {
			player.play();
		}
	}

	public void pause() {
		player.pause();
	}

	public void rewind() {
		player.rewind();
	}

	public float getVolume() {
		return volume;
	}

	public void setVolume(float volume) {
		this.volume = volume;
		player.setGain(volume * 6);
	}

	public float getPan() {
		return player.getPan();
	}

	public void setPan(float pan) {
		player.setPan(pan);
	}

	public boolean getLoop() {
		return this.loop;
	}

	public void setLoop(boolean loop) {
		this.loop = loop;
	}

	public boolean isPlaying() {
		return player.isPlaying();
	}

	public void release() {
		player.close();
	}
}
