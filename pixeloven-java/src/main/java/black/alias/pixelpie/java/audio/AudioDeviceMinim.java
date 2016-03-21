package black.alias.pixelpie.java.audio;

import black.alias.pixelpie.audio.AudioDevice;
import black.alias.pixelpie.audio.AudioFile;
import ddf.minim.*;

public class AudioDeviceMinim implements AudioDevice {
	
	final Minim minim;
	
	public AudioDeviceMinim (Minim minim) {
		this.minim = minim;
	}

	public AudioFile createSound(String filename) {
		return new AudioFileMinim(minim, filename);
	}
}
