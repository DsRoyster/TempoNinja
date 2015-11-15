package edu.asu.fall15.cse535.group42.temponinja;

import android.util.Log;

import java.util.ArrayList;

/**
 * Calculates and displays pace (steps / minute), handles input of desired pace,
 * notifies user if he/she has to go faster or slower.
 * @author Levente Bagi
 */
public class CadenceNotifier implements intCadenceListener {
	private final static String TAG = "CadenceNotifier";

	public interface Listener {
		public void cadenceChanged(double value);
		public void passValue();
	}
	protected ArrayList<Listener> mListeners = new ArrayList<Listener>();

	int mCounter = 0;

	protected long		mLastStepTime = 0;
	protected long[] 	mStepTimes = {-1, -1, -1, -1, -1};	// Record the previous four step timestamps
	protected int 		mStartStepTimesIndex = -1;
	protected int 		mCurStepTimesIndex = -1;
	protected double 	mCadence = 0;

	public CadenceNotifier() {  }



	public void clear() {
		// TODO: clear everything
		// for child functions
	}



	public void addListener(Listener l) {
		mListeners.add(l);
	}

	protected static final long MinFT = 80;		// A step is one greater than 80 ms
	protected static final long MaxFT = 2000;		// A step is one smaller than 2 sec
	public void onStep() {
		long thisStepTime = System.currentTimeMillis();
		long delta = thisStepTime - mLastStepTime;
		DebugLogger.log(TAG, "Delta: " + delta);

		// Calculate pace based on last x steps
		if (delta > MinFT) {
			mCurStepTimesIndex = (mCurStepTimesIndex + 1) % mStepTimes.length;
			mStepTimes[mCurStepTimesIndex] = thisStepTime;
			if (mStartStepTimesIndex < 0) { mStartStepTimesIndex = mCurStepTimesIndex; }
			else if (mStartStepTimesIndex == mCurStepTimesIndex) { mStartStepTimesIndex = (mStartStepTimesIndex + 1) % mStepTimes.length; }

			long mFromStartToCur = mStepTimes[mCurStepTimesIndex] - mStepTimes[mStartStepTimesIndex];
			int cnt = (mCurStepTimesIndex > mStartStepTimesIndex) ? (mCurStepTimesIndex - mStartStepTimesIndex) : (mCurStepTimesIndex + mStepTimes.length - mStartStepTimesIndex);
			double mAverageTime = (double) mFromStartToCur / cnt;
			mCadence = 60.0 * 1000 / mAverageTime;
			notifyListener();
			DebugLogger.log(TAG, "CadenceChange: " + mCadence);
		} else {
			mCadence = -1;
		}

		mLastStepTime = thisStepTime;
	}

	protected void notifyListener() {
		for (Listener listener : mListeners) {
			listener.cadenceChanged(mCadence);
		}
	}

	public void passValue() {
		// not used, just for implementation
	}

}