package edu.asu.fall15.cse535.group42.temponinja;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;



public class MainActivity extends AppCompatActivity {

	// TAG information
	private static final String TAG = "MainActivity";




	/////////////////////////////////////
	// State methods

	/**
	 * True, when current state is running (Run button pressed).
	 */
	private boolean mIsRunning = false;

	/**
	 * True, when service is running.
	 */
	private boolean mIsServiceOn = false;

	/**
	 * Current step value. Not used now.
	 */
	private long 	curSteps = 0;

	/**
	 * Current cadence value.
	 */
	private double curMusicTempo = 0;

	/**
	 * Current cadence value.
	 */
	private double curMusicCadence = 0;

	/**
	 * Current cadence value.
	 */
	private double curCadence = 0;

	/**
	 * Current playing music
	 */
	private File 	curMusicFile = null;

//	/**
//	 * Current playing music
//	 */
//	private int 	curMusicIdx = -1;

	/**
	 * Current step
	 */
	private double	curStep = 0;	// time of the current step




	/////////////////////////////////////
	// Music related variables

	// View Items
	ListView	playlistListView;
	Button		playButton;
	Button		prevButton;
	Button		nextButton;
	TextView	cadenceValTextView;
	TextView	tempoValTextView;
	protected void initViewList () {
		Log.i(TAG, "initViewList");

		// Obtain list of views
		playlistListView 	= (ListView) findViewById(R.id.playlistListView);
		playButton 			= (Button) findViewById(R.id.playButton);
		prevButton 			= (Button) findViewById(R.id.prevButton);
		nextButton 			= (Button) findViewById(R.id.nextButton);
		cadenceValTextView 	= (TextView) findViewById(R.id.cadenceValTextView);
		tempoValTextView 	= (TextView) findViewById(R.id.tempoValTextView);
	}
	List<File>	playlistFiles;				// use the same indexes as in the playlistListView
	PlayListAdapter 	adp = null;
	boolean 	isPlaying = false;

	// Tempo info
	private static final String tempoFilename = "tempos.xml";	// tempo information file name
	private File 				tempoFile;						// tempo information file
	Map<String, TempoInfo>		tempoInfoMap;					// map string (from ClipProperty.getHash()) to its tempo information
	//public static final long [] tempoGroups = {90, 95, 100, 105, 110, 115, 120, 130, 140, 150, 155, 160, 165, 170, 175, 180, 185, 190, 195, 200, 210, 220, 230, 1000};  // Group tempos into groups
	public static final long [] tempoGroups = {90, 100, 110, 120, 130, 140, 150, 160, 170, 180, 190, 200, 210, 220, 230, 1000};  // Group tempos into groups
	List< List >				tempoGroupMusicList;			// list of music in each tempo group
	/**
	 * Find the index of tempo group that a tempo belongs to.
	 * @param tempo
	 * @return index of the tempo group (lower bounding tempo) in tempoGroups
	 */
	public static int 			findTempoGroup (long tempo) {
		int idx = 0;
		while (idx < tempoGroups.length && tempoGroups[idx] < tempo) {
			idx++;
		}
		long tempoGroup = tempoGroups[idx-1];
		return idx;
	}

	// Music list
//	List<File>					musicList;				// list of all music files	// making it local
	Map<File, ClipProperty>		musicMap;				// map music file to ClipProperty
//	List<File>					newMusicList;			// list of music not in musicMap (not having property)

	// These data are shared through DataHolder
	DataHolder					dataHolder;
	private void 				setTempoTask (File musicFile) {
		dataHolder.setCurMusicTempoTask(musicFile);
	}
	private void 				shareData () {
		dataHolder.setMusicMap(musicMap);
		dataHolder.setTempoInfoMap(tempoInfoMap);
		dataHolder.setTempoGroupMusicList(tempoGroupMusicList);
	}
	private void 				getData () {
		musicMap = dataHolder.getMusicMap();
		tempoInfoMap = dataHolder.getTempoInfoMap();
		tempoGroupMusicList = dataHolder.getTempoGroupMusicList();
	}









	/////////////////////////////////////
	// Display methods


	private void displayCadence () {
		cadenceValTextView.setText("" + curCadence);
	}
	private void displayTempo () {
		ClipProperty clipProperty = musicMap.get(curMusicFile);
		TempoInfo tempoInfo = tempoInfoMap.get(clipProperty.getHash());
		tempoValTextView.setText("" + tempoInfo.getTempo());
	}
	private void displayTempo (double tempo) {
		tempoValTextView.setText(""+(int)tempo);
	}





