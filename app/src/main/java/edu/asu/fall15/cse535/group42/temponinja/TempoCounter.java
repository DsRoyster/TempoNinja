package edu.asu.fall15.cse535.group42.temponinja;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * @author royster
 */

/**
 * <p>The main TempoCounter class with input of beating time and continuously calculates 
 * the tempo. After start() the counter, all timestamps before end() will be recorded, 
 * based on which the final cumulative tempo will be calculated. The more (accurate) data
 * points, the more accurate the estimation. </p>
 * Use start() and end() to start and end counting. <br/>
 * Use beat() to beat continuously. <br/>
 * Use clear() to clear all states. <br/>
 * Use lock() to lock the states, so that no beat() or clear() can carry on; use unlock() 
 * to unlock. <br/>
 * Use isStarted(), isEnded() and isLocked() to check the corresponding statuses. <br/>
 * Use getStartTime(), getFirstBeat(), getLastBeat() and getEndTime() to get the corresponding times. <br/>
 * Use getTempo() to get the cumulative tempo (BPM) data. <br/>
 * 
 * @author royster
 * @since 08/18/15
 * @version 1.0
 */
public class TempoCounter {
	
	/** 
     * startTime for a measure of type Long
     */ 
	private Long startTime = new Long (0);

	/** 
     * lastTime for a measure of type Long
     */ 
	private Long lastTime = new Long (0);

	/** 
     * endTime for a measure of type Long
     */ 
	private Long endTime = new Long (0);
	
	/**
     * List of tempo points till now, as an ArrayList of Longs
     */ 
	private ArrayList<Long> beats = new ArrayList<Long> ();
	
	/** 
     * Cumulative time from the first beat
     */ 
	private Long cumTime = new Long (0);
	
	/** 
     * Cumulative tempo at current time
     */ 
	private double cumTempo = 0;
	
	/** 
     * Boolean of whether the counter is locked.
     */ 
	private boolean locked = false;
	
	
	
	/**
	 * Check if the counter is started
	 */
	public boolean isStarted () {
		// Check if the counter is started
		return (!startTime.equals(new Long (0)));
	}
	
	/**
	 * Check if the counter is ended
	 */
	public boolean isEnded () {
		// Check if the counter is ended
		return (!endTime.equals(new Long (0)));
	}
	
	/**
	 * Check if the counter is locked
	 */
	public boolean isLocked () {
		// Check if the counter is locked
		return locked;
	}

	/**
	 * Get the start time of this counter
	 */
	public Long getStartTime () {
		// Get the start time
		return startTime;
	}

	/**
	 * Get the end time of this counter
	 */
	public Long getEndTime () {
		// Get the start time
		return endTime;
	}

	/**
	 * Get the time of the first beat
	 */
	public Long getFirstBeat () {
		// Get the time of the last beat
		if (!isStarted() || beats.isEmpty()) {
			return new Long (0);
		}
		return beats.get(0);
	}

	/**
	 * Get the time of the last beat
	 */
	public Long getLastBeat () {
		// Get the time of the last beat
		return lastTime;
	}

	/**
	 * Get the cumulative tempo till current
	 */
	public double getTempo () {
		return cumTempo;
	}

	/**
	 * Get the anchor (start time (ms)).
	 * @return (double) anchor
	 */
	public double getAnchor () {
		return beats.get(0) - startTime;
	}
	
	
	
	/**
	 * Clear all states
	 */
	public void clear () {
		// Clear all states
		startTime = new Long (0);
		lastTime = new Long (0);
		beats.clear();
		cumTime = new Long (0);
		cumTempo = 0;
		locked = false;
	}
	
	/**
	 * Lock the counter so that no more beats are accepted.
	 */
	public void lock () {
		// lock the tempo
		locked = true;
	}
	
	/**
	 * Unlock the counter so that more beats can be accepted.
	 */
	public void unlock () {
		// unlock the tempo
		locked = false;
	}

	/**
	 * Start the counter. Return true if the counter starts.
	 * @return true if the counter starts
	 */
	public boolean start () {
		if (!isLocked()) {
			if (!isStarted()) {
				// If not started, start it
				clear();
				startTime = System.currentTimeMillis();
			} else {
				System.out.println("[!] Counter has already started.");
				return true;
			}
		} else {
			System.out.println("[X] Counter is locked. Please unlock to start.");
			return false;
		}
		
		return true;
	}

	/**
	 * End the counter. Return true if the cumulative tempo (BPM) during the counter.
	 * @return the cumulative tempo (BPM) during the counter.
	 */
	public double end () {
		if (!isLocked()) {
			if (isStarted()) {
				// If started, end it and lock it
				endTime = System.currentTimeMillis();
				lock();
			} else {
				System.out.println("[!] Counter has not started.");
				return -1;
			}
		} else {
			System.out.println("[X] Counter is locked. Please unlock to end.");
		}
		
		return getTempo();
	}
	
