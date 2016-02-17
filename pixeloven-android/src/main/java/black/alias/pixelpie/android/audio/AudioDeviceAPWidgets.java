package black.alias.pixelpie.android.audio;

import android.app.Activity;
import black.alias.pixelpie.audio.AudioDevice;
import black.alias.pixelpie.audio.AudioFile;

public class AudioDeviceAPWidgets implements AudioDevice {
	
	final Activity activity;
	
	public AudioDeviceAPWidgets (Activity activity) {
		this.activity = activity;
	}

	public AudioFile createSound(String filename) {
		return new AudioFileAPWidgets(activity, filename);
	}

}
