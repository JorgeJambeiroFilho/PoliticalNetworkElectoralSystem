/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package irational;

import org.jscience.mathematics.number.Rational;

/**
 *
 * @author jesjf
 */
public class TrueRational implements IRational 
{
    static TrueRational ZERO = new TrueRational(0,1);
    static TrueRational ONE = new TrueRational(1,1);

    public static class Factory implements IRational.Factory
    {
        public IRational valueOf(int dividend, int divisor)
        {
            if (dividend==0) return ZERO;
            if (dividend==divisor) return ONE;
            return new TrueRational(dividend,divisor);
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
        final TrueRational other = (TrueRational) obj;
        if (this.v != other.v && (this.v == null || !this.v.equals(other.v)))
        {
            return false;
        }
        return true;
    }

    
        
    Rational v;
    TrueRational(Rational v)
    {
        this.v = v;
    }        
    TrueRational(int dividend,int divisor)
    {
        v = Rational.valueOf(dividend, divisor);
    }            
    @Override
    public double doubleValue()
    {
        return v.doubleValue();
    }
    @Override
    public int compareTo(IRational oo)
    {
        TrueRational o = (TrueRational)oo;
        return v.compareTo(o.v);
    }
    @Override
    public IRational times(IRational oo)
    {
         TrueRational o = (TrueRational)oo;
         return new TrueRational(v.times(o.v));
    }
    @Override
    public TrueRational plus(IRational oo)
    {
        TrueRational o = (TrueRational)oo;
        return new TrueRational(v.plus(o.v));
    }
    @Override
    public IRational divide(IRational oo)
    {
        TrueRational o = (TrueRational)oo;
        return new TrueRational(v.divide(o.v));
    }
    @Override
    public IRational minus(IRational oo)
    {
        TrueRational o = (TrueRational)oo;
        return new TrueRational(v.minus(o.v));
    }
    @Override
    public boolean isZero()
    {
        return v.isZero();
    }

}
