/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.tests;

import java.util.Random;

/**
 *
 * @author jesjf
 */
public class Simulacao1
{
    static double lgTemaAvgRelevance = Math.log(0.1); // no espaço logístico
    static double lgTemaStdRelevance = Math.log(10);    
    static int numTemas  = 100;    
    static int numCand = 1500;
    static int numRel = 10;
    
    static class Tema
    {
        double avgRelevance;  
        public Tema(double avgRelevance)
        {
            this.avgRelevance = avgRelevance;
        }        
    }
    Tema[] temas;    
    static class SimulacaoCandidato
    {
        Tema[] temas;
        SimulacaoRelacao[] relacoes;
        public SimulacaoCandidato(Tema[] temas)
        {
            this.temas = temas;
            relacoes = new SimulacaoRelacao[numRel];
        }                 
    }    
    SimulacaoCandidato[] candidatos;
    static class SimulacaoRelacao implements Comparable<SimulacaoRelacao>
    {
        SimulacaoCandidato cand;
        double dist;
        public SimulacaoRelacao(SimulacaoCandidato cand, double dist)
        {
            this.cand = cand;
            this.dist = dist;
        }
        public int compareTo(SimulacaoRelacao o)
        {
            if (dist < o.dist) return 1;
            if (dist > o.dist) return -1;
            return 0;
        }        
    }
    
    public Simulacao1()
    {
        Random rand = new Random();
        temas = new Tema[numTemas];        
        for (int t=0; t<numTemas; t++)
        {            
            double lgRel = rand.nextGaussian() * lgTemaStdRelevance + lgTemaAvgRelevance;
            double rel = 1.0 / (1+Math.exp(-lgRel));
            temas[t] = new Tema(rel);
        }    
        candidatos = new SimulacaoCandidato[numCand];        
        for (int c=0; c<numCand; c++)
        {            
            Tema[] candTemas = new Tema[numTemas];        
            for (int t=0; t<numTemas; t++)
            {   
                double lgAvgRel = Math.log(temas[t].avgRelevance / (1-temas[t].avgRelevance));
                double lgRel = rand.nextGaussian() * lgTemaStdRelevance + lgAvgRel;
                double rel = 1.0 / (1+Math.exp(-lgRel));
                candTemas[t] = new Tema(rel);
            }        
            candidatos[c] = new SimulacaoCandidato(candTemas);
        }   
        for (int c1=0; c1<numCand; c1++)
        {
            SimulacaoRelacao[] allRelsCand = new SimulacaoRelacao[numCand];
            for (int c2=0; c2<numCand; c2++)
            {
                
            }    
        }    
            
    }        
}
