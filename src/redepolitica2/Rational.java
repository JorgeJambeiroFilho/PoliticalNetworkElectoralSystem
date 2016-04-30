/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package redepolitica2;


/**
 *
 * @author jesjf
 */
public class Rational 
{
    static Rational ZERO = new Rational(0);
    static Rational ONE = new Rational(1);

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
        final Rational other = (Rational) obj;
        if (Double.doubleToLongBits(this.v) != Double.doubleToLongBits(other.v))
        {
            return false;
        }
        return true;
    }
    
    
    
    //org.jscience.mathematics.number.Rational v;
    double v;    
    Rational(double v)
    {
        this.v=v;
    }            
    static Rational valueOf(int dividend, int divisor)
    {
        return new Rational((double)dividend/divisor);
    }
 
    public double doubleValue()
    {
        return v;
    }
    
    public int compareTo(Rational o)
    {
        if (v < o.v) return -1;
        if (v > o.v) return 1;
        return 0;
    }
    
    public Rational times(Rational o)
    {
         return new Rational(v*o.v);
    }
    
    public Rational plus(Rational o)
    {
        return new Rational(v+o.v);
    }
    
    public Rational divide(Rational o)
    {
        return new Rational(v/o.v);
    }
    
    public Rational minus(Rational o)
    {
        return new Rational(v - o.v);
    }
    
    public boolean isZero()
    {
        return v == 0;
    }

}
