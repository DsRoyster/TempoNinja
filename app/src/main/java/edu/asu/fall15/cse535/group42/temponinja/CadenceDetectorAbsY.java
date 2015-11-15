package edu.asu.fall15.cse535.group42.temponinja;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

/**
 * Created by royster on 11/7/15.
 */
public class CadenceDetectorAbsY extends CadenceDetector
{
	private final static String TAG = "CadenceDetectorAbsY";

	public CadenceDetectorAbsY( Context context) {
		super();
		mContext = context;
		mVibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
	}


	/**
	 * Algorithm in here:
	 * 		Judge only based on the Y value,
	 */






	protected static DoubleBuffer	ys = new DoubleBuffer (100);
	protected static DoubleBuffer	steps = new DoubleBuffer (10);
	protected static final int		stepAvrNum = 3;

	protected static final int		initDataPoints = 10;


	////////////////////////////
	// For debugging
	private Context mContext;
	private Vibrator mVibrator;


	@Override
	public void clear() {
		ys.clear();
		steps.clear();
	}



	/**
	 * Detection algorithm
	 */
	protected static final double stepPassingYThreshold = 1;
	protected static final double stepLengthAbsThreshold = 60.0 * 1000 / 250;	// No one can run more than 250 steps per minute...
	protected static final double stepLengthRatioThreshold = 1.0 / 2.5;			// No one can run 2.5 times faster within one step...
	public void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;
		synchronized (this) {
			if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				// Extract the magnitude
				double x = event.values[0];
				double y = event.values[1];
				double z = event.values[2];
				double magnitude = Math.sqrt(x * x + y * y + z * z);
				ys.put(y);

				//Log.i(TAG, "" + x + " " + y + " " + z);
				//DebugLogger.log("", "" + ys.getFromRecent(1) + "\t" + y + "\t" + System.currentTimeMillis());


				// First fill initial buffer
				if (ys.element_size() < initDataPoints) return;


				// Detect if passing zero
				double py = ys.getFromRecent(1);
				//if (py > stepPassingYThreshold && y < stepPassingYThreshold) {
				if (py <= stepPassingYThreshold && y > stepPassingYThreshold) {
					long curTime = System.currentTimeMillis();

					// Discard if time too close
					if (steps.element_size() >= stepAvrNum) {
						// If there is enough step records in here
						double avrStepLength = (steps.getFromRecent(0) - steps.getFromRecent(stepAvrNum-1)) / stepAvrNum;
						double curStepLength = (double) curTime - steps.getFromRecent(0);

						// if too short, skip
						if (curStepLength < stepLengthAbsThreshold) {
							//Log.i(TAG, "No one can run more than " + 60.0*1000/stepLengthAbsThreshold + " steps per minute...");
							//DebugLogger.log(TAG, "No one can run more than " + 60.0*1000/stepLengthAbsThreshold + " steps per minute...");
							return;
						}
						if (curStepLength < avrStepLength * stepLengthRatioThreshold) {
							//Log.i(TAG, "No one can run" + 1/stepLengthRatioThreshold + "times faster within one step...");
							//DebugLogger.log(TAG, "No one can run " + 1/stepLengthRatioThreshold + " times faster within one step...");
							return;
						}
					}

					// Step found, and notify listeners
					for (intCadenceListener cadenceListener : mCadenceListeners) {
						logStep("y: pass zero upward");				// Debug log
						cadenceListener.onStep();						// Notify all step listeners
					}

					// Record and birate
					steps.put(curTime);									// Record step time
					mVibrator.vibrate(50);								// Vibrate for debugs
				}
			}
		}
	}

}