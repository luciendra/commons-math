/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.commons.math.stat;
import org.apache.commons.math.stat.distribution.DistributionFactory;
import org.apache.commons.math.stat.distribution.TDistribution;

/**
 * Estimates an ordinary least squares regression model
 * with one independent variable.
 * <p>
 * <code> y = intercept + slope * x  </code>
 * <p>
 * Standard errors for <code>intercept</code> and <code>slope</code> are 
 * available as well as ANOVA, r-square and Pearson's r statistics.
 * <p>
 * Observations (x,y pairs) can be added to the model one at a time or they 
 * can be provided in a 2-dimensional array.  The observations are not stored
 * in memory, so there is no limit to the number of observations that can be
 * added to the model. 
 * <p>
 * <strong>Usage Notes</strong>: <ul>
 * <li> When there are fewer than two observations in the model, or when
 * there is no variation in the x values (i.e. all x values are the same) 
 * all statistics return <code>NaN</code>. At least two observations with
 * different x coordinates are requred to estimate a bivariate regression 
 * model.
 * </li>
 * <li> getters for the statistics always compute values based on the current
 * set of observations -- i.e., you can get statistics, then add more data
 * and get updated statistics without using a new instance.  There is no 
 * "compute" method that updates all statistics.  Each of the getters performs
 * the necessary computations to return the requested statistic.</li>
 * </ul>
 *
 * @author  Phil Steitz
 * @version $Revision: 1.2 $ $Date: 2003/06/11 03:33:05 $
 */
public class BivariateRegression {
    
    /** sum of x values */
    private double sumX = 0d;
    
    /** sum of squared x values */
    private double sumSqX = 0d;
    
    /** sum of y values */
    private double sumY = 0d;
    
    /** sum of squared y values */
    private double sumSqY = 0d;
    
    /** sum of products */
    private double sumXY = 0d;
    
    /** number of observations */
    private long n = 0;
    
    // ---------------------Public methods--------------------------------------
    
    /**
     * Adds the observation (x,y) to the regression data set
     *
     * @param x independent variable value
     * @param y dependent variable value
     */
    public void addData(double x, double y) {
        sumX += x;
        sumSqX += x * x;
        sumY += y;
        sumSqY += y * y;
        sumXY += x * y;
        n++;
    } 
    
    /**
     * Adds the observations represented by the elements in 
     * <code>data</code>.
     * <p>
     * <code>(data[0][0],data[0][1])</code> will be the first observation, then
     * <code>(data[1][0],data[1][1])</code>, etc. <p> 
     * 
     * This method does not replace data that has already been added.  
     * To replace all data, use <code>clear()</code> before adding the new 
     * data.
     * 
     * @param data array of observations to be added
     */
    public void addData(double[][] data) {
       for (int i = 0; i < data.length; i++) {
            addData(data[i][0], data[i][1]);
       }
    }
    
    /**
     * Clears all data from the model.
     */
    public void clear() {
        sumX = 0d;
        sumSqX = 0d;
        sumY = 0d;
        sumSqY = 0d;
        sumXY = 0d;
        n = 0;
    }
          
    /**
     * Returns the number of observations that have been added to the model.
     *
     * @return n number of observations that have been added.
     */
    public long getN() {
        return n;
    }
    
    /**
     * Returns the "predicted" <code>y</code> value associated with the 
     * supplied <code>x</code> value.
     * <p>
     * <code> predict(x) = intercept + slope * x </code>
     * <p>
     * <strong>Preconditions</strong>: <ul>
     * <li>At least two observations (with at least two different x values)
     * must have been added before invoking this method. If this method is 
     * invoked before a model can be estimated, <code>Double,NaN</code> is
     * returned.
     * </li></ul>
     *
     * @param x input <code>x</code> value
     * @return predicted <code>y</code> value
     */
    public double predict(double x) {
        double b1 = getSlope();
        return getIntercept(b1) + b1 * x;
    }
    
