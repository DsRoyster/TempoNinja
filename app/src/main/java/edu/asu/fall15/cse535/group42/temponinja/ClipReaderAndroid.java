package edu.asu.fall15.cse535.group42.temponinja;

import android.media.MediaMetadataRetriever;
import android.util.Log;

/**
 * Created by royster on 10/20/15.
 */
public class ClipReaderAndroid {

	// TAG information
	private static final String TAG = "ClipReaderAndroid";

	/**
	 * Read a clip file from specified file name. Returning a Clip
	 * object containing all information needed.
	 * @param fn file name to read
	 * @return ClipProperty.
	 */
	public static ClipProperty readClip (String fn) {
		ClipProperty cp = null;
		try {
			MediaMetadataRetriever mmr = new MediaMetadataRetriever();
			mmr.setDataSource(fn);
			// Get properties

			// duration
			long duration = Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION), 10);	// This is already in ms

			// title
			String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

			// author
			String author = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR);

			// album
			String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

			// date
			String date = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);

			// version
			// -> right now just leave it as 0, further use may be to distinguish between different tempo / clip versions
			long version = 0;

			// refine the file name
			// -> right now just leave it as it was
			String file_name = fn;

			cp = new ClipProperty(file_name, duration, title, author, album, date, version);
			return cp;

		} catch (Exception e) {
			Log.i(TAG, "[X] Exception: " + e.toString());
		}

		return cp;
	}
}
