package edu.asu.fall15.cse535.group42.temponinja;

import java.util.ArrayList;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Debug;
import android.util.Log;

/**
 * Detects steps and notifies all listeners (that implement intCadenceListener).
 * @author Levente Bagi
 * @todo REFACTOR: SensorListener is deprecated
 */
public class CadenceDetector implements SensorEventListener
{
	private final static String TAG = "CadenceDetector";
	private float   mLimit = 10;
	private float   mLastValues[] = new float[3*2];
	private float   mScale[] = new float[2];
	private float   mYOffset;

	private float   mLastDirections[] = new float[3*2];
	private float   mLastExtremes[][] = { new float[3*2], new float[3*2] };
	private float   mLastDiff[] = new float[3*2];
	private int     mLastMatch = -1;

	protected ArrayList<intCadenceListener> mCadenceListeners = new ArrayList<intCadenceListener>();


	public void clear() {
		// TODO: clear everything and restart
		// for child
	}


	public CadenceDetector() {
		int h = 480; // TODO: remove this constant
		mYOffset = h * 0.5f;
		mScale[0] = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
		mScale[1] = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
	}

	public void setSensitivity(float sensitivity) {
		mLimit = sensitivity; // 1.97  2.96  4.44  6.66  10.00  15.00  22.50  33.75  50.62
	}

	public void addCadenceListener(intCadenceListener sl) {
		mCadenceListeners.add(sl);
	}

	//public void onSensorChanged(int sensor, float[] values) {
	public void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;
		synchronized (this) {
			if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
			}
			else {
				int j = (sensor.getType() == Sensor.TYPE_ACCELEROMETER) ? 1 : 0;
				if (j == 1) {
					float vSum = 0;
					for (int i=0 ; i<3 ; i++) {
						final float v = mYOffset + event.values[i] * mScale[j];		// DsRoyster: Here's where the event values are read
						vSum += v;
						//Log.i(TAG, "Accelerometer: (" + event.values[0] + ", " + event.values[1] + ", " + event.values[2] + ")");
						//DebugLogger.log("Accelerometer", "(" + event.values[0] + ", " + event.values[1] + ", " + event.values[2] + ")");
					}
					int k = 0;
					float v = vSum / 3;

					float direction = (v > mLastValues[k] ? 1 : (v < mLastValues[k] ? -1 : 0));
					if (direction == - mLastDirections[k]) {
						// Direction changed
						int extType = (direction > 0 ? 0 : 1); // minumum or maximum?
						mLastExtremes[extType][k] = mLastValues[k];
						float diff = Math.abs(mLastExtremes[extType][k] - mLastExtremes[1 - extType][k]);

						if (diff > mLimit) {

							boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k]*2/3);
							boolean isPreviousLargeEnough = mLastDiff[k] > (diff/3);
							boolean isNotContra = (mLastMatch != 1 - extType);

							if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
								logStep ();	// log step
								for (intCadenceListener cadenceListener : mCadenceListeners) {
									cadenceListener.onStep();
								}
								mLastMatch = extType;
							}
							else {
								mLastMatch = -1;
							}
						}
						mLastDiff[k] = diff;
					}
					mLastDirections[k] = direction;
					mLastValues[k] = v;
				}
			}
		}
	}


	protected void logStep () {
		Log.i(TAG, "Step: " + System.currentTimeMillis());
		DebugLogger.log(TAG, "Step: " + System.currentTimeMillis());
	}
	protected void logStep (String cmt) {
		Log.i(TAG, "Step (" + cmt + "): " + System.currentTimeMillis());
		DebugLogger.log(TAG, "Step (" + cmt + "): " + System.currentTimeMillis());
	}



	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}






	// Methods for all possible CadenceDetector classes



	/**
	 * Uses a lowPass filter on the accelerometer data
	 */
	protected static final float ALPHA = 0.25f;
	protected static float[] accVals;
	protected static float[] lowPass( float[] input, float[] output ) {
		if ( output == null ) return input;
		for ( int i=0; i<input.length; i++ ) {
			output[i] = (1 - ALPHA) * output[i] + ALPHA * (input[i] - output[i]);
		}
		return output;
	}

}