    /**
     * Returns the intercept of the estimated regression line.
     * <p>
     * The least squares estimate of the intercept is computed using the 
     * <a href="http://www.xycoon.com/estimation4.htm">normal equations</a>.
     * The intercept is sometimes denoted b0. 
     * <p>
     * <strong>Preconditions</strong>: <ul>
     * <li>At least two observations (with at least two different x values)
     * must have been added before invoking this method. If this method is 
     * invoked before a model can be estimated, <code>Double,NaN</code> is
     * returned.
     * </li></ul>
     *
     * @return the intercept of the regression line
     */
     public double getIntercept() {
         return getIntercept(getSlope());
     }
     
     /**
     * Returns the slope of the estimated regression line.  
     * <p>
     * The least squares estimate of the slope is computed using the 
     * <a href="http://www.xycoon.com/estimation4.htm">normal equations</a>.
     * The slope is sometimes denoted b1. 
     * <p>
     * <strong>Preconditions</strong>: <ul>
     * <li>At least two observations (with at least two different x values)
     * must have been added before invoking this method. If this method is 
     * invoked before a model can be estimated, <code>Double,NaN</code> is
     * returned.
     * </li></ul>
     *
     * @return the slope of the regression line
     */
     public double getSlope() {
         if (n < 2) {
             return Double.NaN;  //not enough data 
         }
         double dn = (double) n;
         double denom = sumSqX - (sumX * sumX / dn);
         if (Math.abs(denom) < 10 * Double.MIN_VALUE) { 
             return Double.NaN; //not enough variation in x
         }
         return (sumXY - (sumX * sumY / dn)) / denom;
     }
     
     /**
      * Returns the <a href="http://www.xycoon.com/SumOfSquares.htm">
      * sum of squared errors</a> (SSE) associated with the regression 
      * model.
      * <p>
      * <strong>Preconditions</strong>: <ul>
      * <li>At least two observations (with at least two different x values)
      * must have been added before invoking this method. If this method is 
      * invoked before a model can be estimated, <code>Double,NaN</code> is
      * returned.
      * </li></ul>
      *
      * @return sum of squared errors associated with the regression model
      */
     public double getSumSquaredErrors() {
         return getSumSquaredErrors(getSlope());
     }
     
     /**
      * Returns the sum of squared deviations of the y values about their mean.
      * <p>
      * This is defined as SSTO 
      * <a href="http://www.xycoon.com/SumOfSquares.htm">here</a>.
      * <p>
      * If <code>n < 2</code>, this returns <code>Double.NaN</code>.
      *
      * @return sum of squared deviations of y values
      */
     public double getTotalSumSquares() {
         if (n < 2) {
             return Double.NaN;
         }
         return sumSqY - sumY * sumY / (double) n;
     }
         
     /**
      * Returns the sum of squared deviations of the predicted y values about 
      * their mean (which equals the mean of y).
      * <p>
      * This is usually abbreviated SSR or SSM.  It is defined as SSM 
      * <a href="http://www.xycoon.com/SumOfSquares.htm">here</a>
      * <p>
      * <strong>Preconditions</strong>: <ul>
      * <li>At least two observations (with at least two different x values)
      * must have been added before invoking this method. If this method is 
      * invoked before a model can be estimated, <code>Double,NaN</code> is
      * returned.
      * </li></ul>
      *
      * @return sum of squared deviations of y values
      */
     public double getRegressionSumSquares() {
         double b1 = getSlope();
         return b1 * (sumXY - sumX * sumY / (double) n);
     }
     
     /**
      * Returns the sum of squared errors divided by the degrees of freedom,
      * usually abbreviated MSE. 
      * <p>
      * If there are fewer than <strong>three</strong> data pairs in the model,
      * or if there is no variation in <code>x</code>, this returns 
      * <code>Double.NaN</code>.
      *
      * @return sum of squared deviations of y values
      */
     public double getMeanSquareError() {
         if (n < 3) {
             return Double.NaN;
         }
         double sse = getSumSquaredErrors();
         return sse / (double) (n - 2);
     }
     
