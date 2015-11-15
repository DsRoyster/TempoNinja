package edu.asu.fall15.cse535.group42.temponinja;

import java.io.File;
/**
 * @author royster
 * References: <br />
 * 		Audio processing: 	http://dsp.stackexchange.com/questions/14183/how-to-analyze-audio-input-spectrum-correctly <br />
 * 		JTransforms: 		https://sites.google.com/site/piotrwendykier/software/jtransforms <br />
 * 		TarsosDSP:	 		https://github.com/JorenSix/TarsosDSP <br />
 * 		JLayer - easiest way for mp3 playback: 	http://www.javazoom.net/javalayer/javalayer.html <br />
 * 		MP3SPI - an interface for MP3: 			http://www.javazoom.net/mp3spi/mp3spi.html <br />
 * We uses MP3SPI here. <br />
 * For now, we will not bother with detecting the starting point of the music, but just
 * for manual beat detection. 
 */
import java.util.Arrays;

/**
 * <p>
 * An audio clip is a piece of audio that should/will match a
 * tempo setting. It can either be used for tempo counter for
 * a piece of music, or be played as a piece of music that is
 * tempo-aware.
 * </p>
 * <p>
 * For simplicity of implementation, let us first implement 
 * the MP3 player/analyzer (with JAVA FX). We will focus on the
 * other video formats in future work.
 * </p>
 * <p>
 * The Clip class actually records information about an audio 
 * file, rather than the file itself. It may have deprecated
 * path and other problems. A hash is used to map between the
 * information and the tempo files, and also in other cases.
 * </p>
 * <p>
 * Issue 1: do we necessarily take file_name into account? <br />
 * -> This will influence: 1) instance variables, 2) constructors, 
 * and 3) hashing.
 * </p>
 * @author royster
 * @since 08/22/15
 * @version 1.0
 */
public class ClipProperty {
	
	/**
	 * Audio clip information for hashing:
	 * 		file name, 
	 * 		duration, 
	 * 		title, 
	 * 		author, 
	 * 		album, 
	 * 		date, 
	 * 		version (internal version for records).
	 * (Deprecated) Hashing is used to hide how clip compare with clip files
	 */
	protected String file_name = "";
	protected long duration = 0;
	protected String title = "";
	protected String author = "";
	protected String album = "";
	protected String date = "";
	protected long version = 0;
	protected long hashcode = -1;	// (Deprecated)
	
	
	public ClipProperty () {  }
	/**
	 * ClipProperty constructor.
	 * @param fn file name.
	 * @param du duration.
	 * @param ti title.
	 * @param au author.
	 * @param al album.
	 * @param dt date.
	 */
	public ClipProperty (String fn, long du, String ti, String au, String al, String dt) {
		file_name = fn;
		duration = du;
		title = ti;
		author = au;
		album = al;
		date = dt;
	}
	/**
	 * ClipProperty constructor.
	 * @param fn file name.
	 * @param du duration.
	 * @param ti title.
	 * @param au author.
	 * @param al album.
	 * @param dt date.
	 * @param ve internal version.
	 */
	public ClipProperty (String fn, long du, String ti, String au, String al, String dt, long ve) {
		file_name = fn;
		duration = du;
		title = ti;
		author = au;
		album = al;
		date = dt;
		version = ve;
	}

	
	/**
	 * Get file_name (string)
	 */
	public String getFileName () { return file_name; }
	/**
	 * Get duration (string)
	 */
	public long getDuration () { return duration; }
	/**
	 * Get title (string)
	 */
	public String getTitle () { return title; }
	/**
	 * Get author (string)
	 */
	public String getAuthor () { return author; }
	/**
	 * Get album (string)
	 */
	public String getAlbum () { return album; }
	/**
	 * Get date (string)
	 */
	public String getDate () { return date; }
	/**
	 * Get version (long)
	 */
	public long getVersion () { return version; }
	/**
	 * The hash code is the same as the hash string. Use buildHashString() instead.
	 * @return (String) the hash
	 */	
	public String getHash () {
		return buildHashString ();
	}
	
	
	/**
	 * Build the hash string based on stored information
	 * @return the hash string.
	 */
	public String buildHashString () {
		String hash_str = "";
		
		hash_str += "[";
		hash_str += file_name;
		hash_str += "][";
		hash_str += duration;
		hash_str += "][";
		hash_str += title;
		hash_str += "][";
		hash_str += author;
		hash_str += "][";
		hash_str += album;
		hash_str += "][";
		hash_str += date;
		hash_str += "][";
		hash_str += version;
		hash_str += "]";
		
		return hash_str;
	}
	/**
	 * (Deprecated) Hashing function (with external string): this should not be exposed.
	 * @param hash_str the string to hash.
	 * @return the hash (long).
	 */	
	protected static long hash (String hash_str) {
		long hashcode = hash_str.hashCode();
		return hashcode;
	}
	/**
	 * (Deprecated) Hashing function: call static hash function for hashing
	 * @return the hash (long).
	 */
	public long hash () {
		String hash_str = buildHashString();
		hashcode = hash (hash_str);
		return hashcode;
	}

	
	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		ClipProperty cp = ClipReaderMP3SPI.readClip("2.mp3");
//		File f = new File ("/Users/royster/Desktop/Develop/Java/TempoNinja/1.mp3");
//		System.out.println(cp.getHash());
//		System.out.println(f.getPath());
//
//	   // initializing unsorted int array
//	   int intArr[] = {30,20,5,12,55};
//
//	   // sorting array
//	   Arrays.sort(intArr);
//
//	   // let us print all the elements available in list
//	   System.out.println("The sorted int array is:");
//	   for (int number : intArr) {
//	   System.out.println("Number = " + number);
//	   }
//
//	   // entering the value to be searched
//	   int searchVal = 13;
//
//	   int retVal = Arrays.binarySearch(intArr,searchVal);
//
//	   System.out.println("The index of element 21 is : " + retVal);
//	}

}
