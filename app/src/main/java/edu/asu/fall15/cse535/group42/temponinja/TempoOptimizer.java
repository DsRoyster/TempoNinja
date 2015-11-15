package edu.asu.fall15.cse535.group42.temponinja;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author royster
 */

/**
 * The optimizer class that uses statistics methods to optimize 
 * the beat array for further tempo calculation.
 * @author royster
 * @since 08/19/2015
 * @version 1.0
 */
public class TempoOptimizer {
	
	/**
	 * Outlier detection based on modified Z-score. <br />
	 * <a href="http://d-scholarship.pitt.edu/7948/1/Seo.pdf"> http://d-scholarship.pitt.edu/7948/1/Seo.pdf </a> <br />
	 * <a href="http://www.itl.nist.gov/div898/handbook/eda/section3/eda35h.htm"> http://www.itl.nist.gov/div898/handbook/eda/section3/eda35h.htm </a> <br />
	 * @param input input beat list to be optimized
	 * @return distinguished list of outliers in the beat list
	 */
	private static ArrayList<Integer> detectOutliers (ArrayList<Long> input) {
		// Detect the outliers of the input list
		
		// Return empty list
		if (input==null || input.size()==0) {
			return null;
		}
		
		// Generate interval array, and calculate the mean
		ArrayList<Long> intList = new ArrayList<Long> ();
		double mean = 0;
		for (int i = 0; i < input.size()-1; i++) {
			intList.add(input.get(i+1) - input.get(i));
			mean += intList.get(intList.size()-1);
		}
		mean /= intList.size();
		
		// Find the median
		double median = 0;
		ArrayList<Long> tmpList = (ArrayList<Long>) intList.clone();	// used to find median only
		Collections.sort(tmpList);
		median = tmpList.get(tmpList.size()/2);
		if (tmpList.size() % 2 == 0) {
			median += tmpList.get(tmpList.size()/2+1);
			median /= 2;
		}
		
		// Calculate absolute median deviation
		ArrayList<Double> tmpList2 = new ArrayList<Double> ();			// stores AMD of each interval
		ArrayList<Double> tmpList3 = new ArrayList<Double> ();
		for (int i = 0; i < tmpList.size(); i++) {
			double normMedian = (double)intList.get(i) - median;	// ERROR: need to use intList, not tmpList 		// MOD: changed from abs to norm
			tmpList2.add(normMedian);
			tmpList3.add(Math.abs(normMedian));
		}
		
		// Find the median of AMD
		double mad = 0;
		Collections.sort(tmpList3);
		mad = tmpList3.get(tmpList3.size()/2);
		if (tmpList3.size() % 2 == 0) {
			mad += tmpList3.get(tmpList3.size()/2+1);
			mad /= 2;
		}
		
		// Calculate the modified Z-scores and record outliers
		ArrayList<Integer> outliers = new ArrayList<Integer> ();
		ArrayList<Double> Mis = new ArrayList<Double> ();	// deviating data
		double outlierThreshold = 3.5;
		for (int i = 0; i < tmpList2.size(); i++) {
			double Mi = 0.6475 * tmpList2.get(i) / mad;
			Mis.add(Mi);						// everyone has a Mis
			if (Math.abs(Mi) > outlierThreshold) {			// Finally find outlier intervals
				outliers.add(i);
			}
		}
		
//		// Print out Mis
//		System.out.print("\nMis: ");
//		for (int i = 0; i < Mis.size(); i++) {
//			System.out.print(i + "->" + Mis.get(i) + ", ");
//		}
//		System.out.println("");
		
		// If there is no outlier, just return null
		if (outliers.size() == 0) {
			return null;
		}
		
		// If there are too many outliers, return null
		final double maxIntOutliersTolerance = 0.7;
		if (outliers.size() >= intList.size() * maxIntOutliersTolerance) {
			System.out.println("[X] Too many interval outliers or too few data points. "
					+ "Please resubmit data for optimization.");
			return null;
			//return (ArrayList<Long>) input.clone();
		}
		
		// Find the outliers in initial data
		// -> Problem: how to decide which side of the interval causes the outlier?
		// -> Solution: assume there cannot be many consecutive outlier beats, then
		//		use consecutive outlier intervals to detect outlier beats
		// -> 8/21/15 updated: use positive and negative outliers to detect: neg to 
		//		pos turning point means the start of a new outlier range
		// -> 8/21/15 updated 2: compare the three initial states and find the best one
		
		// 1. Find consecutive outlier intervals
		double inOutlierRange = 0;		// 0: not in range, >0: positive range (starting Mis > 3.5), <0: negative range (starting Mis < -3.5)
		// 1.1 Assuming the first beat is good to go
		inOutlierRange = 0;
		ArrayList<Integer> consOutlierStartNorm = new ArrayList<Integer> ();
		ArrayList<Integer> consOutlierSizeNorm = new ArrayList<Integer> ();
		int consOutlierSizeNormNum = 0;
		for (int i = 0; i < Mis.size(); i++) {		// we are not based on outliers array any more
			if (Math.abs(inOutlierRange) <= outlierThreshold) {
				// currently normal						// may not be right regarding first and last
				if (Math.abs(Mis.get(i)) > outlierThreshold) {
					// Positive / Negative outlier range
					inOutlierRange = Mis.get(i);
					consOutlierStartNorm.add(i);
					consOutlierSizeNorm.add(1);
					consOutlierSizeNormNum ++;
				} else {
					// if normal, nothing happens
					inOutlierRange = 0;
				}
			} else {
				// currently in positive range
				int lastIdx = consOutlierSizeNorm.size()-1;
				inOutlierRange += Mis.get(i);
				if (consOutlierSizeNorm.isEmpty()) {
					consOutlierStartNorm.add(i);
					consOutlierSizeNorm.add(1);
				} else {
					consOutlierSizeNorm.set(lastIdx, 1 + consOutlierSizeNorm.get(lastIdx));
				}
				consOutlierSizeNormNum ++;
			}
		}
		// 1.2 Assuming the first beat is bad
		inOutlierRange = - Mis.get(0);
		ArrayList<Integer> consOutlierStartErr = new ArrayList<Integer> ();
		ArrayList<Integer> consOutlierSizeErr = new ArrayList<Integer> ();
		int consOutlierSizeErrNum = 0;
		for (int i = 0; i < Mis.size(); i++) {		// we are not based on outliers array any more
			if (Math.abs(inOutlierRange) <= outlierThreshold) {
				// currently normal						// may not be right regarding first and last
				if (Math.abs(Mis.get(i)) > outlierThreshold) {
					// Positive / Negative outlier range
					inOutlierRange = Mis.get(i);
					consOutlierStartErr.add(i);
					consOutlierSizeErr.add(1);
					consOutlierSizeErrNum ++;
				} else {
					// if normal, nothing happens
					inOutlierRange = 0;
				}
			} else {
				// currently in non-good range
				int lastIdx = consOutlierSizeErr.size()-1;
				inOutlierRange += Mis.get(i);
				if (consOutlierSizeErr.isEmpty()) {
					consOutlierStartErr.add(i);
					consOutlierSizeErr.add(1);
					consOutlierSizeErrNum ++;
				} else {
					consOutlierSizeErr.set(lastIdx, 1 + consOutlierSizeErr.get(lastIdx));
				}
				consOutlierSizeErrNum ++;
			}
		}
		ArrayList<Integer> consOutlierStart = (consOutlierSizeNormNum <= consOutlierSizeErrNum) ? consOutlierStartNorm : consOutlierStartErr;
		ArrayList<Integer> consOutlierSize = (consOutlierSizeNormNum <= consOutlierSizeErrNum) ? consOutlierSizeNorm : consOutlierSizeErr;

		
//		// Print out consecutive interval outliers list
//		System.out.print("Consecutive outliers: ");
//		for (int i = 0; i < consOutlierStart.size(); i++) {
//			System.out.print("(" + consOutlierStart.get(i) + " , " + consOutlierSize.get(i) + "), ");
//		}
//		System.out.println("");
		
		// Distinguish beat outliers
		// -> maxConsOutliers: maximum consecutive outliers allowed
		final int maxConsBeatsOutliers = 4;
		ArrayList<Integer> beatOutliers = new ArrayList<Integer> ();
		for (int i = 0; i < consOutlierStart.size(); i++) {
			Integer start = consOutlierStart.get(i), size = consOutlierSize.get(i);
			if (size == 1) {
				// If it has only one consecutive outlier interval
				if (i == 0) {
					// If this is the starting interval, then the starting beat is outlier
					beatOutliers.add(0);
				} else if (start == intList.size()-1) {
					// If this is the last interval, then the ending beat is outlier
					beatOutliers.add(input.size()-1);
				} else {
					Integer lastStart = consOutlierStart.get(i-1) + consOutlierSize.get(i-1), lastSize = start - lastStart + 1;
					if (lastSize + consOutlierSize.get(i-1) <= maxConsBeatsOutliers) {
						// This may not be the case when actually two or more consecutive beat outliers happen
						// -> So instead we'd better merge it to the previous outlier data
						System.out.println("[?] Stand-alone outlier data at interval <" + start 
								+ " , " + start+1 + ">. Merging into previous outlier data.");
						for (int j = 0; j <= lastSize; j++) {
							beatOutliers.add(lastStart+j);
						}
					} else {
						// If this outlier is too far away from the last one, there must be something wrong
						System.out.println("[X] Stand-alone outlier data at interval <" + start 
								+ " , " + start+1 + ">. Please check data.");
					}
				}
			} else {
				// Add every beat in the middle to outlier list
				for (int j = 1; j < size; j++) {
					beatOutliers.add(start+j);
				}
			}
		}
		
//		// Print out beats outliers list
//		System.out.print("-> Beats outliers: ");
//		for (int i = 0; i < beatOutliers.size(); i++) {
//			System.out.print(beatOutliers.get(i) + "->" + input.get(beatOutliers.get(i)) + ", ");
//		}
//		System.out.println("");
		
		// If there are too many outliers, return null
		final double maxBeatsOutliersTolerance = maxIntOutliersTolerance / 2;
		if (beatOutliers.size() >= input.size() * maxBeatsOutliersTolerance) {
			System.out.println("[X] Too many beats outliers or too few data points. "
					+ "Please resubmit data for optimization.");
			return null;
			//return (ArrayList<Long>) input.clone();
		}
		
		return beatOutliers;
	}
	/*private static ArrayList<Integer> detectOutliers (ArrayList<Long> input) {
		// Detect the outliers of the input list
		
		// Return empty list
		if (input==null || input.size()==0) {
			return null;
		}
		
		// Generate interval array, and calculate the mean
		ArrayList<Long> intList = new ArrayList<Long> ();
		double mean = 0;
		for (int i = 0; i < input.size()-1; i++) {
			intList.add(input.get(i+1) - input.get(i));
			mean += intList.get(intList.size()-1);
		}
		mean /= intList.size();
		
		// Find the median
		double median = 0;
		ArrayList<Long> tmpList = (ArrayList<Long>) intList.clone();	// used to find median only
		Collections.sort(tmpList);
		median = tmpList.get(tmpList.size()/2);
		if (tmpList.size() % 2 == 0) {
			median += tmpList.get(tmpList.size()/2+1);
			median /= 2;
		}
		
		// Calculate absolute median deviation
		ArrayList<Double> tmpList2 = new ArrayList<Double> ();			// stores AMD of each interval
		ArrayList<Double> tmpList3 = new ArrayList<Double> ();
		for (int i = 0; i < tmpList.size(); i++) {
			double normMedian = (double)intList.get(i) - median;	// ERROR: need to use intList, not tmpList 		// MOD: changed from abs to norm
			tmpList2.add(normMedian);
			tmpList3.add(Math.abs(normMedian));
		}
		
		// Find the median of AMD
		double mad = 0;
		Collections.sort(tmpList3);
		mad = tmpList3.get(tmpList3.size()/2);
		if (tmpList3.size() % 2 == 0) {
			mad += tmpList3.get(tmpList3.size()/2+1);
			mad /= 2;
		}
		
		// Calculate the modified Z-scores and record outliers
		ArrayList<Integer> outliers = new ArrayList<Integer> ();
		ArrayList<Double> Mis = new ArrayList<Double> ();	// deviating data
		double outlierThreshold = 3.5;
		for (int i = 0; i < tmpList2.size(); i++) {
			double Mi = 0.6475 * tmpList2.get(i) / mad;
			Mis.add(Mi);						// everyone has a Mis
			if (Math.abs(Mi) > outlierThreshold) {			// Finally find outlier intervals
				outliers.add(i);
			}
		}
		
		// Print out Mis
		System.out.print("\nMis: ");
		for (int i = 0; i < Mis.size(); i++) {
			System.out.print(i + "->" + Mis.get(i) + ", ");
		}
		System.out.println("");
		
		// If there is no outlier, just return null
		if (outliers.size() == 0) {
			return null;
		}
		
		// If there are too many outliers, return null
		final double maxIntOutliersTolerance = 0.6;
		if (outliers.size() >= intList.size() * maxIntOutliersTolerance) {
			System.out.println("[X] Too many interval outliers or too few data points. "
					+ "Please resubmit data for optimization.");
			return null;
			//return (ArrayList<Long>) input.clone();
		}
		
		// Find the outliers in initial data
		// -> Problem: how to decide which side of the interval causes the outlier?
		// -> Solution: assume there cannot be many consecutive outlier beats, then
		//		use consecutive outlier intervals to detect outlier beats
		// -> 8/21/15 updated: use positive and negative outliers to detect: neg to 
		//		pos turning point means the start of a new outlier range
		// -> 8/21/15 updated 2: compare the three initial states and find the best one
		
		// 1. Find consecutive outlier intervals
		double inOutlierRange = Mis.get(0);		// 0: not in range, >0: positive range (starting Mis > 3.5), <0: negative range (starting Mis < -3.5)
		// 1.1 Starting normal status 0
		ArrayList<Integer> consOutlierStartNorm = new ArrayList<Integer> ();
		ArrayList<Integer> consOutlierSizeNorm = new ArrayList<Integer> ();
		int consOutlierSizeNormNum = 0;
		for (int i = 0; i < Mis.size(); i++) {		// we are not based on outliers array any more
			System.out.print(inOutlierRange + ", ");
			if (Math.abs(inOutlierRange) <= outlierThreshold) {
				// currently normal						// may not be right regarding first and last
				if (Mis.get(i) > outlierThreshold) {
					// Positive outlier range
					inOutlierRange = Mis.get(i);
					consOutlierStartNorm.add(i);
					consOutlierSizeNorm.add(1);
					consOutlierSizeNormNum ++;
				} else if (Mis.get(i) < - outlierThreshold) {
					// Negative outlier range
					inOutlierRange = Mis.get(i);
					consOutlierStartNorm.add(i);
					consOutlierSizeNorm.add(1);
					consOutlierSizeNormNum ++;
				} else {
					// if normal, nothing happens
					inOutlierRange = 0;
				}
			} else if (inOutlierRange > 0) {
				// currently in positive range
				int lastIdx = consOutlierSizeNorm.size()-1;
				if (Mis.get(i) < - outlierThreshold) {
					// if it turns to going down (a lot), assume it is back to normal
					inOutlierRange += Mis.get(i);
					consOutlierSizeNorm.set(lastIdx, 1 + consOutlierSizeNorm.get(lastIdx));
					consOutlierSizeNormNum ++;
				} else {
					inOutlierRange += Mis.get(i);
					consOutlierSizeNorm.set(lastIdx, 1 + consOutlierSizeNorm.get(lastIdx));
					consOutlierSizeNormNum ++;
				}
			} else if (inOutlierRange < 0) {
				// currently in negative range
				int lastIdx = consOutlierSizeNorm.size()-1;
				if (Mis.get(i) > outlierThreshold) {
					// if it turns to going up (a lot), assume it is back to normal
					inOutlierRange += Mis.get(i);
					consOutlierSizeNorm.set(lastIdx, 1 + consOutlierSizeNorm.get(lastIdx));
					consOutlierSizeNormNum ++;
				} else {
					inOutlierRange += Mis.get(i);
					consOutlierSizeNorm.set(lastIdx, 1 + consOutlierSizeNorm.get(lastIdx));
					consOutlierSizeNormNum ++;
				}
			}
		}
		ArrayList<Integer> consOutlierStart = consOutlierStartNorm;
		ArrayList<Integer> consOutlierSize = consOutlierSizeNorm;
//		// 1.2 Starting positive status 1
//		inOutlierRange = Mis.get(0);
//		ArrayList<Integer> consOutlierStartPos = new ArrayList<Integer> ();
//		ArrayList<Integer> consOutlierSizePos = new ArrayList<Integer> ();
//		int consOutlierSizePosNum = 1;
//		consOutlierStartPos.add(0); consOutlierSizePos.add(1);
//		for (int i = 1; i < Mis.size(); i++) {		// we are not based on outliers array any more
//			if (Math.abs(inOutlierRange) <= outlierThreshold) {
//				// currently normal						// may not be right regarding first and last
//				if (Mis.get(i) > outlierThreshold) {
//					// Positive outlier range
//					inOutlierRange = Mis.get(i);
//					consOutlierStartPos.add(i);
//					consOutlierSizePos.add(1);
//					consOutlierSizePosNum ++;
//				} else if (Mis.get(i) < - outlierThreshold) {
//					// Negative outlier range
//					inOutlierRange = Mis.get(i);
//					consOutlierStartPos.add(i);
//					consOutlierSizePos.add(1);
//					consOutlierSizePosNum ++;
//				} else {
//					// if normal, nothing happens
//				}
//			} else if (inOutlierRange > 0) {
//				// currently in positive range
//				int lastIdx = consOutlierSizePos.size()-1;
//				if (Mis.get(i) < - outlierThreshold) {
//					// if it turns to going down (a lot), assume it is back to Posal
//					inOutlierRange += Mis.get(i);
//					consOutlierSizePos.set(lastIdx, 1 + consOutlierSizePos.get(lastIdx));
//					consOutlierSizePosNum ++;
//				} else {
//					consOutlierSizePos.set(lastIdx, 1 + consOutlierSizePos.get(lastIdx));
//					consOutlierSizePosNum ++;
//				}
//			} else if (inOutlierRange < 0) {
//				// currently in negative range
//				int lastIdx = consOutlierSizePos.size()-1;
//				if (Mis.get(i) > outlierThreshold) {
//					// if it turns to going up (a lot), assume it is back to normal
//					inOutlierRange += Mis.get(i);
//					consOutlierSizePos.set(lastIdx, 1 + consOutlierSizePos.get(lastIdx));
//					consOutlierSizePosNum ++;
//				} else {
//					consOutlierSizePos.set(lastIdx, 1 + consOutlierSizePos.get(lastIdx));
//					consOutlierSizePosNum ++;
//				}
//			}
//		}
//		// 1.3 Starting negative status 1
//		inOutlierRange = 1;
//		ArrayList<Integer> consOutlierStartNeg = new ArrayList<Integer> ();
//		ArrayList<Integer> consOutlierSizeNeg = new ArrayList<Integer> ();
//		int consOutlierSizeNegNum = 1;
//		consOutlierStartNeg.add(0); consOutlierSizeNeg.add(1);
//		for (int i = 1; i < Mis.size(); i++) {		// we are not based on outliers array any more
//			if (Math.abs(inOutlierRange) <= outlierThreshold) {
//				// currently normal						// may not be right regarding first and last
//				if (Mis.get(i) > outlierThreshold) {
//					// Positive outlier range
//					inOutlierRange = Mis.get(i);
//					consOutlierStartNeg.add(i);
//					consOutlierSizeNeg.add(1);
//					consOutlierSizeNegNum ++;
//				} else if (Mis.get(i) < - outlierThreshold) {
//					// Negative outlier range
//					inOutlierRange = Mis.get(i);
//					consOutlierStartNeg.add(i);
//					consOutlierSizeNeg.add(1);
//					consOutlierSizeNegNum ++;
//				} else {
//					// if normal, nothing happens
//				}
//			} else if (inOutlierRange > 0) {
//				// currently in positive range
//				int lastIdx = consOutlierSizeNeg.size()-1;
//				if (Mis.get(i) < - outlierThreshold) {
//					// if it turns to going down (a lot), assume it is back to normal
//					inOutlierRange += Mis.get(i);
//					consOutlierSizeNeg.set(lastIdx, 1 + consOutlierSizeNeg.get(lastIdx));
//					consOutlierSizeNegNum ++;
//				} else {
//					consOutlierSizeNeg.set(lastIdx, 1 + consOutlierSizeNeg.get(lastIdx));
//					consOutlierSizeNegNum ++;
//				}
//			} else if (inOutlierRange < 0) {
//				// currently in negative range
//				int lastIdx = consOutlierSizeNeg.size()-1;
//				if (Mis.get(i) > outlierThreshold) {
//					// if it turns to going up (a lot), assume it is back to normal
//					inOutlierRange += Mis.get(i);
//					consOutlierSizeNeg.set(lastIdx, 1 + consOutlierSizeNeg.get(lastIdx));
//					consOutlierSizeNegNum ++;
//				} else {
//					consOutlierSizeNeg.set(lastIdx, 1 + consOutlierSizeNeg.get(lastIdx));
//					consOutlierSizeNegNum ++;
//				}
//			}
//		}
//		// 1.4 Find the best in the three situations
//		ArrayList<Integer> consOutlierStart = (consOutlierSizePosNum < consOutlierSizeNegNum) ? 
//				((consOutlierSizeNormNum < consOutlierSizePosNum) ? consOutlierStartNorm : consOutlierStartPos) : 
//					((consOutlierSizeNormNum < consOutlierSizeNegNum) ? consOutlierStartNorm : consOutlierStartNeg);
//		ArrayList<Integer> consOutlierSize = (consOutlierSizePosNum < consOutlierSizeNegNum) ? 
//				((consOutlierSizeNormNum < consOutlierSizePosNum) ? consOutlierSizeNorm : consOutlierSizePos) : 
//					((consOutlierSizeNormNum < consOutlierSizeNegNum) ? consOutlierSizeNorm : consOutlierSizeNeg);
		
		// Print out consecutive interval outliers list
		System.out.print("Consecutive outliers: ");
		for (int i = 0; i < consOutlierStart.size(); i++) {
			System.out.print("(" + consOutlierStart.get(i) + " , " + consOutlierSize.get(i) + "), ");
		}
		System.out.println("");
		
		// Distinguish beat outliers
		// -> maxConsOutliers: maximum consecutive outliers allowed
		final int maxConsBeatsOutliers = 4;
		ArrayList<Integer> beatOutliers = new ArrayList<Integer> ();
		for (int i = 0; i < consOutlierStart.size(); i++) {
			Integer start = consOutlierStart.get(i), size = consOutlierSize.get(i);
			if (size == 1) {
				// If it has only one consecutive outlier interval
				if (i == 0) {
					// If this is the starting interval, then the starting beat is outlier
					beatOutliers.add(0);
				} else if (start == intList.size()-1) {
					// If this is the last interval, then the ending beat is outlier
					beatOutliers.add(input.size()-1);
				} else {
					Integer lastStart = consOutlierStart.get(i-1) + consOutlierSize.get(i-1), lastSize = start - lastStart + 1;
					if (lastSize + consOutlierSize.get(i-1) <= maxConsBeatsOutliers) {
						// This may not be the case when actually two or more consecutive beat outliers happen
						// -> So instead we'd better merge it to the previous outlier data
						System.out.println("[?] Stand-alone outlier data at interval <" + start 
								+ " , " + start+1 + ">. Merging into previous outlier data.");
						for (int j = 0; j <= lastSize; j++) {
							beatOutliers.add(lastStart+j);
						}
					} else {
						// If this outlier is too far away from the last one, there must be something wrong
						System.out.println("[X] Stand-alone outlier data at interval <" + start 
								+ " , " + start+1 + ">. Please check data.");
					}
				}
			} else {
				// Add every beat in the middle to outlier list
				for (int j = 1; j < size; j++) {
					beatOutliers.add(start+j);
				}
			}
		}
		
		// Print out beats outliers list
		System.out.print("-> Beats outliers: ");
		for (int i = 0; i < beatOutliers.size(); i++) {
			System.out.print(beatOutliers.get(i) + "->" + input.get(beatOutliers.get(i)) + ", ");
		}
		System.out.println("");
		
		// If there are too many outliers, return null
		final double maxBeatsOutliersTolerance = maxIntOutliersTolerance / 2;
		if (beatOutliers.size() >= input.size() * maxBeatsOutliersTolerance) {
			System.out.println("[X] Too many beats outliers or too few data points. "
					+ "Please resubmit data for optimization.");
			return null;
			//return (ArrayList<Long>) input.clone();
		}
		
		return beatOutliers;
	}*/
	/*private static ArrayList<Integer> detectOutliers (ArrayList<Long> input) {
		// Detect the outliers of the input list
		
		// Return empty list
		if (input==null || input.size()==0) {
			return null;
		}
		
		// Generate interval array, and calculate the mean
		ArrayList<Long> intList = new ArrayList<Long> ();
		double mean = 0;
		for (int i = 0; i < input.size()-1; i++) {
			intList.add(input.get(i+1) - input.get(i));
			mean += intList.get(intList.size()-1);
		}
		mean /= intList.size();
		
		// Find the median
		double median = 0;
		ArrayList<Long> tmpList = (ArrayList<Long>) intList.clone();	// used to find median only
		Collections.sort(tmpList);
		median = tmpList.get(tmpList.size()/2);
		if (tmpList.size() % 2 == 0) {
			median += tmpList.get(tmpList.size()/2+1);
			median /= 2;
		}
		
		// Calculate absolute median deviation
		ArrayList<Double> tmpList2 = new ArrayList<Double> ();			// stores AMD of each interval
		for (int i = 0; i < tmpList.size(); i++) {
			double absMedian = Math.abs((double)intList.get(i) - median);	// ERROR: need to use intList, not tmpList
			tmpList2.add(absMedian);
		}
		
		// Find the median of AMD
		double mad = 0;
		ArrayList<Double> tmpList3 = (ArrayList<Double>) tmpList2.clone();
		Collections.sort(tmpList3);
		mad = tmpList3.get(tmpList3.size()/2);
		if (tmpList3.size() % 2 == 0) {
			mad += tmpList3.get(tmpList3.size()/2+1);
			mad /= 2;
		}
		
		// Calculate the modified Z-scores and record outliers
		ArrayList<Integer> outliers = new ArrayList<Integer> ();
		for (int i = 0; i < tmpList2.size(); i++) {
			double Mi = 0.6475 * tmpList2.get(i) / mad;
			if (Mi > 3.5) {			// Finally find outlier intervals
				outliers.add(i);
			}
		}
		
		// If there is no outlier, just return null
		if (outliers.size() == 0) {
			return null;
		}
		
		// If there are too many outliers, return null
		final double maxIntOutliersTolerance = 0.6;
		if (outliers.size() >= intList.size() * maxIntOutliersTolerance) {
			System.out.println("[X] Too many interval outliers or too few data points. "
					+ "Please resubmit data for optimization.");
			return null;
			//return (ArrayList<Long>) input.clone();
		}
		
		// Find the outliers in initial data
		// -> Problem: how to decide which side of the interval causes the outlier?
		// -> Solution: assume there cannot be many consecutive outlier beats, then
		//		use consecutive outlier intervals to detect outlier beats
		
		// 1. Find consecutive outlier intervals
		ArrayList<Integer> consOutlierStart = new ArrayList<Integer> ();
		ArrayList<Integer> consOutlierSize = new ArrayList<Integer> ();
		consOutlierStart.add(outliers.get(0)); consOutlierSize.add(1);
		for (int i = 1; i < outliers.size(); i++) {
			if (!outliers.get(i).equals(outliers.get(i-1)+1)) {
				consOutlierStart.add(outliers.get(i));
				consOutlierSize.add(1);
			} else {
				int lastIndex = consOutlierSize.size()-1;
				consOutlierSize.set(lastIndex, consOutlierSize.get(lastIndex)+1);
			}
		}
		
		System.out.print("Consecutive outliers: ");
		for (int i = 0; i < consOutlierStart.size(); i++) {
			System.out.print("(" + consOutlierStart.get(i) + " , " + consOutlierSize.get(i) + "), ");
		}
		System.out.println("");
		
		// Distinguish beat outliers
		// -> maxConsOutliers: maximum consecutive outliers allowed
		final int maxConsBeatsOutliers = 4;
		ArrayList<Integer> beatOutliers = new ArrayList<Integer> ();
		for (int i = 0; i < consOutlierStart.size(); i++) {
			Integer start = consOutlierStart.get(i), size = consOutlierSize.get(i);
			if (size == 1) {
				// If it has only one consecutive outlier interval
				if (i == 0) {
					// If this is the starting interval, then the starting beat is outlier
					beatOutliers.add(0);
				} else if (start == intList.size()-1) {
					// If this is the last interval, then the ending beat is outlier
					beatOutliers.add(input.size()-1);
				} else {
					Integer lastStart = consOutlierStart.get(i-1) + consOutlierSize.get(i-1), lastSize = start - lastStart + 1;
					if (lastSize + consOutlierSize.get(i-1) <= maxConsBeatsOutliers) {
						// This may not be the case when actually two or more consecutive beat outliers happen
						// -> So instead we'd better merge it to the previous outlier data
						System.out.println("[?] Stand-alone outlier data at interval <" + start 
								+ " , " + start+1 + ">. Merging into previous outlier data.");
						for (int j = 0; j <= lastSize; j++) {
							beatOutliers.add(lastStart+j);
						}
					} else {
						// If this outlier is too far away from the last one, there must be something wrong
						System.out.println("[X] Stand-alone outlier data at interval <" + start 
								+ " , " + start+1 + ">. Please check data.");
					}
				}
			} else {
				// Add every beat in the middle to outlier list
				for (int j = 1; j < size; j++) {
					beatOutliers.add(start+j);
				}
			}
		}
		
		System.out.print("-> Beats outliers: ");
		for (int i = 0; i < beatOutliers.size(); i++) {
			System.out.print(beatOutliers.get(i) + "->" + input.get(beatOutliers.get(i)) + ", ");
		}
		System.out.println("");
		
		// If there are too many outliers, return null
		final double maxBeatsOutliersTolerance = maxIntOutliersTolerance / 2;
		if (beatOutliers.size() >= input.size() * maxBeatsOutliersTolerance) {
			System.out.println("[X] Too many beats outliers or too few data points. "
					+ "Please resubmit data for optimization.");
			return null;
			//return (ArrayList<Long>) input.clone();
		}
		
		return beatOutliers;
	}*/
	
