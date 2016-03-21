package black.alias.pixelpie.android.ui.sound;

import android.app.Activity;
import de.lessvoid.nifty.sound.SoundSystem;
import de.lessvoid.nifty.spi.sound.SoundDevice;
import de.lessvoid.nifty.spi.sound.SoundHandle;
import de.lessvoid.nifty.tools.resourceloader.NiftyResourceLoader;

public class SoundDeviceAPWidgets implements SoundDevice {
	
	Activity activity;
	
	/**
	 * Create an instance of SoundDeviceAPWidgets.
	 * @param minim Instance of APWidgets currently in use by Processing (or Java).
	 */
	public SoundDeviceAPWidgets(Activity activity){
		this.activity = activity;
	}
	
	public void setResourceLoader(NiftyResourceLoader niftyResourceLoader) {
	}
	
	public SoundHandle loadSound(SoundSystem soundSystem, String filename) {
		return new SoundHandleAPWidgets(activity, filename);
	}
	
	public SoundHandle loadMusic(SoundSystem soundSystem, String filename) {
		return new SoundHandleAPWidgets(activity, filename);
	}
	
	public void update(int delta) {
		// Do nothing.
	}
}