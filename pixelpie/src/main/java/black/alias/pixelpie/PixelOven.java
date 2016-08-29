package black.alias.pixelpie;

import black.alias.pixelpie.audio.AudioDevice;
import black.alias.pixelpie.file.FileManager;

/**
 * PixelOven is a container class that initiates all platform specific objects.
 * It keeps the interface exposed to end-user cleaner.
 * @author Xuanming
 *
 */
public interface PixelOven {
	public AudioDevice getAudio();
	public FileManager getManager();
	public String getPlatform();
}