	/**
	 * Optimize the beat list using statistics methods.
	 * @param input input beat list to be optimized
	 * @return optimized beat list
	 */
	public static ArrayList<Long> optimize (ArrayList<Long> input) {
		// statistically optimize the input beat list and outputs the optimized list
		ArrayList<Long> output = new ArrayList<Long> ();
		
		// 1. detect and remove outliers
		ArrayList<Integer> outliers = detectOutliers (input);
		// -> generate beats without outliers
		ArrayList<Long> rBeats = (ArrayList<Long>) input.clone();
		ArrayList<Integer> rIndices = new ArrayList<Integer> ();
		for (int i = 0; i < input.size(); i++) {
			rIndices.add(i);
		}
		if (outliers != null) {
			for (int i = outliers.size()-1; i >= 0; i--) {
				rIndices.remove(outliers.get(i).intValue());
				rBeats.remove(outliers.get(i).intValue());
			}
		}
		
		// 2. generate linear curve
		LinearRegression lr = new LinearRegression (rIndices, rBeats);
		if (!lr.isSucceeded()) {
			System.out.println("[X] Linear regression failed for optimization. Please check data and resubmit.");
		}
		
		// 3. make up for missing/outlier beats
		// -> or just re-sort out every data point in here
		double coef0 = lr.getCoef0(), coef1 = lr.getCoef1();
		for (int i = 0; i < input.size(); i++) {
			double dataPoint = coef1 * i + coef0;
			output.add((long)(dataPoint+0.5));	// Rounding 4-5
		}
		
		return output;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Test optimize method
		final int nBeats = 20;
		ArrayList<Long> beats = new ArrayList<Long> ();
		
		// Generate data
		final Long baseMultiplier = (long)100, normError = (long)5;
		for (int i = 0; i < nBeats; i++) {
			beats.add((Long)(baseMultiplier*i + (long)(normError*Math.random())));
		}
		
		// Generate outliers
		System.out.print("Original outliers: ");
		double probOutlier = 0.1; Long outlierError = (long) baseMultiplier;
		for (int i = 0; i < nBeats; i++) {
			if (Math.random() <= probOutlier) {
				beats.set(i, beats.get(i)+(long)(outlierError*(1+Math.random())/2));	// 0.5-1.0 * outlierError
				System.out.print(i+"->"+beats.get(i)+", ");
			}
		}
		System.out.println("");
		
		// Print out original
		System.out.print("Original: ");
		for (int i = 0; i < nBeats; i++) {
			System.out.print(beats.get(i) + ", ");
		}
		System.out.println("");

		
		
		// Detect outliers
		ArrayList<Integer> outliers = detectOutliers(beats);
		
		// Print out outliers
		if (outliers != null) {
			System.out.print("Detected outliers: ");
			for (int i = 0; i < outliers.size(); i++) {
				System.out.print(outliers.get(i) + "->" + beats.get(outliers.get(i).intValue()) + ", ");
			}
			System.out.println("");
		}
		
		
		
		// Optimize
		ArrayList<Long> oBeats = optimize(beats);
		
		// Print out optimized
		System.out.print("Optimized: ");
		for (int i = 0; i < nBeats; i++) {
			System.out.print(oBeats.get(i) + ", ");
		}
		System.out.println("");
	}
}
