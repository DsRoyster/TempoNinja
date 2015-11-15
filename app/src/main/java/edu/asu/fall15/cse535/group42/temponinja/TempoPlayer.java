package edu.asu.fall15.cse535.group42.temponinja;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by royster on 11/13/15.
 */
public class TempoPlayer {

	// TAG information
	private static final String TAG = "TempoPlayer";

	// Media Player
	protected MediaPlayer	mp = null;
	protected MediaPlayer	mp2 = null;
	protected boolean		isPlaying = false;
	public boolean isPlaying () { return isPlaying; }

	// Main activity
	MainActivity ma = null;

	// Constructor
	public TempoPlayer (MainActivity ima) {
		ma = ima;
	}

	// Switch music
	public boolean switchPlay (File music, double anchor, double tempo, double nextStepTime, double cadence) {
		Log.i(TAG, "switchPlay: " + music.getName());
		boolean playStatus = false;
		try {
			if (mp2 != null) {
				mp2.stop();
			}
			startInProgress = true;
			// Setup player
			mp2 = new MediaPlayer();
			mp2.setDataSource(music.getAbsolutePath());
			mp2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					musicEnds();
				}
			});
			mp2.prepare();

			// Calculate playing delay
			double stepTime = 60.0 * 1000 / cadence;
			double curTime = System.currentTimeMillis();
			double delay = (nextStepTime - curTime) - anchor;
			Log.i(TAG, "switchPlay - " + nextStepTime + " - " + curTime);
			while (delay < 0) {
				nextStepTime += stepTime;
				curTime = System.currentTimeMillis();
				delay = (nextStepTime - curTime) - anchor;
			}
			Log.i(TAG, "switchPlay - delay: " + delay);

			// Start music by delay
			playStatus = startMusic(delay);
		} catch (IOException e) {
			Log.i(TAG, "[X] Play music [" + music.getName() + "] failed.");
			startInProgress = false;
			return false;
		}

		// When mp1 finishes, stop mp1 and switch mp2 to mp1
		isPlaying = true;
		return playStatus;
	}

	// Start the music based on calculated delay
	protected static final long		fadeTime = 3000;	// fade-in/fade-out time
	protected static final long		fadeLevel = 10;		// ten levels to fade-in/fade-out
	protected static final long 	fadeEachTime = fadeTime / fadeLevel;
	protected static final float 	fadeVal = (float) 1.0 / fadeLevel;
	protected long					curLevel = 0;
	protected boolean				startInProgress = false;
	private boolean startMusic (double delay) {
		Log.i(TAG, "startMusic - " + (long)delay);

		// Delay it
		final Timer timer = new Timer(true);
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				realStartMusic();
				Looper.prepare();
				Looper.loop();
			}
		};

		timer.schedule(timerTask, (long)delay, (long)delay);

		return true;
	}

	// Real start music
	private boolean realStartMusic () {
		Log.i(TAG, "realStartMusic - delayed - start playing");
		curLevel = 0;
		mp2.start();
		mp2.setVolume(0, 0);
		startInProgress = false;

		// Begin counting (minus count)
		startFade();

		return true;
	}

	// Fade timer task
	private Timer fadeTimer = new Timer ();
	public void startFade () {
		fadeTimer.schedule(new FadeTask(), fadeEachTime);
	}
	private class FadeTask extends TimerTask  {
		@Override
		public void run() {
			Log.i(TAG, "curLevel: " + curLevel);
			if (!isPlaying || startInProgress) return;
			if (mp2 == null) return;
			if (curLevel < fadeLevel) {
				// Fade-in and fade-out
				mp2.setVolume(fadeVal * curLevel, fadeVal * curLevel);
				if (mp != null) {
					mp.setVolume(1 - fadeVal * curLevel, 1 - fadeVal * curLevel);
				}
				curLevel ++;
				fadeTimer.schedule(new FadeTask(), fadeEachTime);
			} else {
				// Set mp2 music sound
				mp2.setVolume(1, 1);

				// Release mp
				if (mp != null) {
					mp.stop();
				}

				// Assign
				mp = mp2;
				mp2 = null;
			}
		}
	}


	// When current music ends
	protected void musicEnds () {
		Log.i(TAG, "Music finished.");
		mp = null;
		isPlaying = false;

		ma.afterMusicFinish();
	}


	// stop playing
	public void stop () {

		if (mp != null) {
			mp.stop();
			mp = null;
		}
		if (mp2 != null) {
			mp2.stop();
			mp2 = null;
		}
		isPlaying = false;
	}


}
