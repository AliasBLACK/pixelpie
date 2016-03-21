package black.alias.pixelpie.android;

import java.lang.reflect.Field;

import processing.core.PApplet;
import android.app.Activity;
import black.alias.pixelpie.PixelOven;
import black.alias.pixelpie.android.audio.AudioDeviceAPWidgets;
import black.alias.pixelpie.android.file.FileManagerAndroid;
import black.alias.pixelpie.android.ui.sound.SoundDeviceAPWidgets;
import black.alias.pixelpie.audio.AudioDevice;
import black.alias.pixelpie.file.FileManager;
import de.lessvoid.nifty.spi.sound.SoundDevice;

public class PixelOvenAndroid implements PixelOven {
	
	final AudioDeviceAPWidgets audio;
	final FileManagerAndroid manager;
	final SoundDeviceAPWidgets niftyAudio;
	
	public PixelOvenAndroid (PApplet app) {
		Activity activity = getActivity(app);
		audio = new AudioDeviceAPWidgets(activity);
		manager = new FileManagerAndroid(activity);
		niftyAudio = new SoundDeviceAPWidgets(activity);
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
	
	/**
	 * Grab private Activity instance from Processing.
	 * @param app
	 * @return
	 */
	private Activity getActivity(PApplet app) {
		try {
	        Field field = PApplet.class.getDeclaredField("activity");
	        field.setAccessible(true);
	        Object value = field.get(app);
	        field.setAccessible(false);

	        if (value == null) {
	            return null;
	        } else if (Activity.class.isAssignableFrom(value.getClass())) {
	            return (Activity) value;
	        }
	        throw new RuntimeException("Wrong value");
	    } catch (NoSuchFieldException e) {
	        throw new RuntimeException(e);
	    } catch (IllegalAccessException e) {
	        throw new RuntimeException(e);
	    }
	}
}
