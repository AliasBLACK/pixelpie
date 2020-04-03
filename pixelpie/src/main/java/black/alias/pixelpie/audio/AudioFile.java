package black.alias.pixelpie.audio;
import processing.core.PApplet;
import processing.sound.*;

public class AudioFile {
	SoundFile sound;
	float amp = 1.0f;
	float pan = 0.5f;
	boolean loop = false;
	
	public AudioFile(PApplet app, String filename) {
		sound = new SoundFile(app, filename);
	}
	
	public void play() {
		if (loop) {
			sound.loop();
		} else {
			sound.play();
		}
	}
	
	public void pause() {
		sound.pause();
	}
	
	public void rewind() {
		sound.cue(0.0f);
	}
	
	public float getVolume() {
		return amp;
	}
	
	public void setVolume(float volume) {
		amp = volume;
		sound.amp(amp);
	}
	
	public float getPan() {
		return pan;
	}
	
	public void setPan(float pan) {
		this.pan = pan;
		sound.pan(pan);
	}
	
	public boolean getLoop() {
		return loop;
	}
	
	public void setLoop(boolean loop) {
		if (sound.isPlaying()) {
			if (!this.loop && loop) {
				sound.stop();
				sound.loop();
			} else if (this.loop && !loop) {
				sound.stop();
				sound.play();
			}
		}
		this.loop = loop;
	}
	
	public boolean isPlaying() {
		return sound.isPlaying();
	}
	
	public void release() {
		sound.removeFromCache();
	}
}