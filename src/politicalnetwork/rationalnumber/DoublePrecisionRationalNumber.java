/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.rationalnumber;




/**
 * This class supports the RationalNumber interface using double precision
 * floating point values.
 * This class needs to be used in practice when the number of candidates is high 
 * because infinite precision rational numbers are too slow.
 *   
 * @author Removed for Blind Review
 */
public class DoublePrecisionRationalNumber implements RationalNumber 
{
    static DoublePrecisionRationalNumber ZERO = new DoublePrecisionRationalNumber(0);
    static DoublePrecisionRationalNumber ONE = new DoublePrecisionRationalNumber(1);

    public static class Factory implements RationalNumber.Factory
    {
        public RationalNumber valueOf(int dividend, int divisor)
        {
            if (dividend==0) return ZERO;
            if (dividend==divisor) return ONE;
            return new DoublePrecisionRationalNumber((double)dividend/divisor);
        }    
        double maxError = 0;
        public boolean isClose(RationalNumber v1,RationalNumber v2)
        {
            double dif = Math.abs(v1.doubleValue()-v2.doubleValue());
            if (dif > maxError)
                maxError = dif;
            return dif < 1e-8 * (v1.doubleValue()+v2.doubleValue());
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
        final DoublePrecisionRationalNumber other = (DoublePrecisionRationalNumber) obj;
        if (Double.doubleToLongBits(this.v) != Double.doubleToLongBits(other.v))
        {
            return false;
        }
        return true;
    }
              
    double v;    
    DoublePrecisionRationalNumber(double v)
    {
        this.v=v;
    }            
    @Override
    public double doubleValue()
    {
        return v;
    }
    @Override
    public int compareTo(RationalNumber oo)
    {
        DoublePrecisionRationalNumber o = (DoublePrecisionRationalNumber)oo;
        if (v < o.v) return -1;
        if (v > o.v) return 1;
        return 0;
    }
    @Override
    public RationalNumber times(RationalNumber oo)
    {
         DoublePrecisionRationalNumber o = (DoublePrecisionRationalNumber)oo;
         return new DoublePrecisionRationalNumber(v*o.v);
    }
    @Override
    public DoublePrecisionRationalNumber plus(RationalNumber oo)
    {
        DoublePrecisionRationalNumber o = (DoublePrecisionRationalNumber)oo;
        return new DoublePrecisionRationalNumber(v+o.v);
    }
    @Override
    public RationalNumber divide(RationalNumber oo)
    {
        DoublePrecisionRationalNumber o = (DoublePrecisionRationalNumber)oo;
        return new DoublePrecisionRationalNumber(v/o.v);
    }
    @Override
    public RationalNumber minus(RationalNumber oo)
    {
        DoublePrecisionRationalNumber o = (DoublePrecisionRationalNumber)oo;
        return new DoublePrecisionRationalNumber(v - o.v);
    }
    @Override
    public boolean isZero()
    {
        return v == 0;
    }

}
