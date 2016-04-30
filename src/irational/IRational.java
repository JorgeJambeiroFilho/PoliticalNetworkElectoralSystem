/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package irational;

import redepolitica2.*;

/**
 *
 * @author jesjf
 */
public interface IRational extends Comparable<IRational>
{
    public interface Factory
    {
        IRational valueOf(int dividend, int divisor);
    }
    
    int compareTo(IRational o);
    IRational divide(IRational o);
    double doubleValue();
    boolean equals(Object obj);
    int hashCode();
    boolean isZero();
    IRational minus(IRational o);
    IRational plus(IRational o);
    IRational times(IRational o);
    String toString();    
}