	/**
	 * Shoot a beat at the current time to be accounted in the cumulative tempo.
	 * In order to beat, the counter has to be started first, and cannot be locked.
	 * @return Cumulative average tempo including this beat
	 */
	public double beat () {
		if (!isLocked()) {
			if (!isStarted()) {
				// If not started, return failure
				System.out.println("[X] Counter has not started. Please start in order to beat.");
				return -1;
			} else {
				// Otherwise, count it
				Long curTime = System.currentTimeMillis();
				if (lastTime != 0) {
					cumTime += curTime - lastTime;
				}
				lastTime = curTime;
				beats.add(lastTime);
				
				// Set the cumulative tempo
				if (!cumTime.equals(new Long (0))) {
					cumTempo = (double)((beats.size()-1)*1000) * 60 / cumTime;
				}
			}
		} else {
			System.out.println("[X] Counter is locked. Please unlock to beat.");
		}
		
		return getTempo();
	}

	
	
	/**
	 * Recalculate tempo statistics based on beats.
	 */
	public void recalc () {
		// Start, last & end times
		//startTime = beats.get(0);	// no this should be time time when it starts
		lastTime = beats.get(beats.size()-1);
		if (endTime != 0) {
			endTime = lastTime;
		}
		
		// Cumulative time
		cumTime = (long)0;
		for (int i = 0; i < beats.size()-1; i++) {
			cumTime += beats.get(i+1) - beats.get(i);
		}
		
		// Cumulative tempo
		cumTempo = (double)((beats.size()-1)*1000) * 60 / cumTime;
	}
	
	/**
	 * Optimize the beating curve.
	 */
	public double optimize () {
		// Optimize the tempo information and return the optimized result
		beats = TempoOptimizer.optimize(beats);
		recalc();
		return getTempo();
	}
	
	
	
	/**
	 * Get TempoInfo from the counter.
	 * @return (class TempoInfo) tempo information
	 */
	public TempoInfo getTempoInfo () {
		TempoInfo tempoInfo = new TempoInfo (getTempo(), getAnchor());
		return tempoInfo;
	}
	
	
	/**
	 * Get user input given a prompt.
	 * @param prompt prompt information to be displayed to user
	 * @return user input String
	 */
	public static String getUserInput (String prompt) {
		String inputLine = null; 
		System.out.print(prompt + " ");
		try {
			BufferedReader is =new BufferedReader (new InputStreamReader(System.in)); 
			inputLine = is.readLine();
			if (inputLine. length () == 0) return null;
		} catch (IOException e) {
			System.out.println("IOException: " + e);
		}
		
		return inputLine;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Initialize a TempoCounter
		TempoCounter cnter = new TempoCounter ();
		
		// Start counting
		String prompt = "Press ENTER to start beating...";
		String userInput = getUserInput(prompt);
		while ((userInput==null) || (!userInput.toLowerCase().equals("kill") && !userInput.toLowerCase().equals("end"))) {
			// Process lock/unlock operations
			if (userInput!=null && userInput.toLowerCase().equals("lock")) {
				cnter.lock();
				continue;
			} else if (userInput!=null && userInput.toLowerCase().equals("unlock")) {
				cnter.lock();
				continue;
			} else if (userInput!=null && !userInput.equals("")) {
				System.out.println("[X] Command not recognized. Please re-enter.");
				userInput = getUserInput(prompt);
				continue;
			}
			
			// Start if not
			if (!cnter.isStarted()) {
				cnter.start();
			}
			
			// Do the beating otherwise
			double cumTempo = cnter.beat();
			if (!cnter.getLastBeat().equals(new Long (0))) {
				prompt = "Current cumulative BPM: " + cumTempo + "\t(enter \"end\" to stop)";
			} else {
				prompt = "Start beating...";
			}
			userInput = getUserInput(prompt);
		}
		
		// Output final result
		cnter.end();
		prompt = "The final raw BPM is: " + cnter.getTempo();
		System.out.println(prompt);
		prompt = "Non-optimized beats: \n" + cnter.beats.toString();
		System.out.println(prompt);
		
		// Optimizing
		prompt = "Optimization in process...";
		System.out.println(prompt);
		cnter.optimize ();
		prompt = "Optimized beats: \n" + cnter.beats.toString();
		System.out.println(prompt);
		prompt = "The final optimized BPM is: " + cnter.getTempo();
		System.out.println(prompt);
		prompt = "The starting anchor is: " + cnter.getAnchor();
		System.out.println(prompt);
				
		// Save to file?
		prompt = "Save to file (press ENTER to leave): ";
		userInput = getUserInput(prompt);
		// Save to file
		if (userInput != null && userInput.trim().length() > 0) {
			try {
				PrintWriter out = new PrintWriter(userInput.trim());
				out.println(String.valueOf(cnter.getTempo()));
				out.close();
				prompt = "Save to file [" + userInput.trim() + "] succeeded, exiting...";
				System.out.println(prompt);
			} catch (IOException e) {
				System.out.println("IOException: " + e);
			}
		} else {
			prompt = "Exiting without save...";
			System.out.println(prompt);
		}
	}
}
