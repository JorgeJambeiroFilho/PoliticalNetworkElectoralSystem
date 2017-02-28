/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.tests;

import politicalnetwork.core.PoliticalNetwork;
import politicalnetwork.core.IPoliticalNetwork;
import politicalnetwork.rationalnumber.DoublePrecisionRationalNumber;
import politicalnetwork.rationalnumber.RationalNumber;
import java.util.Random;
import politicalnetwork.rationalnumber.InfinitePrecisionRationalNumber;

/**
 *
 * @author jesjf22
 */
public class TestesRedeAleatoriaQuantidadeDeConexoesAleatoria
{
    
    public IPoliticalNetwork montaRede(int seed,String name,int numTh)
    {   
        int n = 200;
        int nrm = 10;
        Random r = new Random(seed);
        RationalNumber.Factory rationalFactory = new InfinitePrecisionRationalNumber.Factory();
        IPoliticalNetwork rp = new PoliticalNetwork(rationalFactory,"Rede "+name,numTh,false);
        for (int t=1; t<=n; t++)   
            rp.addCandidate(t);
        
        for (int t=1; t<=n; t++)
        {  
           int nr = r.nextInt(nrm); 
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
        
        
        rp.setNumberOfSeats(20);
        
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
        IPoliticalNetwork rp1 = evp.montaRede(seed,"0",Integer.parseInt(argv[0]));
        rp1.processElection();
        rp1.close();
    }        
    
}
