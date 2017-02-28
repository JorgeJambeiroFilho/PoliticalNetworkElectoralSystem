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
 * @author jesjf
 */
public class TestesCompleta
{
    
    public IPoliticalNetwork montaRede(int n,int numTh)
    {   
        //int n = 1750;
        RationalNumber.Factory rationalFactory = new DoublePrecisionRationalNumber.Factory();
        IPoliticalNetwork rp = new PoliticalNetwork(rationalFactory,"Rede",numTh,false);
        for (int t=0; t<n; t++)   
            rp.addCandidate(t);
        
        for (int t=0; t<n; t++)
        {  
           for (int tt=0; tt<n; tt++)
           {    
               //if (tt%100==0)
               //   System.out.print(" "+tt/100);
               if (tt!=t)
               try    
               {    
                  rp.addNeighborRelationship(t, tt, rationalFactory.valueOf(1,n-1));
               }  
               catch(RuntimeException e)
               {
                   e.printStackTrace();
                   throw e;
               }
           }     
           //System.out.println("\nAdded "+t);
        } 
        for (int t=0; t<n; t++)   
            rp.setNumberOfVotes(t, t);
        
        
        return rp;
    }        
    public static void main(String argv[])
    {
        long iniTime = System.currentTimeMillis();
        TestesCompleta evp = new TestesCompleta();
        IPoliticalNetwork rp = evp.montaRede(Integer.parseInt(argv[0]),Integer.parseInt(argv[1]));
        rp.setNumberOfSeats(70);
        rp.processElection();     
        rp.close();
        long endTime =  System.currentTimeMillis();
        System.out.println("Tempo total "+argv[0]+" "+argv[1]+" "+(endTime-iniTime)/1000);
    }        
    
}
