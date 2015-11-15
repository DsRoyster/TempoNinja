package edu.asu.fall15.cse535.group42.temponinja;

import java.util.Arrays;

/**
 * Drop tail buffer that keeps up to a certain amount of data.
 * Created by royster on 11/5/15.
 */
public class DoubleBuffer {

	private double[] 	array = null;
	public int 		cur = 0;		// cur-1 stores the most recent value


	private static final double INVALID_VAL = -1000000;

	DoubleBuffer(int size) {
		array = new double [size];
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
	int element_size () { return (array[cur] < 0) ? cur : size(); }






	boolean isValid () { return isValid(cur); }
	boolean isValid (int i) { return (array[i] > INVALID_VAL); }






	int getNextIdx () 				{ return getNextIdx(cur - 1); }		// get the next index from the most recent value
	int getNextIdx (int i)			{ return getNextIdx(i, 1); }
	int getNextIdx (int i, int num) { return (i + num) % array.length; }
	int getPrevIdx () 				{ return getPrevIdx(cur - 1); }
	int getPrevIdx (int i)			{ return getPrevIdx(i, 1); }
	int getPrevIdx (int i, int num) { return (i + array.length - num) % array.length; }
	int getStartIdx ()				{ return getStartIdx(0); }
	int getStartIdx (int i)			{ return getNextIdx((element_size() < size()) ? 0 : cur, i); }





	void put (double val) { array[cur] = val; cur = getNextIdx(cur); }




	double get () { return array[getPrevIdx()]; }								// get the most recent value
	double get (int i) { return array[i]; }										// no, this directly access the array by index
	double getFromRecent (int i) { return array[getPrevIdx(cur, i+1)]; }		// starting from the most recent as 0
	double getFromStart (int i) {
		if (element_size() < size()) {
			return array[getNextIdx(i)];
		} else {
			return array[getNextIdx(0, i)];
		}
	}									// starting from the most far away as 0


	int findFromRecent(double val) {
		for (int i = cur-1; i != cur; i = getPrevIdx(i)) {
			if (val == array[i]) {
				return i;
			}
		}
		return -1;
	}
	int findFromStart(double val) {
		for (int i = (element_size() < size()) ? 0 : cur; i != cur-1; i = getNextIdx(i)) {
			if (val == array[i]) {
				return i;
			}
		}
		return -1;
	}



	double sumFromRecent (int num) {
		double sum = 0;
		for (int i = getPrevIdx(); i != getPrevIdx(num); i = getPrevIdx(i)) {
			if (!isValid(i)) continue;
			sum += (array[i] < 0) ? 0 : array[i];
		}
		return sum;
	}
	double sumFromStart (int num) {
		double sum = 0;
		for (int i = getStartIdx(); i != getStartIdx(num); i = getNextIdx(i)) {
			if (!isValid(i)) continue;
			sum += (array[i] < 0) ? 0 : array[i];
		}
		return sum;
	}
	double avrFromRecent (int num) {
		double sum = 0; int cnt = 0;
		for (int i = getPrevIdx(); i != getPrevIdx(num); i = getPrevIdx(i)) {
			if (!isValid(i)) continue;
			sum += (array[i] < 0) ? 0 : array[i];
			cnt ++;
		}
		return sum / cnt;
	}
	double avrFromStart (int num) {
		double sum = 0; int cnt = 0;
		for (int i = getStartIdx(); i != getStartIdx(num); i = getNextIdx(i)) {
			if (!isValid(i)) continue;
			sum += (array[i] < 0) ? 0 : array[i];
			cnt ++;
		}
		return sum / cnt;
	}

	void rmTop () {
		cur = getPrevIdx(cur);
	}
}
