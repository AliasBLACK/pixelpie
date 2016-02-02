package black.alias.pixelpie.android.audio;

import processing.core.PApplet;
import black.alias.pixelpie.audio.AudioDevice;
import black.alias.pixelpie.audio.AudioFile;

public class AudioDeviceAPWidgets implements AudioDevice {
	
	final PApplet app;
	
	public AudioDeviceAPWidgets (PApplet app) {
		this.app = app;
	}

	public AudioFile createSound(String filename) {
		return new AudioFileAPWidgets(app, filename);
	}

}