     /**
      * Returns <a href="http://www.stt.msu.edu/~xiaoyimi/STT200/Lecture5.pdf">
      * Pearson's product moment correlation coefficient</a>,
      * usually denoted r. 
      * <p>
      * <strong>Preconditions</strong>: <ul>
      * <li>At least two observations (with at least two different x values)
      * must have been added before invoking this method. If this method is 
      * invoked before a model can be estimated, <code>Double,NaN</code> is
      * returned.
      * </li></ul>
      *
      * @return Pearson's r
      */
     public double getR() {
         double b1 = getSlope();
         double result = Math.sqrt(getRSquare(b1));
         if (b1 < 0) {
             result = -result;
         }
         return result;
     }
             
     /** 
      * Returns the <a href="http://www.xycoon.com/coefficient1.htm"> 
      * coefficient of determination</a>,
      * usually denoted r-square. 
      * <p>
      * <strong>Preconditions</strong>: <ul>
      * <li>At least two observations (with at least two different x values)
      * must have been added before invoking this method. If this method is 
      * invoked before a model can be estimated, <code>Double,NaN</code> is
      * returned.
      * </li></ul>
      *
      * @return r-square
      */
     public double getRSquare() {
         return getRSquare(getSlope());
     }
     
         
     /**
      * Returns the <a href="http://www.xycoon.com/standarderrorb0.htm">
      * standard error of the intercept estimate</a>, 
      * usually denoted s(b0). 
      * <p>
      * If there are fewer that <strong>three</strong> observations in the 
      * model, or if there is no variation in x, this returns 
      * <code>Double.NaN</code>.
      *
      * @return standard error associated with intercept estimate
      */
     public double getInterceptStdErr() {
         double ssx = getSumSquaresX();
         return Math.sqrt(getMeanSquareError() * sumSqX / (((double) n) * ssx));
     }
             
     /**
      * Returns the <a href="http://www.xycoon.com/standerrorb(1).htm">standard
      * error of the slope estimate</a>,
      * usually denoted s(b1). 
      * <p>
      * If there are fewer that <strong>three</strong> data pairs in the model,
      * or if there is no variation in x, this returns <code>Double.NaN</code>.
      *
      * @return standard error associated with slope estimate
      */
     public double getSlopeStdErr() {
         double ssx = getSumSquaresX();
         return Math.sqrt(getMeanSquareError() / ssx);
     }
     
     /**
      * Returns the half-width of a 95% confidence interval for the slope
      * estimate.
      * <p>
      * The 95% confidence interval is 
      * <p>
      * <code>(getSlope() - getSlopeConfidenceInterval(), 
      * getSlope() + getSlopeConfidenceInterval())</code>
      * <p>
      * If there are fewer that <strong>three</strong> observations in the 
      * model, or if there is no variation in x, this returns 
      * <code>Double.NaN</code>.
      * <p>
      * <strong>Usage Note</strong>:<br>
      * The validity of this statistic depends on the assumption that the 
      * observations included in the model are drawn from a
      * <a href="http://mathworld.wolfram.com/
      * BivariateNormalDistribution.html">Bivariate Normal Distribution</a>.
      *
      * @return half-width of 95% confidence interval for the slope estimate
      */
     public double getSlopeConfidenceInterval() {   
        return getSlopeConfidenceInterval(0.05d); 
     }
     
