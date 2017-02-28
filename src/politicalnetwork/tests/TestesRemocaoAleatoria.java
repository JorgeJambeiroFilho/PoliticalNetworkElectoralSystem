/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.tests;

import politicalnetwork.core.PoliticalNetwork;
import politicalnetwork.core.IPoliticalNetwork;
import politicalnetwork.rationalnumber.InfinitePrecisionRationalNumber;
import politicalnetwork.rationalnumber.RationalNumber;
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
 * @author jesjf22
 */
public class TestesRemocaoAleatoria
{
    
    public IPoliticalNetwork montaRede(int seed,String name,int numTh)
    {   
        int n = 100;
        int nr = 10;
        Random r = new Random(seed);
        RationalNumber.Factory rationalFactory = new InfinitePrecisionRationalNumber.Factory();
        IPoliticalNetwork rp = new PoliticalNetwork(rationalFactory,"Rede "+name,numTh,false);
        for (int t=1; t<=n; t++)   
            rp.addCandidate(t);
        
        for (int t=1; t<=n; t++)
        {  
           int [] vv = getRandomInts(n-1,nr,r); 
           for (int tt=0; tt<nr; tt++)
           {    
               int i = vv[tt]+1;
               if (i >= t) i++;
               if (i!=t)
                  rp.addNeighborRelationship(t, i, rationalFactory.valueOf(1,nr));
           }     
        } 
        for (int t=1; t<=n; t++)   
            rp.setNumberOfVotes(t, t);
        
        
        rp.setNumberOfSeats(4);
        rp.prepareToProcess();        
        
        //rp.removeFromAllNeighborSets(2);
        //rp.removeFromAllNeighborSets(3);
        //rp.removeFromAllNeighborSets(5);
        
        return rp;
    }        
    void removeAtRandom(IPoliticalNetwork rp,int []nums,Random r)
    {
        nums = Arrays.copyOf(nums,nums.length);
        for (int t=0; t<nums.length; t++)
        {
            System.out.println("Removidos "+t);
            rp.checkConsistency(false);
            int i = r.nextInt(nums.length-t);
            rp.removeFromAllNeighborSets(nums[i]+1);
            nums[i] = nums[nums.length-t-1];
        }   
        System.out.println("Removidos "+nums.length);
    }        
    static int [] getRandomInts(int n,int l, Random r)
    {
        int [] a = new int[n];
        for (int t=0; t<n; t++) 
            a[t] = t;
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
        TestesRemocaoAleatoria evp = new TestesRemocaoAleatoria();
        Random r = new Random(0);
        int seed = r.nextInt();
        IPoliticalNetwork rp1 = evp.montaRede(seed,"0",Integer.parseInt(argv[1]));
        IPoliticalNetwork rp2 = evp.montaRede(seed,"1",Integer.parseInt(argv[1]));
        int rem[] = getRandomInts(100,96,r); 
                //{ 3,4,5,6,7,8,9,10,11,12,50,51,52,53,54,55 };         
        evp.removeAtRandom(rp1,rem,r);
        evp.removeAtRandom(rp2,rem,r);
        
        System.out.println(rp1.toString());
        System.out.println("________________________________________________");        
        System.out.println(rp2.toString());
        System.out.println("________________________________________________");        
        System.out.println(rp1.equals(rp2));
        
        rp1.close();
        rp2.close();
    }        
    
}