	/////////////////////////////////////
	// Event methods




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		// Initialization
		Log.i(TAG, "onStart -> initialize view and music lists");
		initViewList();
		dataHolder = DataHolder.getInstance();	// Get the data holder
		dataHolder.init();
		getData();
		playlistFiles = new ArrayList<>();

		// Also initialize DebugLogger here (without this call, the logger won't initialize)
		DebugLogger debugLogger = DebugLogger.getInstance();

//		// Start service if mIsRunning (Run button pressed)
//		if (mIsRunning) {
//			if (!mIsServiceOn) {
//				startCadenceService();
//				bindCadenceService();
//			} else {
//				bindCadenceService();
//			}
//			displayCadence();
//		}

		// Setup buttons
		playButton.setOnClickListener(new View.OnClickListener() {
			// Start running
			public void onClick(View v) {
				if (!mIsRunning) {
					startRunning();

					playlistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						public void onItemClick(AdapterView parentView, View childView,
												int position, long id) {
							Toast.makeText(getApplicationContext(), "Tempo Ninjutsu is available only when not running...", Toast.LENGTH_SHORT).show();
						}

						public void onNothingSelected(AdapterView parentView) {

						}
					});
				} else {
					stopRunning();

					playlistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						public void onItemClick(AdapterView parentView, View childView,
												int position, long id) {
							String item = (String) playlistListView.getItemAtPosition(position);
							Log.i(TAG, "Item \"" + item + "\" clicked.");

							// Start tempo activity
							File f = playlistFiles.get(position);
							setTempoTask(f);    // share the tempo task to tempo activity
							Intent intent = new Intent(getApplicationContext(), TempoActivity.class);
							startActivity(intent);
						}

						public void onNothingSelected(AdapterView parentView) {

						}
					});
				}
			}
		});
		nextButton.setOnClickListener(new View.OnClickListener() {
			// Next music
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "Next song...", Toast.LENGTH_SHORT).show();
				initMusicSwitch(System.currentTimeMillis(), curCadence);
			}
		});

		// ListView click
		playlistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView parentView, View childView,
									int position, long id) {
				String item = (String) playlistListView.getItemAtPosition(position);
				Log.i(TAG, "Item \"" + item + "\" clicked.");

				// Start tempo activity
				File f = playlistFiles.get(position);
				setTempoTask(f);	// share the tempo task to tempo activity
				Intent intent = new Intent(getApplicationContext(), TempoActivity.class);
				startActivity(intent);
			}

			public void onNothingSelected(AdapterView parentView) {

			}
		});
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

		// Initialize music status
		isPlaying = false;

		// Update data from holder
		dataHolder.update();
		getData();
