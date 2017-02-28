/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.tests;

import politicalnetwork.core.PoliticalNetwork;
import politicalnetwork.core.IPoliticalNetwork;
import politicalnetwork.rationalnumber.DoublePrecisionRationalNumber;
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
public class TestesRedeAleatoria
{
    
    public IPoliticalNetwork montaRede(int seed,String name,int numTh)
    {   
        int n = 500;
        int nr = 10;
        Random r = new Random(seed);
        RationalNumber.Factory rationalFactory = new DoublePrecisionRationalNumber.Factory();
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
        
        
        rp.setNumberOfSeats(40);
        //rp.prepareToProcess();        
        
        //rp.removeFromAllNeighborSets(2);
        //rp.removeFromAllNeighborSets(3);
        //rp.removeFromAllNeighborSets(5);
        
        return rp;
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
        TestesRedeAleatoria evp = new TestesRedeAleatoria();
        Random r = new Random(0);
        int seed = r.nextInt();
        int numTh = Runtime.getRuntime().availableProcessors();
        if (argv.length > 0)
            Integer.parseInt(argv[0]);
        IPoliticalNetwork rp1 = evp.montaRede(seed,"0",numTh);
        rp1.processElection();
        rp1.close();
    }        
    
}
