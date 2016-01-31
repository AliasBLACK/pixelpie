package black.alias.pixelpie.sound;

public interface soundFile {
	public void play();
	public void pause();
	public void rewind();	
	public float getVolume();
	public void setVolume(float volume);	
	public float getPan();
	public void setPan(float pan);	
	public boolean getLoop();
	public void setLoop(boolean loop);	
	public boolean isPlaying();
	public void release();
}