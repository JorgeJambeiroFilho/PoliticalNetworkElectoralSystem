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
        RationalNumber valueOf(int dividend, int divisor);
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
