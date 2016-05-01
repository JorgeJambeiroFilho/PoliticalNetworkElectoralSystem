/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package redepolitica4;

import gnu.trove.map.hash.TIntObjectHashMap;
import irational.IRational;
import java.util.HashMap;

/**
 *
 * @author jesjf
 */
public class Candidato
{
    int numero;
    public static final int ST_REMANESCENTE = 0;
    public static final int ST_ELEITO = 1;
    public static final int ST_ELIMINADO = 2;
    public static final int ST_DESCARTE = 3;
    public static final int ST_EM_ELIMINACAO = 4;
    int status;
    IRational votosProprios;
    IRational votosCorrentes;
    TIntObjectHashMap<Relacao> vizinhosOriginais;
    TIntObjectHashMap<Relacao> vizinhosCorrentes;
    TIntObjectHashMap<Candidato> contraVizinhosOriginais;
    TIntObjectHashMap<Candidato> contraVizinhosCorrentes;
//    HashMap<Integer, Candidato> contraVizinhosTemporarios;
    IRational votosNaDefinicao;
    boolean isBandeira;

    public String toString()
    {
        return "numero " + numero + " status " + status 
                + " votos " + votosCorrentes.doubleValue() + " " + " v " 
                + (vizinhosCorrentes==null?0:vizinhosCorrentes.size()) + " cv " + (contraVizinhosCorrentes==null ? 0 : contraVizinhosCorrentes.size()) 
                + (votosNaDefinicao != null ? " votosdef " + votosNaDefinicao.doubleValue() : "")
                ;
    }
            
    Candidato(Candidato c)
    {
        numero = c.numero;
        status = c.status;
        votosProprios = c.votosProprios;
        votosCorrentes = c.votosCorrentes;
        vizinhosOriginais = new TIntObjectHashMap(c.vizinhosOriginais);        
        if (vizinhosCorrentes!=null)
           vizinhosCorrentes = new TIntObjectHashMap(c.vizinhosCorrentes);
        contraVizinhosOriginais = new TIntObjectHashMap(c.contraVizinhosOriginais);
        if (c.contraVizinhosCorrentes!=null)
            contraVizinhosCorrentes = new TIntObjectHashMap(c.contraVizinhosCorrentes);
        votosNaDefinicao = c.votosNaDefinicao;
        isBandeira = c.isBandeira;                        
    }
            
    Candidato(int numero, boolean isBandeira)
    {
        this.numero = numero;
        this.isBandeira = isBandeira;
        vizinhosOriginais = new TIntObjectHashMap();
        contraVizinhosOriginais = new TIntObjectHashMap();
    }

    void defineVotos(IRational votosProprios)
    {
        this.votosProprios = votosProprios;
        votosCorrentes = this.votosProprios;
    }

    void adicionaVizinho(Candidato vizinho, IRational percentual)
    {
        vizinhosOriginais.put(vizinho.numero, new Relacao(vizinho, percentual));
    }

    void adicionaContraVizinho(Candidato contraVizinho)
    {
        contraVizinhosOriginais.put(contraVizinho.numero, contraVizinho);
    }

    void preparaParaInicioDaApuracao(Candidato descarte,IRational.Factory numberFactory)
    {
        status = ST_REMANESCENTE;
        vizinhosCorrentes = new TIntObjectHashMap();
        for (Relacao r:vizinhosOriginais.valueCollection())
            vizinhosCorrentes.put(r.relacionado.numero,new Relacao(r));
        if (descarte!=null && vizinhosOriginais.isEmpty())
             vizinhosCorrentes.put(descarte.numero, new Relacao(descarte,numberFactory.valueOf(1, 1)));
        contraVizinhosCorrentes = new TIntObjectHashMap(contraVizinhosOriginais);
        
    }
    void preparaParaInicioDaApuracaoDescarte(IRational.Factory numberFactory)
    {
        //preparaParaInicioDaApuracao(null,numberFactory);
        status = ST_DESCARTE;        
        vizinhosCorrentes = new TIntObjectHashMap();
    }
    
    public int getNumero()
    {
        return numero;
    }

    public int getStatus()
    {
        return status;
    }

    public IRational getVotosProprios()
    {
        return votosProprios;
    }

    public IRational getVotosCorrentes()
    {
        return votosCorrentes;
    }