     /**
      * Returns the half-width of a (100-100*alpha)% confidence interval for 
      * the slope estimate.
      * <p>
      * The (100-100*alpha)% confidence interval is 
      * <p>
      * <code>(getSlope() - getSlopeConfidenceInterval(), 
      * getSlope() + getSlopeConfidenceInterval())</code>
      * <p>
      * To request, for example, a 99% confidence interval, use 
      * <code>alpha = .01</code>
      * <p>
      * <strong>Usage Note</strong>:<br>
      * The validity of this statistic depends on the assumption that the 
      * observations included in the model are drawn from a
      * <a href="http://mathworld.wolfram.com/
      * BivariateNormalDistribution.html">Bivariate Normal Distribution</a>.
      * <p>
      * <strong> Preconditions:</strong><ul>
      * <li>If there are fewer that <strong>three</strong> observations in the 
      * model, or if there is no variation in x, this returns 
      * <code>Double.NaN</code>. 
      * </li>
      * <li><code>(0 < alpha < 1)</code>; otherwise an 
      * <code>IllegalArgumentException</code> is thrown.
      * </li></ul>    
      *
      * @param alpha the desired significance level 
      * @return half-width of 95% confidence interval for the slope estimate
      */
     public double getSlopeConfidenceInterval(double alpha) { 
         if (alpha >= 1 || alpha <= 0) {
             throw new IllegalArgumentException();
         }
         return getSlopeStdErr() * 
            getTDistribution().inverseCummulativeProbability(1d - alpha / 2d); 
     }
     
     /**
      * Returns the significance level of the slope (equiv) correlation. 
      * <p>
      * Specifically, the returned value is the smallest <code>alpha</code>
      * such that the slope confidence interval with significance level
      * equal to <code>alpha</code> does not include <code>0</code>.
      * On regression output, this is often denoted <code>Prob(|t| > 0)</code>
      * <p>
      * <strong>Usage Note</strong>:<br>
      * The validity of this statistic depends on the assumption that the 
      * observations included in the model are drawn from a
      * <a href="http://mathworld.wolfram.com/
      * BivariateNormalDistribution.html">Bivariate Normal Distribution</a>.
      * <p>
      * If there are fewer that <strong>three</strong> observations in the 
      * model, or if there is no variation in x, this returns 
      * <code>Double.NaN</code>.
      *
      * @return significance level for slope/correlation
      */
     public double getSignificance() {
         return (1d - getTDistribution().cummulativeProbability(
                Math.abs(getSlope()) / getSlopeStdErr()));
     }
     
     // ---------------------Private methods-----------------------------------
     
     /**
     * Returns the intercept of the estimated regression line, given the slope.
     * <p>
     * Will return <code>NaN</code> if slope is <code>NaN</code>.
     *
     * @param slope current slope
     * @return the intercept of the regression line
     */
     private double getIntercept(double slope) {
         return (sumY - slope * sumX) / ((double) n);
     }
       
     /**
      * Returns the sum of squared errors associated with the regression 
      * model, using the slope of the regression line. 
      * <p> 
      * Returns NaN if the slope is NaN.
      * 
      * @param b1 current slope
      * @return sum of squared errors associated with the regression model
      */
     private double getSumSquaredErrors(double b1) {
         double b0 = getIntercept(b1);
         return sumSqY - b0 * sumY - b1 * sumXY;
     } 
     
     /**
      * Returns the sum of squared deviations of the x values about their mean.
      * <p>
      * If n < 2, this returns NaN.
      *
      * @return sum of squared deviations of x values
      */
     private double getSumSquaresX() {
         if (n < 2) {
             return Double.NaN;
         }
         return sumSqX - sumX * sumX / (double) n;
     }
     
     /** 
      * Computes r-square from the slope.
      * <p>
      * will return NaN if slope is Nan.
      *
      * @param b1 current slope
      * @return r-square
      */
     private double getRSquare(double b1) {
         double ssto = getTotalSumSquares();
         return (ssto - getSumSquaredErrors(b1)) / ssto;
     }
     
     /**
      * Uses distribution framework to get a t distribution instance 
      * with df = n - 2
      *
      * @return t distribution with df = n - 2
      */
     private TDistribution getTDistribution() {
         return DistributionFactory.newInstance().createTDistribution(n - 2);
     }
}

