package edu.asu.fall15.cse535.group42.temponinja;

import java.io.File;
import java.util.Arrays;

/**
 * Created by royster on 11/10/15.
 */
public class FileBuffer {

	private File[] 	array = null;
	public int 		cur = 0;		// cur-1 stores the most recent value


	private static final File INVALID_VAL = null;

	FileBuffer(int size) {
		array = new File [size];
		clear();
	}





	void clear () {
		Arrays.fill(array, INVALID_VAL);
		cur = 0;
	}





	/**
	 * Total capacity of the buffer.
	 * @return
	 */
	int size () { return array.length; }
	/**
	 * Number of elements in the buffer.
	 * @return
	 */
	int element_size () { return (array[cur] == null) ? cur : size(); }






	boolean isValid () { return isValid(cur); }
	boolean isValid (int i) { return (array[i] != INVALID_VAL); }






	int getNextIdx () 				{ return getNextIdx(cur - 1); }		// get the next index from the most recent value
	int getNextIdx (int i)			{ return getNextIdx(i, 1); }
	int getNextIdx (int i, int num) { return (i + num) % array.length; }
	int getPrevIdx () 				{ return getPrevIdx(cur - 1); }
	int getPrevIdx (int i)			{ return getPrevIdx(i, 1); }
	int getPrevIdx (int i, int num) { return (i + array.length - num) % array.length; }
	int getStartIdx ()				{ return getStartIdx(0); }
	int getStartIdx (int i)			{ return getNextIdx((element_size() < size()) ? 0 : cur, i); }





	void put (File val) { array[cur] = val; cur = getNextIdx(cur); }




	File get () { return array[getPrevIdx()]; }								// get the most recent value
	File get (int i) { return array[i]; }										// no, this directly access the array by index
	File getFromRecent (int i) { return array[getPrevIdx(cur, i+1)]; }		// starting from the most recent as 0
	File getFromStart (int i) {
		if (element_size() < size()) {
			return array[getNextIdx(i)];
		} else {
			return array[getNextIdx(0, i)];
		}
	}									// starting from the most far away as 0


	int findFromRecent(File val) {
		if (element_size() == 0) return -1;
		for (int i = cur-1; i != cur; i = getPrevIdx(i)) {
			if (val == array[i]) {
				return i;
			}
		}
		return -1;
	}
	int findFromStart(File val) {
		if (element_size() == 0) return -1;
		for (int i = (element_size() < size()) ? 0 : cur; i != cur-1; i = getNextIdx(i)) {
			if (val == array[i]) {
				return i;
			}
		}
		return -1;
	}
	int findDiffFromRecent(File val) {
		if (element_size() == 0) return -1;
		int cnt = 0;
		for (int i = cur-1; i != cur; i = getPrevIdx(i), cnt++) {
			if (val == array[i]) {
				return cnt;
			}
		}
		return -1;
	}
	int findDiffFromStart(File val) {
		if (element_size() == 0) return -1;
		int cnt = 0;
		for (int i = (element_size() < size()) ? 0 : cur; i != cur-1; i = getNextIdx(i), cnt++) {
			if (val == array[i]) {
				return cnt;
			}
		}
		return -1;
	}


	void rmTop () {
		cur = getPrevIdx(cur);
	}





	@Override
	public String toString () {
		if (element_size() == 0) { return "[]"; }
		String s = "[ ";
		for (int i = 0; i < size() && getFromRecent(i) != null; i ++) {
			s += getFromRecent(i).toString() + ", ";
		}
		s += "]\n";
		return s;
	}
}

