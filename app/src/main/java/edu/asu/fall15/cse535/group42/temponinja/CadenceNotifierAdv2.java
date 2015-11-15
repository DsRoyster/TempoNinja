package edu.asu.fall15.cse535.group42.temponinja;

import android.util.Log;

/**
 * Created by royster on 11/9/15.
 */
public class CadenceNotifierAdv2 extends CadenceNotifier {
	private final static String TAG = "CadenceNotifierAdv2";



	CadenceNotifierAdv2 () {
		super();
	}


	// Steps vector
	protected DoubleBuffer			steps = new DoubleBuffer (20);
	protected static final int		AVR_STEP_NUM = 3;

	// Detected equals estimated if the error is within this ratio
	protected static final double	equalErrorRatio = 0.1;
	protected static boolean 		isStepsEqual (double dStepLen, double eStepLen) { return ((dStepLen >= (1-equalErrorRatio) * eStepLen) && (dStepLen <= (1+equalErrorRatio) * eStepLen)); }

	// Maximum step length: 1.5s
	protected static final double	MAX_STEP_LEN = 1500;

	// State flag
	// 0: init, 1: start searching for pattern, 2: normal, 3: smaller, 4: larger, 5: smaller - false positive in step (deprecated), 6: smaller - cadence changed, 7: larger - skipped one step, 8: stopped and restart (deprecated)
	protected int					state = 0;



	public void clear() {
		steps.clear();
		state = 0;
	}