    public TIntObjectHashMap<Relacao> getVizinhosOriginais()
    {
        return vizinhosOriginais;
    }

    public TIntObjectHashMap<Relacao> getVizinhosCorrentes()
    {
        return vizinhosCorrentes;
    }

    public TIntObjectHashMap<Candidato> getContraVizinhosOriginais()
    {
        return contraVizinhosOriginais;
    }

    public TIntObjectHashMap<Candidato> getContraVizinhosCorrentes()
    {
        return contraVizinhosCorrentes;
    }
     
    
    void checa(IRational quocienteEleitoral, IRational.Factory numberFactory)
    {
        checa(quocienteEleitoral, numberFactory, true);
    }        
    void checa(IRational quocienteEleitoral, IRational.Factory numberFactory,boolean hasJustElected)
    {
        if (status == ST_ELEITO && !votosCorrentes.equals(quocienteEleitoral))
        {
            throw new RuntimeException("Eleito com votos diferentes do coeficiente");
        }
        else 
        if (status == ST_ELIMINADO && !votosCorrentes.equals(numberFactory.valueOf(0, 1)))
        {
            throw new RuntimeException("Eliminado manteve votos");
        }
        else 
        if (hasJustElected && status == ST_REMANESCENTE && !isBandeira && votosCorrentes.compareTo(quocienteEleitoral) >= 0)
        {
            throw new RuntimeException("Remanescente deveria ter sido eleito");
        }
        IRational sum = numberFactory.valueOf(0, 1);
        if (vizinhosCorrentes != null)
        {
            for (Relacao r : vizinhosCorrentes.valueCollection())
            {
                sum = sum.plus(r.percentualRepasse);
                if (r.relacionado == this)
                {
                    throw new RuntimeException("Auto relação");
                }
                if (r.relacionado.contraVizinhosCorrentes==null && r.relacionado.status != ST_DESCARTE)
                    throw new RuntimeException("Inconsistência entre vizinhos e contra vizinhos");
                if (r.relacionado.contraVizinhosCorrentes!=null && !r.relacionado.contraVizinhosCorrentes.containsKey(numero))
                {
                    throw new RuntimeException("Inconsistência entre vizinhos e contra vizinhos");
                }
                if (r.relacionado.status!=Candidato.ST_REMANESCENTE && r.relacionado.status!=Candidato.ST_DESCARTE)                
                    throw new RuntimeException("Vizinho não remanescente");
            }
        }
        if (contraVizinhosCorrentes != null)
        {
            for (Candidato c : contraVizinhosCorrentes.valueCollection())
            {
                if (c.vizinhosCorrentes != null && !c.vizinhosCorrentes.containsKey(numero))
                {
                    throw new RuntimeException("Inconsistência entre vizinhos e contra vizinhos");
                }
                if (c.status==Candidato.ST_ELIMINADO)
                    throw new RuntimeException("Contra vizinho eliminado");
            }
        }
        if (vizinhosCorrentes != null && !vizinhosCorrentes.isEmpty() && !sum.equals(numberFactory.valueOf(1, 1)))
        {
            throw new RuntimeException("Percentuais de repasse inconsistentes");
        }
    }

    public String toStringWithLinks()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("" + numero + "\n");
        for (Relacao r : vizinhosCorrentes.valueCollection())
        {
            sb.append("    " + r.relacionado.numero + "       " + r.percentualRepasse + "\n");
        }
        return sb.toString();
    }

    public boolean equals(Object o)
    {
        Candidato c = (Candidato) o;
        if (c.numero != numero)
        {
            return false;
        }
        if (c.status != status)
        {
            return false;
        }
        if (!c.votosProprios.equals(votosProprios))
        {
            return false;
        }
        if (!c.votosCorrentes.equals(votosCorrentes))
        {
            return false;
        }
        if (vizinhosCorrentes.size() != c.vizinhosCorrentes.size())
        {
            return false;
        }
        for (Relacao r : vizinhosCorrentes.valueCollection())
        {
            Relacao or = c.vizinhosCorrentes.get(r.relacionado.numero);
            if (or == null)
            {
                return false;
            }
            if (!or.percentualRepasse.equals(r.percentualRepasse))
            {
                return false;
            }
        }
        return true;
    }

    public IRational getVotosNaDefinicao()
    {
        return votosNaDefinicao;
    }

    public boolean isIsBandeira()
    {
        return isBandeira;
    }
    
}
