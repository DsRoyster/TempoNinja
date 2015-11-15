package edu.asu.fall15.cse535.group42.temponinja;

/**
 * 
 */

/**
 * TempoInfo keeps information about the tempo properties of
 * a clip file. This information is attributed by the following
 * fields:	<br />
 * -> tempo (long): tempo / frequency / bpm of the clip <br />
 * -> anchor (long): anchor timestamp from the start of the counting 
 * (probably the start of the clip, but not necessarily) <bf />
 * @author royster
 * @since 9/1/2015
 * @version 1.0
 */
public class TempoInfo {
	
	/**
	 * Tempo
	 */
	double tempo = -1;
	/**
	 * Anchor
	 */
	double anchor = -1;
	/**
	 * Version
	 */
	long version = 0;
	
	public TempoInfo () {  }
	public TempoInfo (double itempo) {
		tempo = itempo;
	}
	public TempoInfo(double itempo, double ianchor) {
		this(itempo);
		anchor = ianchor;
	}
	public TempoInfo(double itempo, double ianchor, long iversion) {
		this(itempo, ianchor);
		version = iversion;
	}
	
	/**
	 * Get tempo information.
	 * @return (double) tempo
	 */
	public double getTempo () {
		return tempo;
	}
	/**
	 * Get anchor (start time (ms)) information.
	 * @return (double) anchor
	 */
	public double getAnchor () {
		return anchor;
	}
	/**
	 * Get version.
	 * @return (long) version
	 */
	public long getVersion () {
		return version;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
