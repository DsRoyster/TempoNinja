package edu.asu.fall15.cse535.group42.temponinja;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

import java.util.Arrays;

/**
 * Created by royster on 11/5/15.
 */
public class CadenceDetectorRelAcc extends CadenceDetector
{
	private final static String TAG = "CadenceDetectorRelAcc";

	public CadenceDetectorRelAcc() {
		super();
	}


	/**
	 * Algorithm in here:
	 * 		Continuously record the max several peaks and min several valleys in the previous, as well as their timestamps.
	 * 		These peaks / valleys must be within a small time interval (based on the Hz of walking/running (>= 30 && <= 240))
	 * 		To do these, we have several settings:
	 * 			1. BasicPeakThreshold: any peak <= 12 is not considered
	 * 			2. DynamicPeakThreshold: average peak height among the past PeakVals.length peaks
	 * 			3. We need to count the dynamic peaks as real peaks
	 * 				1). More than basic
	 * 				2). More than dynamic
	 * 				3). First such in the interval (between two real valleys)
	 * 				4). Is a (sharp turn)
	 */





	protected static final double 	accBasicPeakThreshold = 13;
	protected static final double 	accBasicValleyThreshold = 8;

	protected static final int		accDynamicThresholdCount = 5;	// use the five most recent peaks/valleys to compute the dynamic threshold
	protected static double			accDynamicPeakThreshold = 0;
	protected static double			accDynamicValleyThreshold = 1000;

	protected static boolean		isPeaking = false;		// most recent value >= accBasicPeakThreshold
	protected static boolean		isValleying = false;	// most recent value <= accBasicValleyThreshold
	protected static boolean		isPeaked = false;		// isPeaking && there has been value recognized as peak
	protected static boolean		isValleyed = false;		// isValleying && there has been value recognized as valley

	protected static DoubleBuffer	magnitudes = new DoubleBuffer (100);
	protected static DoubleBuffer	peaks = new DoubleBuffer (10);
	protected static DoubleBuffer	peakTimes = new DoubleBuffer (10);
	protected static DoubleBuffer	valleys = new DoubleBuffer (10);
	protected static DoubleBuffer	valleyTimes = new DoubleBuffer (10);

	protected static final int		initDataPoints = 10;


	void calcDynamicPeakThreshold () {
		if (peaks.element_size() < accDynamicThresholdCount) {
			accDynamicPeakThreshold = 0;		// invalid peaking value
		} else {
			accDynamicPeakThreshold = peaks.avrFromRecent(accDynamicThresholdCount);
		}
	}
	void calcDynamicValleyThreshold () {
		if (peaks.element_size() < accDynamicThresholdCount) {
			accDynamicValleyThreshold = 1000;	// invalid valleying value
		} else {
			accDynamicValleyThreshold = valleys.avrFromRecent(accDynamicThresholdCount);
		}
	}



	/**
	 * Detection algorithm
	 */
	public void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;
		synchronized (this) {
			if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				// Extract the magnitude
				double x = event.values[0];
				double y = event.values[1];
				double z = event.values[2];
				double magnitude = Math.sqrt(x * x + y * y + z * z);
				double magDiff = 0;
				magnitudes.put(magnitude);


				// First fill initial buffer
				if (magnitudes.element_size() < initDataPoints) return;


				// Detect current status
				if (magnitude >= accBasicPeakThreshold && magnitude >= accDynamicPeakThreshold) {
					isPeaking = true;
				} else if (magnitude <= accBasicValleyThreshold && magnitude <= accDynamicValleyThreshold) {
					isValleying = true;
				}


				// Detect sharp turn

			}
		}
	}






	private void peakFound () {
		logStep("Peak");
		for (intCadenceListener cadenceListener : mCadenceListeners) {
			cadenceListener.onStep();
		}
	}






	private void valleyFound () {
		logStep("Valley");
		for (intCadenceListener cadenceListener : mCadenceListeners) {
			cadenceListener.onStep();
		}
	}


}
