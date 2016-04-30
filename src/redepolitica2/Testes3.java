/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package redepolitica2;

import java.util.HashSet;
import java.util.Random;

/**
 *
 * @author jesjf
 */
public class Testes3
{
    
    public RedePoliticaR montaRede()
    {   
        int n = 1000;
        int nr = 10; 
        RedePoliticaR rp = new RedePoliticaR();
        for (int t=0; t<n; t++)   
            rp.addCandidato(t);
        Random r = new Random();
        for (int t=0; t<n; t++)
        {  
           HashSet<Integer> vv = new HashSet(); 
           for (int tt=0; tt<nr; tt++)
           {    
               int i = r.nextInt(n);
               if (i!=t)
                  vv.add(i);
           }           
           for (int i:vv)
               rp.addRelacao(t, i, Rational.valueOf(1,vv.size()));
        } 
        for (int t=0; t<n; t++)    
            rp.defineVotos(t, t);
        return rp;
    }        
    public static void main(String argv[])
    {
        Testes3 evp = new Testes3();
        RedePoliticaR rp = evp.montaRede();
        rp.defineCadeiras(70);
        rp.realizaApuracao();        
    }        
    
}
