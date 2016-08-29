package black.alias.pixelpie.java;

import processing.core.PApplet;
import ddf.minim.Minim;
import black.alias.pixelpie.PixelOven;
import black.alias.pixelpie.audio.AudioDevice;
import black.alias.pixelpie.file.FileManager;
import black.alias.pixelpie.java.audio.AudioDeviceMinim;
import black.alias.pixelpie.java.file.FileManagerJava;

public class PixelOvenJava implements PixelOven {
	
	final AudioDeviceMinim audio;
	final FileManagerJava manager;
	
	public PixelOvenJava (PApplet app) {
		this(app, new Minim(app));
	}
	
	public PixelOvenJava (PApplet app, Minim minim) {
		this.audio = new AudioDeviceMinim(minim);
		this.manager = new FileManagerJava(app);
	}

	public AudioDevice getAudio() {
		return audio;
	}

	public FileManager getManager() {
		return manager;
	}
	
	public String getPlatform() {
		return "Java";
	}
}