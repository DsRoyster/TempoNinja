package edu.asu.fall15.cse535.group42.temponinja;

/**
 * Interface implemented by classes that can handle notifications about steps.
 * These classes can be passed to StepDetector.
 * @author Levente Bagi
 */
public interface intCadenceListener {
	public void onStep();
	public void passValue();
}