//		Map<String, TempoInfo> tempoInfoMap1 = dataHolder.getTempoInfoMap();
//		tempoInfoMap.putAll(tempoInfoMap1);
//		Log.i(TAG, "tempoInfoMap: " + dataHolder.getTempoInfoMap().toString());

		// Update music list view
		Log.i(TAG, "onResume -> update playlist");
		File[] musicList = musicMap.keySet().toArray( new File [ musicMap.keySet().size() ] );
		Arrays.sort(musicList);
		String[] musicFiles = new String [ musicMap.keySet().size() ];
		playlistFiles.clear();
		for (int i = 0; i < musicList.length; i++) {
			musicFiles[i] = musicList[i].getName().toString().replace(".mp3", "");
			String hash = musicMap.get(musicList[i]).getHash();
			if (!tempoInfoMap.containsKey(hash)) {
				musicFiles[i] = "[NEW] " + musicFiles[i];
			} else {
				musicFiles[i] = "[" + String.format("%.0f", tempoInfoMap.get(hash).getTempo()) + "] " + musicFiles[i];
			}
			playlistFiles.add(musicList[i]);
		}
		//ArrayAdapter<String> adp = new ArrayAdapter<String>(getApplicationContext(), R.layout.playlist_textstyle, musicFiles);
		adp = new PlayListAdapter(getApplicationContext(), R.layout.playlist_textstyle, musicFiles);
		playlistListView.setAdapter(adp);

		// Share data
		//shareData();
	}


	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "onPause");

		stopRunning();
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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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





	/////////////////////////////////////
	// Message handling

	private void startRunning () {
		Log.i(TAG, "startRunning");
		if (!mIsRunning) {
			startCadenceService();
			bindCadenceService();
			mIsRunning = true;
			displayCadence();
			playButton.setText("Stop");
		}
	}
	private void stopRunning () {
		Log.i(TAG, "stopRunning");
		if (mIsRunning) {
			playButton.setText("Run");
			cadenceValTextView.setText(R.string.invalidValText);
			tempoValTextView.setText(R.string.invalidValText);
			mIsRunning = false;
			unbindCadenceService();
			stopCadenceService();

			// Stop playing
			tempoPlayer.stop();

			// If playing, stop the playing and reset music highlight
			if (isPlaying) {
				isPlaying = false;

				// Find music item
				int prevIdx = -1;
				for (int i = 0; i < playlistFiles.size(); i++) {
					if (playlistFiles.get(i) == curMusicFile) {
						prevIdx = i;
						break;
					}
				}
				Log.i(TAG, curMusicFile + " " + prevIdx);
				if (prevIdx >= 0) {
					TextView prevSelectedView = (TextView) getViewByPosition(prevIdx, playlistListView);
					if (prevSelectedView != null) {
						prevSelectedView.setTextColor(Color.BLACK);
					}
				}
			}

			// Reset playing
			curMusicTempo = 0;
			curMusicCadence = 0;
			curMusicFile = null;
		}
	}





	private CadenceService mService;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = ((CadenceService.StepBinder)service).getService();

			mService.registerCallback(mCallback);
			//mService.reloadSettings();

		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};

	private void startCadenceService() {
		if (! mIsServiceOn) {
			Log.i(TAG, "[SERVICE] Start");
			mIsServiceOn = true;
			startService(new Intent(MainActivity.this,
					CadenceService.class));
		}
	}

	private void bindCadenceService() {
		Log.i(TAG, "[SERVICE] Bind");
		bindService(new Intent(MainActivity.this,
				CadenceService.class), mConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
	}

	private void unbindCadenceService() {
		Log.i(TAG, "[SERVICE] Unbind");
		unbindService(mConnection);
	}

	private void stopCadenceService() {
		Log.i(TAG, "[SERVICE] Stop");
		if (mService != null) {
			Log.i(TAG, "[SERVICE] stopService");
			stopService(new Intent(MainActivity.this,
					CadenceService.class));
		}
		mIsServiceOn = false;
	}








	// TODO: unite all into 1 type of message
	private CadenceService.ICallback mCallback = new CadenceService.ICallback() {
		public void stepsChanged(int value) {
			mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));
		}
		public void cadenceChanged(int value) {
			mHandler.sendMessage(mHandler.obtainMessage(CADENCE_MSG, value, 0));
		}
	};

	private static final int STEPS_MSG = 1;
	private static final int CADENCE_MSG = 2;

	private Handler mHandler = new Handler() {
		@Override public void handleMessage(Message msg) {
			switch (msg.what) {
				case STEPS_MSG:
					curSteps = (int)msg.arg1;
					//mStepValueView.setText("" + mStepValue);
					break;
				case CADENCE_MSG:
					double cadence = msg.arg1;
					if (cadence <= 0) {
						cadenceValTextView.setText("0");
					} else {
						cadenceValTextView.setText("" + (int)cadence);
					}
					curCadence = cadence;
					onCadenceChange(System.currentTimeMillis(), cadence);	// this has to be prior than assigning the new cadence
					break;
				default:
					super.handleMessage(msg);
			}
		}

	};



	// Past played music history
	protected static final int	MIN_MUSIC_INTERVAL = 5;					// no music can be played within this number of plays, unless no other music is available
	FileBuffer					musicHistory = new FileBuffer (100);
	DoubleBuffer				musicTimes = new DoubleBuffer (100);
	DoubleBuffer				musicCadences = new DoubleBuffer (100);



	// Music playing status
	TempoPlayer 				tempoPlayer = new TempoPlayer (this);



	// On cadence change, pass value to the music player
	private static final long 	MIN_SWITCH_TIME_INTERVAL = 10000;	// ms
	private long 				prevSwitchTime = 0;
	private static final long 	MIN_SWITCH_TEMPO = 10;	// minimum cadence change to initiate a switch
	private static final long 	MIN_SWITCH_CADENCE = 7;	// minimum cadence change to initiate a switch
	private static final long 	MIN_CADENCE_THRESHOLD = 60; //tempoGroups[0];	// minimum cadence that uses cadence specific information
	private int					changeTime = 0;
	private static final int 	CHANGE_TIME_TO_CHANGE = 4;

	private void onCadenceChange (double thisTime, double cadence) {
		Log.i(TAG, "onCadenceChange: " + cadence);
		curStep = thisTime;
		double prevTempo = curMusicTempo;
		double prevCadence = curMusicCadence;
		double nextStepTime = thisTime + 60.0 * 1000 / cadence;


		Log.i(TAG, "TimeDiff: " + (thisTime - prevSwitchTime) + "  CadDiff: " + Math.abs(cadence - prevCadence));
		DebugLogger.log(TAG, "TimeDiff: " + (thisTime - prevSwitchTime) + "  CadDiff: " + Math.abs(cadence - prevCadence) + "  TempoDiff: " + Math.abs(cadence - prevTempo));
		if (calcTempoDiff(prevTempo, cadence) < MIN_SWITCH_TEMPO || Math.abs(cadence - prevCadence) < MIN_SWITCH_CADENCE) {
			// Switch the music only when both tempo and cadence difference is large enough
			changeTime = 0;
			return;
		}
		if (isPlaying && tempoPlayer.isPlaying) {
			if (cadence < MIN_CADENCE_THRESHOLD) {
				changeTime = 0;
				return;	// if currently playing and tempo drops below setting, do not change
			}
		}
		changeTime ++;
		if (thisTime - prevSwitchTime < MIN_SWITCH_TIME_INTERVAL) return;

		if (!isPlaying || changeTime > CHANGE_TIME_TO_CHANGE) {
			initMusicSwitch(nextStepTime, cadence);
		}
	}


	private double calcTempoDiff (double tempo, double cadence) {
		cadence *= 2;	// make it >= 100
		double diff = Math.abs(tempo - cadence), diff2 = Math.abs(2*tempo-cadence), diff3 = Math.abs(3*tempo-cadence), diff4 = Math.abs(4*tempo-cadence), diff5 = Math.abs(5*tempo-cadence);
		diff = (diff >= diff2) ? diff2 : diff;
		diff = (diff >= diff3) ? diff3 : diff;
		diff = (diff >= diff4) ? diff4 : diff;
		diff = (diff >= diff5) ? diff5 : diff;
		return diff;
	}


	// This initiates the music switch
	protected boolean		isFinished = false;
	private void initMusicSwitch (double nextStepTime, double cadence) {
		Log.i(TAG, "initMusicSwitch");
		File musicPick = null;

		if (cadence >= MIN_CADENCE_THRESHOLD) {
			// If the cadence is above the minimum threshold, use adjustable music playing
			Log.i(TAG, "initMusicSwitch - find music for cadence: " + cadence);

			List<File> musicList = new ArrayList<>();
			List<Double> tempoDiffList = new ArrayList<>();
			for (File music : dataHolder.getMusicMap().keySet()) {
				ClipProperty cp = dataHolder.getMusicMap().get(music);
				if (!dataHolder.getTempoInfoMap().containsKey(cp.getHash())) continue;
				TempoInfo tempoInfo = dataHolder.getTempoInfoMap().get(cp.getHash());
				//double tempoDiff = Math.abs(tempoInfo.tempo - cadence);
				double tempoDiff = calcTempoDiff(tempoInfo.tempo, cadence);

				// If more than threshold, remove
				if (tempoDiff > MIN_SWITCH_TEMPO) {
					continue;
				}

				// Sort all music by difference to cadence
				boolean added = false;
				for (int i = 0; i < musicList.size(); i++) {
					if (tempoDiff > tempoDiffList.get(i)) {
						musicList.add(i, music);
						tempoDiffList.add(i, tempoDiff);
						added = true;
						break;
					}
				}
				if (!added) {
					musicList.add(music);
					tempoDiffList.add(tempoDiff);
				}
			}

			if (musicList.isEmpty()) {
				Log.i(TAG, "[X] No music file matching this cadence (" + cadence + ") found.");
				DebugLogger.log(TAG, "[X] No music file matching this cadence (" + cadence + ") found.");
				return;
			}

			// Find a music to play
			Log.i(TAG, "musicList: " + musicList.toString());
			Log.i(TAG, "musicHistory: " + musicHistory.toString());
			for (File music : musicList) {
				if (musicHistory.findDiffFromRecent(music) >= MIN_MUSIC_INTERVAL || musicHistory.findDiffFromRecent(music) < 0) {
					musicPick = music;
					break;
				}
			}
			if (musicPick == null) {    // if not that many music
				musicPick = findFarthestMusic(musicList, musicHistory);
			}
		} else {
			// If the cadence is too slow, just play any music
			Log.i(TAG, "initMusicSwitch - find general music. Current cadence:  " + cadence);

			List<File> musicList = new ArrayList<>();
			musicList.addAll(musicMap.keySet());
			Collections.sort(musicList);
			musicPick = findFarthestMusic(musicList, musicHistory);
		}

		// Switch to this pick from previous music
		if (musicPick != null) {
			if (musicPick != curMusicFile || isFinished) {
				isFinished = false;
				switchMusic(musicPick, nextStepTime, cadence);
			}
		} else {
			Log.i(TAG, "[X] No music file picked.");
			DebugLogger.log(TAG, "[X] No music file picked.");
		}
	}


	// This is where you should call the media player
	private void switchMusic(File music, double time, double cadence) {
		Log.i(TAG, "switchMusic: " + music.getName());
		ClipProperty cp = dataHolder.getMusicMap().get(music);
		TempoInfo tempoInfo = null;
		if (dataHolder.getTempoInfoMap().containsKey(cp.getHash())) {
			tempoInfo = dataHolder.getTempoInfoMap().get(cp.getHash());
		} else {
			tempoInfo = new TempoInfo ();
		}
		boolean playStatus = tempoPlayer.switchPlay(music, tempoInfo.getAnchor(), tempoInfo.getTempo(), time, cadence);
		Log.i(TAG, "playStatus: " + playStatus);

		// Update playing status
		if (playStatus) {
			isPlaying = true;
			// Save the play history, and update switch time
			prevSwitchTime = System.currentTimeMillis();
			File prevMusic = curMusicFile;
			curMusicFile = music;

			// Record this switch
			musicHistory.put(music);
			musicTimes.put(prevSwitchTime);
			musicCadences.put(cadence);
			//curMusicTempo = cadence;
			curMusicTempo = tempoInfo.tempo;
			curMusicCadence = cadence;
			DebugLogger.log(TAG, "tempo/cadence: " + curMusicTempo + "/" + curMusicCadence);
			displayTempo(curMusicTempo);

			// Update the view
			int idx = -1, prevIdx = -1;
			for (int i = 0; i < playlistFiles.size(); i++) {
				if (playlistFiles.get(i) == music) {
					idx = i;
				}
				if (playlistFiles.get(i) == prevMusic) {
					prevIdx = i;
				}
				if (idx >= 0 && prevIdx >= 0) {
					break;
				}
			}
			if (idx < 0) {
				Log.i(TAG, "[X] Music not in the playlist!");
				DebugLogger.log(TAG, "[X] Music not in the playlist!");
			} else {
//				TextView selectedView = (TextView) getViewByPosition(idx, playlistListView);
//				if (selectedView != null) {
//					selectedView.setTextColor(Color.RED);
//				} else {
//					Log.i(TAG, "[X] View not found in Listview!");
//					DebugLogger.log(TAG, "[X] View not found in Listview!");
//				}
//
//				if (prevIdx >= 0) {
//					TextView prevSelectedView = (TextView) getViewByPosition(prevIdx, playlistListView);
//					if (prevSelectedView != null) {
//						prevSelectedView.setTextColor(Color.BLACK);
//					}
//				}
				adp.setSelectedItem(idx);
				playlistListView.invalidateViews();
			}
		}
	}

	// Get the view at position pos of the listView
	public static View getViewByPosition(int pos, ListView listView) {
		final int firstListItemPosition = listView.getFirstVisiblePosition();
		final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

		if (pos < firstListItemPosition || pos > lastListItemPosition ) {
			return listView.getAdapter().getView(pos, null, listView);
		} else {
			final int childIndex = pos - firstListItemPosition;
			return listView.getChildAt(childIndex);
		}
	}

	// This is to select the music based on lists
	private static File findFarthestMusic (List<File> musicList, FileBuffer musicHistory) {
		Log.i(TAG, "findFarthestMusic");
		File musicPick = null; int musicIdx = 0;
		for (File music : musicList) {
			if (musicHistory.findFromRecent(music) < 0) {
				musicPick = music;
				break;
			}
			if (musicHistory.findDiffFromRecent(music) >= musicIdx) {
				musicIdx = musicHistory.findDiffFromRecent(music);
				musicPick = music;
			}
		}
		return musicPick;
	}




	public void afterMusicFinish () {
		Log.i(TAG, "afterMusicFinish with cadence " + curMusicTempo);
		isPlaying = false;
		isFinished = true;
		initMusicSwitch(curStep, curMusicTempo);
	}








}
