package com.fun.mediarenderer.display;

import com.fun.mediarenderer.upnp.DefMediaPlayer;

public interface DisplayHandler {
	
	public void onNoMedia(DefMediaPlayer player);
	
    public void onPlay(DefMediaPlayer player);

    public void onStop(DefMediaPlayer player);

}
