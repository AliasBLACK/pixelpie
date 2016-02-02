package black.alias.pixelpie.android;

import processing.core.PApplet;
import black.alias.pixelpie.PixelOven;
import black.alias.pixelpie.android.audio.AudioDeviceAPWidgets;
import black.alias.pixelpie.android.file.FileManagerAndroid;
import black.alias.pixelpie.audio.AudioDevice;
import black.alias.pixelpie.file.FileManager;

public class PixelOvenAndroid implements PixelOven {
	
	final AudioDeviceAPWidgets audio;
	final FileManagerAndroid manager;
	
	public PixelOvenAndroid (PApplet app) {
		audio = new AudioDeviceAPWidgets(app);
		manager = new FileManagerAndroid(app);
	}

	public AudioDevice getAudio() {
		return audio;
	}

	public FileManager getManager() {
		return manager;
	}
}
