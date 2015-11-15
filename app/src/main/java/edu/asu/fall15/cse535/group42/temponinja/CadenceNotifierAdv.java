package edu.asu.fall15.cse535.group42.temponinja;

import android.os.CountDownTimer;
import android.util.Log;

/**
 * Created by royster on 11/9/15.
 */
public class CadenceNotifierAdv extends CadenceNotifier {
	private final static String TAG = "CadenceNotifierAdv";



	CadenceNotifierAdv () {
		super();
	}



	protected DoubleBuffer			detectedSteps = new DoubleBuffer (10);
	protected DoubleBuffer			estimatedSteps = new DoubleBuffer (10);
	protected DoubleBuffer			allSteps = new DoubleBuffer (20);
	protected static final int		AVR_STEP_NUM = 3;

	// Detected equals estimated if the error is within this ratio
	protected static final double	equalErrorRatio = 0.2;

	// Suspect flag for cadence change
	protected static final int		MAX_CADENCE_CHANGE_THRESHOLD = 3;
	protected int					cadenceChangeFlg = 0;


	// onStep function: main logic of CadenceNotifier
	public void onStep() {
		long thisStepTime = System.currentTimeMillis();


		// Compute estimated
		double estimatedNextStepLength = 1;
		if (allSteps.element_size() > AVR_STEP_NUM) {
			estimatedNextStepLength = (allSteps.getFromRecent(0) - allSteps.getFromRecent(AVR_STEP_NUM)) / AVR_STEP_NUM;
		}
		double estimatedNextStep = detectedSteps.getFromRecent(0) + estimatedNextStepLength;


		// Compute this step
		double detectedNextStepLength = thisStepTime - allSteps.getFromRecent(0);


		String debugStr = "";
		// Let's distinguish the different cases about estimated and detected steps
		if (allSteps.element_size() <= AVR_STEP_NUM) {
			// if not enough data points, just store it
			detectedSteps.put(thisStepTime);
			allSteps.put(thisStepTime);
			debugStr = "Initial filling";

		} else if (detectedNextStepLength >= MAX_CADENCE_CHANGE_THRESHOLD * estimatedNextStep) {
			// if the step is too large, we need to start a new run from scratch
			detectedSteps.clear();
			estimatedSteps.clear();
			allSteps.clear();
			mCadence = 0;
			debugStr = "Step too long";

			DebugLogger.log(TAG, "Step too long (" + detectedNextStepLength + "): discard and count from scratch!");

			// reset cadence change flg
			cadenceChangeFlg = 0;


		} else if (detectedNextStepLength >= estimatedNextStepLength * (1 - equalErrorRatio) &&
				detectedNextStepLength <= estimatedNextStepLength * (1 + equalErrorRatio)) {
			// if the step is pretty close to the previous ones, add it to detected step
			detectedSteps.put(thisStepTime);

			// Add the step, and update cadence
			allSteps.put(thisStepTime);
			double actualNextStepLength = (allSteps.getFromRecent(0) - allSteps.getFromRecent(AVR_STEP_NUM)) / AVR_STEP_NUM;
			mCadence = 60.0 * 1000 / actualNextStepLength;
			debugStr = "Equal";

			// reset cadence change flg
			cadenceChangeFlg = 0;


		} else if (detectedNextStepLength >= 2 * estimatedNextStepLength * (1 - equalErrorRatio) &&
				detectedNextStepLength <= 2 * estimatedNextStepLength * (1 + equalErrorRatio)) {
			// if the step is twice the size of the previous one, add both itself and the estimated one, and put a flag on it
			detectedSteps.put(thisStepTime);

			// Add the step and the missed step, and update cadence
			estimatedSteps.put(estimatedNextStep);
			allSteps.put(estimatedNextStep);
			allSteps.put(thisStepTime);
			double actualNextStepLength = (allSteps.getFromRecent(0) - allSteps.getFromRecent(AVR_STEP_NUM)) / AVR_STEP_NUM;
			mCadence = 60.0 * 1000 / actualNextStepLength;
			debugStr = "Equal twice";

			// update cadence change flg
			cadenceChangeFlg ++;


		} else if (detectedNextStepLength < estimatedNextStepLength) {
			// if the step is even smaller than the previous several steps, do not initiate a step but update the cadenceChangeFlg
			detectedSteps.put(thisStepTime);
			//allSteps.put(estimatedNextStep);
			debugStr = "Smaller";

			// update cadence change flg
			cadenceChangeFlg ++;


		} else if (detectedNextStepLength > estimatedNextStepLength) {
			// if the step is larger than estimated, store it, but proceed with estimated
			detectedSteps.put(thisStepTime);

			// Add all missing steps, update cadence change flg, and update cadence
			for (int i = 0; i < detectedNextStepLength / estimatedNextStepLength; i++) {
				allSteps.put(allSteps.getFromRecent(0) + i * estimatedNextStepLength);

				// update cadence change flg
				cadenceChangeFlg ++;
			}
			double actualNextStepLength = (allSteps.getFromRecent(0) - allSteps.getFromRecent(AVR_STEP_NUM)) / AVR_STEP_NUM;
			mCadence = 60.0 * 1000 / actualNextStepLength;
			debugStr = "Larger";
		}
		Log.i(TAG, "----> " + detectedNextStepLength + ", " + estimatedNextStepLength + ", " + debugStr);
		DebugLogger.log(TAG, "----> " + detectedNextStepLength + ", " + estimatedNextStepLength + ", " + debugStr);


		// Then we need to make cadence change based on the candenceChangeFlg
		if (cadenceChangeFlg >= MAX_CADENCE_CHANGE_THRESHOLD) {
			// If the change is great enough, substitute all steps with detected steps, and notify listners
			allSteps = detectedSteps;
			double actualNextStepLength = (allSteps.getFromRecent(0) - allSteps.getFromRecent(AVR_STEP_NUM)) / AVR_STEP_NUM;
			mCadence = 60.0 * 1000 / actualNextStepLength;
			Log.i(TAG, "========>  Cadence change threshold");
			DebugLogger.log(TAG, "========>  Cadence change threshold");

			// reset cadence change flg
			cadenceChangeFlg = 0;
		}
		notifyListener();
		Log.i(TAG, "CadenceChange: " + mCadence);
		DebugLogger.log(TAG, "CadenceChange: " + mCadence);
	}
}
