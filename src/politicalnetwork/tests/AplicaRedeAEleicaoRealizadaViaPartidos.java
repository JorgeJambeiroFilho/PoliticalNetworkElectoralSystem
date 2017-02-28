/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.tests;


import politicalnetwork.core.PoliticalNetwork;
import politicalnetwork.core.IPoliticalNetwork;
import politicalnetwork.rationalnumber.RationalNumber;
import deputados2012.DadosCandidatoEleicaoPassada;
import gnu.trove.map.hash.TIntObjectHashMap;
import politicalnetwork.rationalnumber.DoublePrecisionRationalNumber;
import java.util.HashMap;
import java.util.List;
import politicalnetwork.core.Candidate;

/**
 *
 * @author jesjf
 */
public class AplicaRedeAEleicaoRealizadaViaPartidos
{
    RationalNumber.Factory rationalFactory = new DoublePrecisionRationalNumber.Factory();
    public IPoliticalNetwork montaRede(List<DadosCandidatoEleicaoPassada> cands,int numTh)
    {   
        //List<DadosCandidatoEleicaoPassada> cands = new ArrayList();
        //for (DadosCandidatoEleicaoPassada cand:cands1)
        //    if (cand.status==DadosCandidatoEleicaoPassada.e_bandeira)
        //        cands.add(cand);
        int numLeg = -1;
        int numeroDeCadeiras = 0;
        double validos = 0;
        HashMap<String,Integer> coligNumCand = new HashMap();
        IPoliticalNetwork rp = new PoliticalNetwork(rationalFactory,"Rede Partidos",numTh,false);
        for (DadosCandidatoEleicaoPassada c:cands)   
        {    
            if (c.numero==0)
            {    
                c.numero = numLeg--; // cria números para que os votos de legenda possam entrar na rede 
                rp.addVirtualCandidate(c.numero);
            }    
            else
                rp.addCandidate(c.numero);
            if (c.status == DadosCandidatoEleicaoPassada.e_eleito)
                numeroDeCadeiras++;
            //if (c.status != DadosCandidatoEleicaoPassada.e_bandeira)
            {    
                Integer numCand = coligNumCand.get(c.coligacao);
                if (numCand==null)
                    numCand = 0;
                coligNumCand.put(c.coligacao, numCand+1);
            }   
            //if (c.status!=DadosCandidatoEleicaoPassada.e_bandeira)
              validos += c.votos;
        }
        numeroDeCadeiras = 70;
        double coef = validos / numeroDeCadeiras;
        for (DadosCandidatoEleicaoPassada c1:cands)   
           for (DadosCandidatoEleicaoPassada c2:cands)    
              if (c1!=c2 && c1.coligacao.equals(c2.coligacao)) 
              {
                  //if (c1.identifier < 0 && c2.identifier < 0)
                  //    throw new RuntimeException("Relação entre bandeiras");
                  rp.addNeighborRelationship(c1.numero, c2.numero, rationalFactory.valueOf(1,coligNumCand.get(c1.coligacao)-1));
              }    

        double v_prop = 0;
        double v_excedentes = 0;
        double v_eliminados = 0;
        double v_tot = 0; 
        for (DadosCandidatoEleicaoPassada c:cands)   
        {                
            rp.setNumberOfVotes(c.numero, c.votos);
            if (c.status!=DadosCandidatoEleicaoPassada.e_bandeira)
               v_tot += c.votos; 
            if (c.votos > coef && c.status==DadosCandidatoEleicaoPassada.e_eleito)
            {    
                v_prop += coef;
                v_excedentes += c.votos - coef;
            }    
            else
            if (c.status == DadosCandidatoEleicaoPassada.e_eleito)    
                v_prop += c.votos;
            else
            if (c.status == DadosCandidatoEleicaoPassada.e_eliminado)        
                v_eliminados += c.votos;                
        }    
        rp.setNumberOfSeats(numeroDeCadeiras);
        System.out.println("numeroDeCadeiras "+numeroDeCadeiras+ " validos "+validos+" coef "+coef+" prop "+(v_prop / v_tot)+" exc "+(v_excedentes /v_tot)+" "+(v_eliminados /v_tot));
        return rp;
    }        
    
        
    void compara(TIntObjectHashMap<Candidate> eleitos,List<DadosCandidatoEleicaoPassada> cands)
    {
        for (DadosCandidatoEleicaoPassada c:cands)
        {
            if ( (c.status==DadosCandidatoEleicaoPassada.e_eleito ) != eleitos.containsKey(c.numero))
            {    
                System.out.println(c);
                
            }    
        }    
    }        

    void compara2(PoliticalNetwork rp,List<DadosCandidatoEleicaoPassada> cands)
    {
        for (DadosCandidatoEleicaoPassada c:cands)
        {
            Candidate rc = rp.getCandidate(c.numero);   
            if ((c.status==DadosCandidatoEleicaoPassada.e_eleito )  != (rc.getStatus()==Candidate.ST_ELECTED) )
            {    
                System.out.println("_______________________________________________________");
                System.out.println(rc);
                System.out.println(c);
                System.out.println("_______________________________________________________");
                
            }    
        }    
    }        
    
    public static void main(String argv[])
    {
        AplicaRedeAEleicaoRealizadaViaPartidos evp = new AplicaRedeAEleicaoRealizadaViaPartidos();
        List<DadosCandidatoEleicaoPassada> cands = DadosCandidatoEleicaoPassada.leRecurso("deputados_federais_sp_2012_2.csv");
        IPoliticalNetwork rp = evp.montaRede(cands,Integer.parseInt(argv[1]));
        rp.processElection();
        TIntObjectHashMap<Candidate> eleitos = rp.getElected();
        evp.compara(eleitos,cands);
    }        
    
}
