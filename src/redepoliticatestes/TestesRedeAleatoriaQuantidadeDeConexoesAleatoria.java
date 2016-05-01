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
 * @author jesjf22
 */
public class TestesRedeAleatoriaQuantidadeDeConexoesAleatoria
{
    
    public IRedePolitica montaRede(int seed,String name,int numTh)
    {   
        int n = 500;
        int nrm = 10;
        Random r = new Random(seed);
        IRational.Factory rationalFactory = new FakeRational.Factory();
        IRedePolitica rp = new RedePoliticaR2(rationalFactory,"Rede "+name,numTh);
        for (int t=1; t<=n; t++)   
            rp.addCandidato(t);
        
        for (int t=1; t<=n; t++)
        {  
           int nr = r.nextInt(nrm); 
           int [] vv = getRandomInts(n-1,nr,r); 
           for (int tt=0; tt<nr; tt++)
           {    
               int i = vv[tt]+1;
               if (i >= t) i++;
               if (i!=t)
                  rp.addRelacao(t, i, rationalFactory.valueOf(1,nr));
           }     
        } 
        for (int t=1; t<=n; t++)   
            rp.defineVotos(t, t);
        
        
        rp.defineCadeiras(4);
        
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
        TestesRedeAleatoriaQuantidadeDeConexoesAleatoria evp = new TestesRedeAleatoriaQuantidadeDeConexoesAleatoria();
        Random r = new Random(0);
        int seed = r.nextInt();
        IRedePolitica rp1 = evp.montaRede(seed,"0",Integer.parseInt(argv[1]));
        rp1.realizaApuracao();
        rp1.close();
    }        
    
}
