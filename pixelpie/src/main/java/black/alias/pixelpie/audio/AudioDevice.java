package black.alias.pixelpie.audio;

import processing.core.PApplet;

public class AudioDevice {
	PApplet app;
	
	public AudioDevice(PApplet app) {
		this.app = app;
	}
	
	public AudioFile createSound(String filename) {
		return new AudioFile(app, filename);
	}
}