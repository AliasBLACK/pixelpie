package black.alias.pixelpie.java;

import processing.core.PApplet;
import ddf.minim.Minim;
import de.lessvoid.nifty.spi.sound.SoundDevice;
import black.alias.pixelpie.PixelOven;
import black.alias.pixelpie.audio.AudioDevice;
import black.alias.pixelpie.file.FileManager;
import black.alias.pixelpie.java.audio.AudioDeviceMinim;
import black.alias.pixelpie.java.file.FileManagerJava;
import black.alias.pixelpie.java.ui.sound.SoundDeviceMinim;

public class PixelOvenJava implements PixelOven {
	
	final AudioDeviceMinim audio;
	final FileManagerJava manager;
	final SoundDeviceMinim niftyAudio;
	
	public PixelOvenJava (PApplet app) {
		this(app, new Minim(app));
	}
	
	public PixelOvenJava (PApplet app, Minim minim) {
		this.audio = new AudioDeviceMinim(minim);
		this.manager = new FileManagerJava(app);
		this.niftyAudio = new SoundDeviceMinim(minim);
	}

	public AudioDevice getAudio() {
		return audio;
	}

	public FileManager getManager() {
		return manager;
	}

	public SoundDevice getNiftyAudio() {
		return niftyAudio;
	}
}