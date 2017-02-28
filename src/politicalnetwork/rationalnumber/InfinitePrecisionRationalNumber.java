/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.rationalnumber;

import org.jscience.mathematics.number.Rational;

/**
 * This class supports the RationalNumber interface using JScience Rational. 
 * Number are represented by two unlimitedly large integers, what provides
 * infinite precision.
 * This class can only be used for a small number of candidates, because it
 * becamoes slow fast.
 * It is ideal for tests beacause, with it,  all results match the theoretical 
 * predictions exactly.
 * 
 * 
 * @author Removed for Blind Review
 */
public class InfinitePrecisionRationalNumber implements RationalNumber 
{
    static InfinitePrecisionRationalNumber ZERO = new InfinitePrecisionRationalNumber(0,1);
    static InfinitePrecisionRationalNumber ONE = new InfinitePrecisionRationalNumber(1,1);

    public static class Factory implements RationalNumber.Factory
    {
        public RationalNumber valueOf(int dividend, int divisor)
        {
            if (dividend==0) return ZERO;
            if (dividend==divisor) return ONE;
            return new InfinitePrecisionRationalNumber(dividend,divisor);
        }    
    }    
    
    @Override
    public String toString()
    {
        return ""+v;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 37 * hash + (this.v != null ? this.v.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final InfinitePrecisionRationalNumber other = (InfinitePrecisionRationalNumber) obj;
        if (this.v != other.v && (this.v == null || !this.v.equals(other.v)))
        {
            return false;
        }
        return true;
    }

    
        
    Rational v;
    InfinitePrecisionRationalNumber(Rational v)
    {
        this.v = v;
    }        
    InfinitePrecisionRationalNumber(int dividend,int divisor)
    {
        v = Rational.valueOf(dividend, divisor);
    }            
    @Override
    public double doubleValue()
    {
        return v.doubleValue();
    }
    @Override
    public int compareTo(RationalNumber oo)
    {
        InfinitePrecisionRationalNumber o = (InfinitePrecisionRationalNumber)oo;
        return v.compareTo(o.v);
    }
    @Override
    public RationalNumber times(RationalNumber oo)
    {
         InfinitePrecisionRationalNumber o = (InfinitePrecisionRationalNumber)oo;
         return new InfinitePrecisionRationalNumber(v.times(o.v));
    }
    @Override
    public InfinitePrecisionRationalNumber plus(RationalNumber oo)
    {
        InfinitePrecisionRationalNumber o = (InfinitePrecisionRationalNumber)oo;
        return new InfinitePrecisionRationalNumber(v.plus(o.v));
    }
    @Override
    public RationalNumber divide(RationalNumber oo)
    {
        InfinitePrecisionRationalNumber o = (InfinitePrecisionRationalNumber)oo;
        return new InfinitePrecisionRationalNumber(v.divide(o.v));
    }
    @Override
    public RationalNumber minus(RationalNumber oo)
    {
        InfinitePrecisionRationalNumber o = (InfinitePrecisionRationalNumber)oo;
        return new InfinitePrecisionRationalNumber(v.minus(o.v));
    }
    @Override
    public boolean isZero()
    {
        return v.isZero();
    }

}
