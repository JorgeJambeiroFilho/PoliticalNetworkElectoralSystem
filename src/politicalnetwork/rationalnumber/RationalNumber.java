/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.rationalnumber;



/**
 * Interface to manipulate rational numbers.
 * All numbers involved in a politcal network election are rationals.
 * 
 * @author Removed for Blind Review
 */
public interface RationalNumber extends Comparable<RationalNumber>
{
    public interface Factory
    {
        /**
         * Creates a rational number of the correct class
         * @param dividend 
         * @param divisor
         * @return The rational number 
         */
        RationalNumber valueOf(int dividend, int divisor);
        /**
         * Check if two rationals are closer than a certain limit that should be compatible
         * with the precision of the Rationalnumber class actually used. The margin should
         * consider that erros may grow due to sequential aritimetic operations that occur 
         * in one election.
         * This method is only used in tests to check if theoretical results are being met.
         * 
         * @param v1
         * @param v2
         * @return True if the numbers are close enough
         */
        boolean isClose(RationalNumber v1,RationalNumber v2);
    }
    
    int compareTo(RationalNumber o);
    boolean equals(Object obj);
    int hashCode();    
    double doubleValue();
    boolean isZero();
    RationalNumber divide(RationalNumber o);    
    RationalNumber minus(RationalNumber o);
    RationalNumber plus(RationalNumber o);
    RationalNumber times(RationalNumber o);
    String toString();    
}
