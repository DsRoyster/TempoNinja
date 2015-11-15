package edu.asu.fall15.cse535.group42.temponinja;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;

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
public class CadenceDetectorBuiltin extends CadenceDetector
{
	private final static String TAG = "CadenceDetectorBuiltin";

	public CadenceDetectorBuiltin() {
		super();
	}

	//public void onSensorChanged(int sensor, float[] values) {
	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;
		synchronized (this) {
			if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
				logStep ();	// log step
				for (intCadenceListener cadenceListener : mCadenceListeners) {
					cadenceListener.onStep();
				}
			}
		}
	}
}
