package edu.asu.fall15.cse535.group42.temponinja;

import java.util.ArrayList;

/******************************************************************************
 *  Compilation:  javac LinearRegression.java StdIn.java
 *  Execution:    java LinearRegression < data.txt
 *  
 *  Reads in a sequence of pairs of real numbers and computes the
 *  best fit (least squares) line y  = ax + b through the set of points.
 *  Also computes the correlation coefficient and the standard errror
 *  of the regression coefficients.
 *
 *  Note: the two-pass formula is preferred for stability.
 *
 ******************************************************************************/


public class LinearRegression {
	
	private double		coef0 = 0;
	private double		coef1 = 0;
	private double		ssto = 0;
	private double		sse = 0;
	private double		ssr = 0;
	private double		r2 = 0;
	private boolean		succ = false;
	
	
	public LinearRegression () {  }
	public LinearRegression (ArrayList<Integer> x, ArrayList<Long> y) { 
		regress (x, y);
	}
	
	
	/**
	 * Get coef0
	 * @return coef0
	 */
	public double getCoef0 () {
		if (!succ) return 0;
		return coef0;
	}
	/**
	 * Get coef1
	 * @return coef1
	 */
	public double getCoef1 () {
		if (!succ) return 0;
		return coef1;
	}
	/**
	 * Get SSTO
	 * @return SSTO
	 */
	public double getSSTO () {
		if (!succ) return 0;
		return ssto;
	}
	/**
	 * Get SSE
	 * @return SSE
	 */
	public double getSSE () {
		if (!succ) return 0;
		return sse;
	}
	/**
	 * Get SSR
	 * @return SSR
	 */
	public double getSSR () {
		if (!succ) return 0;
		return ssr;
	}
	/**
	 * Get R^2
	 * @return R^2
	 */
	public double getR2 () {
		if (!succ) return 0;
		return r2;
	}
	/**
	 * @return true if regression succeeded
	 */
	public boolean isSucceeded () {
		return succ;
	}
	
	/**
	 * Clear all status.
	 */
	public void clear () {
		coef0 	= 0;
		coef1 	= 0;
		ssto 	= 0;
		sse 	= 0;
		ssr 	= 0;
		r2 		= 0;
		succ 	= false;
	}
	
	/**
	 * Linear regression saving results to the object.
	 * @param x input x array
	 * @param y input y array
	 * @return true if the regression succeeded
	 */
    public boolean regress (ArrayList<Integer> x, ArrayList<Long> y) { 
    	clear();
    	
    	// If empty or in different dimension, infeasible
        if (x == null || y == null || x.size() != y.size()) {
        	succ = false;
        	return false;
        }
        int n = x.size();

        // first pass: read in data, compute xbar and ybar
        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
        for (int i = 0; i < n; i++) {
            sumx  += x.get(i);
            sumx2 += x.get(i) * x.get(i);
            sumy  += y.get(i);
        }
        double xbar = sumx / n;
        double ybar = sumy / n;

        // second pass: compute summary statistics
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++) {
            xxbar += (x.get(i) - xbar) * (x.get(i) - xbar);
            yybar += (y.get(i) - ybar) * (y.get(i) - ybar);
            xybar += (x.get(i) - xbar) * (y.get(i) - ybar);
        }
        double beta1 = xybar / xxbar;
        double beta0 = ybar - beta1 * xbar;

        // print results
        System.out.println("y   = " + beta1 + " * x + " + beta0);
        coef0 = beta0;
        coef1 = beta1;

        // analyze results
        int df = n - 2;
        double rss = 0.0;      // residual sum of squares
        double ssr = 0.0;      // regression sum of squares
        for (int i = 0; i < n; i++) {
            double fit = beta1*x.get(i) + beta0;
            rss += (fit - y.get(i)) * (fit - y.get(i));
            ssr += (fit - ybar) * (fit - ybar);
        }
        double R2    = ssr / yybar;
        double svar  = rss / df;
        double svar1 = svar / xxbar;
        double svar0 = svar/n + xbar*xbar*svar1;
//        r2 = R2; System.out.println("R^2                 = " + R2);
//        System.out.println("std error of beta_1 = " + Math.sqrt(svar1));
//        System.out.println("std error of beta_0 = " + Math.sqrt(svar0));
//        svar0 = svar * sumx2 / (n * xxbar);
//        System.out.println("std error of beta_0 = " + Math.sqrt(svar0));
//
//        ssto = yybar; System.out.println("SSTO = " + yybar);
//        sse = rss; System.out.println("SSE  = " + rss);
//        ssr = ssr; System.out.println("SSR  = " + ssr);
        
        succ = true;
        return true;
    }

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}

}
