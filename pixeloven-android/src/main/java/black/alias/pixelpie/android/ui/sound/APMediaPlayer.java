/**
 * Copyright 2010 Rikard Lundstedt
 * 
 * This file is part of APWidgets.
 * 
 * APWidgets is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * APWidgets is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with APWidgets. If not, see <http://www.gnu.org/licenses/>.
 */

package black.alias.pixelpie.android.ui.sound;

import java.io.IOException;
import java.util.Vector;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.Log;
/**
 * Wraps PMediaPlayer. For now 
 * only sound files are supported. 
 * For video see PVideoView.
 * @author Rikard Lundstedt
 *
 */
public class APMediaPlayer implements OnPreparedListener, OnErrorListener{
	private static final String TAG = "PMediaPlayer";
	private Activity activity;
	private boolean prepared = false;
	private Vector<MediaPlayerTask> tasks = new Vector<MediaPlayerTask>();

	private String file;
	private String getFile(){
		return file;
	}

	private APMediaPlayer getThis(){
		return this;
	}

	private MediaPlayer mediaPlayer;

	/**
	 * Creates new PMediaPlayer
	 * @param pApplet
	 */
	public APMediaPlayer(Activity activity){
		this.activity = activity;
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnPreparedListener(getThis());
		mediaPlayer.setOnErrorListener(getThis());
	}
	
	/**
	 * Set the media file by filename
	 * @param file
	 */
	public void setMediaFile(String file){
		this.file = file;
		mediaPlayer.reset();
		prepared = false;

		AssetFileDescriptor afd = null;
		try {
			//afd = pApplet.getActivity().getAssets().openFd(getFile());
			afd = activity.getAssets().openFd(getFile());
			mediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
			afd.close();
			mediaPlayer.prepare();
		}catch (IllegalStateException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	
	/**
	 * Start playing the file
	 * 
	 */
	public void start(){
		if(prepared){
			mediaPlayer.start();
		}else{
			tasks.addElement(new MediaPlayerTask(){
				public void doTask(){
					mediaPlayer.start();
				}
			});
		}
	}
	/** 
	 * Pause the playing of the file
	 * 
	 */
	public void pause(){
		if(prepared){
			if(mediaPlayer.isPlaying()){
				mediaPlayer.pause();
			}
		}else{
			tasks.addElement(new MediaPlayerTask(){
				public void doTask(){
					mediaPlayer.pause();
				}
			});
		}
	}

	private int msec;
	private int getMsec(){
		return msec;
	}
	/**
	 * move to a point in the file,
	 * counted from the beginning in millisecond.
	 * @param msec
	 */
	public void seekTo(int msec){
		this.msec = msec;
		if(prepared){
			mediaPlayer.seekTo(getMsec());
		}else{
			tasks.addElement(new SeekToTask(msec));
		}
	}
	/**
	 * Release the MediaPlayer
	 */
	public void release(){
		mediaPlayer.release();
	}

	private boolean looping;
	public boolean getLooping(){
		return looping;
	}
	/**
	 * Set this to true to loop the play back.
	 * @param looping
	 */
	public void setLooping(boolean looping){
		this.looping = looping;
		if(prepared){
			mediaPlayer.setLooping(getLooping());
		}else{
			tasks.addElement(new SetLoopingTask(looping));
		}
	}
	private float left;
	private float right;
	private float getLeft(){
		return left;
	}
	private float getRight(){
		return right;
	}
	/**
	 * Sets the volume. Left and right are flipped it seems.
	 * @param left
	 * @param right
	 */
	public void setVolume(float left, float right){
		this.left = left;
		this.right = right;
		if(prepared){
			mediaPlayer.setVolume(getLeft(), getRight());
		}else{
			tasks.addElement(new SetVolumeTask(left, right));
		}
	}
	/**
	 * Get the current position.
	 * @return
	 */
	public int getCurrentPosition(){
		if(prepared){
			return mediaPlayer.getCurrentPosition();
		}
		return 0;
	}
	/**
	 * Get the duration of the sound
	 * @return
	 */
	public int getDuration(){
		if(prepared){
			return mediaPlayer.getDuration();
		}
		return 0;
	}
	/** 
	 * When this is called, the player is ready to start playing etc.
	 */
	public void onPrepared(MediaPlayer mp){
		prepared = true;
		for(int i = 0;i<tasks.size();i++){
			tasks.elementAt(i).doTask();
		}
		tasks.removeAllElements();

	}

	interface MediaPlayerTask {
		public void doTask();
	}
	class SeekToTask implements MediaPlayerTask{
		int msec;
		public SeekToTask(int msec){
			this.msec = msec;
		}
		public void doTask(){
			mediaPlayer.seekTo(msec);
		}
	}
	class SetLoopingTask implements MediaPlayerTask{
		boolean looping;
		public SetLoopingTask(boolean looping){
			this.looping = looping;
		}
		public void doTask(){
			mediaPlayer.setLooping(looping);
		}
	}
	class SetVolumeTask implements MediaPlayerTask{
		float left;
		float right;
		public SetVolumeTask(float left, float right){
			this.left = left;
			this.right = right;
		}
		public void doTask(){
			mediaPlayer.setVolume(left, right);
		}
	}

	/**
	 * The MediaPlayer must be released.
	 */
	protected void finalize() throws Throwable {
		super.finalize();
		Log.i(TAG,"Finilized");
		mediaPlayer.release();
	}
	public boolean onError(MediaPlayer mediaPlayer, int a, int b){
		Log.e(TAG, a+" " +b);
		return false;
	}
} 
