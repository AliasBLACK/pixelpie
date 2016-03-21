package black.alias.pixelpie.android.ui.sound;

import android.app.Activity;
import de.lessvoid.nifty.spi.sound.SoundHandle;

public class SoundHandleAPWidgets implements SoundHandle {
	
	APMediaPlayer player;
	float volume = 1.0f;
	boolean playing = false;
	
	public SoundHandleAPWidgets(final Activity activity, final String filename){
		player = new APMediaPlayer(activity);
		player.setMediaFile(filename);
	};
	
	public void play() {
		playing = true;
		player.seekTo(0);
		player.start();
	}
	
	public void stop() {
		playing = false;
		player.pause();
	}
	
	public void setVolume(float volume) {
		this.volume = volume;
		player.setVolume(volume, volume);
	}
	
	public float getVolume() {
		return volume;
	}
	
	public boolean isPlaying() {
		return playing;
	}
	
	public void dispose() {
		if(player != null) { 	// must be checked because or else crash when return from landscape mode
			player.release(); 	// release the player
		}
	}
}
