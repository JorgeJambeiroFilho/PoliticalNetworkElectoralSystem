/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package politicalnetwork.tests;

import deputados2012.DadosCandidatoEleicaoPassada;
import java.util.List;

/**
 *
 * @author jesjf
 */
public class MedeRepassesEleicaoPassada
{

    
    public void analisa(List<DadosCandidatoEleicaoPassada> cands)
    {   
        int numLeg = -1;
        int numeroDeCadeiras = 0;
        double validos = 0;
        for (DadosCandidatoEleicaoPassada c:cands)   
        {    
            if (c.numero==0)
                c.numero = numLeg--; // cria números para que os votos de legenda possam entrar na rede 
            if (c.status == DadosCandidatoEleicaoPassada.e_eleito)
                numeroDeCadeiras++;
            validos += c.votos;
        }
        double coef = validos / numeroDeCadeiras;

        double v_prop = 0;
        double v_excedentes = 0;
        double v_eliminados = 0;
        double v_tot = 0; 
        int numAuto = 0;
        for (DadosCandidatoEleicaoPassada c:cands)   
        {                
            if (c.status!=DadosCandidatoEleicaoPassada.e_bandeira)
               v_tot += c.votos; 
            if (c.votos > coef && c.status==DadosCandidatoEleicaoPassada.e_eleito)
            {    
                v_prop += coef;
                v_excedentes += c.votos - coef;
                numAuto++;
            }    
            else
            if (c.status == DadosCandidatoEleicaoPassada.e_eleito)    
                v_prop += c.votos;
            else
            if (c.status == DadosCandidatoEleicaoPassada.e_eliminado)        
                v_eliminados += c.votos;                
        }    
        System.out.println("numeroDeCadeiras "+numeroDeCadeiras);
        System.out.println("validos "+validos);
        System.out.println("coef "+coef);
        System.out.println("prop "+(v_prop / validos));
        System.out.println("exc "+(v_excedentes / validos));
        System.out.println("elim "+(v_eliminados / validos));
        System.out.println("leg "+(validos-v_tot)/validos);
        System.out.println("auto "+numAuto);
        System.out.println("Perc repassados "+(v_excedentes+v_eliminados)/v_tot);
        System.out.println("Perc repassados com leg "+(v_excedentes+v_eliminados+validos-v_tot)/validos);
        System.out.println("Perc repassados elim "+v_eliminados/(v_excedentes+v_eliminados));
    }        
    public static void main(String argv[])
    {
        MedeRepassesEleicaoPassada evp = new MedeRepassesEleicaoPassada();
        List<DadosCandidatoEleicaoPassada> cands = DadosCandidatoEleicaoPassada.leRecurso("deputados_federais_sp_2012_2.csv");
        evp.analisa(cands);
    }        
    
}
