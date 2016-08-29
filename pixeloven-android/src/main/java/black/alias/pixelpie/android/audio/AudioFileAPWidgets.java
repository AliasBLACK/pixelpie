package black.alias.pixelpie.android.audio;

import black.alias.pixelpie.audio.AudioFile;
import android.app.Activity;

public class AudioFileAPWidgets implements AudioFile {
	
	APMediaPlayer player;
	boolean playing = false;
	float volume;		// 0.0 to 1.0
	float pan;			// -1 to 1, 0 is where both speakers are at equal volume.
	
	public AudioFileAPWidgets (Activity activity, String filename) {
		player = new APMediaPlayer(activity); 		//create new APMediaPlayer
		player.setMediaFile(filename); 			//set the file (files are in data folder)
	}

	public void play() {
		playing = true;
		player.start();
	}

	public void pause() {
		playing = false;
		player.pause();
	}

	public void rewind() {
		player.seekTo(0);
	}

	public float getVolume() {
		return volume;
	}

	public void setVolume(float volume) {
		this.volume = volume;
		updatePlayer();
	}

	public float getPan() {
		return pan;
	}

	public void setPan(float pan) {
		this.pan = pan;
		updatePlayer();
	}

	public boolean getLoop() {
		return player.getLooping();
	}

	public void setLoop(boolean loop) {
		player.setLooping(loop);;
	}

	public boolean isPlaying() {
		return playing;
	}

	public void release() {
		if (player != null) { 		// must be checked because or else crash when return from landscape mode
			player.release(); 		// release the player
		}
	}
	
	private void updatePlayer() {
		player.setVolume(
				1 - Math.max(0.0f, volume),				// Left.
				1 - Math.abs(Math.min(0.0f, volume))	// Right.
		);
	}
}
