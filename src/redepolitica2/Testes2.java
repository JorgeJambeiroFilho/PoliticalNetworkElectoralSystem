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
public class Testes2
{
    
    public RedePoliticaR montaRede()
    {   
        int n = 2000;
        RedePoliticaR rp = new RedePoliticaR();
        for (int t=0; t<n; t++)   
            rp.addCandidato(t);
        
        for (int t=0; t<n; t++)
        {  
           for (int tt=0; tt<n; tt++)
           {    
               if (tt!=t)
                  rp.addRelacao(t, tt, Rational.valueOf(1,n-1));
           }     
        } 
        for (int t=0; t<n; t++)   
            rp.defineVotos(t, t);
        
        
        return rp;
    }        
    public static void main(String argv[])
    {
        Testes2 evp = new Testes2();
        RedePoliticaR rp = evp.montaRede();
        rp.defineCadeiras(70);
        rp.realizaApuracao();        
    }        
    
}
