package edu.asu.fall15.cse535.group42.temponinja;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Debug;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by royster on 11/4/15.
 */
public class CadenceDetectorAbsAcc extends CadenceDetector
{
	private final static String TAG = "CadenceDetectorAbsAcc";

	public CadenceDetectorAbsAcc() {
		super();
		Arrays.fill(magVals, -1);
	}





	/**
	 * Detection algorithm
	 */
	protected static final float 	accPeakThreshold = 15;
	protected static final float 	accValleyThreshold = 10;
	protected static final int 		accDataPointsThreshold = 5;
	private static int 				state = 0;			// 0: initial, 1: finding next peak, 2: finding next valley
	private static double[] 		magVals = new double [500];
	private static int				magValIdx = -1;		// index of last value
	private static int				getPrevMagValIdx (int i, int num) 	{ return (i+magVals.length-num)%magVals.length; }
	private static int				getPrevMagValIdx (int i) 			{ return (i+magVals.length-1)%magVals.length; }
	private static int				getNextMagValIdx (int i, int num) 	{ return (i+num)%magVals.length; }
	private static int				getNextMagValIdx (int i) 			{ return (i+1)%magVals.length; }
	private static int				magValCnt = 0;			// number of magnitude values stored
	private static boolean			isPeaking = false;		// consecutive accDataPointsThreshold values >= accPeakThreshold
	private static boolean			isValleying = false;	// consecutive accDataPointsThreshold values <= accValleyThreshold
	private static boolean			isPeaked = false;		// isPeaking & magDiff < 0
	private static boolean			isValleyed = false;		// isValleying & magDiff > 0
	public void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;
		synchronized (this) {
			if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				double x = event.values[0];
				double y = event.values[1];
				double z = event.values[2];
				double magnitude = Math.sqrt(x * x + y * y + z * z);
				double magDiff = 0;

				// Save the magnitude
				magValIdx = getNextMagValIdx(magValIdx);
				magVals[magValIdx] = magnitude;
				if (magValCnt < magVals.length) { magValCnt++; }
				if (magValCnt > 1) { magDiff = magnitude - magVals[ getPrevMagValIdx( magValIdx ) ]; }

				DebugLogger.log("", "" + magnitude + "\t" + y + "\t" + System.currentTimeMillis());

				// Find about whether the data is peaking or valleying
				isPeaking = true; isValleying = true;
				if (magValCnt >= accDataPointsThreshold) {
					for (int i = magValIdx; i != getPrevMagValIdx(magValIdx, accDataPointsThreshold); i = getPrevMagValIdx(i)) {
						if (magVals[i] < accPeakThreshold) {
							isPeaking = false;
						}
						if (magVals[i] > accValleyThreshold) {
							isValleying = false;
						}
					}
				} else {
					isPeaking = isValleying = false;
				}


				// Whether the peak in this interval has been achieved
				boolean oldIsPeaked = isPeaked, oldIsValleyed = isValleyed;
				isPeaked = (isPeaked || (isPeaking && magDiff < 0));
				isValleyed = (isValleyed || (isValleying && magDiff > 0));


				if (oldIsPeaked != isPeaked) {
					// find peak, initiate message
					for (intCadenceListener cadenceListener : mCadenceListeners) {
						logStep("Peak");
						cadenceListener.onStep();
					}
				} else if (oldIsValleyed != isValleyed) {
					// find valley, initiate message
					for (intCadenceListener cadenceListener : mCadenceListeners) {
						//logStep("Valley");
						//cadenceListener.onStep();
					}
				}


				// Process based on states
				if (state == 0) {
					// initial state
					if (isPeaking) {
						state = 1;
						isPeaked = false;
					} else if (isValleying) {
						state = 2;
						isValleyed = false;
					}
				} else if (state == 1) {
					// finding peak
					if (isValleying) {
						state = 2;
						isValleyed = false;
					}
				} else if (state == 2) {
					// finding valley
					if (isPeaking) {
						state = 1;
						isPeaked = false;
					}
				}
			}
		}
	}

}
