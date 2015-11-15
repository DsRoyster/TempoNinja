package edu.asu.fall15.cse535.group42.temponinja;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class CadenceService extends Service {

	// TAG information
	private static final String TAG = "CadenceService";

	// Other variables
	private SharedPreferences mSettings;
	private SharedPreferences mState;
	private SharedPreferences.Editor mStateEditor;
	private SensorManager mSensorManager;
	private Sensor mSensor;
	private CadenceDetector mCadenceDetector;
	// private StepBuzzer mStepBuzzer; // used for debugging
	private CadenceNotifier mCadenceNotifier;

	private PowerManager.WakeLock wakeLock;
	//private NotificationManager mNM;

	private double mCadence;


	// which detector to use
	private static final int		DETECTOR_TYPE = 3;		// 0: CadenceDetector, 1: CadenceDetectorBuiltin, 2: CadenceDetectorAbsAcc, 3: CadenceDetectorAbsY
	// which notifier to use
	private static final int		NOTIFIER_TYPE = 2;		// 0: CadenceNotifier, 1: CadenceNotifierAdv, 2: CadenceNotifierAdv2


	public CadenceService() {

	}

	/**
	 * Class for clients to access.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with
	 * IPC.
	 */
	public class StepBinder extends Binder {
		CadenceService getService() {
			return CadenceService.this;
		}
	}


	@Override
	public void onCreate() {
		Log.i(TAG, "[SERVICE] onCreate");
		super.onCreate();

//		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//		showNotification();

		// Load settings
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		mState = getSharedPreferences("state", 0);

		acquireWakeLock();

		// Start detecting
		switch (DETECTOR_TYPE) {
			default:
			case 0:
				mCadenceDetector = new CadenceDetector ();
				break;
			case 1:
				mCadenceDetector = new CadenceDetectorBuiltin ();
				break;
			case 2:
				mCadenceDetector = new CadenceDetectorAbsAcc();
				break;
			case 3:
				mCadenceDetector = new CadenceDetectorAbsY(getApplicationContext());
				break;
		}
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		registerDetector();

		// Register our receiver for the ACTION_SCREEN_OFF action. This will make our receiver
		// code be called whenever the phone enters standby mode.
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mReceiver, filter);

		switch (NOTIFIER_TYPE) {
			default:
			case 0:
				mCadenceNotifier = new CadenceNotifier();
				break;
			case 1:
				mCadenceNotifier = new CadenceNotifierAdv();
				break;
			case 2:
				mCadenceNotifier = new CadenceNotifierAdv2();
				break;
		}

		//mCadenceNotifier.setPace(mCadence = mState.getInt("pace", 0));
		mCadenceNotifier.addListener(mCadenceListener);
		mCadenceDetector.addCadenceListener(mCadenceNotifier);

		// Used when debugging:
		// mStepBuzzer = new StepBuzzer(this);
		// mCadenceDetector.addCadenceListener(mStepBuzzer);

		// Tell the user we started.
		Toast.makeText(this, getText(R.string.started), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(TAG, "[SERVICE] onStart");
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "[SERVICE] onDestroy");

		// Unregister our receiver.
		unregisterReceiver(mReceiver);
		unregisterDetector();

		mStateEditor = mState.edit();
		mStateEditor.putInt("cadence", (int) mCadence);
		mStateEditor.commit();

		//mNM.cancel(R.string.app_name);

		wakeLock.release();

		super.onDestroy();

		// Stop detecting
		mSensorManager.unregisterListener(mCadenceDetector);

		// Tell the user we stopped.
		Toast.makeText(this, getText(R.string.stopped), Toast.LENGTH_SHORT).show();
	}

	private void registerDetector() {
		switch (DETECTOR_TYPE) {
			default:
			case 3:
			case 2:
			case 0:
				mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				break;
			case 1:
				mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
				break;
		}
		mSensorManager.registerListener(mCadenceDetector,
				mSensor,
				//SensorManager.SENSOR_DELAY_GAME);
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	private void unregisterDetector() {
		mSensorManager.unregisterListener(mCadenceDetector);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "[SERVICE] onBind");

		DebugLogger.getInstance().init();
		if (mCadenceDetector != null) {
			mCadenceDetector.clear();
		}
		if (mCadenceNotifier != null) {
			mCadenceNotifier.clear();
		}

		return mBinder;
	}

	/**
	 * Receives messages from activity.
	 */
	private final IBinder mBinder = new StepBinder();

	public interface ICallback {
		public void cadenceChanged(int value);
	}

	private ICallback mCallback;

	public void registerCallback(ICallback cb) {
		mCallback = cb;
		//mPaceListener.passValue();
	}

	private int mDesiredPace;


	/**
	 * Forwards pace values from PaceNotifier to the activity.
	 */
	private CadenceNotifier.Listener mCadenceListener = new CadenceNotifier.Listener() {
		public void cadenceChanged(double value) {
			mCadence = value;
			passValue();
		}
		public void passValue() {
			if (mCallback != null) {
				mCallback.cadenceChanged((int) mCadence);
			}
		}
	};

//	/**
//	 * Show a notification while this service is running.
//	 */
//	private void showNotification() {
//		CharSequence text = getText(R.string.app_name);
//		Intent pedometerIntent = new Intent();
//		pedometerIntent.setComponent(new ComponentName(this, MainActivity.class));
//		pedometerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//				pedometerIntent, 0);
//
//		Notification notification = new Notification.Builder( this )		// Modified according to stackoverflow
//				.setContentText(null)
//				//.setSmallIcon(R.drawable.ic_notification)
//				.setWhen(System.currentTimeMillis())
//				.setContentTitle(text)
//				.setContentText(getString(R.string.notification_subtitle))
//				.setContentIntent(contentIntent)
//				.build();
//		notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
//		//notification.setLatestEventInfo(this, text,						// deprecated, using Notification.Builder
//		//		getText(R.string.notification_subtitle), contentIntent);
//
//		mNM.notify(R.string.app_name, notification);
//	}


	// BroadcastReceiver for handling ACTION_SCREEN_OFF.
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Check action just to be on the safe side.
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				// Unregisters the listener and registers it again.
				CadenceService.this.unregisterDetector();
				CadenceService.this.registerDetector();
//				if (mPedometerSettings.wakeAggressively()) {
//					wakeLock.release();
//					acquireWakeLock();
//				}
			}
		}
	};

	private void acquireWakeLock() {
		Log.i(TAG, "acquireWakeLock");
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		int wakeFlags;
//		if (mPedometerSettings.wakeAggressively()) {
//			wakeFlags = PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP;
//		}
//		else if (mPedometerSettings.keepScreenOn()) {
//			wakeFlags = PowerManager.SCREEN_DIM_WAKE_LOCK;
//		}
//		else {
		wakeFlags = PowerManager.PARTIAL_WAKE_LOCK;
//		}
		wakeLock = pm.newWakeLock(wakeFlags, TAG);
		wakeLock.acquire();
		Log.i(TAG, "acquireWakeLock succeeded");
	}





}
