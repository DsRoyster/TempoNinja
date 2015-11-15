package edu.asu.fall15.cse535.group42.temponinja;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by royster on 11/3/15.
 */
public class DebugLogger {

	// TAG information
	private static final String TAG = "DebugLogger";


	///////////////////////////////////////////
	// DebugLogger using Singleton class

	// Private constructor. Prevents instantiation from other classes.
	private DebugLogger() {
		Log.i(TAG, "DebugLogger constructor");
		init();
	}
	public void init () {
		Log.i(TAG, "init");
		File oldFile = new File(DataHolder.path + debugFileName);
		if (oldFile.exists()) {
			oldFile.renameTo(new File (oldFile.getName() + ".bak_" + System.currentTimeMillis()));
		}

		try {
			statText = new File(DataHolder.path + debugFileName);
			is = new FileOutputStream(statText);
			osw = new OutputStreamWriter(is);
		} catch (Exception e) {
			Log.i(TAG, "[X] Exception: " + e.toString());
		}
	}

	/**
	 * Initializes singleton.
	 *
	 * @see <a href="https://en.wikipedia.org/wiki/Singleton_pattern#Initialization-on-demand_holder_idiom">Wikipedia: Initialization-on-demand by Dr. Bill Pugh</a>
	 * {@link DebugLoggerHolder} is loaded on the first execution of {@link DataHolder#getInstance()} or the first access to
	 * {@link DebugLoggerHolder#INSTANCE}, not before.
	 */
	private static class DebugLoggerHolder {
		private static final DebugLogger INSTANCE = new DebugLogger();
	}
	public static DebugLogger getInstance() {
		return DebugLoggerHolder.INSTANCE;
	}




	///////////////////////////////////////////
	// Debug file
	private static final String				debugFileName = "tempoDebug.log";
	private static File 					statText = null;	// initialized in constructor
	private static FileOutputStream 		is = null;			// initialized in constructor
	private static OutputStreamWriter 		osw = null;			// initialized in constructor



	///////////////////////////////////////////
	// Log into file
	public static void log (String tag, String content) {
		try {
			if (tag.isEmpty()) {
				osw.write(content + "\n");
			} else {
				osw.write(tag + ": " + content + "\n");
			}
			osw.flush();
		} catch (NullPointerException e) {
			Log.i(TAG, "init for log exception: " + e.toString());
			File oldFile = new File(DataHolder.path + debugFileName);
			if (oldFile.exists()) {
				oldFile.renameTo(new File (oldFile.getName() + ".bak_" + System.currentTimeMillis()));
			}

			try {
				statText = new File(DataHolder.path + debugFileName);
				is = new FileOutputStream(statText);
				osw = new OutputStreamWriter(is);
			} catch (Exception e1) {
				throw e;
			}
		} catch (Exception e) {
			Log.i(TAG, "[X] Exception: " + e.toString());
		}
	}
}