	// onStep function: main logic of CadenceNotifier
	public void onStep() {
		long thisStepTime = System.currentTimeMillis();
		double thisStepLen = 0;
		if (steps.element_size() > 1) thisStepLen = thisStepTime - steps.getFromRecent(0);


		// Compute estimated
		double estimatedNextStepLength = 1;
		if (steps.element_size() > AVR_STEP_NUM) {
			estimatedNextStepLength = (steps.getFromRecent(0) - steps.getFromRecent(AVR_STEP_NUM)) / AVR_STEP_NUM;
		}
		double estimatedNextStep = steps.getFromRecent(0) + estimatedNextStepLength;


		// Compute this step
		double detectedNextStepLength = thisStepTime - steps.getFromRecent(0);




		// Let's start the state machine
		if (state == 0) {
			//-- Init

			// Clear all data and start
			mCadence = 0;
			steps.clear();
			state = 1;
			onStep();


		} else if (state == 1) {
			//-- Some random data: start searching for pattern

			// insert current step, so that this avr includes the current
			steps.put(thisStepTime);

			// seek for pattern
			if (steps.element_size() <= AVR_STEP_NUM) return;
			double avrStepLen = (steps.getFromRecent(0) - steps.getFromRecent(AVR_STEP_NUM)) / AVR_STEP_NUM;
			for (int i = 0; i < AVR_STEP_NUM; i++) {
				// Check the previous several steps (including this), if they equals their average, we say we find a pattern
				double stepLen = steps.getFromRecent(i) - steps.getFromRecent(i+1);
				if (!isStepsEqual(stepLen, avrStepLen)) return;
			}

			// if all steps are equal, we find a pattern
			mCadence = 60.0 * 1000 / avrStepLen;
			notifyListener();
			Log.i(TAG, "CadenceChange (1): " + mCadence);
			DebugLogger.log(TAG, "CadenceChange (1): " + mCadence);
			state = 2;


		} else if (state == 2) {
			//-- Cadence found: normal

			// insert current step, so that this avr includes the current
			steps.put(thisStepTime);

			// check for pattern
			if (steps.element_size() <= AVR_STEP_NUM) return;
			double avrStepLen = (steps.getFromRecent(0) - steps.getFromRecent(AVR_STEP_NUM)) / AVR_STEP_NUM;
			if (isStepsEqual(thisStepLen, avrStepLen)) {
				// If pattern continues
				mCadence = 60.0 * 1000 / avrStepLen;
				notifyListener();
				Log.i(TAG, "CadenceChange (2): " + mCadence);
				DebugLogger.log(TAG, "CadenceChange (2): " + mCadence);
			} else {
				// else, enter the corresponding state
				if (thisStepLen < avrStepLen) {
					// If smaller, do not need to call onStep(), because all dependent on next step
					state = 3;
				} else {
					// If larger, do call onStep(), to decide the cases
					state = 4;
					onStep();
				}
			}


		} else if (state == 3) {
			//-- Smaller
			// Three states from smaller: 1: random data, 5: false positive in step, and 6: cadence changed
			// When enters, this is the next step from the smaller one, and the smaller step is already stored in steps

			// Check cadence change: seems this may lead to inaccuracy, and hence we directly enter the seeking state
//			if (steps.element_size() > 1) {
//				double nextThisLen = thisStepLen;
//				double thisPrevLen = steps.getFromRecent(0) - steps.getFromRecent(1);
//				if (isStepsEqual(nextThisLen, thisPrevLen)) {
//					// If it looks like cadence have changed
//					steps.put(thisStepTime);
//					state = 6;
//					onStep();
//					return;
//				}
//			}

			// Check for false positive in step
			if (steps.element_size() > AVR_STEP_NUM) {
				double nextPrevLen = thisStepTime - steps.getFromRecent(1);
				double avrStepLen = (steps.getFromRecent(1) - steps.getFromRecent(1+AVR_STEP_NUM)) / AVR_STEP_NUM;
				if (isStepsEqual(nextPrevLen, avrStepLen)) {
					// If it looks like false positive
					steps.rmTop();
					steps.put(thisStepTime);
					avrStepLen = (steps.getFromRecent(0) - steps.getFromRecent(AVR_STEP_NUM)) / AVR_STEP_NUM;
					mCadence = 60.0 * 1000 / avrStepLen;
					notifyListener();
					Log.i(TAG, "CadenceChange (3-5): " + mCadence);
					DebugLogger.log(TAG, "CadenceChange (3-5): " + mCadence);
					state = 2;
					return;
				}
			}

			// If not these cases, enter else, and directly
			steps.clear();
			state = 1;
			onStep();


		} else if (state == 4) {
			//-- Larger

			// Check for skipped step
			if (steps.element_size() > AVR_STEP_NUM) {
				double nextThisLen = thisStepLen;
				double avrStepLen = (steps.getFromRecent(1) - steps.getFromRecent(1+AVR_STEP_NUM)) / AVR_STEP_NUM;
				if (isStepsEqual(nextThisLen / 2, avrStepLen)) {
					// If it looks like skipped step
					steps.put(thisStepTime);
					state = 7;
					onStep();
					return;
				}
			}

			// If not these cases, enter else, and directly
			steps.clear();
			state = 1;
			onStep();


		} else if (state == 5) {
			//-- Smaller - False positive within a step
			// -> Deprecated in 3






		} else if (state == 6) {
			//-- Smaller - Cadence changed
			// In this case we claim that the cadence has changed to a smaller value, and we fake the step vector to make it back to normal easily
			// Note that the three steps of the two smaller cadence intervals are already stored

			// Update cadence
			double thisTime = steps.getFromRecent(0);
			double avrStepLen = (thisTime - steps.getFromRecent(2)) / 2;
			mCadence = avrStepLen;
			notifyListener();
			Log.i(TAG, "CadenceChange (3-6): " + mCadence);
			DebugLogger.log(TAG, "CadenceChange (3-6): " + mCadence);

			// Initialize vector
			steps.clear();
			for (int i = AVR_STEP_NUM; i >= 0; i--) {
				steps.put(thisTime - i * avrStepLen);
			}

			// Change state
			state = 2;


		} else if (state == 7) {
			//-- Larger - Skipped one step
			// Note that the twice step is already stored in steps

			// Add the step and continue
			double thisTime = steps.getFromRecent(0);
			double avrStepLen = (thisTime - steps.getFromRecent(AVR_STEP_NUM-1)) / AVR_STEP_NUM;	// because thisStep skipped one
			steps.rmTop();
			steps.put(steps.getFromRecent(0) + avrStepLen);
			steps.put(thisTime);
			notifyListener();
			Log.i(TAG, "CadenceChange (4-7): " + mCadence);
			DebugLogger.log(TAG, "CadenceChange (4-7): " + mCadence);

			// update state
			state = 2;


		} else if (state == 8) {
			//-- Larger - Stopped and restart
			// -> Deprecated in 4




		} else {






		}
	}
}

