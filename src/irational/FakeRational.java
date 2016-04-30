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
public class FakeRational implements IRational 
{
    static FakeRational ZERO = new FakeRational(0);
    static FakeRational ONE = new FakeRational(1);

    public static class Factory implements IRational.Factory
    {
        public IRational valueOf(int dividend, int divisor)
        {
            if (dividend==0) return ZERO;
            if (dividend==divisor) return ONE;
            return new FakeRational((double)dividend/divisor);
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
        int hash = 3;
        hash = 23 * hash + (int) (Double.doubleToLongBits(this.v) ^ (Double.doubleToLongBits(this.v) >>> 32));
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
        final FakeRational other = (FakeRational) obj;
        if (Double.doubleToLongBits(this.v) != Double.doubleToLongBits(other.v))
        {
            return false;
        }
        return true;
    }
    
    
    
    //org.jscience.mathematics.number.FakeRational v;
    double v;    
    FakeRational(double v)
    {
        this.v=v;
    }            
    @Override
    public double doubleValue()
    {
        return v;
    }
    @Override
    public int compareTo(IRational oo)
    {
        FakeRational o = (FakeRational)oo;
        if (v < o.v) return -1;
        if (v > o.v) return 1;
        return 0;
    }
    @Override
    public IRational times(IRational oo)
    {
         FakeRational o = (FakeRational)oo;
         return new FakeRational(v*o.v);
    }
    @Override
    public FakeRational plus(IRational oo)
    {
        FakeRational o = (FakeRational)oo;
        return new FakeRational(v+o.v);
    }
    @Override
    public IRational divide(IRational oo)
    {
        FakeRational o = (FakeRational)oo;
        return new FakeRational(v/o.v);
    }
    @Override
    public IRational minus(IRational oo)
    {
        FakeRational o = (FakeRational)oo;
        return new FakeRational(v - o.v);
    }
    @Override
    public boolean isZero()
    {
        return v == 0;
    }

}
