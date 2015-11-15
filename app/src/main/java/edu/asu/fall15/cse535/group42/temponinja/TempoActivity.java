package edu.asu.fall15.cse535.group42.temponinja;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TempoActivity extends AppCompatActivity {

	// TAG information
	private static final String TAG = "TempoActivity";

	// Views
	RelativeLayout 	tempoLayout;
	TextView	musicInfoTextView;
	TextView	tempoPromptTextView;
	protected void initViewList () {
		Log.i(TAG, "initViewList");

		// Obtain list of views
		tempoLayout 			= (RelativeLayout)findViewById(R.id.tempoLayout);
		musicInfoTextView 		= (TextView) findViewById(R.id.musicInfoTextView);
		tempoPromptTextView 	= (TextView) findViewById(R.id.tempoPromptTextView);
	}

	// Data holder
	DataHolder 	dataHolder;

	// Tempo Counter
	TempoCounter tempoCounter = new TempoCounter();

	// Media Player
	File		curMusic = null;
	MediaPlayer mediaPlayer = new MediaPlayer();

	// Whether it is beating
	boolean 	isBeatingStarting = false;
	boolean 	isBeating = false;

	// Timer to test if idle for some time
	boolean		isIdleTimerStarted = false;
	CountDownTimer 	idleTimer = null;



	/**
	 * Start counting
	 */
	private static final long IDLE_TIME = 3000;
	private void startBeating () {
		Log.i(TAG, "Beating started.");
		isBeating = true;

		// Preparing media
		try {
			mediaPlayer.setDataSource(curMusic.getAbsolutePath());
			mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					endBeating();
				}
			});
			mediaPlayer.prepare();
		} catch (IOException e) {
			Log.i(TAG, "[X] Playing \"" + curMusic.getName() + "failed.");
		}

		// Set beating idle timer
		idleTimer = new CountDownTimer(IDLE_TIME, IDLE_TIME*2) {
			//Toast mToast = Toast.makeText( getApplicationContext()  , "" , Toast.LENGTH_SHORT );
			public void onTick(long millisUntilFinished) {

			}

			public void onFinish() {
				Log.i(TAG, "Idle timeout. Stop beating.");
				isIdleTimerStarted = false;
				mediaPlayer.stop();
				endBeating();
			}
		};

		// Start beating
		tempoPromptTextView.setText(getText(R.string.tempoPromptTextViewText));
		tempoCounter.clear();
		mediaPlayer.start();	// start playing
		tempoCounter.start();	// start counting
	}

	/**
	 * Beat at each tap
	 */
	private void beat () {
		Log.i(TAG, "Beat!");

		if (tempoCounter != null && tempoCounter.isStarted()) {
			tempoCounter.beat();
		}
//		Animation anim = new AlphaAnimation(0.0f, 1.0f);
//		anim.setDuration(50);
//		anim.setStartOffset(20);
//		anim.setRepeatMode(Animation.REVERSE);
//		anim.setRepeatCount(0);
//		tempoLayout.startAnimation(anim);
		int colorFrom = getResources().getColor(R.color.background_material_light);
		int colorTo = Color.BLACK;
		int duration = 200;
		ObjectAnimator.ofObject(tempoLayout, "backgroundColor", new ArgbEvaluator(), colorTo, colorFrom)
				.setDuration(duration)
				.start();

		// Start or refresh idle timer
		if (idleTimer != null) {
			if (isIdleTimerStarted == false) {
				isIdleTimerStarted = true;
				idleTimer.start();
			} else {
				idleTimer.cancel();
				idleTimer.start();
			}
		}
	}

	/**
	 * When finishes
	 */
	private void endBeating () {
		// Stop beating
		Log.i(TAG, "Beating ended.");
		isBeating = false;
		mediaPlayer.stop();
		mediaPlayer.reset();

		// Optimize counter
		tempoCounter.end();
		tempoCounter.optimize();
		Log.i(TAG, "Beating optimized.");

		// Compute the tempo information
//		Map<File, ClipProperty> musicMap = dataHolder.getMusicMap();
//		ClipProperty cp = musicMap.get(curMusic);
//		String hash = cp.getHash();
		dataHolder.updateTempoInfoMap(curMusic, tempoCounter.getTempoInfo());
		Toast.makeText(getApplicationContext(), "Tempo detected: "+tempoCounter.getTempoInfo().getTempo(), Toast.LENGTH_SHORT).show();
		tempoPromptTextView.setText("Tempo detected: " + tempoCounter.getTempoInfo().getTempo());
		Log.i(TAG, "tempoInfoMap: " + dataHolder.getTempoInfoMap().toString());

		// null the idle time
		idleTimer = null;

		// Return to previous activity
		finish();
	}






	/**
	 * Tapping callback
	 */
	private void onTapped () {
		Log.i(TAG, "Tapped.");

		if (isBeating) {
			beat();
		} else if (!isBeatingStarting) {
			isBeatingStarting = true;

			// Begin counting (minus count)
			CountDownTimer timer = new CountDownTimer(3000, 950) {
				Toast mToast = Toast.makeText( getApplicationContext()  , "" , Toast.LENGTH_SHORT );
				public void onTick(long millisUntilFinished) {
					//mToast.setText("" + (millisUntilFinished+100) / 1000);
					//mToast.show();
					tempoPromptTextView.setText("" + (millisUntilFinished+100) / 1000);
					Log.i(TAG, "Countdown: " + (millisUntilFinished+100) / 1000);
				}

				public void onFinish() {
					//mToast.setText("GO!");
					//mToast.show();
					Log.i(TAG, "TempoCounter started!");
					startBeating();
					isBeatingStarting = false;
				}
			}.start();
		}
	}




	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tempo);
	}


	@Override
	protected void onStart() {
		Log.i(TAG, "onStart");
		super.onStart();
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "onResume");
		super.onResume();

		// Initialize
		initViewList();
		dataHolder = DataHolder.getInstance();

		// Retrive the music to be tempoed
		curMusic = dataHolder.getCurMusicTempoTask();
		musicInfoTextView.setText(curMusic.getName());
		//tempoPromptTextView.setText(getString(R.string.tempoWaitPromptTextViewText));
		tempoPromptTextView.setText(getString(R.string.tempoEmptyPromptTextViewText));

//		// Begin counting (minus count)
//		//Toast.makeText(getApplicationContext(), "Be ready for the music!", Toast.LENGTH_SHORT).show();
//		CountDownTimer timer = new CountDownTimer((startCountDown+1) * startCountDownBase, startCountDownBase) {
//			Toast mToast = Toast.makeText( getApplicationContext()  , "" , Toast.LENGTH_SHORT );
//			public void onTick(long millisUntilFinished) {
//				mToast.setText("" + millisUntilFinished / 1000);
//				mToast.show();
//				Log.i(TAG, "Countdown: " + millisUntilFinished / 1000);
//			}
//
//			public void onFinish() {
//				mToast.setText("GO!");
//				mToast.show();
//				Log.i(TAG, "TempoCounter started!");
//				startBeating();
//			}
//		}.start();
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// User pressed the screen
		switch(MotionEventCompat.getActionMasked(event)) {
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				// Cancel is when the current gesture is cancelled
				onTapped();	// Call onTapped()
				break;
			default :
				return super.onTouchEvent(event);
		}
		return false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "onPause");

		// Stop counting / playing
		tempoCounter.clear();
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}

		// Stop the idle counter
		if (idleTimer != null) {
			idleTimer.cancel();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.i(TAG, "onRestart");
	}







	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "onCreateOptionsMenu");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_tempo, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "onOptionsItemSelected");
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
}
