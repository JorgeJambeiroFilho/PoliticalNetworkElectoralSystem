/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package redepolitica2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 *
 * @author jesjf
 */
public class Testes1
{
    
    public RedePoliticaR montaRede(int seed)
    {   
        int n = 100;
        int nr = 10;
        Random r = new Random(seed);
        RedePoliticaR rp = new RedePoliticaR();
        for (int t=0; t<n; t++)   
            rp.addCandidato(t);
        
        for (int t=0; t<n; t++)
        {  
           //rp.addRelacao(t, (t+n-1) % n, Rational.valueOf(1,2));
           //rp.addRelacao(t, (t+1) % n, Rational.valueOf(1,2));            
           for (int tt=0; tt<nr; tt++)
           {    
               int i = r.nextInt(n);
               if (i!=t)
                  rp.addRelacao(t, i, Rational.valueOf(1,nr));
           }     
        } 
        for (int t=0; t<n; t++)   
            rp.defineVotos(t, t);
        
        rp.preparaCandidatos();        
        
        //rp.removeDosConjuntosDeVizinhos(2);
        //rp.removeDosConjuntosDeVizinhos(3);
        //rp.removeDosConjuntosDeVizinhos(5);
        
        return rp;
    }        
    void removeAtRandom(RedePoliticaR rp,int []nums)
    {
        nums = Arrays.copyOf(nums,nums.length);
        Random r = new Random();
        for (int t=0; t<nums.length; t++)
        {
            int i = r.nextInt(nums.length-t);
            rp.removeDosConjuntosDeVizinhos(nums[i]);
            nums[i] = nums[nums.length-t-1];
        }    
    }        
    static int [] getRandomInts(int n,int l)
    {
        int [] a = new int[n];
        for (int t=0; t<n; t++) 
            a[t] = t;
        Random r = new Random();
        int res[] = new int[l]; 
        for (int t=0; t<l; t++)
        {
            int i = r.nextInt(a.length-t);
            res[t] = a[i];
            a[i] = a[a.length-t-1];            
        }    
        return res;
    }
    public static void main(String argv[])
    {
        Testes1 evp = new Testes1();
        Random r = new Random();
        int seed = r.nextInt();
        RedePoliticaR rp1 = evp.montaRede(seed);
        RedePoliticaR rp2 = evp.montaRede(seed);
        int rem[] = getRandomInts(100,90); 
                //{ 3,4,5,6,7,8,9,10,11,12,50,51,52,53,54,55 };         
        evp.removeAtRandom(rp1,rem);
        evp.removeAtRandom(rp2,rem);
        System.out.println(rp1.toString());
        System.out.println("________________________________________________");        
        System.out.println(rp2.toString());
        System.out.println("________________________________________________");        
        System.out.println(rp1.equals(rp2));
    }        
    
}
