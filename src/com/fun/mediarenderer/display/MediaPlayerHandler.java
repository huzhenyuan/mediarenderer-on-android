package com.fun.mediarenderer.display;

import com.fun.mediarenderer.upnp.DefMediaPlayer;

import android.util.Log;

public class MediaPlayerHandler implements DisplayHandler{
	
	final static String TAG = "MediaPlayerHandler";
	
	@Override
	public void onNoMedia(DefMediaPlayer player) {
		Log.d(TAG,"onNoMedia");
	}
	
	@Override
	public void onPlay(DefMediaPlayer player) {
		Log.d(TAG,"onPlay");
	}

	@Override
	public void onStop(DefMediaPlayer player) {
		// TODO Auto-generated method stub
		Log.d(TAG,"onStop");

	}
	
	

}
