/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package redepoliticatestes;

import irational.FakeRational;
import redepolitica4.*;
import irational.TrueRational;
import irational.IRational;
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
    
    public IRedePolitica montaRede()
    {   
        int n = 5000;
        IRational.Factory rationalFactory = new FakeRational.Factory();
        IRedePolitica rp = new RedePoliticaR2(rationalFactory,"Rede");
        for (int t=0; t<n; t++)   
            rp.addCandidato(t);
        
        for (int t=0; t<n; t++)
        {  
           for (int tt=0; tt<n; tt++)
           {    
               if (tt%100==0)
                  System.out.print(" "+tt/100);
               if (tt!=t)
               try    
               {    
                  rp.addRelacao(t, tt, rationalFactory.valueOf(1,n-1));
               }  
               catch(RuntimeException e)
               {
                   e.printStackTrace();
                   throw e;
               }
           }     
           System.out.println("\nAdded "+t);
        } 
        for (int t=0; t<n; t++)   
            rp.defineVotos(t, t);
        
        
        return rp;
    }        
    public static void main(String argv[])
    {
        TestesCompleta evp = new TestesCompleta();
        IRedePolitica rp = evp.montaRede();
        rp.defineCadeiras(70);
        rp.realizaApuracao();     
        rp.close();
    }        
    
}
